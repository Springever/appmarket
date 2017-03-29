

#include "DirectWriteStream.h"

DirectWriteStream::DirectWriteStream(){
	
}

DirectWriteStream::~DirectWriteStream(){
}


ZipWriteHelper* DirectWriteStream::create(ZipFile* zipFile
										  , const char* fileName){
	if(!zipFile || !fileName)
		return NULL;
	DirectWriteStream* s = new DirectWriteStream();
	if(NO_ERROR == s->init(zipFile, fileName))
		return s;
	delete s;
	return NULL;	
}

status_t DirectWriteStream::init(ZipFile* zipFile
								, const char* fileName){
	m_zipFile = zipFile;
	return zipFile->CreateNewDeflatedFile(fileName, &m_param);
}

status_t DirectWriteStream::doWrite(unsigned char* data,unsigned long dataSize, bool finish){
	return m_zipFile->WriteDeflatedData(data, dataSize, m_param, finish);
}

status_t DirectWriteStream::doFinish(unsigned long& crc){
	m_zipFile->FinishAddDeflatedFile(m_param);
	//m_zipFile->flush();
	crc = m_param->crc;
}

