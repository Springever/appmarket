/*
*****************************************************************************
* Copyright (C) 2005-2011 UCWEB Inc. All rights reserved
* File    		: M9Codec.h
*
* Description	: M9编解码	  
*
* Version     	: 1.0
* Creation, 2011/08/01, huangmy, Create the file
******************************************************************************
*/

#ifndef M9Codec_h
#define M9Codec_h

#ifdef JNIEXPORT
#undef JNIEXPORT
#endif

#if defined(WTF_PLATFORM_ANDROID) && WTF_PLATFORM_ANDROID
#define JNIEXPORT __attribute__((visibility("default")))
#else
#define JNIEXPORT
#endif

namespace WebCore
{
    /*
     * @return the size of str after encode by m9
     * */
    JNIEXPORT int m9encodeSize(const int srcSize);
    
    /*
     * @return the size of str after decode by m9
     * */
    JNIEXPORT int m9decodeSize(const int srcSize);
    
    /*
     * @return the size of str after encode by m8
     * */
    JNIEXPORT int m8encodeSize(const int srcSize);
    
    /*
     * @return the size of str after decode by m8
     * */
    JNIEXPORT int m8decodeSize(const int srcSize);
    
    
    /*
     * @retcode :
     * 0 : succeeded
     * -1 : invalid platform param
     * -2 : not enough buffer
     * -3 : invalid src
     * -4 : decode failed,not same mask
     * */
    JNIEXPORT int m9Encode(const unsigned char* src,
                 const int srcSize, 
                 unsigned char* dest,
                 int* destSize);
    
    /*
     * @retcode :
     * 0 : succeeded
     * -2 : not enough buffer
     * */
    JNIEXPORT int m8Encode(const unsigned char* src,
                 const int srcSize,
                 unsigned char* dest,
                 int* destSize);
    
    
    
    /*
     * @retcode :
     * 0 : succeeded
     * -2 : not enough buffer
     * -3 : invalid src
     * -4 : decode failed,not same mask
     * */
    JNIEXPORT int m9Decode(const unsigned char* src,
                 const int srcSize,
                 unsigned char* dest,
                 int* destSize);
    
    /*
     * @retcode :
     * 0 : succeeded
     * -2 : not enough buffer
     * -3 : invalid src
     * -4 : decode failed,not same mask
     * */
    JNIEXPORT int m8Decode(const unsigned char* src,
                 const int srcSize,
                 unsigned char* dest,
                 int* destSize);


    //直接使用上述函数也可以进行加密/解密操作，不过需要调用者
    //自行维护加密/解密输出的缓冲区(即先分配unsigned char* dest 的大小，
    //使用结束后要自行释放该缓冲区的内存空间).
    //M9加密/解密类，建议使用这个类进行加密/解密操作。
    class M9Codec
    {
    public:
        JNIEXPORT M9Codec();
        JNIEXPORT ~M9Codec();

        /*
         * @retcode :
         * 0 : succeeded
         * -2 : not enough buffer
         * -3 : invalid src
         * -4 : decode failed,not same mask
         * */
        JNIEXPORT int decode(const unsigned char* src,  const int srcSize);

        /*
         * @retcode :
         * 0 : succeeded
         * -1 : invalid platform param
         * -2 : not enough buffer
         * -3 : invalid src
         * -4 : decode failed,not same mask
         * */
        JNIEXPORT int encode(const unsigned char* src,  const int srcSize);

        //获取解密后的数据
        JNIEXPORT char* data() const { return m_buffer; }
        JNIEXPORT int   size() const { return m_size; }

    protected:
        void clear();

    private:
        char* m_buffer;
        int   m_size;
    };
}

#endif

