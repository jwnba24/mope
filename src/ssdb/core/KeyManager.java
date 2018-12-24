package ssdb.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * <h3>��Կ������</h3><br>
 * ���������������Կ��������Կ�������ļ��У��Ա����ʹ�á�
 * @author ����ΰ
 *
 */
public class KeyManager {

	/**
	 * ����������ڻ�ȡBlowFish�㷨��Ҫ����Կ<br>
	 * <li>1.���ȼ����Կ�ļ��Ƿ���ڣ����������������һ������Կ����ʹ�ö������л��ķ�ʽ������key.dat�ļ��У�<br>
	 * <li>2.�����Կ�ļ�key.dat�ļ��Ѿ����ڣ���ֱ�Ӷ�ȡ�ļ��е���Կ����.<br>
	 * @return key ��Կ
	 * @throws GeneralSecurityException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Key blowfishKey() throws GeneralSecurityException, IOException, ClassNotFoundException{
		
		File f = new File("key.dat");
		//�����Կ�ļ�������˵����û����Կ����Ҫ����һ����Կ
		if(!f.exists()){
			KeyGenerator keyGenerator = KeyGenerator.getInstance("Blowfish");
			keyGenerator.init(128);
			Key key = keyGenerator.generateKey();
			FileOutputStream fileOut = new FileOutputStream("key.dat");
			//�Զ������л��ķ�ʽ�洢��key.dat��
			ObjectOutputStream objOutput = new ObjectOutputStream(fileOut);
			objOutput.writeObject(key);
			return key;
		}else{
			//����ļ����ڣ����ʾ�Ѿ�����Կ������Ҫ��ȡ�����Կ
			FileInputStream fileIn = new FileInputStream("key.dat");
			//��ȡkey.dat�е���Կ���������л�������װΪ����key��
			ObjectInputStream objInput = new ObjectInputStream(fileIn);
			Key key = (Key)objInput.readObject();
			return key;
		}
	}
	
	public static Key generateDETKey(String password,String columnName,String onionType) throws NoSuchAlgorithmException{
		String secretInfo = password + columnName + onionType;
/*		KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128, new SecureRandom(secretInfo.getBytes()));
        SecretKey secretKey = kgen.generateKey();
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
        return key;*/
    	byte[] colNameBytes=columnName.getBytes();
    	//�����ɵ�16λbytes���飬��ʼΪmasterKey��bytes��֮�����ڴ洢������key
    	//byte[] keyBytes=password.getBytes();
    	byte[] keyBytes="1234567812345678".getBytes();
    	//�������ɶ�����16λ��byte����
    	int row=(int) Math.ceil((double)colNameBytes.length/16);
    	byte[][] bytesArray=new byte[row][16];
    	//�������byte��ά����
    	for(int i=0,k=0;i<row;i++){
    		for(int j=0;j<16;j++){
    			//��colName���Ȳ��㣬���ͷ��ʼ���
    			if(k==colNameBytes.length){
    				k=0;
    			}
    			bytesArray[i][j]=colNameBytes[k++];
    			}
    		}
    	//������
    	for(int i=0;i<row;i++){
    		for(int j=0;j<16;j++){
    			keyBytes[j]=(byte)(keyBytes[j]^bytesArray[i][j]);
    		}
    	}
    	SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
    	return key;
    	

	}
	
	public static double[] generateOpeKey(double sens){
		double[] opeKey = new double[3];
		opeKey[0] = Math.random()*1000;
		opeKey[1] = Math.random()*1000;
		//opeKey�ĵ���������������sens
		opeKey[2] = sens;
		return opeKey;
		
	}
	public static double[][] generateHomKey(){
		double[][] homKey = new double[5][3];
		for(int i = 0 ; i < 5-1 ; i++ ){
			//����k1...kn-1,��-100��+100�в����������
			homKey[i][0] = Math.random()*200-100;
			
			//����si:s1+...s(n-2) != 0,s(n-1) !=0�����ǲ�ȡһ���򵥵Ľ���취����sʼ��Ϊ����
			homKey[i][1] = Math.random()*100;
			
			//����t��Լ������ֻ��һ����kn+sn+tn != 0,��t1...t(n-1)û��Ҫ��,���������ݶ�t�ķ�Χ�ǣ�(200~500)֮��������
			homKey[i][2] = Math.random()*100+500;
		}
		//ע�⣺kn+sn+tn != 0
		homKey[4][0] = Math.random()*200-100;
		homKey[4][1] = Math.random()*100;
		//Ϊ������kn+sn+tn != 0��������������tn����������������õ�,������kn+sn+tn = 1000�����ǻ�������kn+sn+tn = randomNumber
		homKey[4][2] = 1000.0-homKey[4][0]-homKey[4][1];
		//System.out.println("��ʹ��KeyManager�е�generateHomKey()����������Կ���飡");
		return homKey;		
	}
}
