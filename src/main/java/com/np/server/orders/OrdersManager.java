package com.np.server.orders;

import com.np.common.Order;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * *This class handles the submitting, retrieval and persisting order details on disks.
 */
public class OrdersManager {

    private static OrdersManager ordersManager;

    private static Map<Long, List<Order>> ordersMap;
    private static final String FILE_NAME = "orders.file";

    public static OrdersManager getInstance() throws IOException {
        if (ordersManager == null) {
            ordersManager = new OrdersManager();
        }
        return ordersManager;
    }

    private OrdersManager() throws IOException {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
                ordersMap = new HashMap<>();
                save();
                return;
            } catch (IOException e) {
                throw e;
            }
        }

        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            try {
                ordersMap = (Map<Long, List<Order>>) objectInputStream.readObject();
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Class should be found!");
            }
        }
    }

    private void save() throws IOException {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            objectOutputStream.writeObject(ordersMap);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("File must have already been created!");
        }
    }

    public List<Order> getCustomerOrders(long customerId) {
        if (!ordersMap.containsKey(customerId)) return Arrays.asList();
        return ordersMap.get(customerId);
    }

    public Order submitOrder(long customerId, Order order) throws IOException {

        if (!ordersMap.containsKey(customerId)) ordersMap.put(customerId, new ArrayList<>());
        Set<Integer> codes = ordersMap.get(customerId).stream().map(it -> it.code).collect(Collectors.toSet());

        order.code = (int) UUID.randomUUID().getLeastSignificantBits();
        while (codes.contains(order.code)) order.code = (int) UUID.randomUUID().getLeastSignificantBits();
        ordersMap.get(customerId).add(order);
        save();
        return order;
    }

}
