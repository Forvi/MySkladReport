package org.example.myskladreport.interfaces;

public interface PassCipher {
    public String encrypt(String secretKey, String plainText);
    public String decrypt(String secretKey, String cipherText);
}

