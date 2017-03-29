#ifndef _ZIP_WRITE_STREAM_H__
#define _ZIP_WRITE_STREAM_H__

#include "ZipWriteHelper.h"

using namespace android;

class ZipWriteStream : public ZipWriteHelper{
public:
	virtual ~ZipWriteStream();
	static ZipWriteHelper* create(ZipFile* zipFile, const char* fileName);
private:
	virtual status_t doWrite(unsigned char* data, unsigned long dataSize, bool finish);
	virtual status_t doFinish(unsigned long& crc);
private:
	ZipWriteStream();
	status_t init(ZipFile* zipFile, const char* fileName);
};
#endif
