#include "Bridge.h"
#include <jni.h>
#include <sys/types.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include "Inc_log.h"
#include "../Package/Package.h"
#include "cJSON.h"

inline void releaseString(JNIEnv* env, jstring jStr, const char* cStr ){
	if(env && cStr && jStr){
		env->ReleaseStringUTFChars(jStr, cStr);
	}
}

enum{
	ENew = 1,
	EDelete = 2,
	EModify = 4,
	EPatch = 8,
	ECompress = 16,
};

const char* tempPatchFileName = ".patch";
const char* tempOldFileName = ".old";

char* getFolderAndPrifex(const char* path){
	int len = strlen(path);
	int i = len -1;
	for(; i >= 0; i--){
		if(path[i] == '.')
			break;
	}
	if(i < 0)
		return NULL;
	
	char* str = new char[i + 2];
	memset(str, 0, i + 2);
	memcpy(str, path, i);
	str[i] = '_';
	return str;
}


char* getFileName(const char* path){
	if(!path)
		return NULL;
	int len = strlen(path);
	int i = len -1;
	for(;i >= 0; i--){
		if(path[i] == '/'){
			break;	
		}
	}
	char* str = 0;
	if(i < 0){
		str = new char[len + 1];
		strcpy(str, path);
	}
	else{
		int namelen = len - i +1;
		str = new char[namelen];
		strcpy(str, path + i + 1);
	}
	return str;
}

void makeTempPath(const char* path, const char* filePath, char** ptrPatch, char** ptrOld){
	LOG("in makeTempPath path = %s filePath = %s", path, filePath);
	char* folder = getFolderAndPrifex(path);
	char* fileName = getFileName(filePath);
	LOG("folder = %s, fileName = %s", folder, fileName);

	int folderLen = strlen(folder);
	int fileNameLen = strlen(fileName);
	*ptrPatch = new char[folderLen + fileNameLen + strlen(tempPatchFileName) + 1];
	strcpy(*ptrPatch, folder);
	strcat(*ptrPatch, fileName);
	strcat(*ptrPatch, tempPatchFileName);

	*ptrOld = new char[folderLen + fileNameLen + strlen(tempOldFileName) + 1];
	strcpy(*ptrOld, folder);
	strcat(*ptrOld, fileName);
	strcat(*ptrOld, tempOldFileName);


	delete []folder;
	delete []fileName;
	LOG("ptrPatch = %s, ptrOld = %s", *ptrPatch, *ptrOld);
}

int parsePatchInfo(ZipFile* file, OpInfo*& infos, int& infoSize){
	const ZipEntry* pEntry = file->getEntryByName(kJsonName);
	int ret = -1;
	if(!pEntry)
		return ret;
	char* buffer = new char[pEntry->getUncompressedLen() + 1];
	buffer[pEntry->getUncompressedLen()] = 0;
	void* data = file->uncompress(pEntry);
	memcpy(buffer, data, pEntry->getUncompressedLen());
	free(data);
	char** pPath = NULL;
	cJSON* pRoot = cJSON_Parse((const char*)buffer);
	if(pRoot){
		cJSON* patchJSON = cJSON_GetObjectItem(pRoot,"patch");
		cJSON* pathJSON = cJSON_GetObjectItem(patchJSON, "d");
		int pathSize = cJSON_GetArraySize(pathJSON);

		pPath = new char*[pathSize];
		for(int i = 0; i < pathSize; i++){
			cJSON* pathItem = cJSON_GetArrayItem(pathJSON, i);
			pPath[i] = pathItem->valuestring;
		}
		
		cJSON* fileJSON = cJSON_GetObjectItem(patchJSON, "f");
		int fileSize = cJSON_GetArraySize(fileJSON);
		
		infos = new OpInfo[fileSize];
		infoSize = fileSize;
		for(int j = 0; j < fileSize; j++){
			cJSON* fileItem = cJSON_GetArrayItem(fileJSON, j);

			cJSON* crcItem = cJSON_GetObjectItem(fileItem, "c");
			cJSON* indexItem = cJSON_GetObjectItem(fileItem, "i");
			cJSON* nameItem = cJSON_GetObjectItem(fileItem, "p");
			cJSON* codeItem = cJSON_GetObjectItem(fileItem, "o");
			if(indexItem && nameItem && codeItem){
				if(crcItem){
					infos[j].m_crc = crcItem->valuedouble;
				}
				infos[j].m_opCode = codeItem->valueint;
				char* folder = NULL;
				int length = strlen(nameItem->valuestring) + 1;
				if(indexItem->valueint >= 0){
					folder = pPath[indexItem->valueint];
					length += strlen(folder);
				}
				
				infos[j].m_path = new char[length];
				memset(infos[j].m_path, 0, length);
				if(folder)
					strcpy(infos[j].m_path, folder);
				strcat(infos[j].m_path, nameItem->valuestring);
				//LOG("folder = %s, nameItem->valuestring = %s, infos[j].m_path = %s", folder,nameItem->valuestring, infos[j].m_path );
				//LOG("code = %d, path = %s, crc = %ld", infos[j].m_opCode, infos[j].m_path , infos[j].m_crc);
			}
		}
		ret = 0;
		cJSON_Delete(pRoot);
	}
	delete []buffer;
	delete pPath;
	return ret;	
}

JNIEXPORT jint JNICALL nativeMakeApk(JNIEnv* env, jobject thiz
									, jstring joldApkPath
									, jstring jnewApkPath
									, jstring jpatchFilePath){
	const char* newFilePath = env->GetStringUTFChars(jnewApkPath, NULL);
	const char* patchFilePath = env->GetStringUTFChars(jpatchFilePath, NULL);
	const char* oldFilePath = env->GetStringUTFChars(joldApkPath, NULL);
	LOG("newFilePath = %s", newFilePath);
	LOG("patchFilePath = %s", patchFilePath);
	LOG("oldFilePath = %s", oldFilePath);
	ZipFile* package = NULL;
	ZipFile* patchZipFile = NULL;
	ZipFile* oldZip = NULL;
	status_t ret = UNKNOWN_ERROR;
	jstring path_obj = NULL;
	unsigned long  targetCrc = 0;
	const char* path = NULL;
	jclass infoCls = NULL;
	OpInfo* infos = NULL;
	int infoSize = 0;
	try{

		if(!newFilePath || !patchFilePath){
			throw "params error.";
		}
		//Create new Apk zip file;
		package = new ZipFile();
		ret = package->open(newFilePath, ZipFile::kOpenReadWrite);
		patchZipFile = new ZipFile();
		patchZipFile->open(patchFilePath,ZipFile::kOpenReadWrite);
		oldZip = new ZipFile();
		oldZip->open(oldFilePath,ZipFile::kOpenReadOnly);
		int size = patchZipFile->getNumEntries();
		/*for(int i = 0; i < size; i++){
			LOG("name =%s", patchZipFile->getEntryByIndex(i)->getFileName());
		}*/

		if(NO_ERROR != ret || !patchZipFile)
			throw "open Package failed.";

		if(!(0 == parsePatchInfo(patchZipFile, infos, infoSize) && infoSize > 0 && infos)){

			delete []infos;
			throw "parse json failed.";
		}
		


		
		for(int i = 0; i < infoSize; i++){
			targetCrc = infos[i].m_crc;
			path = infos[i].m_path;
			int opcode = infos[i].m_opCode;
			//LOG("path = %s", path);
			//LOG("targetCrc = %d", targetCrc);
			//LOG("opcode = %d", opcode);
			if(opcode & ENew){
				ZipEntry* pEntry = oldZip->getEntryByName(path);
				if(pEntry){
					if(NO_ERROR != package->add(oldZip,oldZip->getEntryByName(path), 0, NULL))
						throw "addEntry failed";
				}
			}
			else if(opcode & EDelete){
				
				if(NO_ERROR != package->remove(package->getEntryByName(path)))
					LOG("remove entry failed");
				
			}
			else if(opcode & EModify){
				if(NO_ERROR != package->remove(package->getEntryByName(path)))
						throw "remove entry failed";
				if(opcode & EPatch){
					ZipWriteHelper* newStream = NULL;
					if(opcode & ECompress)
						newStream = ZipWriteStream::create(package,path);
					else
						newStream = DirectWriteStream::create(package,path);

					if(!newStream){
						throw "Stream is NULL";
					}
					char* tempPatch = NULL;
					char* tempOld = NULL;
					makeTempPath(newFilePath, path, &tempPatch, &tempOld);
					FILE* tempPatchFile = fopen(tempPatch, "w+b");
					FILE* tempOldFile = fopen(tempOld, "w+b");
					struct timeval t_start,t_end; 
					gettimeofday(&t_start, NULL); 
					long start = ((long)t_start.tv_sec)*1000+(long)t_start.tv_usec/1000; 

					bool suc = patchZipFile->uncompressToFile(patchZipFile->getEntryByName(path), tempPatchFile)
								&& oldZip->uncompressToFile(oldZip->getEntryByName(path), tempOldFile);
					
					gettimeofday(&t_end, NULL); 
					long end = ((long)t_end.tv_sec)*1000+(long)t_end.tv_usec/1000; 
					long time =  end - start;
					//LOG("uncompressToFile time = %d", time);
					fclose(tempPatchFile);
					fclose(tempOldFile);
					if(!suc){
						remove(tempPatch);
						remove(tempOld);
						delete[] tempPatch;
						delete[] tempOld;
						throw "uncompressToFile failed";
					}
					unsigned long crc = 0;
					
					int ret = doPatchInMem(tempOld, *newStream, tempPatch, crc);
					gettimeofday(&t_end, NULL); 
					end = ((long)t_end.tv_sec)*1000+(long)t_end.tv_usec/1000; 
					time =  end - start;
					//LOG("merge time = %d", time);

					remove(tempPatch);
					remove(tempOld);
					delete []tempPatch;
					delete []tempOld;
					delete newStream;
					
					//if(ret != 0 || crc != targetCrc){
					//	throw "doPatchInMem failed";
					//}
				}
				else{
					if(NO_ERROR != package->add(patchZipFile,patchZipFile->getEntryByName(path), 0, NULL))
						throw "addEntry failed";
				}
			}
		}
		//É¾³ýjson.patchÎÄ¼þ
		if(NO_ERROR != package->remove(package->getEntryByName("json.patch")))
				throw "remove json.patch failed";
		ret = NO_ERROR;
	}
	catch(const char* errmsg){
		if(errmsg)
			LOG(errmsg);
	}
	

	releaseString(env, jnewApkPath, newFilePath);
	releaseString(env,jpatchFilePath, patchFilePath);
	releaseString(env, joldApkPath, oldFilePath);

	delete []infos;
	delete package;
	delete patchZipFile;
	delete oldZip;
	return ret;	
}
										

static JNINativeMethod gMethods[] = {
	{"nativeMakeApk","(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I", (void*)nativeMakeApk},
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env = NULL;
	if(vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK)
		return -1;
	jclass cls = env->FindClass("com/uc/appmarket/common/Utils");
	env->RegisterNatives(cls, gMethods,  sizeof(gMethods) / sizeof(gMethods[0]));
	return JNI_VERSION_1_4;
}





