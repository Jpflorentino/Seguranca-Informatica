package App;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertPathBuilderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

import static App.CertificateVerifier.verificarCadeiaCertificados;

public class Funcoes {
    private static final String headerHS256 = "{\n" +
            "  \"alg\": \"HS256\",\n" +
            "  \"typ\": \"JWT\"\n" +
            "}";

    private static final String payloadHS256 = "{\n" +
            "  \"sub\": \"1234567890\",\n" +
            "  \"name\": \"John Doe\",\n" +
            "  \"iat\": 1516239022\n" +
            "}";

    private static final String headerRS256 = "{\n" +
            "  \"alg\": \"RS256\",\n" +
            "  \"typ\": \"JWT\"\n" +
            "}";

    private static final String payloadRS256 = "{\n" +
            "  \"sub\": \"1234567890\",\n" +
            "  \"name\": \"John Doe\",\n" +
            "  \"admin\": true,\n" +
            "  \"iat\": 1516239022\n" +
            "}";


    public static void assinarRS256(String pfx, String password) throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException, UnrecoverableEntryException, InvalidKeyException, SignatureException {
        /**
         * Gets instances of KeyStore and Signature
         */
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        Signature signature = Signature.getInstance("SHA256withRSA");

        /**
         * Tries to get the private key from the given KeyStore
         */
        FileInputStream fileInputStream = new FileInputStream(pfx);
        keyStore.load(fileInputStream, password.toCharArray());
        KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(password.toCharArray());
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry("1", protectionParameter);
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        /**
         * Encoding header/payload
         */
        String headerEncoding = Base64.getUrlEncoder().withoutPadding().encodeToString(headerRS256.getBytes());
        String payloadEncoding = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadRS256.getBytes());
        String toSign = headerEncoding + "." + payloadEncoding;

        /**
         * Creates the signature
         */
        signature.initSign(privateKey);
        signature.update(toSign.getBytes());
        byte[] sign = signature.sign();

        /**
         * Constructs the final signature
         */
        String signEncoding = Base64.getUrlEncoder().withoutPadding().encodeToString(sign);
        String result = toSign + "." + signEncoding;

        /**
         * Writes the signature inside a documents
         */
        Files.write(Paths.get("signed.txt"), result.getBytes());
    }


    public static void assinarHS256(String password) throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException, UnrecoverableEntryException, InvalidKeyException, SignatureException {
        /**
         * Gets instances of Mac
         */
        Mac mac = Mac.getInstance("HmacSHA256");

        /**
         * Tries to get the private key from the given KeyStore
         */

        SecretKeySpec privateKey = new SecretKeySpec(password.getBytes(), "HmacSHA256");


        /**
         * Encoding header/payload
         */
        String headerEncoding = Base64.getUrlEncoder().withoutPadding().encodeToString(headerHS256.getBytes());
        String payloadEncoding = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadHS256.getBytes());
        String toSign = headerEncoding + "." + payloadEncoding;

        /**
         * Creates the signature
         */
        mac.init(privateKey);
        mac.update(toSign.getBytes());
        byte[] sign = mac.doFinal();

        /**
         * Constructs the final signature
         */
        String signEncoding = Base64.getUrlEncoder().withoutPadding().encodeToString(sign);
        String result = toSign + "." + signEncoding;

        /**
         * Writes the signature inside a documents
         */
        Files.write(Paths.get("signed.txt"), result.getBytes());
    }


    public static void verificarHS256(String fileToVerify, String password) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {


        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec privateKey = new SecretKeySpec(password.getBytes(), "HmacSHA256");

        //Ler o ficheiro de forma a dividir a assinatura
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileToVerify));
        String line = bufferedReader.readLine();
        String[] content = line.split("\\.");
        String body = content[0] + "." + content[1];
        String sign = content[2];

        mac.init(privateKey);
        mac.update(body.getBytes());
        byte[] newSign = mac.doFinal();

        /**
         * Constructs the final signature
         */
        String signEncoding = Base64.getUrlEncoder().withoutPadding().encodeToString(newSign);

        if (signEncoding.equals(sign)) {
            System.out.println("Assinatura Valida");
        } else {
            System.out.println("Assinatura Invalida");
        }


    }

    public static void verificarRS256(String fileToVerify)
            throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
            InvalidAlgorithmParameterException, CertPathBuilderException, NoSuchProviderException {

        /**
         *  Gets instances of CertificateFactory
         */
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        /**
         * Gets the certificate containing the privet key used for the signature
         */
        Scanner scan = new Scanner(System.in);
        System.out.println("Insert .cer File");
        String certificate = scan.nextLine();
        FileInputStream cert = new FileInputStream(certificate);
        Certificate certificate1 = certificateFactory.generateCertificate(cert);

        /**
         * Tries to build a valid certificate chain
         */
        if (!verificarCadeiaCertificados(certificate1)) {
            System.out.println("Certificate Chain is invalid / No Chain was possible ");
            return;
        }

        //Ler o ficheiro de forma a dividir a assinatura
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileToVerify));
        String line = bufferedReader.readLine();
        String[] content = line.split("\\.");
        String body = content[0] + "." + content[1];
        String sign = content[2];
        byte[] signDecoded = Base64.getUrlDecoder().decode(sign);

        Signature signature = Signature.getInstance("SHA256withRSA");

        signature.initVerify(certificate1);
        signature.update(body.getBytes());

        if (signature.verify(signDecoded)) {
            System.out.println("Assinatura Valida");
        } else {
            System.out.println("Assinatura Invalida");
        }


    }
}
