#ifndef __DIRECT_WRITE_STREAM_H
#define __DIRECT_WRITE_STREAM_H


#include "ZipWriteHelper.h"

using namespace android;

class DirectWriteStream : public ZipWriteHelper{
public:
	virtual ~DirectWriteStream();
	static ZipWriteHelper* create(ZipFile* zipFile, const char* fileName);
private:
	virtual status_t doWrite(unsigned char* data, unsigned long dataSize, bool finish);
	virtual status_t doFinish(unsigned long& crc);
private:
	DirectWriteStream();
	status_t init(ZipFile* zipFile, const char* fileName);
};

#endif