#include <stdio.h>
#include <string.h>
#include <jni.h>
#include <sys/types.h>
#include <inttypes.h>
#include <stdlib.h>
#include "utils.h"
#include "sqlite.h"
#include "image.h"

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
	JNIEnv *env = 0;
    srand(time(NULL));
    
	if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6) != JNI_OK) {
		return -1;
	}
    
    if (sqliteOnJNILoad(vm, reserved, env) == -1) {
        return -1;
    }
    
    if (imageOnJNILoad(vm, reserved, env) == -1) {
        return -1;
    }

	return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
}

uint64_t gcd(uint64_t a, uint64_t b){
    while(a != 0 && b != 0) {
        while((b & 1) == 0) b >>= 1;
        while((a & 1) == 0) a >>= 1;
        if(a > b) a -= b; else b -= a;
    }
    return b == 0 ? a : b;
}

