package com.np.server;

import com.google.common.hash.Hashing;
import com.np.common.Item;
import com.np.common.Order;
import com.np.common.Session;
import com.np.common.opcodes.OpCodes;
import com.np.common.responsecodes.ResponseCodes;
import com.np.server.auth.AdminAuthentication;
import com.np.server.auth.CustomerAuthentication;
import com.np.server.auth.SessionServer;
import com.np.server.exceptions.*;
import com.np.server.items.ItemsManager;
import com.np.server.orders.OrdersManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is the engine of the server where requests are mapped to their respective views (methods).
 */
public class RequestsHandler {

    public static void handleRequest(Socket client) throws IOException, UnsupportedOperationException, ClassNotFoundException {

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
        ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());

        int opCode = objectInputStream.readInt();
        System.out.println("Code is: " + opCode);
        switch (opCode) {
            case OpCodes.OP_CHECK_PRIVATE_KEY:
                checkPrivateKey(objectInputStream, objectOutputStream);
                break;
            case OpCodes.OP_SIGN_UP_ADMIN:
                signUpAdmin(objectInputStream, objectOutputStream);
                break;
            case OpCodes.OP_SIGN_UP_CUSTOMER:
                signUpCustomer(objectInputStream, objectOutputStream);
                break;
            case OpCodes.OP_LOGIN_ADMIN:
                loginAdmin(objectInputStream, objectOutputStream);
                break;
            case OpCodes.OP_LOGIN_CUSTOMER:
                loginCustomer(objectInputStream, objectOutputStream);
                break;
            case OpCodes.OP_ADD_ITEM:
                addItem(objectInputStream, objectOutputStream);
                break;
            case OpCodes.OP_MODIFY_ITEM:
                modifyItem(objectInputStream, objectOutputStream);
            case OpCodes.OP_GET_ITEMS:
                getItems(objectInputStream, objectOutputStream);
                break;
            case OpCodes.OP_REMOVE_ITEM:
                removeItem(objectInputStream, objectOutputStream);
                break;
            case OpCodes.OP_SUBMIT_ORDER:
                submitOrder(objectInputStream, objectOutputStream);
                break;
            case OpCodes.OP_GET_ORDERS:
                getOrders(objectInputStream, objectOutputStream);
                break;
            case OpCodes.OP_SUBMIT_REVIEW:
                submitReview(objectInputStream, objectOutputStream);
                break;
            default:
                throw new UnsupportedOperationException("Op Code " + opCode + " is unsupported!");
        }
    }

    private static void submitReview(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException {

        try {
            Session session = (Session) objectInputStream.readObject();
            int itemId = objectInputStream.readInt();
            String review = (String) objectInputStream.readObject();

            if (!TCPServer.sessionMap.containsKey(session.sessionID))
                throw new RuntimeException("Session not found: " + session.sessionID);

            ItemsManager manager = ItemsManager.getInstance();
            Optional<Item> optionalItem = manager.getAllItems().filter(it -> it.id == itemId).findAny();
            if (optionalItem.isEmpty()) throw new RuntimeException("Item does not exist id: " + itemId);
            Optional<Item> optionalCopy = optionalItem.get().copy();
            if (optionalItem.isEmpty()) throw new RuntimeException("Was not able to copy Item : " + optionalItem.get());
            Item newItem = optionalCopy.get();
            newItem.addReview(review);
            manager.modifyItem(itemId, newItem);
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_REVIEW_SUBMITTED);

        } catch (Exception e) {
            e.printStackTrace();
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_REVIEW_FAILED);
        }
        objectOutputStream.flush();

    }

    private static void getOrders(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException {
        try {
            Session session = (Session) objectInputStream.readObject();
            SessionServer sessionServer = TCPServer.sessionMap.get(session.sessionID);
            long customerId = sessionServer.userId;
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_GET_ORDERS_SUCCEED);
            objectOutputStream.writeObject(OrdersManager.getInstance().getCustomerOrders(customerId));
        } catch (Exception e) {
            e.printStackTrace();
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_GET_ORDERS_FAILED);
        }
        objectOutputStream.flush();
    }

    private static void submitOrder(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException {
        try {
            Session session = (Session) objectInputStream.readObject();
            Order order = (Order) objectInputStream.readObject();

            SessionServer sessionServer = TCPServer.sessionMap.get(session.sessionID);
            Order code = OrdersManager.getInstance().submitOrder(sessionServer.userId, order);
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_SUBMIT_ORDER_SUCCEED);
            objectOutputStream.writeObject(code);
        } catch (Exception e) {
            e.printStackTrace();
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_SUBMIT_ORDER_FAILED);
        }
        objectOutputStream.flush();
    }

    private static void removeItem(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException {

        try {
            ItemsManager manager = ItemsManager.getInstance();
            manager.removeItem(objectInputStream.readInt());
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_REMOVE_ITEM_SUCCEED);
        } catch (IOException | ItemNotFoundException e) {
            e.printStackTrace();
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_REMOVE_ITEM_FAILED);
        }
        objectOutputStream.flush();
    }

    private static void getItems(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException {
        try {
            ItemsManager manager = ItemsManager.getInstance();
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_GET_ITEMS_SUCCESSFULLY);
            objectOutputStream.writeObject(manager.getAllItems().collect(Collectors.toList()));
        } catch (CreateNewFileException e) {
            e.printStackTrace();
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_FAILED_GET_ITEMS);
        } catch (ReadFileException e) {
            e.printStackTrace();
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_FAILED_GET_ITEMS);
        } catch (IOException e) {
            e.printStackTrace();
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_FAILED_GET_ITEMS);
        }
        objectOutputStream.flush();
    }

    private static void modifyItem(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException, ClassNotFoundException {

        int oldId = objectInputStream.readInt();
        Item item = (Item) objectInputStream.readObject();

        try {
            ItemsManager manager = ItemsManager.getInstance();
            manager.modifyItem(oldId, item);
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_ITEM_MODIFIED_SUCCESSFULLY);
        } catch (ItemAlreadyExistException e) {
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_FAILED_TO_MODIFY_ITEM);
        }
        objectOutputStream.flush();
    }

    private static void addItem(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException, ClassNotFoundException, ItemAlreadyExistException {

        int itemId = objectInputStream.readInt();
        String itemName = (String) objectInputStream.readObject();
        float itemPrice = objectInputStream.readFloat();
        float itemTax = objectInputStream.readFloat();

        Item item = new Item(itemName, itemPrice, itemId);
        item.setSalesTax(itemTax);

        try {
            ItemsManager manager = ItemsManager.getInstance();
            manager.addItem(item);
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_ITEM_ADDED_SUCCESSFULLY);
        } catch (ItemAlreadyExistException e) {
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_FAILED_TO_ADD_NEW_ITEM);
        }
        objectOutputStream.flush();
    }

    private static void loginCustomer(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException, ClassNotFoundException {
        String username = (String) objectInputStream.readObject();
        String password = (String) objectInputStream.readObject();

        try {
            CustomerAuthentication customerAuthentication = CustomerAuthentication.getInstance();
            long customerId = customerAuthentication.authenticate(username, password);
            SessionServer sessionServer = new SessionServer();
            sessionServer.accountType = Session.TYPES_CUSTOMER;
            sessionServer.userId = customerId;

            Session session = new Session();
            session.accountType = Session.TYPES_CUSTOMER;
            session.sessionID = Hashing.sha256().hashLong((long) (customerId + Math.random() * 1000000)).toString();

            while (TCPServer.sessionMap.containsKey(session.sessionID)) {
                session.sessionID = Hashing.sha256().hashLong((long) (customerId + Math.random() * 1000000)).toString();
            }
            TCPServer.sessionMap.put(session.sessionID, sessionServer);

            objectOutputStream.writeInt(ResponseCodes.RESPONSE_LOGIN_SUCCEEDED);
            objectOutputStream.writeObject(session);
        } catch (WrongAuthDetailsException e) {
            if (e.reason == WrongAuthDetailsException.REASON_USERNAME) {
                objectOutputStream.writeInt(ResponseCodes.RESPONSE_USERNAME_WRONG);
            } else {
                objectOutputStream.writeInt(ResponseCodes.RESPONSE_PASSWORD_WRONG);
            }
        }
        objectOutputStream.flush();
    }

    private static void loginAdmin(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException, ClassNotFoundException {
        String username = (String) objectInputStream.readObject();
        String password = (String) objectInputStream.readObject();

        try {
            AdminAuthentication adminAuthentication = AdminAuthentication.getInstance();
            long adminId = adminAuthentication.authenticate(username, password);
            SessionServer sessionServer = new SessionServer();
            sessionServer.accountType = Session.TYPES_ADMIN;
            sessionServer.userId = adminId;

            Session session = new Session();
            session.accountType = Session.TYPES_ADMIN;
            session.sessionID = Hashing.sha256().hashLong((long) (adminId + Math.random() * 1000000)).toString();

            while (TCPServer.sessionMap.containsKey(session.sessionID)) {
                session.sessionID = Hashing.sha256().hashLong((long) (adminId + Math.random() * 1000000)).toString();
            }
            TCPServer.sessionMap.put(session.sessionID, sessionServer);

            objectOutputStream.writeInt(ResponseCodes.RESPONSE_LOGIN_SUCCEEDED);
            objectOutputStream.writeObject(session);
        } catch (WrongAuthDetailsException e) {
            if (e.reason == WrongAuthDetailsException.REASON_USERNAME) {
                objectOutputStream.writeInt(ResponseCodes.RESPONSE_USERNAME_WRONG);
            } else {
                objectOutputStream.writeInt(ResponseCodes.RESPONSE_PASSWORD_WRONG);
            }
        }
        objectOutputStream.flush();
    }

    private static void signUpCustomer(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException, ClassNotFoundException {
        String username = (String) objectInputStream.readObject();
        String password = (String) objectInputStream.readObject();

        CustomerAuthentication customerAuthentication = CustomerAuthentication.getInstance();
        try {
            long customerId = customerAuthentication.signUp(username, password);
            System.out.println(customerId + " customerId");
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_SIGNUP_SUCCEEDED);
            objectOutputStream.writeLong(customerId);
        } catch (UsernameExistException exception) {
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_USERNAME_ALREADY_EXIST);
        }

        objectOutputStream.flush();
    }

    private static void checkPrivateKey(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException, ClassNotFoundException {
        String privateCode = (String) objectInputStream.readObject();
        try {
            AdminAuthentication.getInstance().validateSecretKey(privateCode);
            objectOutputStream.writeBoolean(true);
        } catch (WrongPrivateKeyException e) {
            objectOutputStream.writeBoolean(false);
        }
        objectOutputStream.flush();

    }

    private static void signUpAdmin(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException, ClassNotFoundException {
        String privateCode = (String) objectInputStream.readObject();
        String username = (String) objectInputStream.readObject();
        String password = (String) objectInputStream.readObject();

        AdminAuthentication adminAuthentication = AdminAuthentication.getInstance();

        try {
            long adminId = adminAuthentication.signUp(username, password, privateCode);
            System.out.println(adminId + " adminID");
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_SIGNUP_SUCCEEDED);
            objectOutputStream.writeLong(adminId);
        } catch (UsernameExistException exception) {
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_USERNAME_ALREADY_EXIST);
        } catch (WrongPrivateKeyException exception) {
            objectOutputStream.writeInt(ResponseCodes.RESPONSE_PRIVATE_CODE_IS_WRONG);
        }

        objectOutputStream.flush();
    }


}
