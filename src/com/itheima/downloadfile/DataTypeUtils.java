package com.itheima.downloadfile;

import java.math.BigInteger;

/**
 * 数据类型转换工具类
 * @author Cheyy
 *
 */
public class DataTypeUtils {

	/**
	 * 将字符串转为16进制字节数组
	 * 
	 * @param hexString
	 * @return
	 */
	public static byte[] getHexByteArray(String hexString) {
		byte[] b = new BigInteger(hexString, 16).toByteArray();
		if(b[0]==0){
			//去掉第一个字节，由于首个数据是字母的话第一个数组就是0
			b = subBytes(b, 1, b.length);
		}
		return b; 
	}

	/**
	 * 将字节数值转为16进制字符串
	 * 
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public static String getHexString(byte[] b) throws Exception {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			result.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
		}
		return result.toString();
	}
	
	/**
	 * 截取字节数组
	 * @param src
	 * @param begin
	 * @param end
	 * @return
	 */
	public static byte[] subBytes(byte[] src, int begin, int end) {
		if(src.length < end || begin > end)
			return null;
        byte[] bs = new byte[end - begin];
        for (int i=begin; i<end; i++) bs[i-begin] = src[i];
        return bs;
    }
	
	public static void sub(){}
}
