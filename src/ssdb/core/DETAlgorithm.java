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
	// �Զ����ƽ���Base64����
	/**
	 * DETAlgorithm�ǽ���ȷ�����ܵ�
	 * @param content ��Ҫ���ܵ�����
	 * @param key ������Կ����Ҫͨ��KeyManager������
	 * @return
	 */
	public static String encrypt(String content, Key key) {
		try {

			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// ����������
			// IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
			//���ַ������͵�����ת�����ֽ����ͣ���utf-8���б���
			byte[] byteContent = content.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);// ��ʼ��
			byte[] result = cipher.doFinal(byteContent);
			return Base64.encode(result); // ����
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
	 *            ���������ݣ����ݿ��е����ݶ�����Base64�������ַ���
	 * @param key
	 *            ������Կ
	 * @return ���ؽ�����Զ����Ʊ�ʾ
	 */
	public static String decrypt(String content, Key key) {
		try {
			byte[] byteContent  = Base64.decode(content);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// ����������
			cipher.init(Cipher.DECRYPT_MODE, key);// ��ʼ��
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
		// ����
		System.out.println("����ǰ��" + content);
		DETAlgorithm detAlg = new DETAlgorithm();
		Key detKey = KeyManager.generateDETKey(password, "id", "det");		
		String encryptResult = detAlg.encrypt(content, detKey);
		System.out.println(encryptResult);
		/*
		 * SecureRandom sr = new SecureRandom(password.getBytes()); byte[] b =
		 * new byte[16]; sr.nextBytes(b);
		 * System.out.println(Base64_2.encode(b));
		 */
		// ����
		
		 String decryptResult = detAlg.decrypt(encryptResult,detKey);
		 System.out.println("���ܺ�" + decryptResult);
		 
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
