#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <sys/time.h>
#include <time.h>

#include "../Package/Package.h"
#include "../Package/ZipWriteStream.h"
#include "../Package/ZipReadStream.h"
#include "../Package/ZipFile.h"


extern "C" int doPatch(char* patchFilePath, char* oldFilePath, char* newFilePath);
extern "C" int doPathWithoutBz2(char* patchFilePath, char* oldFilePath, char* newFilePath);
extern "C" int doDiffWithNoBz2(char* oldFilePath, char* newFilePath, char* patchFilePath);


using namespace android;
using namespace std;


int testMerege(int argc, char *argv[]){
	/*if(argc < 5){
		cout<<"bad arg"<<endl;
		return -1;
	}
	IncPackage* package = IncPackage::createPackage(argv[2]);
	if(!package){
		cout<< "createPackage fail" << endl;
	}
	int ret = -1;
	for(int i = 3; i < argc -1; i++){
		ret = package->addFile(argv[i], ZipEntry::kCompressDeflated);
		if(NO_ERROR != ret){
			cout<< "addFile failed code = " << ret <<endl;
			delete package;
			return -1;
		}
	}
	ret = package->doZipFileMerege(argv[argc - 1 ]);
	if(ret != NO_ERROR)
		cout <<"doMeregeFailed code = "<<ret<<endl; 
	delete package;
	return 0;*/
}

int testPatch(int argc, char* argv[]){
	/*int ret = doPathWithoutBz2((char*)argv[2], (char*)argv[3], (char*)argv[4]);	
	if(!ret)
		cout << "doPath done!" << endl;*/
}

int testPatch2(int argc, char* argv[]){
	/*ZipFile* zf = new ZipFile();
	cout << "open zip error = " << zf->open(argv[2],ZipFile::kOpenReadOnly) << endl;
	void* data = zf->uncompress(zf->getEntryByName("pp"));
	FILE* f = fopen("test", "w+");
	fwrite(data, 1, zf->getEntryByName("pp")->getUncompressedLen(), f);
	fclose(f);
	int ret = doPathWithoutBz2("./test", (char*)argv[3], (char*)argv[4]);	
	if(!ret)
		cout << "doPath done!" << endl;*/
}


/*
	1, newApkName
    2~x, singlefile
    x+1, zipfile
*/

int testDiff(int argc, char* argv[]){
	int ret = doDiffWithNoBz2((char*)argv[2], (char*)argv[3], (char*)argv[4]);
	if(!ret)
		cout << "doDiff done!" << endl;

	
}

void zipCompare(const char* oldFileName, const char* newFileName, const char* entryName){
	status_t ret = NO_ERROR;
	ZipFile* oldZip = new ZipFile();
	ZipFile* newZip = new ZipFile();
	if(oldZip->open(oldFileName,ZipFile::kOpenReadOnly) == NO_ERROR
   		&& newZip->open(newFileName, ZipFile::kOpenReadOnly) == NO_ERROR){
		long oldSize, newSize;
		oldSize = newSize = 0;
		unsigned char* oldData = oldZip->getCompressedData(entryName,oldSize);
		unsigned char* newData = newZip->getCompressedData(entryName,newSize);
		if(oldData && newData)
			printf("oldSize = %d, newSize = %d, memcmp = %d ", oldSize, newSize, memcmp(oldData, newData, oldSize));
		else
			printf("getData failed!");
		delete oldData;
		delete newData;
	 }

	delete oldZip;
	delete newZip;	

}

int main(int argc, char *argv[]){
	char* oldFileName = argv[1];
	char* newFileName = argv[2];
	char* entryName = argv[3];
	zipCompare(oldFileName, newFileName, entryName);
	/*printf("sizeof ungiend long  = %d, sizeof signed long = %d", sizeof(unsigned long), sizeof(signed long));
	return 1;
	if(argc < 5){
		cout<<"bad arg"<<endl;
		return -1;
	}
	struct timeval t_start,t_end;
	gettimeofday(&t_start, NULL);
	cout << argv[1] << endl;
	if(strcmp(argv[1], "merege") == 0){
		testMerege(argc, argv);
	}
	else if(strcmp(argv[1], "patch") == 0){
		testPatch2(argc, argv);
	}
	else if(strcmp(argv[1], "diff") == 0){
		testDiff(argc, argv);
	}
	gettimeofday(&t_end, NULL);
	long interval = (t_end.tv_sec - t_start.tv_sec)*1000 + (t_end.tv_usec - t_start.tv_usec)/1000;
	cout<< "time used = " << interval << endl;*/

/*	printf("zipName = %s\n", argv[1]);
	const char* zipName = argv[1];
	const char* bFileName = argv[2];
	const char* entryName = argv[3];
	
	FILE* f = fopen(bFileName, "w+b");
	if(!f)
		return -1;
	ZipFile* zp = new ZipFile();
	zp->open(zipName ,ZipFile::kOpenReadOnly);
	const ZipEntry* pe = zp->getEntryByName(entryName);
	printf("pe = %d\n", pe);
	ZipReadStream* zs = ZipReadStream::createReadStream(zp, zp->getEntryByName(entryName));
	const int bufferSize = 1024*8;
	char* buffer = new char[bufferSize];
	printf("zs = %d\n", zs);
	/*int ret = zs->readAsStream(buffer, 1024*8);
	printf("ret = %d\n", ret);
	while(ret > 0){
		fwrite(buffer, 1, ret, f);
		ret = zs->readAsStream(buffer, 1024*8);
	}
	long offset = 0;
	while(offset < pe->getUncompressedLen()){
		int i;
		for(i = 0 ; i < bufferSize; i++){
			if(offset + i == pe->getUncompressedLen())
				break;
			if(!zs->getAt(offset + i, buffer[i])){
				printf("getAt failed!\n");
				delete []buffer;
				fclose(f);
				return -1;
			}
		}
		offset += i;
//		offset ++;
		fwrite(buffer, 1, i, f);
	}
	delete []buffer;
	fclose(f);*/

/*	const int KBufferSize = 1024 * 8;
	char* buffer = new char[KBufferSize];
	const char* zipName = argv[1];
	const char* bFileName = argv[2];
	const char* entryName = argv[3];
	remove(zipName);
	ZipFile* zipFile = new ZipFile();
	zipFile->open(zipName, ZipFile::kOpenCreate | ZipFile::kOpenReadWrite);

	ZipWriteStream* ws = ZipWriteStream::createZipWriteStream(zipFile, entryName);

	FILE* f = fopen(bFileName, "r");
	fseek(f, 0,SEEK_END);
	long size = ftell(f);
	fseek(f, 0,SEEK_SET);
	long haveSize = 0;
	while( haveSize < size){
		//printf("haveSize = %d size = %d\n", haveSize, size);
		long remainSize = size - haveSize; 
		long readSize = remainSize > KBufferSize ? KBufferSize:remainSize;
		int readRet = fread(buffer, 1, readSize, f);
		if(readRet < 0){
			printf("read failed ret = %d", readRet);
			return -1;
		}
		haveSize+= readRet;
		bool finish = haveSize >= size ? true: false;
		printf("readRet = %d, haveSize = %d finish = %d\n", readRet, haveSize, finish);
		ws->writeData((unsigned char*)buffer,readRet, finish);
		
	}
	unsigned long crc = 0;
	ws->finishWrite(crc);
	printf("1");
	delete ws;
		printf("2");
	delete zipFile;
		printf("3");
	fclose(f);
	printf("4");

	delete buffer;*/
}

