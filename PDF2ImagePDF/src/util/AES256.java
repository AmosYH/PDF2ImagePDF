package util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES256 {
	private static final String DEFAULT_KEY = "H0w@rdVVOrIDsl07";
	private String ips;
	private Key keySpec;

	public AES256(String key) {
		try {
			byte[] keyBytes = new byte[16];
			byte[] b = key.getBytes("UTF-8");
			System.arraycopy(b, 0, keyBytes, 0, keyBytes.length);
			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
			this.ips = key.substring(0, 16);
			this.keySpec = keySpec;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public byte[] encrypt(byte[] data) {
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, keySpec,
					new IvParameterSpec(ips.getBytes("UTF-8")));
			return cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public byte[] decrypt(byte[] data) {
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, keySpec,
					new IvParameterSpec(ips.getBytes("UTF-8")));
			System.out.println("processing..");
			return cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] encryptByDefaultKey(byte[] data){
		return new AES256(DEFAULT_KEY).encrypt(data);
	}
	public static byte[] decryptByDefaultKey(byte[] data){
		return new AES256(DEFAULT_KEY).decrypt(data);
	}
	public static void main(String[] args) throws IOException{
		System.out.println(new String(Base64.getEncoder().encode(encryptByDefaultKey("abc".getBytes()))));
	}
}
