#ifndef _INC_LOG_H__
#define __INC_LOG_H__

#include <android/log.h>

#define TAG "inc_log"
#define TAGZ "zip"

#define LOG(a,args...)  __android_log_print(ANDROID_LOG_ERROR, TAG, a, ##args);
#define LOGZ(a,args...)  __android_log_print(ANDROID_LOG_ERROR, TAGZ, a, ##args);

#define LOGW(a,args...)  __android_log_print(ANDROID_LOG_WARN, TAGZ, a, ##args);


#define LOGERRX(a, args...){ \
		LOG(a,##args); \
		return -1;}
#endif
