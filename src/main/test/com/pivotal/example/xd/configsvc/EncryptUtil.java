package com.pivotal.example.xd.configsvc;

import org.bouncycastle.util.encoders.Hex;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.rsa.crypto.RsaAlgorithm;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class EncryptUtil {
    // The secret for encryption is random (so dictionary attack is not a danger)
    private final static String DEFAULT_SALT = "deadbeef";

    private String salt;

    private static RsaAlgorithm RSA_ALG = RsaAlgorithm.DEFAULT;

    public static void main(String[] args) throws Exception {
        String cipherText = testEncrypt("Kathy Wan client decrypt test");
    }

    public static String testEncrypt(String message) throws Exception {
        //First generate a public/private key pair
        KeyPair pair = getKeyPairFromKeyStore();

        //Encrypt the message
        String cipherText = encrypt(message, pair.getPublic());

        System.out.println(cipherText);
        return cipherText;
    }

    //keytool -genkeypair -alias doittkeystore -keyalg RSA -keysize 4096 -sigalg SHA512withRSA -keypass doittkey -keystore config-server.jks -storepass doittkey
    //keytool -genkeypair -alias mykey -storepass s3cr3t -keypass s3cr3t -keyalg RSA -keystore keystore.jks

    public static KeyPair getKeyPairFromKeyStore() throws Exception {
        InputStream ins = CryptoUtils.class.getResourceAsStream("/config-server.jks");
        //JKS or PKCS12 works
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(ins, "doittkey".toCharArray());   //Keystore password
        KeyStore.PasswordProtection keyPassword =       //Key password
                new KeyStore.PasswordProtection("doittkey".toCharArray());

        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry("doittkeystore", keyPassword);

        java.security.cert.Certificate cert = keyStore.getCertificate("doittkeystore");
        PublicKey publicKey = cert.getPublicKey();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        return new KeyPair(publicKey, privateKey);
    }

    //get this code from spring-security-rsa RsaSecretEncryptor.java
    public static String encrypt(String plainText, PublicKey publicKey) throws Exception {

        byte[] random = KeyGenerators.secureRandom(16).generateKey();
        BytesEncryptor aes = Encryptors.standard(new String(Hex.encode(random)), DEFAULT_SALT);
        try {
            final Cipher cipher = Cipher.getInstance(RSA_ALG.getJceName());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] secret = cipher.doFinal(random);
            byte[] text = plainText.getBytes(UTF_8);
            ByteArrayOutputStream result = new ByteArrayOutputStream(text.length + 20);
            writeInt(result, secret.length);
            result.write(secret);
            result.write(aes.encrypt(text));

            return Base64.getEncoder().encodeToString(result.toByteArray());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot encrypt", e);
        }
    }

    private static void writeInt(ByteArrayOutputStream result, int length)
            throws IOException {
        byte[] data = new byte[2];
        data[0] = (byte) ((length >> 8) & 0xFF);
        data[1] = (byte) (length & 0xFF);
        result.write(data);
    }

}
