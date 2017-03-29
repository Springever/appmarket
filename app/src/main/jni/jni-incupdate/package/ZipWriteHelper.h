
#ifndef __ZIP_WRITE_HELPER_H
#define __ZIP_WRITE_HELPER_H

#include "Defs.h"
#include "ZipFile.h"

using namespace android;

class ZipWriteHelper{
public:
	virtual ~ZipWriteHelper(){
		delete m_param;
		delete []m_cache;
	};
    ZipWriteHelper():m_zipFile(NULL), m_param(NULL), m_bufferSize(0), m_cache(NULL), writeCounter(0), callCounter(0){};
public:
	status_t write(unsigned char* data, unsigned long dataSize, bool finish);
	status_t finish(unsigned long& crc);
protected:
	virtual status_t doWrite(unsigned char* data, unsigned long dataSize, bool finish) = 0;
	virtual status_t doFinish(unsigned long& crc) = 0;
	ZipFile *m_zipFile;
	struct AddFileParam* m_param; 
	static const UInt32 kCacheSize = 1024 * 100;
	UInt32 m_bufferSize;
	unsigned char* m_cache;
	int writeCounter;
	int callCounter;
};


#endif