package org.example.myskladreport.utils;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.example.myskladreport.exceptions.NotValidLengthException;
import org.example.myskladreport.interfaces.PassCipher;

/**
 * <p>Позволяет шифровать данные, реализаует интерфейс PassCipher.</p>
 * <p>Использует алгоритм <b>AES-Шифрование</b>.</p>
 * <p>AES — симметричный алгоритм блочного шифрования, принятый в качестве стандарта.</p>
 * 
 * @param IV_SIZE вектор инициализации
 * @author Lavrov Nikita
 */
public class AesCipher implements PassCipher {
    private final int IV_SIZE;

    public AesCipher(int ivSize) {
        this.IV_SIZE = ivSize;
    }

    
    /** 
     * <p>Шифрование текста по принципу [вектор инициализации + шифр].</p>
     * 
     * @param secretKey секретный ключ
     * @param plainText текст для шишфрования
     * @return String зашифрованный текст 
     */
    public String encrypt(String secretKey, String plainText) {
        
        isValidKey(secretKey);
        isValidPass(plainText.trim());
        byte[] encrypted = null;
        byte[] result = null;

        try {
            byte[] initVector = genRanVec();

            IvParameterSpec ivParameterSpec = new IvParameterSpec(initVector);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

            encrypted = cipher.doFinal(plainText.getBytes());
            result = new byte[initVector.length + encrypted.length];

            // combined iv with encr, res = [iv..enc]
            System.arraycopy(initVector, 0, result, 0, initVector.length); // copy from iv to res
            System.arraycopy(encrypted, 0, result, initVector.length, encrypted.length); // copy from encr to res

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException("Error: ", e);
        } finally {
            clean(result, encrypted);
        }
    }

    /** 
     * <p>Расшифровка</p>
     * 
     * @param secretKey секретный ключ
     * @param cipherText зашифрованный текст
     * @return String расшифрованный текст
     */
    public String decrypt(String secretKey, String cipherText) {
        
        isValidKey(secretKey);
        byte[] ivencrypt = null;
        byte[] iv = null;
        byte[] encrypt = null;
        byte[] result = null;

        try {
            ivencrypt = Base64.getDecoder().decode(cipherText);
            iv = Arrays.copyOfRange(ivencrypt, 0, IV_SIZE);
            encrypt = Arrays.copyOfRange(ivencrypt, IV_SIZE, ivencrypt.length);
            
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            
            result = cipher.doFinal(encrypt);
            return new String(result);
        } catch (Exception e) {
            throw new RuntimeException("Error: ", e);
        } finally {
            clean(ivencrypt, iv, encrypt);
        }

    }

    /** 
     * <p>Валидация ключа на null, пустоту и длину.</p>
     * 
     * @param key ключ
     */
    private void isValidKey(String key) {
        int bits = 16;

        if (Objects.isNull(key)) {
            throw new IllegalArgumentException("Secret key cannot be null");
        }
        
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Secret key cannot be empty");
        }

        if (key.length() != bits) {
            throw new NotValidLengthException("Secret key must be 128 bits");
        }

    }

    /** 
     * <p>Валидация паролдя на null, пустоту,и длину</p>
     * 
     * @param pass
     */
    private void isValidPass(String pass) {

        if (Objects.isNull(pass)) {
            throw new IllegalArgumentException("Password cannot be null");
        }

        if (pass.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }


        if (pass.length() < 2 || pass.length() > 64) {
            throw new NotValidLengthException("Password must be normal length");
        }

    }

    /** 
     * <p>Генерация случайного инициализирующего вектора</p>
     * 
     * @return byte[]
     */
    private byte[] genRanVec() {
        byte[] len = new byte[this.IV_SIZE];
        new SecureRandom().nextBytes(len);
        return len;
    }

    /** 
     * <p>Очистка ключа из памяти</p>
     * 
     * @param args
     */
    private static void clean(byte[]... args) {
        for (var b : args) {
            Arrays.fill(b, (byte) 0);
        }
    }

}
