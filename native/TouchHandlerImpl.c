#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <mtview.h>
#include "com_asdev_libjam_mt_TouchHandler.h"

JNIEnv *lenv;
jclass th_class;
jmethodID update_method;

JNIEXPORT void JNICALL Java_com_asdev_libjam_mt_TouchHandler_init(JNIEnv *env, jobject obj, jint dev){
	lenv = env;
	th_class = (*env)->FindClass(env, "com/asdev/libjam/mt/TouchHandler");
	update_method = (*env)->GetStaticMethodID(env, th_class, "onUpdate", "(DDIII)V");	

	int devNum = (int) dev;
	/* event device number stored in devNum */
	run(devNum);
}

void update(double x, double y, int id, int is_touch, int frame_counter){
	/* called when touch update has happened 
	* this needs to call the java code to notify it
	*/
	(*lenv)->CallStaticVoidMethod(lenv, th_class, update_method, (jdouble) x,  (jdouble) y, (jint) id, (int) is_touch, (jint) frame_counter);
}