package com.appmall.market.common;

/**
 * M9编解码调用
 *  
 *
 */
public class Encoder {

	static {
		System.loadLibrary("encoder");
	}
	
	public static native byte[] encode(byte[] data);
	public static native byte[] decode(byte[] data);
	
}
