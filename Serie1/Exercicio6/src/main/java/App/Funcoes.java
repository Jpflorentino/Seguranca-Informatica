package App;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

public class Funcoes {
    public static void cifrar(String pathName) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, InvalidAlgorithmParameterException {
        // creates object for symmetric encryption/decryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // creates random key for AES algorithm
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        SecretKey key = keyGen.generateKey();

        // create a file with key
        byte[] toKey = key.getEncoded();
        Files.write(Paths.get("key.txt"), toKey);

        // generate random byte[] to IV
        byte[] toIv = new byte[16];
        new Random().nextBytes(toIv);

        // create file with IV
        Files.write(Paths.get("iv.txt"), toIv);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(toIv));
        byte[] file = Files.readAllBytes(Paths.get(pathName));
        byte[] crypto = cipher.doFinal(file);

        // output file
        Files.write(Paths.get(getFileExtension(pathName, "Crypto")), crypto);
        System.out.println(Arrays.toString(crypto));
    }

    public static void decifrar(String pathName, String pathKey, String pathIv) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // get Key
        byte[] decodedKey = Files.readAllBytes(Paths.get(pathKey));
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        // get IV
        byte[] originalIv = Files.readAllBytes(Paths.get(pathIv));
        // byte[] decodedKey = Base64.getDecoder().decode(pathKey);
        cipher.init(Cipher.DECRYPT_MODE, originalKey, new IvParameterSpec(originalIv));
        byte[] file = Files.readAllBytes(Paths.get(pathName));
        byte[] original = cipher.doFinal(file);

        // output original file
        Files.write(Paths.get(getFileExtension(pathName, "Decrypt")), original);
        System.out.println(Arrays.toString(original));
    }

    public static String getFileExtension(String filename, String mode) {
        String[] array = filename.split("\\.");
        return mode + "." + array[array.length - 1];
    }
}
