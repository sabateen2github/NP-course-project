package com.np.client.auth;

import com.np.client.exceptions.WrongPrivateKeyException;
import com.np.common.Session;
import com.np.common.opcodes.OpCodes;
import com.np.common.responsecodes.ResponseCodes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import static com.np.client.TCPClient.SERVICE_HOST;
import static com.np.client.TCPClient.SERVICE_PORT;

public class AdminAuth {

    /**
     * This method is is the main entry point for the login process, as an admin.
     *
     * @param scanner
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Session handleLogin(Scanner scanner) throws IOException, ClassNotFoundException {

        while (true) {

            System.out.println("Please enter your username: ");
            String username = scanner.nextLine();
            System.out.println("Please enter your password: ");
            String password = scanner.nextLine();

            try (Socket server = new Socket(SERVICE_HOST, SERVICE_PORT)) {

                ObjectOutputStream objectOutputStream = new ObjectOutputStream(server.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(server.getInputStream());

                objectOutputStream.writeInt(OpCodes.OP_LOGIN_ADMIN);
                objectOutputStream.writeObject(username);
                objectOutputStream.writeObject(password);
                objectOutputStream.flush();

                int response = objectInputStream.readInt();
                switch (response) {
                    case ResponseCodes.RESPONSE_LOGIN_SUCCEEDED:
                        System.out.println("Login Succeeded.");
                        return (Session) objectInputStream.readObject();
                    case ResponseCodes.RESPONSE_PASSWORD_WRONG:
                        System.out.println("Password is wrong!");
                        break;
                    case ResponseCodes.RESPONSE_USERNAME_WRONG:
                        System.out.println("Username does not exist!");
                        break;
                    default:
                        throw new IllegalStateException("Response type not supported! #" + response);
                }

            } catch (UnknownHostException e) {
                throw e;
            } catch (IOException e) {
                throw e;
            } catch (ClassNotFoundException e) {
                throw e;
            }
        }
    }

    /**
     * This method is the main entry point for the signup process, as an admin.
     *
     * @param scanner
     * @return
     * @throws IOException
     * @throws WrongPrivateKeyException
     */
    public static long handleSignup(Scanner scanner) throws IOException, WrongPrivateKeyException {

        System.out.println("Please enter the private code to signup: ");
        String privateCode = scanner.nextLine();

        try (Socket server = new Socket(SERVICE_HOST, SERVICE_PORT)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(server.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(server.getInputStream());

            objectOutputStream.writeInt(OpCodes.OP_CHECK_PRIVATE_KEY);
            objectOutputStream.writeObject(privateCode);
            objectOutputStream.flush();

            boolean proceed = objectInputStream.readBoolean();
            if (!proceed) {
                throw new WrongPrivateKeyException();
            }
        } catch (IOException e) {
            throw e;
        }

        while (true) {
            try (Socket server = new Socket(SERVICE_HOST, SERVICE_PORT)) {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(server.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(server.getInputStream());

                String username, password;

                System.out.println("Please enter the username:");
                username = scanner.nextLine();
                if (username == null || username.length() == 0) {
                    System.out.println("User name should not be null!");
                    continue;
                }
                System.out.println("Please enter the password:");
                password = scanner.nextLine();
                if (password == null || password.length() == 0) {
                    System.out.println("User name should not be null!");
                    continue;
                }

                if (signupLoop(privateCode, username, password, objectInputStream, objectOutputStream, scanner)) {
                    long id = objectInputStream.readLong();
                    System.out.println("Admin has been created. #ID: " + id);
                    return id;
                }
            } catch (IOException e) {
                throw e;
            }
        }
    }

    /**
     * This method communicates with the server in order to signup an admin
     *
     * @param privateCode
     * @param username
     * @param password
     * @param objectInputStream
     * @param objectOutputStream
     * @param scanner
     * @return
     * @throws IOException
     */
    private static boolean signupLoop(String privateCode, String username, String password, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream, Scanner scanner) throws IOException {
        objectOutputStream.writeInt(OpCodes.OP_SIGN_UP_ADMIN);
        objectOutputStream.writeObject(privateCode);
        objectOutputStream.writeObject(username);
        objectOutputStream.writeObject(password);
        objectOutputStream.flush();

        int responseCode = 0;
        responseCode = objectInputStream.readInt();


        switch (responseCode) {
            case ResponseCodes.RESPONSE_PRIVATE_CODE_IS_WRONG:
                throw new IllegalStateException("PrivateCode should be correct!");
            case ResponseCodes.RESPONSE_USERNAME_ALREADY_EXIST:
                System.out.println("Please choose a different username.");
                return false;
            case ResponseCodes.RESPONSE_SIGNUP_SUCCEEDED:
                return true;
            default:
                throw new RuntimeException("Unknown response code");
        }
    }


}
