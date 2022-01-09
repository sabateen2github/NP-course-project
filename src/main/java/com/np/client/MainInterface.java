package com.np.client;

import com.np.client.admin.AdminInterface;
import com.np.client.auth.AdminAuth;
import com.np.client.auth.CustomerAuth;
import com.np.client.customer.CustomerInterface;
import com.np.client.exceptions.WrongPrivateKeyException;
import com.np.common.Session;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

import static com.np.common.Session.TYPES_ADMIN;
import static com.np.common.Session.TYPES_CUSTOMER;

public class MainInterface {


    public static Session session;

    public static final int LOGIN_OPTION = 1;
    public static final int SIGN_UP_OPTION = 2;

    /**
     * This is the first entry point when starting the program.
     */
    public static void startInteracting() {
        System.out.println("Welcome to our ordering system!");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                getMainAction(scanner);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    /**
     * This method checks whether a user is logged in or not and shows the suitable menu accordingly [login/signup, customer main, admin main]
     *
     * @param scanner
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void getMainAction(Scanner scanner) throws IOException, ClassNotFoundException {
        if (session != null) {
            handleLoggedin(scanner);
        } else {
            handleAuthentication(scanner);
        }
    }

    /**
     * This method checks the account type and shows the suitable menu accordingly [customer main, admin main].
     *
     * @param scanner
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void handleLoggedin(Scanner scanner) throws IOException, ClassNotFoundException {
        switch (session.accountType) {
            case TYPES_CUSTOMER:
                CustomerInterface.handleCustomerInterface(scanner);
                break;
            case TYPES_ADMIN:
                AdminInterface.handleAdminInterface(scanner);
                break;
            default:
                throw new IllegalStateException("Unknown account type: #" + session.accountType);
        }
    }


    /**
     * This method is the main entry point for starting the authentication process [login/signup]
     *
     * @param scanner
     */
    private static void handleAuthentication(Scanner scanner) {

        System.out.println("Please choose one of the following actions: " +
                "\n[1] Login" +
                "\n[2] Signup");
        int option = scanner.nextInt();
        scanner.nextLine();
        if (option == LOGIN_OPTION) {
            int type = getUserType(scanner);
            if (type == TYPES_CUSTOMER) {
                loginAdmin(scanner);
            } else {
                loginCustomer(scanner);
            }

        } else if (option == SIGN_UP_OPTION) {
            int type = getUserType(scanner);
            if (type == TYPES_CUSTOMER) {
                signUpAdmin(scanner);
            } else {
                signUpUser(scanner);
            }
        } else {
            System.out.println("Please choose a valid option!");
        }
    }

    /**
     * This method is the entry point to walk through the process of logging with a customer.
     *
     * @param scanner
     */
    private static void loginCustomer(Scanner scanner) {
        try {
            session = CustomerAuth.handleLogin(scanner);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method is the entry point to walk through the process of logging with an admin.
     *
     * @param scanner
     */
    private static void loginAdmin(Scanner scanner) {

        try {
            session = AdminAuth.handleLogin(scanner);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method is the entry point to walk through the process of signing up with an admin.
     *
     * @param scanner
     */
    private static void signUpAdmin(Scanner scanner) {
        try {
            AdminAuth.handleSignup(scanner);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("\n\n\n\n");
        } catch (WrongPrivateKeyException e) {
            System.out.println("Please enter the correct Private key!");
        }
    }

    /**
     * This method is the entry point to walk through the process of signing up with a customer.
     *
     * @param scanner
     */
    private static void signUpUser(Scanner scanner) {
        try {
            CustomerAuth.handleSignup(scanner);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("\n\n\n\n");
        }
    }

    /**
     * This method asks the user to select the account to type when signing in or signing up.
     *
     * @param scanner
     * @return
     */
    public static int getUserType(Scanner scanner) {
        while (true) {
            System.out.println("Please select account type : " +
                    "\n[1] Admin" +
                    "\n[2] Customer");
            try {
                int type = scanner.nextInt();
                scanner.nextLine();
                if (type == 1 || type == 2) {
                    return type;
                }
                throw new InputMismatchException();
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid option!");
            }

        }
    }

}
