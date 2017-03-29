#ifndef __ZIP_READ_STREAM_H
#define __ZIP_READ_STREAM_H

#include "Defs.h"
#include <zlib.h>

#include "ZipEntry.h"
#include "ZipFile.h"

using namespace android;

const unsigned long KZipBufferSize = 20*1024;

class ZipReadStream{
	
public:
	static ZipReadStream* createReadStream(const ZipFile* pFile, const ZipEntry* pEntry);
	status_t init(const ZipFile* pFile, const ZipEntry* pEntry);
	~ZipReadStream();
	bool getAt(unsigned long offset, char& c);
	int read(char* readBuffer, long readSize, long startOffset);
	int readAsStream(char* readBuffer, long readSize);
	inline unsigned long  getUncompressedLen(){return m_uncompressSize;}
	unsigned char* uncompressAll();
private:
	ZipReadStream();
	status_t inflateToBuffer();
		
private:
	const ZipFile* m_file;
	const ZipEntry* m_entry;

	char* m_readBuffer;
	char* m_zipBuffer;

private:
	//offset in file;
	long m_currentFileOffset;
	//offset in unzip buffer;
	int m_currentBufferOffset;
	int m_bufferSize;
	int m_readDataSize;
	long m_readBuffserSize;
	long m_readStreamOffset;
	unsigned long m_uncompressSize;
	unsigned long m_compressSize;
	z_stream m_zSstream;
	bool m_needReleaseStream;
};


#endif
