package App;

import java.util.Scanner;

import static App.Funcoes.cifrar;
import static App.Funcoes.decifrar;

public class App {
    static int Menu() {
        Scanner scan = new Scanner(System.in);
        int option;
        do {
            System.out.println("######## MENU ##########");
            System.out.println("Options:");
            System.out.println(" 0: Cifrar");
            System.out.println(" 1: Decifrar");
            System.out.println("..........");
            System.out.println("99: Exit");
            System.out.print("Enter an Option:");
            option = scan.nextInt();
        } while (!((option >= 0 && option <= 1) || option == 99));
        return option;
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        boolean end = false;
        while (!end) {
            try {
                int option = Menu();
                switch (option) {
                    case 0:
                        System.out.println("Name of File");
                        String name = scan.nextLine();
                        cifrar(name);
                        System.out.println("\n");
                        break;
                    case 1:
                        System.out.println("Name of File");
                        String name1 = scan.nextLine();
                        System.out.println("Path File of Key");
                        String key = scan.nextLine();
                        System.out.println("Path File of IV");
                        String iv = scan.nextLine();
                        decifrar(name1, key, iv);
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
