#include "ZipWriteHelper.h"
#include "Inc_log.h"

status_t ZipWriteHelper::write(unsigned char* data, unsigned long dataSize, bool finish){
	if(!m_cache)
		m_cache = new unsigned char[kCacheSize];
	status_t ret = NO_ERROR;
	if(m_bufferSize + dataSize > kCacheSize){
		ret = doWrite(m_cache, m_bufferSize, false);
		m_bufferSize = 0;
		writeCounter++;
		//LOG(a,args...)
		
	}
	callCounter++;
	memcpy(m_cache + m_bufferSize, data, dataSize);
	m_bufferSize += dataSize;
	return ret;
}

status_t ZipWriteHelper::finish(unsigned long& crc){
	if(m_bufferSize > 0){
		doWrite(m_cache, m_bufferSize, true);
		m_bufferSize = 0;
	}
	LOG("callCounter = %d, writeCounter = %d", callCounter, writeCounter);
	return doFinish(crc);
}


