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
	1��dex��so�ļ�ͨ������merege�õ����������ļ����м��뵽zip�ļ��С�
	2��ʣ�µ���Դ�ļ�ͨ������һ��zip�ļ��·���
	3����Ҫ��ɵ�������zip�ļ��ĺϲ����������ڴ��н��С�
*/
	status_t doZipFileMerege(const char* filePath);
	
	//zip����ļ�
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
