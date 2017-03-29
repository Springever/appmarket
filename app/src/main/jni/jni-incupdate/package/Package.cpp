#include "Package.h"
#include "Inc_log.h"

#define ASSERTPOINTER(p) \
		if(!p)return BAD_VALUE;
		

IncPackage::IncPackage():m_zipFile(NULL){

}

IncPackage::~IncPackage(){
	close();
}

IncPackage* IncPackage::openPackage(const char* packagePath){
	if(!packagePath)
		return NULL;
	IncPackage* pack = new IncPackage();
	if(pack->internalOpen(packagePath) != NO_ERROR){
		delete pack;
		pack = NULL;
	}
	return pack;
}

status_t IncPackage::internalOpen(const char* filePath){
	if(m_zipFile)
		return ALREADY_EXISTS;
	m_zipFile = new ZipFile();
	return m_zipFile->open(filePath,ZipFile::kOpenReadWrite);
}

status_t IncPackage::addFile(const char* filePath, char* storePath, int compressionMethod){
	ASSERTPOINTER(m_zipFile);
	ASSERTPOINTER(filePath);
	ASSERTPOINTER(storePath);
	LOG("filePath = %s, storePath = %s", filePath, storePath);
	const char* pStoreName = storeName(filePath);
	ASSERTPOINTER(pStoreName);
	int ret = m_zipFile->add(filePath, storePath,compressionMethod,NULL);
	m_zipFile->flush();
	return ret;
}

status_t IncPackage::addEntry(ZipFile* newFile, ZipEntry* newEntry){
	return m_zipFile->add(newFile ,newEntry ,0, NULL);
}

status_t IncPackage::remove(const char* name){
	return m_zipFile->remove(m_zipFile->getEntryByName(name));
}

status_t IncPackage::uncompressToFile(const char * pEntryName,FILE * pf){
	LOG("uncompressToFile pEntryName = %s, m_zipFile = %x",pEntryName, m_zipFile);
	if(m_zipFile)
		LOG("getEntryByName = %x",m_zipFile->getEntryByName(pEntryName));
	return m_zipFile->uncompressToFile(m_zipFile->getEntryByName(pEntryName), pf);
}


const char* IncPackage::storeName(const char* path){
	if(!path)
		return NULL;
	int len = strlen(path);
	const char* pStoreName = NULL;
	for(int i = len - 1; i > 0; i--){
		if(path[i] == '/'){
			if(i < len -1 && i > 0)
				pStoreName = path + i + 1;
			break;
		}
	}
	return pStoreName;
}


void IncPackage::close(){
	if(!m_zipFile)
		return;
	m_zipFile->flush();
	delete m_zipFile;
	m_zipFile = NULL;
}

status_t IncPackage::doZipFileMerege(const char* filePath){
	ASSERTPOINTER(m_zipFile);
	ASSERTPOINTER(filePath);
	ZipFile* newFile = new ZipFile();
	status_t ret = newFile->open(filePath, ZipFile::kOpenReadOnly);
	if(NO_ERROR != ret){
		delete newFile;
		return ret;
	}
	int entryCount = newFile->getNumEntries();
	for(int i = 0; i < entryCount; i++){
		ZipEntry* pEntry = newFile->getEntryByIndex(i);
		if(NO_ERROR != m_zipFile->add(newFile ,pEntry ,0, NULL))
			break;
	}
	m_zipFile->flush();
	delete newFile;
	return NO_ERROR;
}

status_t doCustomFileMerege(const char* filePath){
	ASSERTPOINTER(filePath);
	/*FileParser* pParser = FileParser::openCustomFile(filePath);
	
	if(!pParser){
		return INVALID_OPERATION;
	}
	for
	*/
}



