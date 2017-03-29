/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
// Access to Zip archives.
//

//#define LOG_TAG "zip"

#include <Inc_log.h>

#include "ZipFile.h"

#include <zlib.h>
#define DEF_MEM_LEVEL 8                // normally in zutil.h?
#include <unistd.h>

#include <memory.h>
#include <sys/stat.h>
#include <errno.h>
#include <assert.h>
#include "Inc_log.h"

using namespace android;

/*
 * Some environments require the "b", some choke on it.
 */
#define FILE_OPEN_RO        "rb"
#define FILE_OPEN_RW        "r+b"
#define FILE_OPEN_RW_CREATE "w+b"

/* should live somewhere else? */
static status_t errnoToStatus(int err)
{
    if (err == ENOENT)
        return NAME_NOT_FOUND;
    else if (err == EACCES)
        return PERMISSION_DENIED;
    else
        return UNKNOWN_ERROR;
}

/*
 * Open a file and parse its guts.
 */
status_t ZipFile::open(const char* zipFileName, int flags)
{
    bool newArchive = false;

    assert(mZipFp == NULL);     // no reopen

    if ((flags & kOpenTruncate))
        flags |= kOpenCreate;           // trunc implies create

    if ((flags & kOpenReadOnly) && (flags & kOpenReadWrite))
        return INVALID_OPERATION;       // not both
    if (!((flags & kOpenReadOnly) || (flags & kOpenReadWrite)))
        return INVALID_OPERATION;       // not neither
    if ((flags & kOpenCreate) && !(flags & kOpenReadWrite))
        return INVALID_OPERATION;       // create requires write

    if (flags & kOpenTruncate) {
        newArchive = true;
    } else {
        newArchive = (access(zipFileName, F_OK) != 0);
        if (!(flags & kOpenCreate) && newArchive) {
            /* not creating, must already exist */
            LOGZ("File %s does not exist", zipFileName);
            return NAME_NOT_FOUND;
        }
    }

    /* open the file */
    const char* openflags;
    if (flags & kOpenReadWrite) {
        if (newArchive)
            openflags = FILE_OPEN_RW_CREATE;
        else
            openflags = FILE_OPEN_RW;
    } else {
        openflags = FILE_OPEN_RO;
    }
    mZipFp = fopen(zipFileName, openflags);
    if (mZipFp == NULL) {
        int err = errno;
        LOGZ("fopen failed: %d\n", err);
        return errnoToStatus(err);
    }

    status_t result;
    if (!newArchive) {
        /*
         * Load the central directory.  If that fails, then this probably
         * isn't a Zip archive.
         */
        result = readCentralDir();
    } else {
        /*
         * Newly-created.  The EndOfCentralDir constructor actually
         * sets everything to be the way we want it (all zeroes).  We
         * set mNeedCDRewrite so that we create *something* if the
         * caller doesn't add any files.  (We could also just unlink
         * the file if it's brand new and nothing was added, but that's
         * probably doing more than we really should -- the user might
         * have a need for empty zip files.)
         */
        mNeedCDRewrite = true;
        result = NO_ERROR;
    }

    if (flags & kOpenReadOnly)
        mReadOnly = true;
    else
        assert(!mReadOnly);

    return result;
}

/*
 * Return the Nth entry in the archive.
 */
ZipEntry* ZipFile::getEntryByIndex(int idx) const
{
    if (idx < 0 || idx >= (int) mEntries.size())
        return NULL;

    return mEntries[idx];
}

/*
 * Find an entry by name.
 */
ZipEntry* ZipFile::getEntryByName(const char* fileName) const
{
    /*
     * Do a stupid linear string-compare search.
     *
     * There are various ways to speed this up, especially since it's rare
     * to intermingle changes to the archive with "get by name" calls.  We
     * don't want to sort the mEntries vector itself, however, because
     * it's used to recreate the Central Directory.
     *
     * (Hash table works, parallel list of pointers in sorted order is good.)
     */
    int idx;

    for (idx = mEntries.size()-1; idx >= 0; idx--) {
        ZipEntry* pEntry = mEntries[idx];
        if (!pEntry->getDeleted() &&
            strcmp(fileName, pEntry->getFileName()) == 0)
        {
            return pEntry;
        }
    }

    return NULL;
}

/*
 * Empty the mEntries vector.
 */
void ZipFile::discardEntries(void)
{
    int count = mEntries.size();

    while (--count >= 0)
        delete mEntries[count];

    mEntries.clear();
}


/*
 * Find the central directory and read the contents.
 *
 * The fun thing about ZIP archives is that they may or may not be
 * readable from start to end.  In some cases, notably for archives
 * that were written to stdout, the only length information is in the
 * central directory at the end of the file.
 *
 * Of course, the central directory can be followed by a variable-length
 * comment field, so we have to scan through it backwards.  The comment
 * is at most 64K, plus we have 18 bytes for the end-of-central-dir stuff
 * itself, plus apparently sometimes people throw random junk on the end
 * just for the fun of it.
 *
 * This is all a little wobbly.  If the wrong value ends up in the EOCD
 * area, we're hosed.  This appears to be the way that everbody handles
 * it though, so we're in pretty good company if this fails.
 */
status_t ZipFile::readCentralDir(void)
{
    status_t result = NO_ERROR;
    unsigned char* buf = NULL;
    off_t fileLength, seekStart;
    long readAmount;
    int i;

    fseek(mZipFp, 0, SEEK_END);
    fileLength = ftell(mZipFp);
    rewind(mZipFp);

    /* too small to be a ZIP archive? */
    if (fileLength < EndOfCentralDir::kEOCDLen) {
        LOGZ("Length is %ld -- too small\n", (long)fileLength);
        result = INVALID_OPERATION;
        goto bail;
    }

    buf = new unsigned char[EndOfCentralDir::kMaxEOCDSearch];
    if (buf == NULL) {
        LOGZ("Failure allocating %d bytes for EOCD search",
             EndOfCentralDir::kMaxEOCDSearch);
        result = NO_MEMORY;
        goto bail;
    }

    if (fileLength > EndOfCentralDir::kMaxEOCDSearch) {
        seekStart = fileLength - EndOfCentralDir::kMaxEOCDSearch;
        readAmount = EndOfCentralDir::kMaxEOCDSearch;
    } else {
        seekStart = 0;
        readAmount = (long) fileLength;
    }
    if (fseek(mZipFp, seekStart, SEEK_SET) != 0) {
        LOGZ("Failure seeking to end of zip at %ld", (long) seekStart);
        result = UNKNOWN_ERROR;
        goto bail;
    }

    /* read the last part of the file into the buffer */
    if (fread(buf, 1, readAmount, mZipFp) != (size_t) readAmount) {
        LOGZ("short file? wanted %ld\n", readAmount);
        result = UNKNOWN_ERROR;
        goto bail;
    }

    /* find the end-of-central-dir magic */
    for (i = readAmount - 4; i >= 0; i--) {
        if (buf[i] == 0x50 &&
            ZipEntry::getLongLE(&buf[i]) == EndOfCentralDir::kSignature)
        {
            LOGW("+++ Found EOCD at buf+%d\n", i);
            break;
        }
    }
    if (i < 0) {
        LOGW("EOCD not found, not Zip\n");
        result = INVALID_OPERATION;
        goto bail;
    }

    /* extract eocd values */
    result = mEOCD.readBuf(buf + i, readAmount - i);
    if (result != NO_ERROR) {
        LOGZ("Failure reading %ld bytes of EOCD values", readAmount - i);
        goto bail;
    }
    //mEOCD.dump();

    if (mEOCD.mDiskNumber != 0 || mEOCD.mDiskWithCentralDir != 0 ||
        mEOCD.mNumEntries != mEOCD.mTotalNumEntries)
    {
        LOGZ("Archive spanning not supported\n");
        result = INVALID_OPERATION;
        goto bail;
    }

    /*
     * So far so good.  "mCentralDirSize" is the size in bytes of the
     * central directory, so we can just seek back that far to find it.
     * We can also seek forward mCentralDirOffset bytes from the
     * start of the file.
     *
     * We're not guaranteed to have the rest of the central dir in the
     * buffer, nor are we guaranteed that the central dir will have any
     * sort of convenient size.  We need to skip to the start of it and
     * read the header, then the other goodies.
     *
     * The only thing we really need right now is the file comment, which
     * we're hoping to preserve.
     */
    if (fseek(mZipFp, mEOCD.mCentralDirOffset, SEEK_SET) != 0) {
        LOGZ("Failure seeking to central dir offset %ld\n",
             mEOCD.mCentralDirOffset);
        result = UNKNOWN_ERROR;
        goto bail;
    }

    /*
     * Loop through and read the central dir entries.
     */
    LOGW("Scanning %d entries...\n", mEOCD.mTotalNumEntries);
    int entry;
    for (entry = 0; entry < mEOCD.mTotalNumEntries; entry++) {
        ZipEntry* pEntry = new ZipEntry;

        result = pEntry->initFromCDE(mZipFp);
        if (result != NO_ERROR) {
            LOGZ("initFromCDE failed\n");
            delete pEntry;
            goto bail;
        }

        mEntries.push_back(pEntry);
    }


    /*
     * If all went well, we should now be back at the EOCD.
     */
    {
        unsigned char checkBuf[4];
        if (fread(checkBuf, 1, 4, mZipFp) != 4) {
            LOGZ("EOCD check read failed\n");
            result = INVALID_OPERATION;
            goto bail;
        }
        if (ZipEntry::getLongLE(checkBuf) != EndOfCentralDir::kSignature) {
            LOGZ("EOCD read check failed\n");
            result = UNKNOWN_ERROR;
            goto bail;
        }
        LOGW("+++ EOCD read check passed\n");
    }

bail:
    delete[] buf;
    return result;
}


status_t ZipFile::CreateNewFileAndDeflate(const char* storageName
							,struct AddFileParam** pparam){
    if (getEntryByName(storageName) != NULL)
        return ALREADY_EXISTS;
    if (fseek(mZipFp, mEOCD.mCentralDirOffset, SEEK_SET) != 0) {
        return UNKNOWN_ERROR;
    }

    ZipEntry* pEntry = new ZipEntry;
    pEntry->initNew(storageName, NULL);
	AddFileParam * param = new struct AddFileParam();
	memset(param, 0, sizeof(struct AddFileParam));
	/*
	 * From here on out, failures are more interesting.
	 */
	mNeedCDRewrite = true;
    /*
     * Write the LFH, even though it's still mostly blank.  We need it
     * as a place-holder.  In theory the LFH isn't necessary, but in
     * practice some utilities demand it.
     */
    param->lfhPosn = ftell(mZipFp);
    pEntry->mLFH.write(mZipFp);
    param->startPosn = ftell(mZipFp);
	param->pEntry = pEntry;
	*pparam = param;
	return NO_ERROR;
}

status_t ZipFile::FinishAddAndDeflateFile(struct AddFileParam *param){
    /*
     * We could write the "Data Descriptor", but there doesn't seem to
     * be any point since we're going to go back and write the LFH.
     *
     * Update file offsets.
     */
     LOG("FinishAddFile");
    deflateEnd(&param->zstream); 
    int endPosn = ftell(mZipFp);            // seeked to end of compressed data
	status_t result = NO_ERROR;
    /*
     * Success!  Fill out new values.
     */
     LOG("param->uncompressedLen =%d, endPosn - param->startPosn = %d"
     	, param->uncompressedLen, endPosn - param->startPosn);
    param->pEntry->setDataInfo(param->uncompressedLen, endPosn - param->startPosn, param->crc,
        				ZipEntry::kCompressDeflated);
    time_t modWhen = getModTime(fileno(mZipFp));
    param->pEntry->setModWhen(modWhen);
    param->pEntry->setLFHOffset(param->lfhPosn);
    mEOCD.mNumEntries++;
    mEOCD.mTotalNumEntries++;
    mEOCD.mCentralDirSize = 0;      // mark invalid; set by flush()
    mEOCD.mCentralDirOffset = endPosn;

    /*
     * Go back and write the LFH.
     */
    if (fseek(mZipFp, param->lfhPosn, SEEK_SET) != 0) {
        result = UNKNOWN_ERROR;
    }
    param->pEntry->mLFH.write(mZipFp);

    /*
     * Add pEntry to the list.
     */
    mEntries.push_back(param->pEntry);
	param->pEntry = NULL;
    return result;	
	
}

status_t ZipFile::WriteAndDeflateData(unsigned char* data, int dataSize
									, struct AddFileParam *param, bool finish){
    const size_t kBufSize = kCacheSize * 5;
    int zerr;
    /*
     * Initialize the zlib stream.
     */
    if(!param->streamInited){
		param->outBuffer = new unsigned char[kBufSize];
	    param->zstream.zalloc = Z_NULL;
	    param->zstream.zfree = Z_NULL;
	    param->zstream.opaque = Z_NULL;
	    param->zstream.next_in = NULL;
	    param->zstream.avail_in = 0;
		param->streamInited = true;
	    param->zstream.data_type = Z_UNKNOWN;
		
		param->crc = crc32(0L, Z_NULL, 0);
		zerr = deflateInit2(&param->zstream, Z_BEST_SPEED,
        	Z_DEFLATED, -MAX_WBITS, DEF_MEM_LEVEL, Z_DEFAULT_STRATEGY);
		if (zerr != Z_OK) {
			LOGZ("deflateInit2 failed");
        	return UNKNOWN_ERROR;
    	}
	}
	param->zstream.avail_in = dataSize;
	param->zstream.next_in = data;
	
	param->zstream.avail_out = kBufSize;
	param->zstream.next_out = param->outBuffer;
	int flush = finish ? Z_FINISH : Z_NO_FLUSH;
	
	zerr = deflate(&param->zstream, flush);
	param->compressedLen += param->zstream.next_out - param->outBuffer;
	//LOGZ("dataSize = %d, flush = %d, avail_in = %d, avail_out = %d, compressedLen = %u,"
	//	,dataSize, flush, param->zstream.avail_in, param->zstream.avail_out, param->compressedLen );
	//LOGZ("param->zstream.next_out - param->outBuffer = %d", param->zstream.next_out - param->outBuffer);

	if(zerr != Z_OK && zerr != Z_STREAM_END){
		LOGZ("deflate  failed zerr = %d", zerr);
		return UNKNOWN_ERROR;
	}
	if(param->zstream.next_out - param->outBuffer > 0){
		if(fwrite(param->outBuffer
			, 1
			, param->zstream.next_out - param->outBuffer
			, mZipFp) != param->zstream.next_out - param->outBuffer){
					LOGZ("write file failed");
					return UNKNOWN_ERROR;
		}
			
	}
	
	param->crc = crc32(param->crc, data, dataSize);
	param->uncompressedLen += dataSize;
	//LOG("param->crc = %u, param->uncompressedLen = %d", param->crc, param->uncompressedLen);
  	if(zerr == Z_OK || zerr == Z_STREAM_END)
   	 	return NO_ERROR;	
	else
		return UNKNOWN_ERROR;
}

status_t ZipFile::WriteDeflatedData(unsigned char* data, unsigned long dataSize
									,struct AddFileParam* param, bool finish){
    /*
     * Initialize the zlib stream.
     */
    if(!param->streamInited){
		param->outBuffer = 0;
	    param->zstream.zalloc = Z_NULL;
	    param->zstream.zfree = Z_NULL;
	    param->zstream.opaque = Z_NULL;
	    param->zstream.next_in = NULL;
	    param->zstream.avail_in = 0;
		param->streamInited = true;
	    param->zstream.data_type = Z_UNKNOWN;
		param->crc = crc32(0L, Z_NULL, 0);						
    }	
	param->crc = crc32(param->crc, data, dataSize);
	if(fwrite(data, 1, dataSize, mZipFp) != dataSize){
		LOG("WriteDeflatedData failed!");
		return INVALID_OPERATION;
	}
	return NO_ERROR;
}

status_t ZipFile::CreateNewDeflatedFile(const char * storageName								
										, struct AddFileParam** pparam){
    if (getEntryByName(storageName) != NULL)
        return ALREADY_EXISTS;
    if (fseek(mZipFp, mEOCD.mCentralDirOffset, SEEK_SET) != 0) {
        return UNKNOWN_ERROR;
    }

    ZipEntry* pEntry = new ZipEntry;
    pEntry->initNew(storageName, NULL);
	AddFileParam * param = new struct AddFileParam();
	memset(param, 0, sizeof(struct AddFileParam));
	/*
	 * From here on out, failures are more interesting.
	 */
	mNeedCDRewrite = true;
    /*
     * Write the LFH, even though it's still mostly blank.  We need it
     * as a place-holder.  In theory the LFH isn't necessary, but in
     * practice some utilities demand it.
     */
    param->lfhPosn = ftell(mZipFp);
    pEntry->mLFH.write(mZipFp);
    param->startPosn = ftell(mZipFp);
	param->pEntry = pEntry;
//	param->compressedLen = uncompressSize;
//	param->uncompressedLen = uncompressSize;
	*pparam = param;
	return NO_ERROR;

}

status_t ZipFile::FinishAddDeflatedFile(struct AddFileParam *param){
   /*
     * We could write the "Data Descriptor", but there doesn't seem to
     * be any point since we're going to go back and write the LFH.
     *
     * Update file offsets.
     */
    int endPosn = ftell(mZipFp);            // seeked to end of compressed data
	status_t result = NO_ERROR;
    /*
     * Success!  Fill out new values.
     */
    param->pEntry->setDataInfo(endPosn - param->startPosn, endPosn - param->startPosn, param->crc,
        				ZipEntry::kCompressStored);
    time_t modWhen = getModTime(fileno(mZipFp));
    param->pEntry->setModWhen(modWhen);
    param->pEntry->setLFHOffset(param->lfhPosn);
    mEOCD.mNumEntries++;
    mEOCD.mTotalNumEntries++;
    mEOCD.mCentralDirSize = 0;      // mark invalid; set by flush()
    mEOCD.mCentralDirOffset = endPosn;

    /*
     * Go back and write the LFH.
     */
    if (fseek(mZipFp, param->lfhPosn, SEEK_SET) != 0) {
        result = UNKNOWN_ERROR;
    }
    param->pEntry->mLFH.write(mZipFp);

    /*
     * Add pEntry to the list.
     */
    mEntries.push_back(param->pEntry);
	param->pEntry = NULL;
    return result;		

}



unsigned char* ZipFile::getCompressedData(const char* entryName, long& size){
	unsigned char* data = NULL;
	ZipEntry* pEntry = getEntryByName(entryName);
	if(pEntry){
		size = pEntry->getCompressedLen();
		long offset = pEntry->getFileOffset();
		if(fseek(mZipFp, offset, SEEK_SET) != -1){
			data = new unsigned char[size];
			if(fread(data, 1, size, mZipFp) == size)
				return data;
		}
		
	}
	delete data;
	return NULL;
}



/*
 * Add a new file to the archive.
 *
 * This requires creating and populating a ZipEntry structure, and copying
 * the data into the file at the appropriate position.  The "appropriate
 * position" is the current location of the central directory, which we
 * casually overwrite (we can put it back later).
 *
 * If we were concerned about safety, we would want to make all changes
 * in a temp file and then overwrite the original after everything was
 * safely written.  Not really a concern for us.
 */
status_t ZipFile::addCommon(const char* fileName, const void* data, size_t size,
    const char* storageName, int sourceType, int compressionMethod,
    ZipEntry** ppEntry)
{
    ZipEntry* pEntry = NULL;
    status_t result = NO_ERROR;
    long lfhPosn, startPosn, endPosn, uncompressedLen;
    FILE* inputFp = NULL;
    unsigned long crc;
    time_t modWhen;

    if (mReadOnly)
        return INVALID_OPERATION;

    assert(compressionMethod == ZipEntry::kCompressDeflated ||
           compressionMethod == ZipEntry::kCompressStored);

    /* make sure we're in a reasonable state */
    assert(mZipFp != NULL);
    assert(mEntries.size() == mEOCD.mTotalNumEntries);

    /* make sure it doesn't already exist */
    if (getEntryByName(storageName) != NULL)
        return ALREADY_EXISTS;

    if (!data) {
        inputFp = fopen(fileName, FILE_OPEN_RO);
        if (inputFp == NULL)
            return errnoToStatus(errno);
    }

    if (fseek(mZipFp, mEOCD.mCentralDirOffset, SEEK_SET) != 0) {
        result = UNKNOWN_ERROR;
        goto bail;
    }

    pEntry = new ZipEntry;
    pEntry->initNew(storageName, NULL);

    /*
     * From here on out, failures are more interesting.
     */
    mNeedCDRewrite = true;

    /*
     * Write the LFH, even though it's still mostly blank.  We need it
     * as a place-holder.  In theory the LFH isn't necessary, but in
     * practice some utilities demand it.
     */
	
    lfhPosn = ftell(mZipFp);
    pEntry->mLFH.write(mZipFp);
    startPosn = ftell(mZipFp);

    /*
     * Copy the data in, possibly compressing it as we go.
     */
    if (sourceType == ZipEntry::kCompressStored) {
        if (compressionMethod == ZipEntry::kCompressDeflated) {
            bool failed = false;
            result = compressFpToFp(mZipFp, inputFp, data, size, &crc);
            if (result != NO_ERROR) {
                LOGZ("compression failed, storing\n");
                failed = true;
            } else {
                /*
                 * Make sure it has compressed "enough".  This probably ought
                 * to be set through an API call, but I don't expect our
                 * criteria to change over time.
                 */
                long src = inputFp ? ftell(inputFp) : size;
                long dst = ftell(mZipFp) - startPosn;
                if (dst + (dst / 10) > src) {
                    LOGZ("insufficient compression (src=%ld dst=%ld), storing\n",
                        src, dst);
                    failed = true;
                }
            }

            if (failed) {
                compressionMethod = ZipEntry::kCompressStored;
                if (inputFp) rewind(inputFp);
                fseek(mZipFp, startPosn, SEEK_SET);
                /* fall through to kCompressStored case */
            }
        }
        /* handle "no compression" request, or failed compression from above */
        if (compressionMethod == ZipEntry::kCompressStored) {
            if (inputFp) {
                result = copyFpToFp(mZipFp, inputFp, &crc);
            } else {
                result = copyDataToFp(mZipFp, data, size, &crc);
            }
            if (result != NO_ERROR) {
                // don't need to truncate; happens in CDE rewrite
                LOGZ("failed copying data in\n");
                goto bail;
            }
        }

        // currently seeked to end of file
        uncompressedLen = inputFp ? ftell(inputFp) : size;
    } else if (sourceType == ZipEntry::kCompressDeflated) {
        /* we should support uncompressed-from-compressed, but it's not
         * important right now */
        assert(compressionMethod == ZipEntry::kCompressDeflated);

        bool scanResult;
        int method;
        long compressedLen;

        scanResult = ZipFile::examineGzip(inputFp, &method, &uncompressedLen,
                        &compressedLen, &crc);
        if (!scanResult || method != ZipEntry::kCompressDeflated) {
            LOGZ("this isn't a deflated gzip file?");
            result = UNKNOWN_ERROR;
            goto bail;
        }

        result = copyPartialFpToFp(mZipFp, inputFp, compressedLen, NULL);
        if (result != NO_ERROR) {
            LOGZ("failed copying gzip data in\n");
            goto bail;
        }
    } else {
        assert(false);
        result = UNKNOWN_ERROR;
        goto bail;
    }

    /*
     * We could write the "Data Descriptor", but there doesn't seem to
     * be any point since we're going to go back and write the LFH.
     *
     * Update file offsets.
     */
    endPosn = ftell(mZipFp);            // seeked to end of compressed data

    /*
     * Success!  Fill out new values.
     */
    pEntry->setDataInfo(uncompressedLen, endPosn - startPosn, crc,
        compressionMethod);
    modWhen = getModTime(inputFp ? fileno(inputFp) : fileno(mZipFp));
    pEntry->setModWhen(modWhen);
    pEntry->setLFHOffset(lfhPosn);
    mEOCD.mNumEntries++;
    mEOCD.mTotalNumEntries++;
    mEOCD.mCentralDirSize = 0;      // mark invalid; set by flush()
    mEOCD.mCentralDirOffset = endPosn;

    /*
     * Go back and write the LFH.
     */
    if (fseek(mZipFp, lfhPosn, SEEK_SET) != 0) {
        result = UNKNOWN_ERROR;
        goto bail;
    }
    pEntry->mLFH.write(mZipFp);

    /*
     * Add pEntry to the list.
     */
    mEntries.push_back(pEntry);
    if (ppEntry != NULL)
        *ppEntry = pEntry;
    pEntry = NULL;

bail:
    if (inputFp != NULL)
        fclose(inputFp);
    delete pEntry;
    return result;
}

/*
 * Add an entry by copying it from another zip file.  If "padding" is
 * nonzero, the specified number of bytes will be added to the "extra"
 * field in the header.
 *
 * If "ppEntry" is non-NULL, a pointer to the new entry will be returned.
 */
status_t ZipFile::add(const ZipFile* pSourceZip, const ZipEntry* pSourceEntry,
    int padding, ZipEntry** ppEntry)
{
    ZipEntry* pEntry = NULL;
    status_t result;
    long lfhPosn, endPosn;

    if (mReadOnly)
        return INVALID_OPERATION;

    /* make sure we're in a reasonable state */
    assert(mZipFp != NULL);
    assert(mEntries.size() == mEOCD.mTotalNumEntries);

    if (fseek(mZipFp, mEOCD.mCentralDirOffset, SEEK_SET) != 0) {
        result = UNKNOWN_ERROR;
        goto bail;
    }

    pEntry = new ZipEntry;
    if (pEntry == NULL) {
        result = NO_MEMORY;
        goto bail;
    }

    result = pEntry->initFromExternal(pSourceZip, pSourceEntry);
    if (result != NO_ERROR)
        goto bail;
    if (padding != 0) {
        result = pEntry->addPadding(padding);
        if (result != NO_ERROR)
            goto bail;
    }

    /*
     * From here on out, failures are more interesting.
     */
    mNeedCDRewrite = true;

    /*
     * Write the LFH.  Since we're not recompressing the data, we already
     * have all of the fields filled out.
     */
    lfhPosn = ftell(mZipFp);
    pEntry->mLFH.write(mZipFp);

    /*
     * Copy the data over.
     *
     * If the "has data descriptor" flag is set, we want to copy the DD
     * fields as well.  This is a fixed-size area immediately following
     * the data.
     */
    if (fseek(pSourceZip->mZipFp, pSourceEntry->getFileOffset(), SEEK_SET) != 0)
    {
        result = UNKNOWN_ERROR;
        goto bail;
    }

    off_t copyLen;
    copyLen = pSourceEntry->getCompressedLen();
    if ((pSourceEntry->mLFH.mGPBitFlag & ZipEntry::kUsesDataDescr) != 0)
        copyLen += ZipEntry::kDataDescriptorLen;

    if (copyPartialFpToFp(mZipFp, pSourceZip->mZipFp, copyLen, NULL)
        != NO_ERROR)
    {
        LOGZ("copy of '%s' failed\n", pEntry->mCDE.mFileName);
        result = UNKNOWN_ERROR;
        goto bail;
    }

    /*
     * Update file offsets.
     */
    endPosn = ftell(mZipFp);

    /*
     * Success!  Fill out new values.
     */
    pEntry->setLFHOffset(lfhPosn);      // sets mCDE.mLocalHeaderRelOffset
    mEOCD.mNumEntries++;
    mEOCD.mTotalNumEntries++;
    mEOCD.mCentralDirSize = 0;      // mark invalid; set by flush()
    mEOCD.mCentralDirOffset = endPosn;

    /*
     * Add pEntry to the list.
     */
    mEntries.push_back(pEntry);
    if (ppEntry != NULL)
        *ppEntry = pEntry;
    pEntry = NULL;

    result = NO_ERROR;

bail:
    delete pEntry;
    return result;
}

/*
 * Copy all of the bytes in "src" to "dst".
 *
 * On exit, "srcFp" will be seeked to the end of the file, and "dstFp"
 * will be seeked immediately past the data.
 */
status_t ZipFile::copyFpToFp(FILE* dstFp, FILE* srcFp, unsigned long* pCRC32)
{
    unsigned char tmpBuf[32768];
    size_t count;

    *pCRC32 = crc32(0L, Z_NULL, 0);

    while (1) {
        count = fread(tmpBuf, 1, sizeof(tmpBuf), srcFp);
        if (ferror(srcFp) || ferror(dstFp))
            return errnoToStatus(errno);
        if (count == 0)
            break;

        *pCRC32 = crc32(*pCRC32, tmpBuf, count);

        if (fwrite(tmpBuf, 1, count, dstFp) != count) {
            LOGZ("fwrite %d bytes failed\n", (int) count);
            return UNKNOWN_ERROR;
        }
    }

    return NO_ERROR;
}

/*
 * Copy all of the bytes in "src" to "dst".
 *
 * On exit, "dstFp" will be seeked immediately past the data.
 */
status_t ZipFile::copyDataToFp(FILE* dstFp,
    const void* data, size_t size, unsigned long* pCRC32)
{
    size_t count;

    *pCRC32 = crc32(0L, Z_NULL, 0);
    if (size > 0) {
        *pCRC32 = crc32(*pCRC32, (const unsigned char*)data, size);
        if (fwrite(data, 1, size, dstFp) != size) {
            LOGZ("fwrite %d bytes failed\n", (int) size);
            return UNKNOWN_ERROR;
        }
    }

    return NO_ERROR;
}

/*
 * Copy some of the bytes in "src" to "dst".
 *
 * If "pCRC32" is NULL, the CRC will not be computed.
 *
 * On exit, "srcFp" will be seeked to the end of the file, and "dstFp"
 * will be seeked immediately past the data just written.
 */
status_t ZipFile::copyPartialFpToFp(FILE* dstFp, FILE* srcFp, long length,
    unsigned long* pCRC32)
{
    unsigned char tmpBuf[32768];
    size_t count;

    if (pCRC32 != NULL)
        *pCRC32 = crc32(0L, Z_NULL, 0);

    while (length) {
        long readSize;
        
        readSize = sizeof(tmpBuf);
        if (readSize > length)
            readSize = length;

        count = fread(tmpBuf, 1, readSize, srcFp);
        if ((long) count != readSize) {     // error or unexpected EOF
            LOGZ("fread %d bytes failed\n", (int) readSize);
            return UNKNOWN_ERROR;
        }

        if (pCRC32 != NULL)
            *pCRC32 = crc32(*pCRC32, tmpBuf, count);

        if (fwrite(tmpBuf, 1, count, dstFp) != count) {
            LOGZ("fwrite %d bytes failed\n", (int) count);
            return UNKNOWN_ERROR;
        }

        length -= readSize;
    }

    return NO_ERROR;
}

/*
 * Compress all of the data in "srcFp" and write it to "dstFp".
 *
 * On exit, "srcFp" will be seeked to the end of the file, and "dstFp"
 * will be seeked immediately past the compressed data.
 */
status_t ZipFile::compressFpToFp(FILE* dstFp, FILE* srcFp,
    const void* data, size_t size, unsigned long* pCRC32)
{
    status_t result = NO_ERROR;
    const size_t kBufSize = 32768;
    unsigned char* inBuf = NULL;
    unsigned char* outBuf = NULL;
    z_stream zstream;
    bool atEof = false;     // no feof() aviailable yet
    unsigned long crc;
    int zerr;

    /*
     * Create an input buffer and an output buffer.
     */
    inBuf = new unsigned char[kBufSize];
    outBuf = new unsigned char[kBufSize];
    if (inBuf == NULL || outBuf == NULL) {
        result = NO_MEMORY;
        goto bail;
    }

    /*
     * Initialize the zlib stream.
     */
    memset(&zstream, 0, sizeof(zstream));
    zstream.zalloc = Z_NULL;
    zstream.zfree = Z_NULL;
    zstream.opaque = Z_NULL;
    zstream.next_in = NULL;
    zstream.avail_in = 0;
    zstream.next_out = outBuf;
    zstream.avail_out = kBufSize;
    zstream.data_type = Z_UNKNOWN;

    zerr = deflateInit2(&zstream, Z_BEST_COMPRESSION,
        Z_DEFLATED, -MAX_WBITS, DEF_MEM_LEVEL, Z_DEFAULT_STRATEGY);
    if (zerr != Z_OK) {
        result = UNKNOWN_ERROR;
        if (zerr == Z_VERSION_ERROR) {
            LOGZ("Installed zlib is not compatible with linked version (%s)\n",
                ZLIB_VERSION);
        } else {
            LOGZ("Call to deflateInit2 failed (zerr=%d)\n", zerr);
        }
        goto bail;
    }

    crc = crc32(0L, Z_NULL, 0);

    /*
     * Loop while we have data.
     */
    do {
        size_t getSize;
        int flush;

        /* only read if the input buffer is empty */
        if (zstream.avail_in == 0 && !atEof) {
            LOGW("+++ reading %d bytes\n", (int)kBufSize);
            if (data) {
                getSize = size > kBufSize ? kBufSize : size;
                memcpy(inBuf, data, getSize);
                data = ((const char*)data) + getSize;
                size -= getSize;
            } else {
                getSize = fread(inBuf, 1, kBufSize, srcFp);
                if (ferror(srcFp)) {
                    LOGW("deflate read failed (errno=%d)\n", errno);
                    goto z_bail;
                }
            }
            if (getSize < kBufSize) {
                LOGW("+++  got %d bytes, EOF reached\n",
                    (int)getSize);
                atEof = true;
            }

            crc = crc32(crc, inBuf, getSize);

            zstream.next_in = inBuf;
            zstream.avail_in = getSize;
        }

        if (atEof)
            flush = Z_FINISH;       /* tell zlib that we're done */
        else
            flush = Z_NO_FLUSH;     /* more to come! */

        zerr = deflate(&zstream, flush);
        if (zerr != Z_OK && zerr != Z_STREAM_END) {
            LOGZ("zlib deflate call failed (zerr=%d)\n", zerr);
            result = UNKNOWN_ERROR;
            goto z_bail;
        }

        /* write when we're full or when we're done */
        if (zstream.avail_out == 0 ||
            (zerr == Z_STREAM_END && zstream.avail_out != (uInt) kBufSize))
        {
            LOGW("+++ writing %d bytes\n", (int) (zstream.next_out - outBuf));
            if (fwrite(outBuf, 1, zstream.next_out - outBuf, dstFp) !=
                (size_t)(zstream.next_out - outBuf))
            {
                LOGZ("write %d failed in deflate\n",
                    (int) (zstream.next_out - outBuf));
                goto z_bail;
            }

            zstream.next_out = outBuf;
            zstream.avail_out = kBufSize;
        }
    } while (zerr == Z_OK);

    assert(zerr == Z_STREAM_END);       /* other errors should've been caught */

    *pCRC32 = crc;

z_bail:
    deflateEnd(&zstream);        /* free up any allocated structures */

bail:
    delete[] inBuf;
    delete[] outBuf;

    return result;
}

/*
 * Mark an entry as deleted.
 *
 * We will eventually need to crunch the file down, but if several files
 * are being removed (perhaps as part of an "update" process) we can make
 * things considerably faster by deferring the removal to "flush" time.
 */
status_t ZipFile::remove(ZipEntry* pEntry)
{
    /*
     * Should verify that pEntry is actually part of this archive, and
     * not some stray ZipEntry from a different file.
     */

    /* mark entry as deleted, and mark archive as dirty */
    pEntry->setDeleted();
    mNeedCDRewrite = true;
    return NO_ERROR;
}

/*
 * Flush any pending writes.
 *
 * In particular, this will crunch out deleted entries, and write the
 * Central Directory and EOCD if we have stomped on them.
 */
status_t ZipFile::flush(void)
{
    status_t result = NO_ERROR;
    long eocdPosn;
    int i, count;

    if (mReadOnly)
        return INVALID_OPERATION;
    if (!mNeedCDRewrite)
        return NO_ERROR;

    assert(mZipFp != NULL);

    result = crunchArchive();
    if (result != NO_ERROR)
        return result;

    if (fseek(mZipFp, mEOCD.mCentralDirOffset, SEEK_SET) != 0)
        return UNKNOWN_ERROR;

    count = mEntries.size();
    for (i = 0; i < count; i++) {
        ZipEntry* pEntry = mEntries[i];
        pEntry->mCDE.write(mZipFp);
    }

    eocdPosn = ftell(mZipFp);
    mEOCD.mCentralDirSize = eocdPosn - mEOCD.mCentralDirOffset;

    mEOCD.write(mZipFp);

    /*
     * If we had some stuff bloat up during compression and get replaced
     * with plain files, or if we deleted some entries, there's a lot
     * of wasted space at the end of the file.  Remove it now.
     */
    if (ftruncate(fileno(mZipFp), ftell(mZipFp)) != 0) {
        LOGZ("ftruncate failed %ld: %s\n", ftell(mZipFp), strerror(errno));
        // not fatal
    }

    /* should we clear the "newly added" flag in all entries now? */

    mNeedCDRewrite = false;
    return NO_ERROR;
}

/*
 * Crunch deleted files out of an archive by shifting the later files down.
 *
 * Because we're not using a temp file, we do the operation inside the
 * current file.
 */
status_t ZipFile::crunchArchive(void)
{
    status_t result = NO_ERROR;
    int i, count;
    long delCount, adjust;

#if 0
    printf("CONTENTS:\n");
    for (i = 0; i < (int) mEntries.size(); i++) {
        printf(" %d: lfhOff=%ld del=%d\n",
            i, mEntries[i]->getLFHOffset(), mEntries[i]->getDeleted());
    }
    printf("  END is %ld\n", (long) mEOCD.mCentralDirOffset);
#endif

    /*
     * Roll through the set of files, shifting them as appropriate.  We
     * could probably get a slight performance improvement by sliding
     * multiple files down at once (because we could use larger reads
     * when operating on batches of small files), but it's not that useful.
     */
    count = mEntries.size();
    delCount = adjust = 0;
    for (i = 0; i < count; i++) {
        ZipEntry* pEntry = mEntries[i];
        long span;

        if (pEntry->getLFHOffset() != 0) {
            long nextOffset;

            /* Get the length of this entry by finding the offset
             * of the next entry.  Directory entries don't have
             * file offsets, so we need to find the next non-directory
             * entry.
             */
            nextOffset = 0;
            for (int ii = i+1; nextOffset == 0 && ii < count; ii++)
                nextOffset = mEntries[ii]->getLFHOffset();
            if (nextOffset == 0)
                nextOffset = mEOCD.mCentralDirOffset;
            span = nextOffset - pEntry->getLFHOffset();

            assert(span >= ZipEntry::LocalFileHeader::kLFHLen);
        } else {
            /* This is a directory entry.  It doesn't have
             * any actual file contents, so there's no need to
             * move anything.
             */
            span = 0;
        }

        //printf("+++ %d: off=%ld span=%ld del=%d [count=%d]\n",
        //    i, pEntry->getLFHOffset(), span, pEntry->getDeleted(), count);

        if (pEntry->getDeleted()) {
            adjust += span;
            delCount++;

            delete pEntry;
            mEntries.erase(mEntries.begin() + i);

            /* adjust loop control */
            count--;
            i--;
        } else if (span != 0 && adjust > 0) {
            /* shuffle this entry back */
            //printf("+++ Shuffling '%s' back %ld\n",
            //    pEntry->getFileName(), adjust);
            result = filemove(mZipFp, pEntry->getLFHOffset() - adjust,
                        pEntry->getLFHOffset(), span);
            if (result != NO_ERROR) {
                /* this is why you use a temp file */
                LOGZ("error during crunch - archive is toast\n");
                return result;
            }

            pEntry->setLFHOffset(pEntry->getLFHOffset() - adjust);
        }
    }

    /*
     * Fix EOCD info.  We have to wait until the end to do some of this
     * because we use mCentralDirOffset to determine "span" for the
     * last entry.
     */
    mEOCD.mCentralDirOffset -= adjust;
    mEOCD.mNumEntries -= delCount;
    mEOCD.mTotalNumEntries -= delCount;
    mEOCD.mCentralDirSize = 0;  // mark invalid; set by flush()

    assert(mEOCD.mNumEntries == mEOCD.mTotalNumEntries);
    assert(mEOCD.mNumEntries == count);

    return result;
}

/*
 * Works like memmove(), but on pieces of a file.
 */
status_t ZipFile::filemove(FILE* fp, off_t dst, off_t src, size_t n)
{
    if (dst == src || n <= 0)
        return NO_ERROR;

    unsigned char readBuf[32768];

    if (dst < src) {
        /* shift stuff toward start of file; must read from start */
        while (n != 0) {
            size_t getSize = sizeof(readBuf);
            if (getSize > n)
                getSize = n;

            if (fseek(fp, (long) src, SEEK_SET) != 0) {
                LOGZ("filemove src seek %ld failed\n", (long) src);
                return UNKNOWN_ERROR;
            }

            if (fread(readBuf, 1, getSize, fp) != getSize) {
                LOGZ("filemove read %ld off=%ld failed\n",
                    (long) getSize, (long) src);
                return UNKNOWN_ERROR;
            }

            if (fseek(fp, (long) dst, SEEK_SET) != 0) {
                LOGZ("filemove dst seek %ld failed\n", (long) dst);
                return UNKNOWN_ERROR;
            }

            if (fwrite(readBuf, 1, getSize, fp) != getSize) {
                LOGZ("filemove write %ld off=%ld failed\n",
                    (long) getSize, (long) dst);
                return UNKNOWN_ERROR;
            }

            src += getSize;
            dst += getSize;
            n -= getSize;
        }
    } else {
        /* shift stuff toward end of file; must read from end */
        assert(false);      // write this someday, maybe
        return UNKNOWN_ERROR;
    }

    return NO_ERROR;
}


/*
 * Get the modification time from a file descriptor.
 */
time_t ZipFile::getModTime(int fd)
{
    struct stat sb;

    if (fstat(fd, &sb) < 0) {
        LOGZ("HEY: fstat on fd %d failed\n", fd);
        return (time_t) -1;
    }

    return sb.st_mtime;
}


#if 0       /* this is a bad idea */
/*
 * Get a copy of the Zip file descriptor.
 *
 * We don't allow this if the file was opened read-write because we tend
 * to leave the file contents in an uncertain state between calls to
 * flush().  The duplicated file descriptor should only be valid for reads.
 */
int ZipFile::getZipFd(void) const
{
    if (!mReadOnly)
        return INVALID_OPERATION;
    assert(mZipFp != NULL);

    int fd;
    fd = dup(fileno(mZipFp));
    if (fd < 0) {
        LOGZ("didn't work, errno=%d\n", errno);
    }

    return fd;
}
#endif


#if 0
/*
 * Expand data.
 */
bool ZipFile::uncompress(const ZipEntry* pEntry, void* buf) const
{
    return false;
}
#endif


bool ZipFile::uncompressToFile(ZipEntry* pEntry, FILE* pf)
{
 bool ret = true;
 try{
		const long kLargeSize = kCacheSize * 5;
		if(!pf)
			return INVALID_OPERATION;
		if(pEntry->getUncompressedLen() > kLargeSize){
		    fseek(mZipFp, 0, SEEK_SET);
    		off_t offset = pEntry->getFileOffset();
    		if (fseek(mZipFp, offset, SEEK_SET) != 0) {
        		throw("fseek failed");
    		}
			
			if(!inflateToFile(mZipFp, pf, pEntry))
				throw("uncompressToFile failed1");
		}
		else{
			void* data = uncompress(pEntry);
			if(!data || fwrite(data, pEntry->getUncompressedLen(), 1, pf) <= 0)
				throw("uncompressToFile failed2");
			free(data);
		}
	}
 catch(const char* errormsg){
	LOG(errormsg);
	ret = false;
 }
 return ret;
}


// free the memory when you're done
void* ZipFile::uncompress(const ZipEntry* entry)
{
    size_t unlen = entry->getUncompressedLen();
    size_t clen = entry->getCompressedLen();

    void* buf = malloc(unlen);
    if (buf == NULL) {
        return NULL;
    }

    fseek(mZipFp, 0, SEEK_SET);

    off_t offset = entry->getFileOffset();
    if (fseek(mZipFp, offset, SEEK_SET) != 0) {
        goto bail;
    }

    switch (entry->getCompressionMethod())
    {
        case ZipEntry::kCompressStored: {
            ssize_t amt = fread(buf, 1, unlen, mZipFp);
            if (amt != (ssize_t)unlen) {
                goto bail;
            }
#if 0
            printf("data...\n");
            const unsigned char* p = (unsigned char*)buf;
            const unsigned char* end = p+unlen;
            for (int i=0; i<32 && p < end; i++) {
                printf("0x%08x ", (int)(offset+(i*0x10)));
                for (int j=0; j<0x10 && p < end; j++) {
                    printf(" %02x", *p);
                    p++;
                }
                printf("\n");
            }
#endif

            }
            break;
        case ZipEntry::kCompressDeflated: {
            if (!ZipFile::inflateToBuffer(mZipFp, buf, unlen, clen)) {
                goto bail;
            }
            }
            break;
        default:
            goto bail;
    }
    return buf;

bail:
    free(buf);
    return NULL;
}


/*
 * ===========================================================================
 *      ZipFile::EndOfCentralDir
 * ===========================================================================
 */

/*
 * Read the end-of-central-dir fields.
 *
 * "buf" should be positioned at the EOCD signature, and should contain
 * the entire EOCD area including the comment.
 */
status_t ZipFile::EndOfCentralDir::readBuf(const unsigned char* buf, int len)
{
    /* don't allow re-use */
    assert(mComment == NULL);

    if (len < kEOCDLen) {
        /* looks like ZIP file got truncated */
        LOGZ(" Zip EOCD: expected >= %d bytes, found %d\n",
            kEOCDLen, len);
        return INVALID_OPERATION;
    }

    /* this should probably be an assert() */
    if (ZipEntry::getLongLE(&buf[0x00]) != kSignature)
        return UNKNOWN_ERROR;

    mDiskNumber = ZipEntry::getShortLE(&buf[0x04]);
    mDiskWithCentralDir = ZipEntry::getShortLE(&buf[0x06]);
    mNumEntries = ZipEntry::getShortLE(&buf[0x08]);
    mTotalNumEntries = ZipEntry::getShortLE(&buf[0x0a]);
    mCentralDirSize = ZipEntry::getLongLE(&buf[0x0c]);
    mCentralDirOffset = ZipEntry::getLongLE(&buf[0x10]);
    mCommentLen = ZipEntry::getShortLE(&buf[0x14]);

    // TODO: validate mCentralDirOffset

    if (mCommentLen > 0) {
        if (kEOCDLen + mCommentLen > len) {
            LOGZ("EOCD(%d) + comment(%d) exceeds len (%d)\n",
                kEOCDLen, mCommentLen, len);
            return UNKNOWN_ERROR;
        }
        mComment = new unsigned char[mCommentLen];
        memcpy(mComment, buf + kEOCDLen, mCommentLen);
    }

    return NO_ERROR;
}

/*
 * Write an end-of-central-directory section.
 */
status_t ZipFile::EndOfCentralDir::write(FILE* fp)
{
    unsigned char buf[kEOCDLen];

    ZipEntry::putLongLE(&buf[0x00], kSignature);
    ZipEntry::putShortLE(&buf[0x04], mDiskNumber);
    ZipEntry::putShortLE(&buf[0x06], mDiskWithCentralDir);
    ZipEntry::putShortLE(&buf[0x08], mNumEntries);
    ZipEntry::putShortLE(&buf[0x0a], mTotalNumEntries);
    ZipEntry::putLongLE(&buf[0x0c], mCentralDirSize);
    ZipEntry::putLongLE(&buf[0x10], mCentralDirOffset);
    ZipEntry::putShortLE(&buf[0x14], mCommentLen);

    if (fwrite(buf, 1, kEOCDLen, fp) != kEOCDLen)
        return UNKNOWN_ERROR;
    if (mCommentLen > 0) {
        assert(mComment != NULL);
        if (fwrite(mComment, mCommentLen, 1, fp) != mCommentLen)
            return UNKNOWN_ERROR;
    }

    return NO_ERROR;
}

/*
 * Dump the contents of an EndOfCentralDir object.
 */
void ZipFile::EndOfCentralDir::dump(void) const
{
    LOGW(" EndOfCentralDir contents:\n");
    LOGW("  diskNum=%u diskWCD=%u numEnt=%u totalNumEnt=%u\n",
        mDiskNumber, mDiskWithCentralDir, mNumEntries, mTotalNumEntries);
    LOGW("  centDirSize=%lu centDirOff=%lu commentLen=%u\n",
        mCentralDirSize, mCentralDirOffset, mCommentLen);
}

void ZipFile::releaseStream(z_stream& zstream){
	inflateEnd(&zstream); 
}
/*
status_ZipFIle::inflateToBuffer2(z_stream& zstream
								 , char* outBuffer
 	         			         , int outSize
								 , int& inflatedSize){
	zstream.next_out = (Bytef*) outBuffer;
	zstream.avail_out = outSize;
	int zerr = inflate(&zstream, Z_NO_FLUSH);
	if(Z_OK == zerr || Z_STREAM_END == zerr){
		inflatedSize += (outSize - zstream.avail_out);
	}
	return zerr;
}*/

status_t ZipFile::inflateToBuffer(z_stream& zstream, char* outBuffer, char* zipBuffer
									, long& startOffset
									, int outSize
									, int readSize){
	int zerr = 0;

	if(zstream.total_out == 0){
	   	memset(&zstream, 0, sizeof(zstream));
	   	zstream.zalloc = Z_NULL;
	   	zstream.zfree = Z_NULL;
	   	zstream.opaque = Z_NULL;
	   	zstream.next_in = NULL;
	   	zstream.avail_in = 0;
	   	zstream.next_out = (Bytef*) outBuffer;
	   	zstream.avail_out = outSize;
	   	zstream.data_type = Z_UNKNOWN;
		zerr = inflateInit2(&zstream, -MAX_WBITS);
		LOG("INIT!");
		if(zerr != Z_OK){
			return Z_STREAM_ERROR;
		}
	}
	else{
		zstream.next_out = (Bytef*) outBuffer;
	   	zstream.avail_out = outSize;
	}
	if (fseek(mZipFp, startOffset, SEEK_SET) != 0) {
		return Z_STREAM_ERROR;
	}

	
	int readRet = fread(zipBuffer, 1, readSize, mZipFp);
	if(readRet < 0){
		return Z_STREAM_ERROR;
	}
	zstream.next_in = (Bytef*)zipBuffer;
   	zstream.avail_in = readRet;
	zerr = inflate(&zstream, Z_NO_FLUSH);
	LOG("zerr = %d", zerr);
	/*while(zerr != Z_STREAM_END && zstream.avail_out){
		zerr =inflate(&zstream, Z_NO_FLUSH);
		LOG("zerr = %d zstream.avail_out = %d", zerr, zstream.avail_out);
		if(zerr != Z_OK)
			return zerr;
	}*/
	startOffset += readRet;
	LOG("zstream.avail_in = %d, zstream.avail_out = %d, zstream.total_out =%d",zstream.avail_in ,zstream.avail_out, zstream.total_out);
	return zerr;
	
	
}

bool ZipFile::inflateToFile(FILE* sourceFp,FILE* destFp, ZipEntry* pEntry)
{
	unsigned char* readBuf = NULL;
	const unsigned long kReadBufSize = 32768;
	readBuf = new unsigned char[kReadBufSize];
	bool result = false;
	long uncompressedLen = pEntry->getUncompressedLen();
	long compressedLen = pEntry->getCompressedLen();
	z_stream zstream;
	unsigned char* outBuf = NULL;
	if(pEntry->getCompressionMethod() == ZipEntry::kCompressStored){
		int remainSize = uncompressedLen;
		int readSize = 0;
		int readRet = 0;
		int writeRet = 0;
		do{
			readSize = remainSize > kReadBufSize ? kReadBufSize : remainSize;
			readRet = fread(readBuf, 1, readSize, sourceFp);
			if(readRet != readSize)
				goto z_bail;
			remainSize -= readRet;
			writeRet = fwrite(readBuf, readRet, 1, destFp);
		}while(remainSize > 0 && readRet == readSize && writeRet > 0);
	}
	else{
   		const unsigned long kOutBufSize = (uncompressedLen/compressedLen + 4) * kReadBufSize;
   	    int zerr;
   	    unsigned long compRemaining;
   
   	    assert(uncompressedLen >= 0);
   	    assert(compressedLen >= 0);
   
   
   		outBuf = new unsigned char[kOutBufSize];
   		long lastOutputSize = 0;
   		if (readBuf == NULL)
   	        goto bail;
   	    compRemaining = compressedLen;
   
   	    /*
   	     * Initialize the zlib stream.
   	     */
   		memset(&zstream, 0, sizeof(zstream));
   	    zstream.zalloc = Z_NULL;
   	    zstream.zfree = Z_NULL;
   	    zstream.opaque = Z_NULL;
   	    zstream.next_in = NULL;
   	    zstream.avail_in = 0;
   	    zstream.next_out = (Bytef*) outBuf;
   	    zstream.avail_out = kOutBufSize;
   	    zstream.data_type = Z_UNKNOWN;
   
   		/*
   		 * Use the undocumented "negative window bits" feature to tell zlib
   		 * that there's no zlib header waiting for it.
   		 */
   	    zerr = inflateInit2(&zstream, -MAX_WBITS);
   	    if (zerr != Z_OK) {
   	        if (zerr == Z_VERSION_ERROR) {
   	            LOGZ("Installed zlib is not compatible with linked version (%s)\n",
   	                ZLIB_VERSION);
   	        } else {
   	            LOGZ("Call to inflateInit2 failed (zerr=%d)\n", zerr);
   	        }
   	        goto bail;
   	    }
   	    /*
   	     * Loop while we have data.
   	     */
   	    do {
   	        unsigned long getSize;
   
   	        /* read as much as we can */
   	        if (zstream.avail_in == 0) {
   	            getSize = (compRemaining > kReadBufSize) ?
   	                        kReadBufSize : compRemaining;
   	            LOGW("+++ reading %ld bytes (%ld left)\n",
   	                getSize, compRemaining);
   
   	            int cc = fread(readBuf, 1, getSize, sourceFp);
    	            if (cc != (int) getSize) {
   	                LOGZ("inflate read failed (%d vs %ld)\n",
   	                    cc, getSize);
   	                goto z_bail;
   	            }
   
   	            compRemaining -= getSize;
   
   	            zstream.next_in = readBuf;
   	            zstream.avail_in = getSize;
   	        }
   
   	        /* uncompress the data */
   	        zerr = inflate(&zstream, Z_NO_FLUSH);
   			zstream.next_out = (Bytef*) outBuf;
   			zstream.avail_out = kOutBufSize;
   			int currentOutSize = zstream.total_out - lastOutputSize;
   			lastOutputSize = zstream.total_out;
   			int writeFileRet = 0;
   			if(currentOutSize){
   				writeFileRet = fwrite(outBuf, currentOutSize, 1, destFp);
   			}
   
   	        if (zerr != Z_OK && zerr != Z_STREAM_END && writeFileRet > 0) {
   	            LOGZ("zlib inflate call failed (zerr=%d)\n", zerr);
   	            goto z_bail;
   	        }
   			
   			/* output buffer holds all, so no need to write the output */
   	    } while (zerr == Z_OK);
   
   	    assert(zerr == Z_STREAM_END);       /* other errors should've been caught */
   
   	    if ((long) zstream.total_out != uncompressedLen) {
   	        LOGZ("Size mismatch on inflated file (%ld vs %ld)\n",
   	            zstream.total_out, uncompressedLen);
   	        goto z_bail;
   	    }
	}
    // success!
    result = true;

z_bail:
    inflateEnd(&zstream);        /* free up any allocated structures */
	

bail:
	delete[] readBuf;
	delete[] outBuf;
    return result;
}

/*
 * Utility function that expands zip/gzip "deflate" compressed data
 * into a buffer.
 *
 * (This is a clone of the previous function, but it takes a FILE* instead
 * of an fd.  We could pass fileno(fd) to the above, but we can run into
 * trouble when "fp" has a different notion of what fd's file position is.)
 *
 * "fp" is an open file positioned at the start of the "deflate" data
 * "buf" must hold at least "uncompressedLen" bytes.
 */
 bool ZipFile::inflateToBuffer(FILE* fp, void* buf,
    long uncompressedLen, long compressedLen)
{
    bool result = false;
	const unsigned long kReadBufSize = 32768;
	unsigned char* readBuf = NULL;
    z_stream zstream;
    int zerr;
    unsigned long compRemaining;

    assert(uncompressedLen >= 0);
    assert(compressedLen >= 0);

	readBuf = new unsigned char[kReadBufSize];
	if (readBuf == NULL)
        goto bail;
    compRemaining = compressedLen;

    /*
     * Initialize the zlib stream.
     */
	memset(&zstream, 0, sizeof(zstream));
    zstream.zalloc = Z_NULL;
    zstream.zfree = Z_NULL;
    zstream.opaque = Z_NULL;
    zstream.next_in = NULL;
    zstream.avail_in = 0;
    zstream.next_out = (Bytef*) buf;
    zstream.avail_out = uncompressedLen;
    zstream.data_type = Z_UNKNOWN;

	/*
	 * Use the undocumented "negative window bits" feature to tell zlib
	 * that there's no zlib header waiting for it.
	 */
    zerr = inflateInit2(&zstream, -MAX_WBITS);
    if (zerr != Z_OK) {
        if (zerr == Z_VERSION_ERROR) {
            LOGZ("Installed zlib is not compatible with linked version (%s)\n",
                ZLIB_VERSION);
        } else {
            LOGZ("Call to inflateInit2 failed (zerr=%d)\n", zerr);
        }
        goto bail;
    }

    /*
     * Loop while we have data.
     */
    do {
        unsigned long getSize;

        /* read as much as we can */
        if (zstream.avail_in == 0) {
            getSize = (compRemaining > kReadBufSize) ?
                        kReadBufSize : compRemaining;
            LOGW("+++ reading %ld bytes (%ld left)\n",
                getSize, compRemaining);

            int cc = fread(readBuf, 1, getSize, fp);
            if (cc != (int) getSize) {
                LOGZ("inflate read failed (%d vs %ld)\n",
                    cc, getSize);
                goto z_bail;
            }

            compRemaining -= getSize;

            zstream.next_in = readBuf;
            zstream.avail_in = getSize;
        }

        /* uncompress the data */
        zerr = inflate(&zstream, Z_NO_FLUSH);
        if (zerr != Z_OK && zerr != Z_STREAM_END) {
            LOGZ("zlib inflate call failed (zerr=%d)\n", zerr);
            goto z_bail;
        }

		/* output buffer holds all, so no need to write the output */
    } while (zerr == Z_OK);

    assert(zerr == Z_STREAM_END);       /* other errors should've been caught */

    if ((long) zstream.total_out != uncompressedLen) {
        LOGZ("Size mismatch on inflated file (%ld vs %ld)\n",
            zstream.total_out, uncompressedLen);
        goto z_bail;
    }

    // success!
    result = true;

z_bail:
    inflateEnd(&zstream);        /* free up any allocated structures */

bail:
	delete[] readBuf;
    return result;
}


/*
 * Look at the contents of a gzip archive.  We want to know where the
 * data starts, and how long it will be after it is uncompressed.
 *
 * We expect to find the CRC and length as the last 8 bytes on the file.
 * This is a pretty reasonable thing to expect for locally-compressed
 * files, but there's a small chance that some extra padding got thrown
 * on (the man page talks about compressed data written to tape).  We
 * don't currently deal with that here.  If "gzip -l" whines, we're going
 * to fail too.
 *
 * On exit, "fp" is pointing at the start of the compressed data.
 */
/*static*/ bool ZipFile::examineGzip(FILE* fp, int* pCompressionMethod,
    long* pUncompressedLen, long* pCompressedLen, unsigned long* pCRC32)
{
    enum {  // flags
        FTEXT       = 0x01,
        FHCRC       = 0x02,
        FEXTRA      = 0x04,
        FNAME       = 0x08,
        FCOMMENT    = 0x10,
    };
    int ic;
    int method, flags;
    int i;

    ic = getc(fp);
    if (ic != 0x1f || getc(fp) != 0x8b)
        return false;       // not gzip
    method = getc(fp);
    flags = getc(fp);

    /* quick sanity checks */
    if (method == EOF || flags == EOF)
        return false;
    if (method != ZipEntry::kCompressDeflated)
        return false;

    /* skip over 4 bytes of mod time, 1 byte XFL, 1 byte OS */
    for (i = 0; i < 6; i++)
        (void) getc(fp);
    /* consume "extra" field, if present */
    if ((flags & FEXTRA) != 0) {
        int len;

        len = getc(fp);
        len |= getc(fp) << 8;
        while (len-- && getc(fp) != EOF)
            ;
    }
    /* consume filename, if present */
    if ((flags & FNAME) != 0) {
        do {
            ic = getc(fp);
        } while (ic != 0 && ic != EOF);
    }
    /* consume comment, if present */
    if ((flags & FCOMMENT) != 0) {
        do {
            ic = getc(fp);
        } while (ic != 0 && ic != EOF);
    }
    /* consume 16-bit header CRC, if present */
    if ((flags & FHCRC) != 0) {
        (void) getc(fp);
        (void) getc(fp);
    }

    if (feof(fp) || ferror(fp))
        return false;

    /* seek to the end; CRC and length are in the last 8 bytes */
    long curPosn = ftell(fp);
    unsigned char buf[8];
    fseek(fp, -8, SEEK_END);
    *pCompressedLen = ftell(fp) - curPosn;

    if (fread(buf, 1, 8, fp) != 8)
        return false;
    /* seek back to start of compressed data */
    fseek(fp, curPosn, SEEK_SET);

    *pCompressionMethod = method;
    *pCRC32 = ZipFile::get4LE(&buf[0]);
    *pUncompressedLen = ZipFile::get4LE(&buf[4]);

    return true;
}


