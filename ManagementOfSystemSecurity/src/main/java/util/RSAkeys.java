package util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class RSAkeys {
	private BigInteger eKey1;
	private BigInteger dKey1;
	private BigInteger key2;

	
	public RSAkeys(BigInteger eKey1, BigInteger dKey1, BigInteger key2) {
		this.eKey1 = eKey1;
		this.dKey1 = dKey1;
		this.key2 = key2;
	}
	

	
	public static RSAkeys getKey() {
        BigInteger random1 = BigInteger.valueOf(Math.abs(randomPrimeGenerator()));
        BigInteger random2 = BigInteger.valueOf(Math.abs(randomPrimeGenerator()));
		
		
        BigInteger n = random1.multiply(random2);
        
        
        BigInteger tempOne = BigInteger.valueOf(1);
        BigInteger decreasedRandom1 = random1.subtract(tempOne);
        BigInteger phi = decreasedRandom1.multiply(random2.subtract(tempOne));
        
        
		BigInteger e = getCoPrimeOf(n, phi);
				
		
		BigInteger d =e.modInverse(phi);
				
		
		RSAkeys key = new RSAkeys(e, d, n);
		
		
		return key;

	}
	
	public static int randomPrimeGenerator() {
		int num = 0;
        SecureRandom rand = new SecureRandom();
        num = Math.abs(rand.nextInt());

        while (!isPrime(num)) {          
            num = Math.abs(rand.nextInt());
        }
	    
		return num;
	}
	
	private static boolean isPrime(int inputNum){
        if (inputNum <= 3 || inputNum % 2 == 0) 
            return inputNum == 2 || inputNum == 3; //this returns false if number is <=1 & true if number = 2 or 3
        int divisor = 3;
        while ((divisor <= Math.sqrt(inputNum)) && (inputNum % divisor != 0)) 
            divisor += 2; //iterates through all possible divisors
        return inputNum % divisor != 0; //returns true/false
    }
	
    public static boolean isCoPrimeOf(BigInteger candidate, BigInteger demo) {
    	
    	if (demo.gcd(candidate).compareTo(BigInteger.valueOf(1)) == 0) {
    		return true;
    	}
    	
    	else {
    		return false;
    	}
    	
}
    public static BigInteger getCoPrimeOf(BigInteger n, BigInteger phi) {
    	int i = 2;
    	while (BigInteger.valueOf(i).compareTo(phi) == -1) {
    		if (isCoPrimeOf(phi, BigInteger.valueOf(i)) == true) {
    			return BigInteger.valueOf(i);
    		}
    		else {
    			i++;
    		}
    	}
    	return BigInteger.valueOf(1);
    }
		
		public BigInteger geteKey1() {
			return eKey1;
		}

		
		public BigInteger getdKey1 () {
			return dKey1;
		}
		
		public BigInteger getkey2 () {
			return key2;
		}
		
		public String toString(RSAkeys key) {
			String output = key.eKey1 + " " + key.dKey1 + " " + key.key2;
			
			return output;
		}
		
		public static BigInteger[] encrypt (String input, BigInteger e, BigInteger n) {
			BigInteger [] charToEncrypt = new BigInteger [input.length()];
			BigInteger [] encryptedMessage = new BigInteger [input.length()];

			for (int i = 0; i < input.length(); i ++) {
				char character = input.charAt(i);
				int charInt = (int) character;
				charToEncrypt[i] = BigInteger.valueOf(charInt);
			}
			
			
			for (int k = 0; k < charToEncrypt.length; k++) {
				BigInteger toEncryptNum = charToEncrypt[k];
				BigInteger encryptedNum = toEncryptNum.modPow(e, n.abs());
				encryptedMessage[k] = encryptedNum;
			}
			

			
			return encryptedMessage;

		}
		
		public static String decrypt(BigInteger[] ciphertext, BigInteger d, BigInteger n) {
			BigInteger decryptedInts [] = new BigInteger [ciphertext.length];
			for (int i =0; i < ciphertext.length; i++) {
				BigInteger toDecryptNum = ciphertext[i];
				BigInteger decryptedNum = toDecryptNum.modPow(d, n.abs());
				decryptedInts[i] = decryptedNum;
			}
			
			String decrypted = "";
			
			for (int k = 0; k < decryptedInts.length; k++) {
				char decryptedChar = (char) decryptedInts[k].intValue();
				decrypted = decrypted + "" + decryptedChar;
			}

			return decrypted;
		}
		
		public static BigInteger[] getList(String input) {
			ArrayList<BigInteger> list = new ArrayList();
			Scanner scan = new Scanner(input);
			while (scan.hasNextBigInteger()) {
				BigInteger Int =scan.nextBigInteger();
				list.add(Int);
			}
			BigInteger[] array = new BigInteger[list.size()];
			for (int i = 0; i < list.size(); i++) {
				array[i] = list.get(i);
			}
			return array;
		}
		
		public static String getString(BigInteger[] list) {
			String output ="";
			for (int i = 0; i < list.length; i++) {
				BigInteger Int = list[i];
				output = output + " " + Int;
			}
			
			return output;
		}
	



}
