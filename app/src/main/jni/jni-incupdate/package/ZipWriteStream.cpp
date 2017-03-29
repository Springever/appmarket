#include "ZipWriteStream.h"



ZipWriteStream::ZipWriteStream(){
	
}

ZipWriteStream::~ZipWriteStream(){

}

ZipWriteHelper* ZipWriteStream::create(ZipFile* zipFile, const char* fileName){
	if(!zipFile || !fileName)
		return NULL;
	ZipWriteStream* s = new ZipWriteStream();
	if(NO_ERROR == s->init(zipFile, fileName))
		return s;
	delete s;
	return NULL;	
}

status_t ZipWriteStream::init(ZipFile* zipFile, const char* fileName){
	m_zipFile = zipFile;
	return zipFile->CreateNewFileAndDeflate(fileName, &m_param);
}

status_t ZipWriteStream::doWrite(unsigned char* data,unsigned long dataSize, bool finish){
	return m_zipFile->WriteAndDeflateData(data, dataSize, m_param, finish);
}

status_t ZipWriteStream::doFinish(unsigned long& crc){
	m_zipFile->FinishAddAndDeflateFile(m_param);
	//m_zipFile->flush();
	crc = m_param->crc;
	delete m_param;
	m_param = NULL;
}



