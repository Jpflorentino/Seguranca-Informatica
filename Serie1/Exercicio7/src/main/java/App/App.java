package App;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertificateException;
import java.util.Scanner;

import static App.Funcoes.*;

public class App {
    static int Menu() {
        Scanner scan = new Scanner(System.in);
        int option;
        do {
            System.out.println("######## MENU ##########");
            System.out.println("Options:");
            System.out.println(" 0: Assinar");
            System.out.println(" 1: Verificar");
            System.out.println("..........");
            System.out.println("99: Exit");
            System.out.print("Enter an Option:");
            option = scan.nextInt();
        } while (!((option >= 0 && option <= 1) || option == 99));
        return option;
    }

    static void assinar(String signature_type) throws CertificateException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, SignatureException, UnrecoverableEntryException, IOException {
        Scanner scan = new Scanner(System.in);
        switch (signature_type) {
            case "RS256":
            case "rs256":
                System.out.println("Insert .pfx File");
                String pfx = scan.nextLine();
                System.out.println(".pfx password");
                String password = scan.nextLine();
                assinarRS256(pfx, password);
                System.out.println("\n");
                break;
            case "HS256":
            case "hs256":
                System.out.println("What password do you want to use?");
                password = scan.nextLine();
                assinarHS256(password);
                System.out.println("\n");
                break;
            default:
                System.out.println("Specifications invalid");
        }
    }

    static void verificar(String signature_type) throws CertificateException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, SignatureException, UnrecoverableEntryException, IOException, InvalidAlgorithmParameterException, CertPathBuilderException, NoSuchProviderException {
        Scanner scan = new Scanner(System.in);
        switch (signature_type) {
            case "HS256":
            case "hs256":
                System.out.println("File to Verify");
                String file = scan.nextLine();
                System.out.println("Password");
                String password = scan.nextLine();
                verificarHS256(file, password);
                System.out.println("\n");
                break;
            case "RS256":
            case "rs256":
                System.out.println("File to Verify");
                file = scan.nextLine();
                verificarRS256(file);
                System.out.println("\n");
                break;
            default:
                System.out.println("Specifications invalid");
        }
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        boolean end = false;
        while (!end) {
            try {
                int option = Menu();
                switch (option) {
                    case 0:
                        System.out.println("What king of signature do you want to use??");
                        System.out.println("(HS256 / RS256)");
                        String signature_type = scan.nextLine();
                        assinar(signature_type);
                        System.out.println("\n");
                        break;
                    case 1:
                        System.out.println("What king of signature do you want to use??");
                        System.out.println("(HS256 / RS256)");
                        signature_type = scan.nextLine();
                        verificar(signature_type);
                        System.out.println("\n");
                        break;
                    case 99:
                        System.exit(0);
                }
            } catch (Exception ex) {
                System.out.println("Erro executing operations!");
                ex.printStackTrace();
            }
        }
    }
}
