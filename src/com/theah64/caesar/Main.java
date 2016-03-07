package com.theah64.caesar;

import com.theah64.caesar.utils.CommonUtils;

import java.io.Console;
import java.util.Scanner;

/**
 * Created by shifar on 7/3/16.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        //Asking password

        //Asking email
        String email;
        do {
            System.out.print("Enter your google email: ");
            email = scanner.nextLine();
        } while (!CommonUtils.isValidEmail(email));


        String password;
        final Console console = System.console();
        if (console == null) {
            System.out.println("Error: couldn't find console. Using demo password.");
            
        } else {
            final char[] passArr = console.readPassword("Enter password: ");
            password = new String(passArr);
        }


    }

}
