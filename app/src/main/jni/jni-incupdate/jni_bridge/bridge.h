#ifndef _BRIDGE_H
#define _BRIDGE_H
#include "../Package/ZipReadStream.h"
#include "../Package/ZipWriteStream.h"
#include "../Package/DirectWriteStream.h"

class ZipWriteHelper;

class OpInfo{
public:
	OpInfo():m_crc(0),m_path(0), m_opCode(0){};
	~OpInfo(){
		delete m_path;
	}
	unsigned long m_crc;
	char* m_path;
	int m_opCode;
};

const char* kJsonName = "json.patch";

extern "C"  int doDiff(char* oldFilePath, char* newFilePath, char* patchFilePath);

extern  int doPatchWithoutBz2(char* patchFilePath, char* oldFilePath, char* newFilePath);
extern  int doPatchInMem(const char* oldFileName, ZipWriteHelper& newFileStream, const char* patchFilePath, unsigned long& crc);

#endif
