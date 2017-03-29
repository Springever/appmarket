#ifndef _DEFS_H_
#define _DEFS_H_

#include <stdio.h>

#include <errno.h>
#include <sys/types.h>


typedef int32_t     status_t;
typedef unsigned int UInt32;
static const UInt32 kCacheSize = 1024 * 100;

enum { 
	NO_ERROR                = 0,    // Everything's swell.    NO_ERROR          = 0,    // No errors.
    UNKNOWN_ERROR       = 0x80000000, 
    NO_MEMORY           = -ENOMEM,   
    INVALID_OPERATION   = -ENOSYS,
    BAD_VALUE           = -EINVAL,
    NAME_NOT_FOUND      = -ENOENT, 
    PERMISSION_DENIED   = -EPERM,
    ALREADY_EXISTS      = -EEXIST
};

#endif
