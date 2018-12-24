package ssdb.core;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class DETAlgorithm {
	// 对二进制进行Base64编码
	/**
	 * DETAlgorithm是进行确定加密的
	 * @param content 需要加密的内容
	 * @param key 加密密钥，需要通过KeyManager来产生
	 * @return
	 */
	public static String encrypt(String content, Key key) {
		try {

			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// 创建密码器
			// IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
			//将字符串类型的数据转换成字节类型，以utf-8进行编码
			byte[] byteContent = content.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(byteContent);
			return Base64.encode(result); // 加密
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * 
	 * @param content
	 *            待解密内容，数据库中的数据都是用Base64编码后的字符串
	 * @param key
	 *            解密密钥
	 * @return 返回结果，以二进制表示
	 */
	public static String decrypt(String content, Key key) {
		try {
			byte[] byteContent  = Base64.decode(content);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(byteContent);
			//return Base64.encode(result);
			return new String(result);
			//return result; 
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		try {
		String content = "1";
		String password = "1234567812345678";
		// 加密
		System.out.println("加密前：" + content);
		DETAlgorithm detAlg = new DETAlgorithm();
		Key detKey = KeyManager.generateDETKey(password, "id", "det");		
		String encryptResult = detAlg.encrypt(content, detKey);
		System.out.println(encryptResult);
		/*
		 * SecureRandom sr = new SecureRandom(password.getBytes()); byte[] b =
		 * new byte[16]; sr.nextBytes(b);
		 * System.out.println(Base64_2.encode(b));
		 */
		// 解密
		
		 String decryptResult = detAlg.decrypt(encryptResult,detKey);
		 System.out.println("解密后：" + decryptResult);
		 
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
