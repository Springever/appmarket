/*-
 * Copyright 2003-2005 Colin Percival
 * All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted providing that the following conditions 
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

#if 0
__FBSDID("$FreeBSD: src/usr.bin/bsdiff/bspatch/bspatch.c,v 1.1 2005/08/06 01:59:06 cperciva Exp $");
#endif

#include <bzlib.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <err.h>
#include <unistd.h>
#include <fcntl.h>
#include <jni.h>
#include <zlib.h>
#include "../Package/ZipReadStream.h"
#include "../Package/ZipWriteStream.h"
#include "../Package/ZipWriteHelper.h"
#include "Inc_log.h"

static off_t offtin(u_char *buf)
{
	off_t y;

	y=buf[7]&0x7F;
	y=y*256;y+=buf[6];
	y=y*256;y+=buf[5];
	y=y*256;y+=buf[4];
	y=y*256;y+=buf[3];
	y=y*256;y+=buf[2];
	y=y*256;y+=buf[1];
	y=y*256;y+=buf[0];

	if(buf[7]&0x80) y=-y;

	return y;
}
/*
int doPatchWithZipStream(ZipFile* rawZipFile
				, ZipWriteStream& newFileStream
				, const char* patchFilePath
				,const char* entryName
				, unsigned long& crc){
	long readfiletime,writeziptime, temptime;
	readfiletime = writeziptime = temptime =0;
	FILE * f, * cpf, * dpf, * epf;
	int cbz2err, dbz2err, ebz2err;
	int fd;
	const int KNewBufferSize = 1024 * 8;
	ssize_t oldsize,newsize;
	ssize_t bzctrllen,bzdatalen;
	u_char header[32],buf[8];
	u_char *newBuffer;
	off_t oldpos,newpos;
	off_t ctrl[3];
	off_t lenread;
	off_t i;
	LOG("in doPatch");
	if(!patchFilePath || !entryName){
		LOGERRX("File name NULL!");
	}
	
	/* Open patch file *
	if ((f = fopen(patchFilePath, "r")) == NULL){
		LOGERRX("open file failed!");
	}
	LOG("in doPatch2");

	/*
	File format:
		0	8	"BSDIFF40"
		8	8	X
		16	8	Y
		24	8	sizeof(newfile)
		32	X	bzip2(control block)
		32+X	Y	bzip2(diff block)
		32+X+Y	???	bzip2(extra block)
	with control block a set of triples (x,y,z) meaning "add x bytes
	from oldfile to x bytes from the diff block; copy y bytes from the
	extra block; seek forwards in oldfile by z bytes".
	*/

	/* Read header *
	if (fread(header, 1, 32, f) < 32) {
		if (feof(f)){
			LOGERRX("Read head failed!");
			}
		
	}

	/* Check for appropriate magic *
	if (memcmp(header, "BSDIFF40", 8) != 0){
		LOGERRX("magic error!");
	
	}
	/* Read lengths from header *
	bzctrllen=offtin(header+8);
	bzdatalen=offtin(header+16);
	newsize=offtin(header+24);
	if((bzctrllen<0) || (bzdatalen<0) || (newsize<0)){
		LOGERRX("Corrupt patch!");
		
	}

	/* Close patch file and re-open it via libbzip2 at the right places *
	if (fclose(f)){
		LOGERRX("fclose(%s)", patchFilePath);
		}
	if ((cpf = fopen(patchFilePath, "r")) == NULL){
		LOGERRX("fopen(%s)", patchFilePath);
		}
	if (fseeko(cpf, 32, SEEK_SET)){
		LOGERRX("fseeko(%s, %lld)", patchFilePath,
		    (long long)32);
		}

	if ((dpf = fopen(patchFilePath, "r")) == NULL){
		LOGERRX( "fopen(%s)", patchFilePath);
		}
	if (fseeko(dpf, 32 + bzctrllen, SEEK_SET)){
		LOGERRX("fseeko(%s, %lld)", patchFilePath,
		    (long long)(32 + bzctrllen));
		}

	if ((epf = fopen(patchFilePath, "r")) == NULL)
		{
		LOGERRX("fopen(%s)", patchFilePath);
		}
	if (fseeko(epf, 32 + bzctrllen + bzdatalen, SEEK_SET))
		{
		LOGERRX("fseeko(%s, %lld)", patchFilePath,
		    (long long)(32 + bzctrllen + bzdatalen));
		}
	
	/*if(((fd=open(oldFilePath,O_RDONLY,0))<0) ||
		((oldsize=lseek(fd,0,SEEK_END))==-1) ||
		((old=malloc(oldsize+1))==NULL) ||
		(lseek(fd,0,SEEK_SET)!=0) ||
		(read(fd,old,oldsize)!=oldsize) ||
		(close(fd)==-1)){
			LOGERRX("%s",oldFilePath);
		}*
	//new=malloc(newsize+1);
	newBuffer = (u_char*)malloc(KNewBufferSize);
	oldsize = 0;
	unsigned char *old = rawZipFile->getCompressedData(entryName , oldsize);
	
	oldpos=0;newpos=0;
	unsigned long logSize= 0;
	struct timeval time; 
	while(newpos<newsize) {
		/* Read control data *
		for(i=0;i<=2;i++) {
			lenread = fread(buf, 1, 8, cpf);
			//lenread = BZ2_bzRead(&cbz2err, cpfbz2, buf, 8);
			if (lenread < 8){
				LOGERRX("Corrupt patch\n");
				}
			ctrl[i]=offtin(buf);
		};

		/* Sanity-check *
		if(newpos+ctrl[0]>newsize){
			LOGERRX("Corrupt patch\n");
		}

	//	LOG("newpos = %u, newSize = %u", newpos, newsize);

		unsigned long readSize = 0;
		unsigned long totalReadSize = 0;
		unsigned long dataSize = ctrl[0];
		unsigned long remainSize = 0;
		bool lastBlock = false;
		bool finish = false;
		if(	newsize == newpos + ctrl[0] + ctrl[1])
			lastBlock = true;
		logSize += ctrl[0];
		logSize += ctrl[1];
		
		while(totalReadSize < dataSize){
			remainSize = dataSize - totalReadSize;
		//	LOG("remainSize = %d dataSize = %d totalReadSize = %d", remainSize, dataSize, totalReadSize);
		//	readSize = remainSize > KNewBufferSize ? KNewBufferSize : remainSize;


			gettimeofday(&time, NULL); 
			temptime = ((long)time.tv_sec)*1000+(long)time.tv_usec/1000; 

			lenread = fread(newBuffer , 1, readSize, dpf);

			gettimeofday(&time, NULL);
			readfiletime += (((long)time.tv_sec)*1000+(long)time.tv_usec/1000 - temptime); 

			
			if(lenread != readSize)
				LOGERRX("read file error\n");
			for(i=0;i<readSize;i++){
				if((oldpos+i>=0) && (oldpos+i<oldsize)){
						newBuffer[i] += old[oldpos+i];
					}
				}
			oldpos += lenread;
			//Write newBuffer to new file's zipStream;
			finish = false;
			totalReadSize += lenread;
			if(lastBlock && totalReadSize == dataSize && ctrl[1] == 0)
				finish = true;

			gettimeofday(&time, NULL); 
			temptime = ((long)time.tv_sec)*1000+(long)time.tv_usec/1000; 

			
			if(NO_ERROR != newFileStream.write(newBuffer, lenread, finish))
				LOGERRX("newFileStream.writeData failed!");

			gettimeofday(&time, NULL); 
			writeziptime += (((long)time.tv_sec)*1000+(long)time.tv_usec/1000 - temptime);
			
		}
		

		//--------------------------------


		/* Read diff string */
	/*	lenread = fread(new + newpos, 1, ctrl[0], dpf);
		//lenread = BZ2_bzRead(&dbz2err, dpfbz2, new + newpos, ctrl[0]);
		if (lenread < ctrl[0]){
			LOGERRX("Corrupt patch\n");
			}
		//* Add old data to diff string
		for(i=0;i<ctrl[0];i++)
			if((oldpos+i>=0) && (oldpos+i<oldsize))
				new[newpos+i]+=rawFileStream.getAt(oldpos+i);*/




		/* Adjust pointers *
		newpos+=ctrl[0];
		//oldpos+=ctrl[0];

		/* Sanity-check *
		if(newpos+ctrl[1]>newsize){
			LOGERRX("Corrupt patch\n");
		}

		readSize = 0;
		totalReadSize = 0;
		dataSize = ctrl[1];
		remainSize = 0;

		while(totalReadSize < dataSize){
			remainSize = dataSize - totalReadSize;
			readSize = remainSize > KNewBufferSize ? KNewBufferSize : remainSize;
 
			gettimeofday(&time, NULL); 
			temptime= ((long)time.tv_sec)*1000+(long)time.tv_usec/1000; 

			
			lenread = fread(newBuffer, 1, readSize, epf);

			gettimeofday(&time, NULL); 
			readfiletime += (((long)time.tv_sec)*1000+(long)time.tv_usec/1000 - temptime); 

			
			if(lenread != readSize)
				LOGERRX("read file error2\n");
			totalReadSize += lenread;
			if(lastBlock && totalReadSize == dataSize)
				finish = true;

			gettimeofday(&time, NULL); 
			temptime = ((long)time.tv_sec)*1000+(long)time.tv_usec/1000; 

			
			newFileStream.write(newBuffer, lenread, finish);
			
			gettimeofday(&time, NULL); 
			
			writeziptime += (((long)time.tv_sec)*1000+(long)time.tv_usec/1000 - temptime); 

		}
		
		* Read extra string *
	/*	lenread = fread(newBuffer + newpos, 1, ctrl[1], epf);
		//lenread = BZ2_bzRead(&ebz2err, epfbz2, new + newpos, ctrl[1]);
		if (lenread < ctrl[1]){
			LOGERRX("Corrupt patch\n");
		}
		/* Adjust pointers *
		newpos+=ctrl[1];
		oldpos+=ctrl[2];
	};

	gettimeofday(&time, NULL); 
	temptime = ((long)time.tv_sec)*1000+(long)time.tv_usec/1000; 

	
	newFileStream.finish(crc);

	gettimeofday(&time, NULL); 
	
	writeziptime += (((long)time.tv_sec)*1000+(long)time.tv_usec/1000 - temptime); 

	
	LOG("readfiletime = %d writeziptime = %d",readfiletime, writeziptime);

	/* Clean up the bzip2 reads *
	if (fclose(cpf) || fclose(dpf) || fclose(epf)){
		LOGERRX("fclose(%s)", patchFilePath);
	}
	free(newBuffer);
	free(old);
	return 0;

}
*/

int doPatchInMem(const char* oldFileName
				, ZipWriteHelper& newFileStream
				, const char* patchFilePath
				, unsigned long& crc){
	long readfiletime,writeziptime, temptime;
	readfiletime = writeziptime = temptime =0;
	FILE * f, * cpf, * dpf, * epf;
	int cbz2err, dbz2err, ebz2err;
	int fd;
	const int KNewBufferSize = 1024 * 80;
	ssize_t oldsize,newsize;
	ssize_t bzctrllen,bzdatalen;
	u_char header[32],buf[8];
	u_char *newBuffer;
	off_t oldpos,newpos;
	off_t ctrl[3];
	off_t lenread;
	off_t i;
	LOG("in doPatch");
	if(!patchFilePath){
		LOGERRX("File name NULL!");
	}
	
	/* Open patch file */
	if ((f = fopen(patchFilePath, "r")) == NULL){
		LOGERRX("open file failed!");
	}
	LOG("in doPatch2");

	/*
	File format:
		0	8	"BSDIFF40"
		8	8	X
		16	8	Y
		24	8	sizeof(newfile)
		32	X	bzip2(control block)
		32+X	Y	bzip2(diff block)
		32+X+Y	???	bzip2(extra block)
	with control block a set of triples (x,y,z) meaning "add x bytes
	from oldfile to x bytes from the diff block; copy y bytes from the
	extra block; seek forwards in oldfile by z bytes".
	*/

	/* Read header */
	if (fread(header, 1, 32, f) < 32) {
		if (feof(f)){
			LOGERRX("Read head failed!");
			}
		
	}

	/* Check for appropriate magic */
	if (memcmp(header, "BSDIFF40", 8) != 0){
		LOGERRX("magic error!");
	
	}
	/* Read lengths from header */
	bzctrllen=offtin(header+8);
	bzdatalen=offtin(header+16);
	newsize=offtin(header+24);
	if((bzctrllen<0) || (bzdatalen<0) || (newsize<0)){
		LOGERRX("Corrupt patch1!");
		
	}

	/* Close patch file and re-open it via libbzip2 at the right places */
	if (fclose(f)){
		LOGERRX("fclose(%s)", patchFilePath);
		}
	if ((cpf = fopen(patchFilePath, "r")) == NULL){
		LOGERRX("fopen(%s)", patchFilePath);
		}
	if (fseeko(cpf, 32, SEEK_SET)){
		LOGERRX("fseeko(%s, %lld)", patchFilePath,
		    (long long)32);
		}

	if ((dpf = fopen(patchFilePath, "r")) == NULL){
		LOGERRX( "fopen(%s)", patchFilePath);
		}
	if (fseeko(dpf, 32 + bzctrllen, SEEK_SET)){
		LOGERRX("fseeko(%s, %lld)", patchFilePath,
		    (long long)(32 + bzctrllen));
		}

	if ((epf = fopen(patchFilePath, "r")) == NULL)
		{
		LOGERRX("fopen(%s)", patchFilePath);
		}
	if (fseeko(epf, 32 + bzctrllen + bzdatalen, SEEK_SET))
		{
		LOGERRX("fseeko(%s, %lld)", patchFilePath,
		    (long long)(32 + bzctrllen + bzdatalen));
		}
	
	/*if(((fd=open(oldFilePath,O_RDONLY,0))<0) ||
		((oldsize=lseek(fd,0,SEEK_END))==-1) ||
		((old=malloc(oldsize+1))==NULL) ||
		(lseek(fd,0,SEEK_SET)!=0) ||
		(read(fd,old,oldsize)!=oldsize) ||
		(close(fd)==-1)){
			LOGERRX("%s",oldFilePath);
		}*/
	//new=malloc(newsize+1);
	FILE* oldFile = fopen(oldFileName, "r");
	if(oldFile && fseek(oldFile, 0, SEEK_END) != -1
		&& 0 < (oldsize = ftell(oldFile))
		&& fseek(oldFile, 0,SEEK_SET) != -1){
		LOG("oldSize = %u", oldsize);
	}
	else{
		LOGERRX("oldFile failed!");
	}
	
	
	newBuffer = new unsigned char[KNewBufferSize];
	unsigned char *old = new unsigned char[KNewBufferSize];
	oldpos=0;newpos=0;
	struct timeval time; 
	while(newpos<newsize) {
		/* Read control data */
		for(i=0;i<=2;i++) {
			lenread = fread(buf, 1, 8, cpf);
			//lenread = BZ2_bzRead(&cbz2err, cpfbz2, buf, 8);
			if (lenread < 8){
				LOGERRX("Corrupt patch2\n");
				}
			ctrl[i]=offtin(buf);
		};

		/* Sanity-check */
	//	LOG("newpos = %u, newSize = %u ctrl[0] = %u", newpos, newsize, ctrl[0]);
		if(newpos+ctrl[0]>newsize){
			LOGERRX("Corrupt patch3\n");
		}

		
		if(fseek(oldFile, oldpos, SEEK_SET) == -1){
			LOG("seek oldfile Failed!");
		}

		unsigned long readSize = 0;
		unsigned long totalReadSize = 0;
		unsigned long dataSize = ctrl[0];
		unsigned long remainSize = 0;
		bool lastBlock = false;
		bool finish = false;
		if(	newsize == newpos + ctrl[0] + ctrl[1])
			lastBlock = true;
		while(totalReadSize < dataSize){
			remainSize = dataSize - totalReadSize;
			readSize = remainSize > KNewBufferSize ? KNewBufferSize : remainSize;


			gettimeofday(&time, NULL); 
			temptime = ((long)time.tv_sec)*1000+(long)time.tv_usec/1000; 

			//lenread = fread(newBuffer , 1, readSize, dpf);
			if((lenread = fread(newBuffer , 1, readSize, dpf)) != readSize 
				//|| fseek(oldFile, oldpos, SEEK_SET) == -1
				|| lenread != fread(old, 1, lenread, oldFile))
				LOGERRX("read file error\n");
			

			gettimeofday(&time, NULL);
			readfiletime += (((long)time.tv_sec)*1000+(long)time.tv_usec/1000 - temptime); 

			for(i=0;i<readSize;i++){
				if((oldpos+i>=0) && (oldpos+i<oldsize)){

						newBuffer[i] += old[i];
					}
				}
			//Write newBuffer to new file's zipStream;
			finish = false;
			totalReadSize += lenread;
			if(lastBlock && totalReadSize == dataSize && ctrl[1] == 0)
				finish = true;

			gettimeofday(&time, NULL); 
			temptime = ((long)time.tv_sec)*1000+(long)time.tv_usec/1000; 

			
			if(NO_ERROR != newFileStream.write(newBuffer, lenread, finish))
				LOGERRX("newFileStream.writeData failed!");

			gettimeofday(&time, NULL); 
			writeziptime += (((long)time.tv_sec)*1000+(long)time.tv_usec/1000 - temptime);
			
		}
		

		//--------------------------------


		/* Read diff string */
	/*	lenread = fread(new + newpos, 1, ctrl[0], dpf);
		//lenread = BZ2_bzRead(&dbz2err, dpfbz2, new + newpos, ctrl[0]);
		if (lenread < ctrl[0]){
			LOGERRX("Corrupt patch\n");
			}
		//* Add old data to diff string
		for(i=0;i<ctrl[0];i++)
			if((oldpos+i>=0) && (oldpos+i<oldsize))
				new[newpos+i]+=rawFileStream.getAt(oldpos+i);*/




		/* Adjust pointers */
		newpos+=ctrl[0];
		oldpos+=ctrl[0];

		/* Sanity-check */
		if(newpos+ctrl[1]>newsize){
			LOGERRX("Corrupt patch4\n");
		}

		readSize = 0;
		totalReadSize = 0;
		dataSize = ctrl[1];
		remainSize = 0;

		while(totalReadSize < dataSize){
			remainSize = dataSize - totalReadSize;
			readSize = remainSize > KNewBufferSize ? KNewBufferSize : remainSize;
 
			gettimeofday(&time, NULL); 
			temptime= ((long)time.tv_sec)*1000+(long)time.tv_usec/1000; 

			
			lenread = fread(newBuffer, 1, readSize, epf);

			gettimeofday(&time, NULL); 
			readfiletime += (((long)time.tv_sec)*1000+(long)time.tv_usec/1000 - temptime); 

			
			if(lenread != readSize)
				LOGERRX("read file error2\n");
			totalReadSize += lenread;
			if(lastBlock && totalReadSize == dataSize)
				finish = true;

			gettimeofday(&time, NULL); 
			temptime = ((long)time.tv_sec)*1000+(long)time.tv_usec/1000; 

			
			newFileStream.write(newBuffer, lenread, finish);
			
			gettimeofday(&time, NULL); 
			
			writeziptime += (((long)time.tv_sec)*1000+(long)time.tv_usec/1000 - temptime); 

		}
		
		/* Read extra string */
	/*	lenread = fread(newBuffer + newpos, 1, ctrl[1], epf);
		//lenread = BZ2_bzRead(&ebz2err, epfbz2, new + newpos, ctrl[1]);
		if (lenread < ctrl[1]){
			LOGERRX("Corrupt patch\n");
		}
		/* Adjust pointers */
		newpos+=ctrl[1];
		oldpos+=ctrl[2];
	};

	gettimeofday(&time, NULL); 
	temptime = ((long)time.tv_sec)*1000+(long)time.tv_usec/1000; 

	
	newFileStream.finish(crc);

	gettimeofday(&time, NULL); 
	
	writeziptime += (((long)time.tv_sec)*1000+(long)time.tv_usec/1000 - temptime); 

	
	LOG("readfiletime = %d writeziptime = %d",readfiletime, writeziptime);

	/* Clean up the bzip2 reads */
	if (fclose(cpf) || fclose(dpf) || fclose(epf) || fclose(oldFile)){
		LOGERRX("fclose(%s)", patchFilePath);
	}
	delete[] newBuffer;
	delete[] old;
	return 0;

}

static long time = 0;
/*
int doPatchWithoutBz2(char* patchFilePath, char* oldFilePath, char* newFilePath){
	FILE * f, * cpf, * dpf, * epf;
	int cbz2err, dbz2err, ebz2err;
	int fd;
	ssize_t oldsize,newsize;
	ssize_t bzctrllen,bzdatalen;
	u_char header[32],buf[8];
	u_char *old, *newBuffer;
	off_t oldpos,newpos;
	off_t ctrl[3];
	off_t lenread;
	off_t i;
	LOG("in doPatch");
	if(!patchFilePath || !oldFilePath || !newFilePath){
		LOGERRX("File name NULL!");
	}
	
	/* Open patch file *
	if ((f = fopen(patchFilePath, "r")) == NULL){
		LOGERRX("open file failed!");
	}
	LOG("in doPatch2");

	/*
	File format:
		0	8	"BSDIFF40"
		8	8	X
		16	8	Y
		24	8	sizeof(newfile)
		32	X	bzip2(control block)
		32+X	Y	bzip2(diff block)
		32+X+Y	???	bzip2(extra block)
	with control block a set of triples (x,y,z) meaning "add x bytes
	from oldfile to x bytes from the diff block; copy y bytes from the
	extra block; seek forwards in oldfile by z bytes".
	*/

	/* Read header *
	if (fread(header, 1, 32, f) < 32) {
		if (feof(f)){
			LOGERRX("Read head failed!");
			}
		
	}

	/* Check for appropriate magic *
	if (memcmp(header, "BSDIFF40", 8) != 0){
		LOGERRX("magic error!");
	
	}
	/* Read lengths from header *
	bzctrllen=offtin(header+8);
	bzdatalen=offtin(header+16);
	newsize=offtin(header+24);
	if((bzctrllen<0) || (bzdatalen<0) || (newsize<0)){
		LOGERRX("Corrupt patch!");
		
	}

	/* Close patch file and re-open it via libbzip2 at the right places *
	if (fclose(f)){
		LOGERRX("fclose(%s)", patchFilePath);
		}
	if ((cpf = fopen(patchFilePath, "r")) == NULL){
		LOGERRX("fopen(%s)", patchFilePath);
		}
	if (fseeko(cpf, 32, SEEK_SET)){
		LOGERRX("fseeko(%s, %lld)", patchFilePath,
		    (long long)32);
		}

	if ((dpf = fopen(patchFilePath, "r")) == NULL){
		LOGERRX( "fopen(%s)", patchFilePath);
		}
	if (fseeko(dpf, 32 + bzctrllen, SEEK_SET)){
		LOGERRX("fseeko(%s, %lld)", patchFilePath,
		    (long long)(32 + bzctrllen));
		}

	if ((epf = fopen(patchFilePath, "r")) == NULL)
		{
		LOGERRX("fopen(%s)", patchFilePath);
		}
	if (fseeko(epf, 32 + bzctrllen + bzdatalen, SEEK_SET))
		{
		LOGERRX("fseeko(%s, %lld)", patchFilePath,
		    (long long)(32 + bzctrllen + bzdatalen));
		}
	
	if(((fd=open(oldFilePath,O_RDONLY,0))<0) ||
		((oldsize=lseek(fd,0,SEEK_END))==-1) ||
		((old=(u_char*)malloc(oldsize+1))==NULL) ||
		(lseek(fd,0,SEEK_SET)!=0) ||
		(read(fd,old,oldsize)!=oldsize) ||
		(close(fd)==-1)){
			LOGERRX("%s",oldFilePath);
		}
	newBuffer=(u_char*)malloc(newsize+1);

	oldpos=0;newpos=0;
	while(newpos<newsize) {
		/* Read control data *
		for(i=0;i<=2;i++) {
			lenread = fread(buf, 1, 8, cpf);
			//lenread = BZ2_bzRead(&cbz2err, cpfbz2, buf, 8);
			if (lenread < 8){
				LOGERRX("Corrupt patch\n");
				}
			ctrl[i]=offtin(buf);
		};

		/* Sanity-check *
		if(newpos+ctrl[0]>newsize){
			LOGERRX("Corrupt patch\n");
		}

		/* Read diff string *
		lenread = fread(newBuffer + newpos, 1, ctrl[0], dpf);
		//lenread = BZ2_bzRead(&dbz2err, dpfbz2, new + newpos, ctrl[0]);
		if (lenread < ctrl[0]){
			LOGERRX("Corrupt patch\n");
			}
		/* Add old data to diff string *
		for(i=0;i<ctrl[0];i++)
			if((oldpos+i>=0) && (oldpos+i<oldsize))
				newBuffer[newpos+i]+=old[oldpos+i];

		/* Adjust pointers *
		newpos+=ctrl[0];
		oldpos+=ctrl[0];

		/* Sanity-check *
		if(newpos+ctrl[1]>newsize){
			LOGERRX("Corrupt patch\n");
		}
		/* Read extra string *
		lenread = fread(newBuffer + newpos, 1, ctrl[1], epf);
		//lenread = BZ2_bzRead(&ebz2err, epfbz2, new + newpos, ctrl[1]);
		if (lenread < ctrl[1]){
			LOGERRX("Corrupt patch\n");
		}
		/* Adjust pointers *
		newpos+=ctrl[1];
		oldpos+=ctrl[2];
	};

	/* Clean up the bzip2 reads *
	if (fclose(cpf) || fclose(dpf) || fclose(epf)){
		LOGERRX("fclose(%s)", patchFilePath);
	}
	/* Write the new file *
	if(((fd=open(newFilePath,O_CREAT|O_TRUNC|O_WRONLY,0666))<0) ||
		(write(fd,newBuffer,newsize)!=newsize) || (close(fd)==-1)){
		LOGERRX("%s",newFilePath);
	}
	free(newBuffer);
	free(old);

	return 0;


}
/*
int doPatch(char* patchFilePath, char* oldFilePath, char* newFilePath)
{
	FILE * f, * cpf, * dpf, * epf;
	BZFILE * cpfbz2, * dpfbz2, * epfbz2;
	int cbz2err, dbz2err, ebz2err;
	int fd;
	ssize_t oldsize,newsize;
	ssize_t bzctrllen,bzdatalen;
	u_char header[32],buf[8];
	u_char *old, *newBuffer;
	off_t oldpos,newpos;
	off_t ctrl[3];
	off_t lenread;
	off_t i;
	LOG("in doPatch");
	if(!patchFilePath || !oldFilePath || !newFilePath){
		LOGERRX("File name NULL!");
	}
	
	/* Open patch file /
	if ((f = fopen(patchFilePath, "r")) == NULL){
		LOGERRX("open file failed!");
	}
	LOG("in doPatch2");

	/*
	File format:
		0	8	"BSDIFF40"
		8	8	X
		16	8	Y
		24	8	sizeof(newfile)
		32	X	bzip2(control block)
		32+X	Y	bzip2(diff block)
		32+X+Y	???	bzip2(extra block)
	with control block a set of triples (x,y,z) meaning "add x bytes
	from oldfile to x bytes from the diff block; copy y bytes from the
	extra block; seek forwards in oldfile by z bytes".
	*/

	/* Read header/
	if (fread(header, 1, 32, f) < 32) {
		if (feof(f)){
			LOGERRX("Read head failed!");
			}
		
	}

	/* Check for appropriate magic 
	if (memcmp(header, "BSDIFF40", 8) != 0){
		LOGERRX("magic error!");
	
	}
	/* Read lengths from header /
	bzctrllen=offtin(header+8);
	bzdatalen=offtin(header+16);
	newsize=offtin(header+24);
	if((bzctrllen<0) || (bzdatalen<0) || (newsize<0)){
		LOGERRX("Corrupt patch!");
		
	}

	/* Close patch file and re-open it via libbzip2 at the right places 
	if (fclose(f)){
		LOGERRX("fclose(%s)", patchFilePath);
		}
	if ((cpf = fopen(patchFilePath, "r")) == NULL){
		LOGERRX("fopen(%s)", patchFilePath);
		}
	if (fseeko(cpf, 32, SEEK_SET)){
		LOGERRX("fseeko(%s, %lld)", patchFilePath,
		    (long long)32);
		}
	if ((cpfbz2 = BZ2_bzReadOpen(&cbz2err, cpf, 0, 0, NULL, 0)) == NULL){
		LOGERRX("BZ2_bzReadOpen, bz2err = %d", cbz2err);
		}
	if ((dpf = fopen(patchFilePath, "r")) == NULL){
		LOGERRX( "fopen(%s)", patchFilePath);
		}
	if (fseeko(dpf, 32 + bzctrllen, SEEK_SET)){
		LOGERRX("fseeko(%s, %lld)", patchFilePath,
		    (long long)(32 + bzctrllen));
		}
	if ((dpfbz2 = BZ2_bzReadOpen(&dbz2err, dpf, 0, 0, NULL, 0)) == NULL){
		LOGERRX("BZ2_bzReadOpen, bz2err = %d", dbz2err);
		}
	if ((epf = fopen(patchFilePath, "r")) == NULL)
		{
		LOGERRX("fopen(%s)", patchFilePath);
		}
	if (fseeko(epf, 32 + bzctrllen + bzdatalen, SEEK_SET))
		{
		LOGERRX("fseeko(%s, %lld)", patchFilePath,
		    (long long)(32 + bzctrllen + bzdatalen));
		}
	struct timeval t_start,t_end;
	gettimeofday(&t_start, NULL);
	
	if ((epfbz2 = BZ2_bzReadOpen(&ebz2err, epf, 0, 0, NULL, 0)) == NULL){
		LOGERRX("BZ2_bzReadOpen, bz2err = %d", ebz2err);
		}
	gettimeofday(&t_end, NULL);
	long interval = (t_end.tv_sec - t_start.tv_sec)*1000 + (t_end.tv_usec - t_start.tv_usec)/1000;
	time += interval;
	
	if(((fd=open(oldFilePath,O_RDONLY,0))<0) ||
		((oldsize=lseek(fd,0,SEEK_END))==-1) ||
		((old=(u_char*)malloc(oldsize+1))==NULL) ||
		(lseek(fd,0,SEEK_SET)!=0) ||
		(read(fd,old,oldsize)!=oldsize) ||
		(close(fd)==-1)){
			LOGERRX("%s",oldFilePath);
		}
	newBuffer=(u_char*)malloc(newsize+1);

	oldpos=0;newpos=0;
	while(newpos<newsize) {
		 Read control data 
		for(i=0;i<=2;i++) {
			gettimeofday(&t_start, NULL);
			lenread = BZ2_bzRead(&cbz2err, cpfbz2, buf, 8);
			gettimeofday(&t_end, NULL);
			long interval = (t_end.tv_sec - t_start.tv_sec)*1000 + (t_end.tv_usec - t_start.tv_usec)/1000;
			time += interval;
			if ((lenread < 8) || ((cbz2err != BZ_OK) &&
			    (cbz2err != BZ_STREAM_END))){
				LOGERRX("Corrupt patch\n");
				}
			ctrl[i]=offtin(buf);
		};

		/* Sanity-check 
		if(newpos+ctrl[0]>newsize){
			LOGERRX("Corrupt patch\n");
		}

		/* Read diff string 
		gettimeofday(&t_start, NULL);
		lenread = BZ2_bzRead(&dbz2err, dpfbz2, new + newpos, ctrl[0]);
		gettimeofday(&t_end, NULL);
		interval = (t_end.tv_sec - t_start.tv_sec)*1000 + (t_end.tv_usec - t_start.tv_usec)/1000;
		time += interval;
		if ((lenread < ctrl[0]) ||
		    ((dbz2err != BZ_OK) && (dbz2err != BZ_STREAM_END))){
			LOGERRX("Corrupt patch\n");
			}
		/* Add old data to diff string 
		for(i=0;i<ctrl[0];i++)
			if((oldpos+i>=0) && (oldpos+i<oldsize))
				new[newpos+i]+=old[oldpos+i];

		/* Adjust pointers 
		newpos+=ctrl[0];
		oldpos+=ctrl[0];

		/* Sanity-check 
		if(newpos+ctrl[1]>newsize){
			LOGERRX("Corrupt patch\n");
		}
		/* Read extra string 
		gettimeofday(&t_start, NULL);
		lenread = BZ2_bzRead(&ebz2err, epfbz2, new + newpos, ctrl[1]);
		gettimeofday(&t_end, NULL);
		interval = (t_end.tv_sec - t_start.tv_sec)*1000 + (t_end.tv_usec - t_start.tv_usec)/1000;
		time += interval;
		if ((lenread < ctrl[1]) ||
		    ((ebz2err != BZ_OK) && (ebz2err != BZ_STREAM_END))){
			LOGERRX("Corrupt patch\n");
		}
		/* Adjust pointers 
		newpos+=ctrl[1];
		oldpos+=ctrl[2];
	};

	/* Clean up the bzip2 reads 
	gettimeofday(&t_start, NULL);
	BZ2_bzReadClose(&cbz2err, cpfbz2);
	BZ2_bzReadClose(&dbz2err, dpfbz2);
	BZ2_bzReadClose(&ebz2err, epfbz2);
	gettimeofday(&t_end, NULL);
	interval = (t_end.tv_sec - t_start.tv_sec)*1000 + (t_end.tv_usec - t_start.tv_usec)/1000;
	time += interval;
	printf("bzRead time = %d", time);
	time = 0;
	if (fclose(cpf) || fclose(dpf) || fclose(epf)){
		LOGERRX("fclose(%s)", patchFilePath);
	}
	/* Write the new file 
	if(((fd=open(newFilePath,O_CREAT|O_TRUNC|O_WRONLY,0666))<0) ||
		(write(fd,new,newsize)!=newsize) || (close(fd)==-1)){
		LOGERRX("%s",newFilePath);
	}
	free(new);
	free(old);

	return 0;
}*/

