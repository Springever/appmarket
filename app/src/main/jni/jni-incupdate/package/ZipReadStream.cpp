#include "ZipReadStream.h"
#include "inc_log.h"

ZipReadStream::ZipReadStream():m_currentFileOffset(0),m_currentBufferOffset(0)
							  , m_bufferSize(0), m_readBuffer(0)
							  ,m_uncompressSize(0),m_compressSize(0)
							   ,m_readDataSize(0),m_readStreamOffset(0)
							   ,m_needReleaseStream(false),m_zipBuffer(0)
{
	
}

ZipReadStream::~ZipReadStream(){
	delete[] m_readBuffer;
	delete[] m_zipBuffer;
	if(m_needReleaseStream)
		ZipFile::releaseStream(m_zSstream);
}

status_t ZipReadStream::init(const ZipFile* pFile, const ZipEntry* pEntry){
	if(m_readBuffer)
		return UNKNOWN_ERROR;
	m_uncompressSize = pEntry->getUncompressedLen();
	m_compressSize = pEntry->getCompressedLen();
	m_currentFileOffset = pEntry->getFileOffset();
	int ratio = m_uncompressSize/m_compressSize;
	m_readBuffserSize = (ratio + 4) *  KZipBufferSize;
	m_readBuffer = new char[m_readBuffserSize];
	m_zipBuffer = new char[KZipBufferSize];
	m_file = pFile;
	m_entry = pEntry;
	return NO_ERROR;
}

ZipReadStream* ZipReadStream::createReadStream(const ZipFile * pFile,const ZipEntry * pEntry){
	ZipReadStream* pStream = new ZipReadStream();
	if(pStream->init(pFile, pEntry) != NO_ERROR){
		delete pStream;
		pStream = NULL;
	}
	return pStream;
}

int ZipReadStream::readAsStream(char* readBuffer, long readSize){
	int readRet = read(readBuffer, readSize, m_readStreamOffset);
	if(readRet > 0)
		m_readStreamOffset += readRet;
	return readRet;
}

int ZipReadStream::read(char* readBuffer, long readSize, long startOffset){
	//LOG("readSize = %d, startOffset = %d, m_uncompressSize = %d, m_currentBufferOffset = %d,m_readDataSize = %d "
	//	, readSize, startOffset, m_uncompressSize, m_currentBufferOffset, m_readDataSize);

	if(startOffset < m_currentBufferOffset)
		return -1;
	if(startOffset >= m_uncompressSize)
		return 0;
	while(startOffset > m_currentBufferOffset + m_readDataSize){
		int ret = inflateToBuffer();
		
		if(Z_OK != ret && Z_STREAM_END != ret){
			LOG("inflateFailed!");
			return -1;
		}
	}

	int copyOffset = startOffset - m_currentBufferOffset;
	int remainLength = m_readDataSize - copyOffset;
	int copiedLength = readSize > remainLength ? remainLength : readSize;
	//LOG("copyOffset = %d, remainLength = %d, copiedLength = %d",
		//	copyOffset, remainLength, copiedLength);
	memcpy(readBuffer, m_readBuffer + copyOffset , copiedLength);
	while(startOffset + readSize > m_currentBufferOffset + m_readDataSize
			&& copiedLength < readSize && startOffset + readSize <= m_uncompressSize){
		int ret = inflateToBuffer();
		if(Z_OK != ret && Z_STREAM_END != ret){
				LOG("inflateFailed!2 ret = %d", ret);
				return -1;
			}
		int needCopy = (readSize - copiedLength) > m_readDataSize ? m_readDataSize:(readSize - copiedLength);
		memcpy(readBuffer + copiedLength, m_readBuffer, needCopy);
		copiedLength += needCopy;
		//LOG("copeidLength = %d, needCopy = %d, m_currentBufferOffset = %d, m_readDataSize = %d",
		//	copiedLength, needCopy, m_currentBufferOffset, m_readDataSize);
	}
	return copiedLength;
}

status_t ZipReadStream::inflateToBuffer(){

	int lastOffset = m_currentBufferOffset;
	int remainFileSize = m_compressSize - m_currentFileOffset + m_entry->getFileOffset();
	if(remainFileSize <= 0){
		LOG("ZipReadStream::inflateToBuffer m_compressSize = %d,m_currentFileOffset = %d, remainFileSize = %d"
			,m_compressSize, m_currentFileOffset, remainFileSize);
		return Z_STREAM_END;
	}
	long readZipSize = remainFileSize > KZipBufferSize ? KZipBufferSize:remainFileSize;
		
	int ret = ((ZipFile*)m_file)->inflateToBuffer(m_zSstream,
												  m_readBuffer,
												  m_zipBuffer,
												  m_currentFileOffset,
												  m_readBuffserSize,
												  readZipSize);

	if(ret == Z_OK)
		m_needReleaseStream = true;
	if(Z_OK == ret || Z_STREAM_END == ret){
		m_currentBufferOffset += m_readDataSize;
		m_readDataSize = m_readBuffserSize - m_zSstream.avail_out;
		
	}
	else
		m_readDataSize = 0;
	if(Z_STREAM_END == ret){
		ZipFile::releaseStream(m_zSstream);
		m_needReleaseStream = false;
	}
	//LOG("ZipReadStream::inflateToBuffer ret = %d,m_readBuffserSize =%d,  m_currentBufferOffset = %d, m_currentFileOffset =%d, m_readDataSize =%d"
		//, ret, m_readBuffserSize, m_currentBufferOffset, m_currentFileOffset, m_readDataSize);
	return ret;
}

unsigned char* ZipReadStream:: uncompressAll(){
	return (unsigned char*)((ZipFile*)m_file)->uncompress(m_entry);
}



bool ZipReadStream::getAt(unsigned long offset, char& c){
	//LOG("offset = %d, m_currentBufferOffset = %d m_readDataSize = %d",
	//		offset, m_currentBufferOffset, m_readDataSize);
	if(offset < m_currentBufferOffset || offset > m_uncompressSize){
		LOG("Invalide offset = %d, m_currentBufferOffset = %d, m_uncompressSize = %d",
			offset, m_currentBufferOffset, m_uncompressSize);
		return false;
	}
	if(offset >= m_currentBufferOffset + m_readDataSize){
		//Need to uncompress more data;
		do{
			int ret = inflateToBuffer();
			if(Z_OK != ret && Z_STREAM_END != ret){
				LOG("inflateToBuffer ret in getAt = %d", ret);
			   return false;
			}
		}while(offset > m_currentBufferOffset + m_readDataSize);
	}
	
	c = m_readBuffer[offset - m_currentBufferOffset];
	return true;
}



