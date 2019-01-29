package com.pivotal.example.xd.configsvc;

import org.bouncycastle.util.encoders.Hex;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.rsa.crypto.RsaAlgorithm;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CryptoUtils {

    // The secret for encryption is random (so dictionary attack is not a danger)
    private final static String DEFAULT_SALT = "deadbeef";

    private String salt;

    private static RsaAlgorithm RSA_ALG = RsaAlgorithm.DEFAULT;

    public static void main(String[] args) throws Exception {
        String cipherText = testEncrypt("Kathy Wan client decrypt test");
        testDecrypt(cipherText);
    }

    public static String testEncrypt(String message) throws Exception {
        //First generate a public/private key pair
        KeyPair pair = getKeyPairFromKeyStore();

        //Encrypt the message
        String cipherText = encrypt(message, pair.getPublic());

        System.out.println(cipherText);
        return cipherText;
    }

    public static void testDecrypt(String cipherTxt) throws Exception {
        //First generate a public/private key pair
        KeyPair pair = getKeyPairFromKeyStore();

        //Now decrypt it
        String decipheredMessage = decrypt(cipherTxt, pair.getPrivate());

        System.out.println(decipheredMessage);
    }

    public static void testSignVer(String plainText) throws Exception {
        KeyPair pair = generateKeyPair();

        String signature = sign(plainText, pair.getPrivate());

        //Let's check the signature
        boolean isCorrect = verify(plainText, signature, pair.getPublic());
        System.out.println("Signature correct: " + isCorrect);
    }


    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();

        return pair;
    }

    //keytool -genkeypair -alias doittkeystore -keyalg RSA -keysize 4096 -sigalg SHA512withRSA -keypass doittkey -keystore config-server.jks -storepass doittkey
    //keytool -genkeypair -alias mykey -storepass s3cr3t -keypass s3cr3t -keyalg RSA -keystore keystore.jks

    public static KeyPair getKeyPairFromKeyStore() throws Exception {
        InputStream ins = CryptoUtils.class.getResourceAsStream("/config-server.jks");
        //not JCEKS, JKS or PKCS12 worked
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

    public static String decrypt(String cipherText, PrivateKey privateKey) throws Exception {
        byte[] text = Base64.getDecoder().decode(cipherText);

        /*Cipher decriptCipher = Cipher.getInstance("RSA");
        decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        return new String(decriptCipher.doFinal(bytes), UTF_8);*/

        ByteArrayInputStream input = new ByteArrayInputStream(text);
        ByteArrayOutputStream output = new ByteArrayOutputStream(text.length);
        try {
            int length = readInt(input);
            byte[] random = new byte[length];
            input.read(random);
            final Cipher cipher = Cipher.getInstance(RSA_ALG.getJceName());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            String secret = new String(Hex.encode(cipher.doFinal(random)));
            byte[] buffer = new byte[text.length - random.length - 2];
            input.read(buffer);
            BytesEncryptor aes = Encryptors.standard(secret, DEFAULT_SALT);
            output.write(aes.decrypt(buffer));
            return Base64.getEncoder().encodeToString(output.toByteArray());
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IllegalStateException("Cannot decrypt", e);
        }
    }

    private static int readInt(ByteArrayInputStream result) throws IOException {
        byte[] b = new byte[2];
        result.read(b);
        return ((b[0] & 0xFF) << 8) | (b[1] & 0xFF);
    }

    public static boolean verify(String plainText, String signature, PublicKey publicKey) throws Exception {
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(plainText.getBytes(UTF_8));

        byte[] signatureBytes = Base64.getDecoder().decode(signature);

        return publicSignature.verify(signatureBytes);
    }

    public static String sign(String plainText, PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(plainText.getBytes(UTF_8));

        byte[] signature = privateSignature.sign();

        return Base64.getEncoder().encodeToString(signature);
    }
}
