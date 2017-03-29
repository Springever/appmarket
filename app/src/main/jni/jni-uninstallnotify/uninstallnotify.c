#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <unistd.h>
#include <stdarg.h>
#include <errno.h>

#include <dirent.h>
#include <dlfcn.h>
#include "mylog.h"

#include <sys/Inotify.h>

#include <linux/unistd.h>
#include <linux/inotify.h>
#include <errno.h>



char * event_array[] = {
	"File was accessed",
	"File was modified",
	"File attributes were changed",
	"writtable file closed",
	"Unwrittable file closed",
	"File was opened",
	"File was moved from X",
	"File was moved to Y",
	"Subfile was created",
	"Subfile was deleted",
	"Self was deleted",
	"Self was moved",
	"",
	"Backing fs was unmounted",
	"Event queued overflowed",
	"File was ignored"
};

char* path = "/data/data/com.uc.appmarket";
#define EVENT_NUM 16
#define MAX_BUF_SIZE 1024
	
int main(int argc,char** argv)
{
	int fd;
	int wd;
	char buffer[1024];
	char * offset = NULL;
	struct inotify_event * event;
	int len, tmp_len;
	char strbuf[16];
	int i = 0;
	LOGW("IN Main!\n");
	char* userSerial = NULL;
	LOGW("argc = %d", argc);
	if(argc > 1){
		userSerial = argv[1];
		LOGW("userSerial = %s", userSerial);
	}

	fd = inotify_init();
	if (fd < 0) {
		LOGW("Fail to initialize inotify.\n");
		exit(-1);
	}
	LOGW("fd = %d!\n",fd);
	wd = inotify_add_watch(fd, path, IN_DELETE);
	if (wd < 0) {
		LOGW("Can't add watch");
		exit(-1);
	}
	LOGW("add watch done\n");
	while(len = read(fd, buffer, MAX_BUF_SIZE)) {
		offset = buffer;
		LOGW("Some event happens, len = %d.\n", len);
		event = (struct inotify_event *)buffer;
		while (((char *)event - buffer) < len) {
			if (event->mask & IN_ISDIR) {
				memcpy(strbuf, "Direcotory", 11);
			}
			else {
				memcpy(strbuf, "File", 5);
			}
			LOGW("Object type: %s\n", strbuf);
			LOGW("Event mask: %08X\n", event->mask);
			for (i=0; i<EVENT_NUM; i++) {
				if (event_array[i][0] == '\0') continue;
				if (event->mask & (1<<i)) {
					LOGW("Event: %s, i = %d", event_array[i], i);
					if(i == 9)
					{
					sleep(1);
					int exist = access(path, 0);
					if(exist != -1){
						continue;
					}
					if(userSerial){
						char cmd[256];
						memset(cmd, 0, 256);
					//	sprintf(cmd, "am start -n com.android.browser/com.android.browser.BrowserActivity -d  http://www.google.cn/ --user %s", userSerial);
						sprintf(cmd, "am start -a android.intent.action.VIEW -d http://api.appchaoshi.cn/v1.0/uninstall-stat.html?vername=1.0.0.0 --user %s", userSerial);
						LOGW("cmd = %s", cmd);
						system(cmd);
					}
					else
						system("am start -a android.intent.action.VIEW -d http://api.appchaoshi.cn/v1.0/uninstall-stat.html?vername=1.0.0.0");
					inotify_rm_watch(fd, wd);
					close(fd);
					exit(1);
					}
				}
			}
			tmp_len = sizeof(struct inotify_event) + event->len;
			event = (struct inotify_event *)(offset + tmp_len); 
			offset += tmp_len;
		}
	}

}

