package com.np.client.admin;

import com.np.common.Item;
import com.np.common.opcodes.OpCodes;
import com.np.common.responsecodes.ResponseCodes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static com.np.client.ItemsHelper.getAllItems;
import static com.np.client.TCPClient.SERVICE_HOST;
import static com.np.client.TCPClient.SERVICE_PORT;

public class AdminInterface {

    /**
     * This is the main entry point after an admin login
     *
     * @param scanner
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void handleAdminInterface(Scanner scanner) throws IOException, ClassNotFoundException {

        System.out.println("Please choose one of the following actions:" +
                "\n[1] Add Item" +
                "\n[2] Modify Item" +
                "\n[3] Remove Item");

        int action = scanner.nextInt();
        scanner.nextLine();
        switch (action) {
            case 1:
                handleAddItem(scanner);
                break;
            case 2:
                handleModifyItem(scanner);
                break;
            case 3:
                handleRemoveItem(scanner);
                break;
            default:
                System.out.println("Please choose a valid option!");
                break;
        }

    }

    /**
     * This method is the main entry point for initiating the remove item process
     *
     * @param scanner
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void handleRemoveItem(Scanner scanner) throws IOException, ClassNotFoundException {
        List<Item> items = getAllItems();
        System.out.println("Printing items....");
        items.forEach(item -> System.out.println(item));
        System.out.println("Please enter the item id you want to remove.");

        boolean removed = false;
        while (!removed) {
            try {
                int itemId = scanner.nextInt();
                scanner.nextLine();
                Optional<Item> optionalItem = items.stream().filter(item -> item.id == itemId).findAny();
                if (optionalItem.isEmpty()) {
                    throw new InputMismatchException();
                }

                Item item = optionalItem.get();

                removeItem(item.id);
                removed = true;

            } catch (InputMismatchException e) {
                System.out.println("You need to enter a valid id");
                scanner.nextLine();
            }
        }
    }

    /**
     * This method communicates with the server to remove an item
     *
     * @param id
     * @throws IOException
     */
    private static void removeItem(int id) throws IOException {
        try (Socket socket = new Socket(SERVICE_HOST, SERVICE_PORT)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            objectOutputStream.writeInt(OpCodes.OP_REMOVE_ITEM);
            objectOutputStream.writeInt(id);
            objectOutputStream.flush();

            int response = objectInputStream.readInt();
            switch (response) {
                case ResponseCodes.RESPONSE_REMOVE_ITEM_SUCCEED:
                    System.out.println("Item has been removed successfully!");
                    break;
                case ResponseCodes.RESPONSE_REMOVE_ITEM_FAILED:
                    System.out.println("Item failed to be modified.");
                    break;
                default:
                    throw new IllegalStateException("Unknown response code: " + response);
            }
        } catch (UnknownHostException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * This method is the entry point for the process of modifying an item as an Admin
     *
     * @param scanner
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void handleModifyItem(Scanner scanner) throws IOException, ClassNotFoundException {
        List<Item> items = getAllItems();
        System.out.println("Printing items....");
        items.forEach(item -> System.out.println(item));
        System.out.println("Please enter the item id you want to modify.");

        boolean modified = false;
        while (!modified) {
            try {
                int itemId = scanner.nextInt();
                scanner.nextLine();
                Optional<Item> optionalItem = items.stream().filter(item -> item.id == itemId).findAny();
                if (optionalItem.isEmpty()) {
                    throw new InputMismatchException();
                }

                Item item = optionalItem.get();
                modifyId(item, scanner);
                modifyName(item, scanner);
                modifyPrice(item, scanner);
                modifyTax(item, scanner);

                saveModification(itemId, item);
                modified = true;

            } catch (InputMismatchException e) {
                System.out.println("You need to enter a valid id");
                scanner.nextLine();
            }
        }
    }

    /**
     * This method is used to communicate with the server to submit the modifications of an item
     *
     * @param oldId
     * @param newItem
     * @throws IOException
     */
    private static void saveModification(int oldId, Item newItem) throws IOException {

        try (Socket socket = new Socket(SERVICE_HOST, SERVICE_PORT)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            objectOutputStream.writeInt(OpCodes.OP_MODIFY_ITEM);
            objectOutputStream.writeInt(oldId);
            objectOutputStream.writeObject(newItem);
            objectOutputStream.flush();

            int response = objectInputStream.readInt();
            switch (response) {
                case ResponseCodes.RESPONSE_ITEM_MODIFIED_SUCCESSFULLY:
                    System.out.println("Item has been modified successfully!");
                    break;
                case ResponseCodes.RESPONSE_FAILED_TO_MODIFY_ITEM:
                    System.out.println("Item failed to be modified.");
                    break;
                default:
                    throw new IllegalStateException("Unknown response code: " + response);
            }
        } catch (UnknownHostException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * This is a helper method to capture the id of an item from the admin using command line interface
     *
     * @param it
     * @param scanner
     */
    private static void modifyId(Item it, Scanner scanner) {
        System.out.println("Current item id is : " + it.id);
        int id = getItemId(scanner);
        it.id = id;
    }

    /**
     * This is a helper method to capture the name of an item from the admin using command line interface
     *
     * @param item
     * @param scanner
     */
    private static void modifyName(Item item, Scanner scanner) {
        System.out.println("Current item name is : " + item.name);
        String name = getItemName(scanner);
        item.name = name;
    }

    /**
     * This is a helper method to capture the price of an item from the admin using command line interface
     *
     * @param item
     * @param scanner
     */
    private static void modifyPrice(Item item, Scanner scanner) {
        System.out.println("Current item price is (without tax): " + item.getPriceWithoutTax());
        float priceWithoutTax = getItemPriceWithoutTax(scanner);
        item.setPriceWithoutTax(priceWithoutTax);
    }

    /**
     * This is a helper method to capture the tax of an item from the admin using command line interface
     *
     * @param item
     * @param scanner
     */
    private static void modifyTax(Item item, Scanner scanner) {
        System.out.println("Current item tax is : " + item.getSalesTax());
        float taxPercentage = getItemTaxPercentage(scanner);
        item.setSalesTax(taxPercentage);
    }


    /**
     * This method is the entry point for the process of adding new items as an admin
     *
     * @param scanner
     * @throws IOException
     */
    private static void handleAddItem(Scanner scanner) throws IOException {
        System.out.println("Please add Item details.");
        int itemId = getItemId(scanner);
        String itemName = getItemName(scanner);
        float itemPrice = getItemPriceWithoutTax(scanner);
        float itemTaxPct = getItemTaxPercentage(scanner);

        try (Socket server = new Socket(SERVICE_HOST, SERVICE_PORT)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(server.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(server.getInputStream());

            objectOutputStream.writeInt(OpCodes.OP_ADD_ITEM);
            objectOutputStream.writeInt(itemId);
            objectOutputStream.writeObject(itemName);
            objectOutputStream.writeFloat(itemPrice);
            objectOutputStream.writeFloat(itemTaxPct);
            objectOutputStream.flush();

            int response = objectInputStream.readInt();
            switch (response) {
                case ResponseCodes.RESPONSE_ITEM_ADDED_SUCCESSFULLY:
                    System.out.println("Item added successfully!");
                    break;
                case ResponseCodes.RESPONSE_FAILED_TO_ADD_NEW_ITEM:
                    System.out.println("Item failed to be added, please check for any id conflicts!");
                    break;
                default:
                    throw new IllegalStateException("Unknown respose code: " + response);
            }
        } catch (UnknownHostException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
    }


    /**
     * This a helper method to capture the item id from the admin using a command line interface.
     *
     * @param scanner
     * @return
     */
    private static int getItemId(Scanner scanner) {

        while (true) {
            System.out.println("Please enter the id of the item:");
            try {
                int item = scanner.nextInt();
                scanner.nextLine();
                return item;
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid Item price! (e.g. 12345)");
                scanner.nextLine();
            }
        }
    }

    /**
     * This a helper method to capture the item name from the admin using a command line interface.
     *
     * @param scanner
     * @return
     */
    private static String getItemName(Scanner scanner) {

        while (true) {
            System.out.println("Please enter the name of the Item:");
            String item = scanner.nextLine();
            if (item == null || item.length() == 0) {
                System.out.println("Please enter a valid Item name!");
                continue;
            }
            return item;
        }
    }

    /**
     * This a helper method to capture the item price without tax from the admin from a command line interface.
     *
     * @param scanner
     * @return
     */
    private static float getItemPriceWithoutTax(Scanner scanner) {

        while (true) {
            System.out.println("Please enter the price of the Item (without tax):");
            try {
                float item = scanner.nextFloat();
                scanner.nextLine();
                return item;
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid Item price! (e.g. 12.75)");
                scanner.nextLine();
            }
        }
    }

    /**
     * This a helper method to capture the item tax percentage from the admin from a command line interface
     *
     * @param scanner
     * @return
     */
    private static float getItemTaxPercentage(Scanner scanner) {

        while (true) {
            System.out.println("Please enter the percentage of the tax (positive fractions):");
            try {
                float item = scanner.nextFloat();
                scanner.nextLine();
                if (item < 0) throw new InputMismatchException();
                return item;
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid Item price! (e.g. 12.75)");
                scanner.nextLine();
            }
        }
    }
}
