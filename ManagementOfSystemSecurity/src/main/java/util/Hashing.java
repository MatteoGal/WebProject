package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing {
	
	public static String getDigest(String inputString)  {
	StringBuilder sb = null;
	try {
	MessageDigest digest = MessageDigest.getInstance("SHA-256");

	byte[] encodedhash = digest.digest(
			  inputString.getBytes(StandardCharsets.UTF_8));
	
	 sb = hex(encodedhash);
	}
	catch (NoSuchAlgorithmException e) {
		e.printStackTrace();
		
	}
	
	
	return sb.toString();
	}
	
	public static StringBuilder hex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(String.format("%02x", bytes[i]));

		}
		return sb;
	}


}
