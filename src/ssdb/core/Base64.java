package ssdb.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
//import org.apache.commons.codec.binary.Base64;
public class Base64 {
	
	/**
	 * ���룬�������ƴ�ת�����ַ���
	 * 
	 * @param binary �����ƴ�
	 * @return �ַ���
	 * @see org.apache.commons.codec.binary.Base64
	 */
	public static String encode(byte[] binary){
		return org.apache.commons.codec.binary.Base64.encodeBase64String(binary);
	}
	
	/**
	 * ���룬�����ַ���ת�������ƴ�
	 * 
	 * @param str �ַ���
	 * @return �����ƴ�
	 * @see org.apache.commons.codec.binary.Base64
	 */
	public static byte[] decode(String str){
		return org.apache.commons.codec.binary.Base64.decodeBase64(str);
	}
	
}