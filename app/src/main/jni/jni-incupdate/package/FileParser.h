#ifndef _FileParser_H_
#define _FileParser_H_

#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>
#include "Defs.h"


struct FileElement{
	UInt32 nameIndex;
	UInt32 pathIndex;
	UInt32 fileSize;
	UInt32 unCompressSize;
	UInt32 offset;
	/*
	  0,Overwrite directly 
	  1,Diff 
	  2,Is compress data or not
	  8,Delete  
	  16,Need compress in target
	*/
	UInt32 info; 

	UInt32 reserved;

};

struct FileHead{
	char magicNumber[4];
	UInt32 pathNameTableOffset;
	UInt32 pathNameCount;
	UInt32 pathNameSize;
	
	UInt32 fileNameTableOffset;
	UInt32 fileNameCount;
	UInt32 fileNameSize;
	
	UInt32 fileElementTableOffset;
	UInt32 fileElementSize;
	UInt32 fileElementCount;
	UInt32 wholeFileCrc;
	UInt32 fileVersion;

	UInt32 reserved;
};

const char* MAGIC = "FZDF";


class FileParser{
private:
		FileParser();
public:
		~FileParser();
public:
		static FileParser* openCustomFile(const char* filePath);
		FileElement* getFileElement(int index);
		int getFileElementCount(){
			return m_head.fileElementCount;
		}
		
private:
		status_t openInternal(FILE* pFile);
		void doSeek(long offset);
		void doRead(char* buffer, UInt32 size);
		void clearData();
private:
		FILE* m_fptr;
		char* m_fileNameBuffer;
		char* m_pathNameBuffer;
		char** m_fileNames;
		char** m_pathNames;
		FileElement* m_fileElements;
		int m_fileNameCount;
		int m_pathNameCount;
		struct FileHead m_head;
		char m_errmsg[256];
};

#endif
