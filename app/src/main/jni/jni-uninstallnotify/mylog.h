#ifndef __MYLOG_H__
#define __MYLOG_H__

#include <android/log.h>

#define TAG "notifyTest"
#define LOGW(a,args...)  __android_log_print(ANDROID_LOG_WARN, TAG, a, ##args);

#endif //__MYLOG_H__
