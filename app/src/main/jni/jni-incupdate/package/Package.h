#ifndef _PACKAGE_H_
#define _PACKAGE_H_

#include "ZipFile.h"

using namespace android;

class IncPackage{

private:
	IncPackage();
public:
	~IncPackage();

public:
	static IncPackage* openPackage(const char* packagePath);
/*
	1，dex和so文件通过增量merege得到。这两个文件先行加入到zip文件中。
	2，剩下的资源文件通过另外一个zip文件下发。
	3，需要完成的是两个zip文件的合并工作，在内存中进行。
*/
	status_t doZipFileMerege(const char* filePath);
	
	//zip添加文件
	status_t addFile(const char* filePath, char* storePath, int compressionMethod);
	status_t makeCustomFileToApk(const char* filePath, const char* oldApkPath
								, const char* newApkPath);
	status_t addEntry(ZipFile* newFile, ZipEntry* newEntry);
	status_t remove(const char* name);
	status_t uncompressToFile(const char * pEntryName,FILE * pf);
private:
	void close();
	status_t internalOpen(const char* filePath);
	const char* storeName(const char* path);
private:
	ZipFile* m_zipFile;
};

#endif
