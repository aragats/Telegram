LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_CFLAGS := -Wall -DANDROID -DHAVE_MALLOC_H -DHAVE_PTHREAD -DWEBP_USE_THREAD -finline-functions -ffast-math -ffunction-sections -fdata-sections -O2
LOCAL_ARM_MODE := arm
LOCAL_STATIC_LIBRARIES := cpufeatures
LOCAL_MODULE := webp

ifneq ($(findstring armeabi-v7a, $(TARGET_ARCH_ABI)),)
  # Setting LOCAL_ARM_NEON will enable -mfpu=neon which may cause illegal
  # instructions to be generated for armv7a code. Instead target the neon code
  # specifically.
  NEON := c.neon
else
  NEON := c
endif

include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)
ifeq ($(TARGET_ARCH_ABI),armeabi)
	LOCAL_ARM_MODE  := thumb
else
	LOCAL_ARM_MODE  := arm
endif
LOCAL_MODULE := sqlite
LOCAL_CFLAGS 	:= -w -std=gnu99 -O2 -DNULL=0 -DSOCKLEN_T=socklen_t -DLOCALE_NOT_USED -D_LARGEFILE_SOURCE=1 -D_FILE_OFFSET_BITS=64
LOCAL_CFLAGS 	+= -DANDROID_NDK -DDISABLE_IMPORTGL -fno-strict-aliasing -fprefetch-loop-arrays -DAVOID_TABLES -DANDROID_TILE_BASED_DECODE -DANDROID_ARMV6_IDCT -DHAVE_STRCHRNUL=0

LOCAL_SRC_FILES     := \
./sqlite/sqlite3.c

include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_PRELINK_MODULE := false
LOCAL_STATIC_LIBRARIES := webp sqlite
LOCAL_MODULE 	:= tmessages.8
LOCAL_CFLAGS 	:= -w -std=gnu99 -O2 -DNULL=0 -DSOCKLEN_T=socklen_t -DLOCALE_NOT_USED -D_LARGEFILE_SOURCE=1 -D_FILE_OFFSET_BITS=64
LOCAL_CFLAGS 	+= -Drestrict='' -D__EMX__ -DFIXED_POINT -DUSE_ALLOCA -DHAVE_LRINT -DHAVE_LRINTF -fno-math-errno
LOCAL_CFLAGS 	+= -DANDROID_NDK -DDISABLE_IMPORTGL -fno-strict-aliasing -fprefetch-loop-arrays -DAVOID_TABLES -DANDROID_TILE_BASED_DECODE -DANDROID_ARMV6_IDCT -ffast-math
LOCAL_CPPFLAGS 	:= -DBSD=1 -ffast-math -O2 -funroll-loops
LOCAL_LDLIBS 	:= -ljnigraphics -llog
ifeq ($(TARGET_ARCH_ABI),armeabi)
	LOCAL_ARM_MODE  := thumb
else
	LOCAL_ARM_MODE  := arm
endif


LOCAL_SRC_FILES     += \
./libjpeg/jcapimin.c \
./libjpeg/jcapistd.c \
./libjpeg/armv6_idct.S \
./libjpeg/jccoefct.c \
./libjpeg/jccolor.c \
./libjpeg/jcdctmgr.c \
./libjpeg/jchuff.c \
./libjpeg/jcinit.c \
./libjpeg/jcmainct.c \
./libjpeg/jcmarker.c \
./libjpeg/jcmaster.c \
./libjpeg/jcomapi.c \
./libjpeg/jcparam.c \
./libjpeg/jcphuff.c \
./libjpeg/jcprepct.c \
./libjpeg/jcsample.c \
./libjpeg/jctrans.c \
./libjpeg/jdapimin.c \
./libjpeg/jdapistd.c \
./libjpeg/jdatadst.c \
./libjpeg/jdatasrc.c \
./libjpeg/jdcoefct.c \
./libjpeg/jdcolor.c \
./libjpeg/jddctmgr.c \
./libjpeg/jdhuff.c \
./libjpeg/jdinput.c \
./libjpeg/jdmainct.c \
./libjpeg/jdmarker.c \
./libjpeg/jdmaster.c \
./libjpeg/jdmerge.c \
./libjpeg/jdphuff.c \
./libjpeg/jdpostct.c \
./libjpeg/jdsample.c \
./libjpeg/jdtrans.c \
./libjpeg/jerror.c \
./libjpeg/jfdctflt.c \
./libjpeg/jfdctfst.c \
./libjpeg/jfdctint.c \
./libjpeg/jidctflt.c \
./libjpeg/jidctfst.c \
./libjpeg/jidctint.c \
./libjpeg/jidctred.c \
./libjpeg/jmemmgr.c \
./libjpeg/jmemnobs.c \
./libjpeg/jquant1.c \
./libjpeg/jquant2.c \
./libjpeg/jutils.c

LOCAL_SRC_FILES     += \
./jni.c \
./sqlite_cursor.c \
./sqlite_database.c \
./sqlite_statement.c \
./sqlite.c \
./utils.c \
./image.c

include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/cpufeatures)