#include "FileParser.h"
#include "Inc_log.h"

FileParser::FileParser():m_fptr(NULL){

}


FileParser::~FileParser(){
	if(m_fptr)
		fclose(m_fptr);
	delete []m_fileNameBuffer;
	delete []m_pathNameBuffer;
	delete []m_fileNames;
	delete []m_pathNames;
	delete []m_fileElements;	
}

FileParser* FileParser::openCustomFile(const char * filePath){
	FILE* pFile = NULL;
	if(filePath || (pFile = fopen(filePath, "r"))){
		return NULL;
	}
	FileParser* parser = new FileParser();
	status_t ret = parser->openInternal(pFile);
	if(ret ==  NO_ERROR)
		return parser;
	else{
		delete parser;
		return NULL;
	}
}

void FileParser::doSeek(long offset){
		if(fseek(m_fptr, offset, 0) == -1){
			throw "doSeek error";	
		}
	
	}

void FileParser::doRead(char* buffer, UInt32 size){
		int ret = fread(buffer, 1, size ,m_fptr);
		if(ret <=0){
			sprintf(m_errmsg, "doRead error = %d", ret);
			throw m_errmsg;
		}
	}

status_t FileParser::openInternal(FILE* pFile){
	if(m_fptr)
		return ALREADY_EXISTS;
	int ret = fread((char*)&m_head, 1, sizeof(FileHead), pFile);
	LOG("sizeof(FileHead) = ", sizeof(FileHead));
	if(ret != sizeof(FileHead))
		return ret;
	//check magic
	if(memcmp(MAGIC, m_head.magicNumber, 4) != 0){
		return INVALID_OPERATION;
	}
	//Check crc here or in java network transact?
	try{
		m_fptr = pFile;
		//parse names
		doSeek(m_head.fileNameTableOffset);
		m_fileNameBuffer = new char[m_head.fileNameSize];
		doRead(m_fileNameBuffer, m_head.fileNameSize);
		m_fileNames = new char*[m_head.fileNameCount];
		int nameIndex = 0;
		char *strStart = m_fileNameBuffer;
		for(int i = 0; i < m_head.fileNameSize; i++){
			if(m_fileNameBuffer[i] == 0){
				 m_fileNames[nameIndex] = strStart;
				 if(nameIndex == m_head.fileNameCount - 1)
				 	break;
				 nameIndex++;
				 strStart = m_fileNameBuffer + i + 1;
			}
		}
		
		//parse paths
		doSeek(m_head.pathNameTableOffset);
		m_fileNameBuffer = new char[m_head.pathNameSize];
		doRead(m_pathNameBuffer, m_head.pathNameSize);
		m_pathNames = new char*[m_head.pathNameCount];
		nameIndex = 0;
		strStart = m_pathNameBuffer;
		for(int i = 0; i < m_head.pathNameSize; i++){
			if(m_pathNameBuffer[i] == 0){
				 m_pathNames[nameIndex] = strStart;
				 if(nameIndex == m_head.pathNameCount - 1)
				 	break;
				 nameIndex++;
				 strStart = m_pathNameBuffer + i + 1;
			}
		}
		//parse fileElement
		if(m_head.fileElementSize != m_head.fileElementCount * sizeof(FileElement))
			throw "Bad file format!";
		doSeek(m_head.fileElementTableOffset);
		m_fileElements = (FileElement*)new char[m_head.fileElementSize];
		doRead((char*)m_fileElements, 	m_head.fileElementSize); 
	}
	catch(const char* msg){
		LOG(msg);
		return INVALID_OPERATION;
	}
	return NO_ERROR;
}


