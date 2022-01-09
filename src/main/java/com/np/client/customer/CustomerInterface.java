package com.np.client.customer;

import com.np.client.MainInterface;
import com.np.common.Item;
import com.np.common.Order;
import com.np.common.opcodes.OpCodes;
import com.np.common.responsecodes.ResponseCodes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.np.client.ItemsHelper.getAllItems;
import static com.np.client.TCPClient.SERVICE_HOST;
import static com.np.client.TCPClient.SERVICE_PORT;

public class CustomerInterface {

    /**
     * This is the main entry point to handle the customer related actions [make new order, list orders, exit].
     *
     * @param scanner
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void handleCustomerInterface(Scanner scanner) throws IOException, ClassNotFoundException {
        int option = getMainOption(scanner);
        if (option == 1) handleMakeNewOrder(scanner);
        if (option == 2) handleListOrders(scanner);
        if (option == 3) handleExit(scanner);
    }

    /**
     * This is the main entry point to handle the process of making a new order for a customer
     *
     * @param scanner
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void handleMakeNewOrder(Scanner scanner) throws IOException, ClassNotFoundException {
        List<Item> itemList = getAllItems();

        System.out.println("Printing items....");
        itemList.stream().forEach(it -> System.out.println(it));

        boolean wantToAddMoreItems = true;

        Set<Item> finalItemsList = new HashSet<>();

        while (wantToAddMoreItems) {
            finalItemsList = getListOfItem(itemList, finalItemsList, scanner);
            wantToAddMoreItems = wantToAddMoreItems(scanner);
        }

        final Order order = new Order();
        finalItemsList.stream().forEach(it -> order.addItem(it));

        Order result = submitOrder(order);
        System.out.println("Order submitted successfully:");
        System.out.println(result);
    }

    /**
     * This method communicates with server to submit an order for a customer
     *
     * @param order
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static Order submitOrder(Order order) throws IOException, ClassNotFoundException {

        try (Socket socket = new Socket(SERVICE_HOST, SERVICE_PORT)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            objectOutputStream.writeInt(OpCodes.OP_SUBMIT_ORDER);
            objectOutputStream.writeObject(MainInterface.session);
            objectOutputStream.writeObject(order);
            objectOutputStream.flush();

            int response = objectInputStream.readInt();
            if (response != ResponseCodes.RESPONSE_SUBMIT_ORDER_SUCCEED)
                throw new RuntimeException("Failed to submit order!");
            return (Order) objectInputStream.readObject();
        } catch (UnknownHostException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (ClassNotFoundException e) {
            throw e;
        }
    }

    /**
     * This method captures the response of the user regarding whether he wants to add more items to the order or not.
     *
     * @param scanner
     * @return
     */
    private static boolean wantToAddMoreItems(Scanner scanner) {
        while (true) {
            try {
                System.out.println("Do you want to add more items?" +
                        "\n[1] Yes" +
                        "\n[2] No");
                int option = scanner.nextInt();
                scanner.nextLine();
                return option == 1;
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid option!");
                scanner.nextLine();
            }
        }
    }


    /**
     * This method captures the response of the user regarding whether he wants to remove more items from the order or not.
     *
     * @param scanner
     * @return
     */
    private static boolean wantToRemoveMoreItems(Scanner scanner) {
        while (true) {
            try {
                System.out.println("Do you want to remove more items?" +
                        "\n[1] Yes" +
                        "\n[2] No");
                int option = scanner.nextInt();
                scanner.nextLine();
                return option == 1;
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid option!");
                scanner.nextLine();
            }
        }
    }

    /**
     * This method captures the set of items that a user wants to add to the order [comma separated].
     * This method can be called multiple times, depending on the user.
     *
     * @param allItems
     * @param finalItemSet
     * @param scanner
     * @return
     */
    private static Set<Item> getListOfItem(List<Item> allItems, Set<Item> finalItemSet, Scanner scanner) {
        while (true) {
            System.out.println("Please enter the ids of the items that you want to order (comma seperated):");
            String line = scanner.nextLine();
            String[] numbers = line.replace(" ", "").split(",");
            try {
                List<Item> idList = Stream.of(numbers).map(it -> {
                    int id = Integer.parseInt(it);
                    Optional<Item> item = allItems.stream().filter(i -> i.id == id).findAny();
                    if (item.isEmpty())
                        throw new InputMismatchException();
                    return item.get();
                }).collect(Collectors.toList());

                finalItemSet.addAll(idList);

                return finalItemSet;
            } catch (InputMismatchException | NumberFormatException e) {
                System.out.println("Please enter valid ids!");
            }
        }
    }

    /**
     * This method captures the set of items that a user wants to remove from the order [comma separated].
     * This method can be called multiple times, depending on the user.
     *
     * @param order
     * @param scanner
     * @return
     */
    private static List<Item> getItemsToRemoveFromOrder(Order order, Scanner scanner) {
        while (true) {

            System.out.println("Printing current order items.....");
            order.getItems().stream().forEach(it -> System.out.println(it));
            System.out.println("Please enter the ids of the items that you want to remove (comma seperated):");
            String line = scanner.nextLine();
            String[] numbers = line.replace(" ", "").split(",");
            try {
                List<Item> idList = Stream.of(numbers).map(it -> {
                    int id = Integer.parseInt(it);
                    Optional<Item> item = order.getItems().stream().filter(i -> i.id == id).findAny();
                    if (item.isEmpty())
                        throw new InputMismatchException();
                    return item.get();
                }).collect(Collectors.toList());

                return idList;
            } catch (InputMismatchException | NumberFormatException e) {
                System.out.println("Please enter valid ids!");
            }
        }
    }

    /**
     * This is the main entry point for the process of listing the historic orders of the customer and doing further actions on them.
     *
     * @param scanner
     */
    private static void handleListOrders(Scanner scanner) {
        System.out.println("Printing orders....");
        List<Order> orderList = getOrderList();
        if (orderList.isEmpty()) {
            System.out.println("No orders!");
            return;
        }
        orderList.stream().forEach(it -> System.out.println(it.code + " - " + it.getDateCreated()));
        handlePreviousOrdersOps(scanner, orderList);

    }

    /**
     * This method captures the user input regarding the actions that he/she wants to do on the historic orders [reorder, show orders' details]
     *
     * @param scanner
     * @param history
     */
    private static void handlePreviousOrdersOps(Scanner scanner, List<Order> history) {

        int option = getPreviousOrdersOption(scanner);
        switch (option) {
            case 1:
                handleReorder(history, scanner);
                break;
            case 2:
                handleShowOrder(history, scanner);
                break;
            default:
                throw new RuntimeException("Unknown option : " + option);
        }

    }

    /**
     * This method asks the user to select an item that he had already purchased to give it a review.
     *
     * @param itemList
     * @param scanner
     * @return
     */
    private static Item getItemForReview(List<Item> itemList, Scanner scanner) {
        System.out.println("Printing Items....");
        itemList.stream().forEach(it -> System.out.println(it));
        while (true) {
            System.out.println("Please enter the id of the item.");
            try {
                int itemId = scanner.nextInt();
                scanner.nextLine();
                Optional<Item> item = itemList.stream().filter(it -> it.id == itemId).findAny();
                if (item.isEmpty()) throw new InputMismatchException();
                return item.get();
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid Item id!");
            }
        }
    }

    /**
     * This method asks the user to select a historic order for further operations [reorder, show orders' details]
     *
     * @param orderList
     * @param scanner
     * @param reorder
     * @return
     */
    private static Order getOrderFromUser(List<Order> orderList, Scanner scanner, boolean reorder) {
        while (true) {
            if (reorder)
                System.out.println("Please enter the code of the order you want to reorder.");
            else System.out.println("Please enter the code of the order you want to show.");


            try {
                int orderCode = scanner.nextInt();
                scanner.nextLine();
                Optional<Order> order = orderList.stream().filter(it -> it.code == orderCode).findAny();
                if (order.isEmpty()) throw new InputMismatchException();
                return order.get();
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid orderCode!");
            }
        }
    }

    /**
     * This the main entry point for the process of reordering a previous order
     *
     * @param orderList
     * @param scanner
     */
    private static void handleReorder(List<Order> orderList, Scanner scanner) {
        Order oldOlder = getOrderFromUser(orderList, scanner, true);
        Optional<Order> orderOptional = oldOlder.copy();
        if (orderOptional.isEmpty()) throw new RuntimeException("Order copy should not be null!");
        Order order = orderOptional.get();

        int options = getReorderOptions(scanner);
        if (options == 1) {
            handleReorderAddItems(order, scanner);
        } else if (options == 2) {
            handleReorderRemoveItems(order, scanner);
        }

        try {
            Order result = submitOrder(order);
            System.out.println("Order submitted successfully:");
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to submit order");
        }
    }


    /**
     * This methods asks the user whether he wants to remove/add items to the order before proceeding or not.
     *
     * @param scanner
     * @return
     */
    private static int getReorderOptions(Scanner scanner) {
        while (true) {
            System.out.println("Please choose one of the following options:" +
                    "\n[1] Add Items." +
                    "\n[2] Remove Items." +
                    "\n[3] Submit Order.");
            try {
                int option = scanner.nextInt();
                scanner.nextLine();
                if (option > 3 || option < 1) throw new InputMismatchException();
                return option;
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid option!");
                scanner.nextLine();
            }
        }
    }

    /**
     * This method is used in the reordering process to add more items to the order.
     * This method may be called multiple times depending on the user.
     *
     * @param order
     * @param scanner
     */
    private static void handleReorderAddItems(Order order, Scanner scanner) {
        try {
            List<Item> itemList = getAllItems();
            System.out.println("Printing Items...");
            itemList.stream().forEach(it -> System.out.println(it));
            boolean wantToAddMoreItems = true;
            Set<Item> finalItemsList = order.getItems().stream().collect(Collectors.toSet());
            while (wantToAddMoreItems) {
                finalItemsList = getListOfItem(itemList, finalItemsList, scanner);
                wantToAddMoreItems = wantToAddMoreItems(scanner);
            }
            finalItemsList.stream().forEach(it -> order.addItem(it));

        } catch (Exception e) {
            throw new RuntimeException("Error occurred");
        }
    }

    /**
     * This method is used in the reordering process to remove  items from the order.
     * This method may be called multiple times depending on the user.
     *
     * @param order
     * @param scanner
     */
    private static void handleReorderRemoveItems(Order order, Scanner scanner) {
        boolean wantToRemoveMoreItems = true;
        while (wantToRemoveMoreItems) {
            List<Item> itemList = getItemsToRemoveFromOrder(order, scanner);
            order.getItems().removeAll(itemList);
            wantToRemoveMoreItems = wantToRemoveMoreItems(scanner);
        }
    }

    /**
     * This is the main entry point for the show order option after choosing to list the previous orders
     *
     * @param history
     * @param scanner
     */
    private static void handleShowOrder(List<Order> history, Scanner scanner) {
        Order order = getOrderFromUser(history, scanner, false);
        System.out.println(order);
        if (wantsToAddReview(scanner)) {
            handleAddReview(order, scanner);
        }
    }

    /**
     * This method is main entry point to add review for an item. It works as follows:
     * 1. Choose an item from a previously ordered item.
     * 2. Get a review from user.
     * 3. Submit review to a server.
     *
     * @param order
     * @param scanner
     */
    private static void handleAddReview(Order order, Scanner scanner) {

        Item item = getItemForReview(order.getItems(), scanner);
        String review = getReview(scanner);
        submitReview(item, review);
    }


    /**
     * This method communicates with the server to submit a review for an item
     *
     * @param item
     * @param review
     */
    private static void submitReview(Item item, String review) {
        try (Socket server = new Socket(SERVICE_HOST, SERVICE_PORT)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(server.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(server.getInputStream());

            objectOutputStream.writeInt(OpCodes.OP_SUBMIT_REVIEW);
            objectOutputStream.writeObject(MainInterface.session);
            objectOutputStream.writeInt(item.id);
            objectOutputStream.writeObject(review);
            objectOutputStream.flush();

            int response = objectInputStream.readInt();

            if (response == ResponseCodes.RESPONSE_REVIEW_FAILED)
                System.out.println("Failed to save review for item :" + item + "  review: " + review);
            else if (response == ResponseCodes.RESPONSE_REVIEW_SUBMITTED)
                System.out.println("Submitted review for item :" + item + "  review: " + review);
            else throw new RuntimeException("Unknown Response! " + response);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error in submitting a review!");
        }

    }

    /**
     * This method is used to get a review from the customer using a command line interface.
     *
     * @param scanner
     * @return
     */
    private static String getReview(Scanner scanner) {
        while (true) {
            try {
                System.out.println("Please enter a review:");
                String review = scanner.nextLine();
                return review;
            } catch (Exception e) {
                System.out.println("Please enter a valid Review!");
            }
        }
    }

    /**
     * This method is used to ask the user whether he wants to add a review for an item that was purchased in a previously selected order.
     *
     * @param scanner
     * @return
     */
    private static boolean wantsToAddReview(Scanner scanner) {
        while (true) {
            try {
                System.out.println("Do you want to add review for an item?" +
                        "\n[1] Yes" +
                        "\n[2] No");
                int option = scanner.nextInt();
                scanner.nextLine();
                return option == 1;
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid option!");
                scanner.nextLine();
            }
        }
    }

    /**
     * This method asks the user to select an action to do on a previous order. [order, show orders' details]
     *
     * @param scanner
     * @return
     */
    private static int getPreviousOrdersOption(Scanner scanner) {
        while (true) {
            System.out.println("Please choose one of the following options:" +
                    "\n[1] Reorder." +
                    "\n[2] Show Order.");
            try {
                int option = scanner.nextInt();
                scanner.nextLine();
                if (option > 2 || option < 1) throw new InputMismatchException();
                return option;
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid option!");
                scanner.nextLine();
            }
        }
    }


    /**
     * This method communicates with the server to get all the historic orders of the customer.
     *
     * @return
     */
    private static List<Order> getOrderList() {

        try (Socket server = new Socket(SERVICE_HOST, SERVICE_PORT)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(server.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(server.getInputStream());

            objectOutputStream.writeInt(OpCodes.OP_GET_ORDERS);
            objectOutputStream.writeObject(MainInterface.session);
            objectOutputStream.flush();

            int response = objectInputStream.readInt();
            if (response != ResponseCodes.RESPONSE_GET_ORDERS_SUCCEED)
                throw new RuntimeException("Failed to retrieve order!");
            List<Order> orderList = (List<Order>) objectInputStream.readObject();
            return orderList;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get order list");
        }
    }

    /**
     * This methods exits the program
     *
     * @param scanner
     */
    private static void handleExit(Scanner scanner) {
        System.exit(0);
    }


    /**
     * This method asks the user to choose one of the following options [Make new order, list order, exit]
     *
     * @param scanner
     * @return
     */
    private static int getMainOption(Scanner scanner) {
        while (true) {
            System.out.println("Please choose one of the following options:" +
                    "\n[1] Make a new order." +
                    "\n[2] List orders." +
                    "\n[3] Exit");
            try {
                int option = scanner.nextInt();
                scanner.nextLine();
                if (option > 3 || option < 1) throw new InputMismatchException();
                return option;
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid option!");
                scanner.nextLine();
            }
        }
    }
}
