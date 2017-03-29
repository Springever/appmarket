/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <stdio.h>
#include <string.h>
#include <jni.h>
#include "M9Codec.h"
#include "Encoder.h"

using namespace WebCore;

/*
 * Author: qiaozhi@ucweb.com
 * Class:     com_uc_appmarket_common_Encoder
 * Method:    encode
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_appmall_market_common_Encoder_encode
  (JNIEnv *env, jclass cls, jbyteArray data) {
  
  if (data == NULL)
    return env->NewByteArray(0);
  
  jbyte* src = env->GetByteArrayElements(data, NULL);
  jsize srcLength = env->GetArrayLength(data);

  M9Codec codec;
  int result = codec.encode((unsigned char*) src, (int) srcLength);
  if (result != 0)
    return env->NewByteArray(0);
  
  jbyte* dest = (jbyte*) codec.data();
  jsize destLength = (jsize) codec.size();
  
  jbyteArray ret = env->NewByteArray(destLength);
  env->SetByteArrayRegion(ret, 0, destLength, dest);
  env->ReleaseByteArrayElements(data, src, JNI_ABORT);
  
  return ret;
}

/*
 * Author: qiaozhi@ucweb.com
 * Class:     com_uc_appmarket_common_Encoder
 * Method:    decode
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_appmall_market_common_Encoder_decode
  (JNIEnv *env, jclass cls, jbyteArray data) {
  
  if (data == NULL)
    return env->NewByteArray(0);
    
  jbyte* src = env->GetByteArrayElements(data, NULL);
  jsize srcLength = env->GetArrayLength(data);

  M9Codec codec;
  int result = codec.decode((unsigned char*) src, (int) srcLength);
  if (result != 0)
    return env->NewByteArray(0);
  
  jbyte* dest = (jbyte*) codec.data();
  jsize destLength = (jsize) codec.size();
  jbyteArray ret = env->NewByteArray(destLength);
  env->SetByteArrayRegion(ret, 0, destLength, dest);
  env->ReleaseByteArrayElements(data, src, JNI_ABORT);
  
  return ret;
}