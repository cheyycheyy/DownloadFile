package com.itheima.downloadfile;

import java.math.BigInteger;

/**
 * ��������ת��������
 * @author Cheyy
 *
 */
public class DataTypeUtils {

	/**
	 * ���ַ���תΪ16�����ֽ�����
	 * 
	 * @param hexString
	 * @return
	 */
	public static byte[] getHexByteArray(String hexString) {
		byte[] b = new BigInteger(hexString, 16).toByteArray();
		if(b[0]==0){
			//ȥ����һ���ֽڣ������׸���������ĸ�Ļ���һ���������0
			b = subBytes(b, 1, b.length);
		}
		return b; 
	}

	/**
	 * ���ֽ���ֵתΪ16�����ַ���
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
	 * ��ȡ�ֽ�����
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
