package com.np.common;

import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class Order implements Serializable {
    public int code;
    private ArrayList<Item> items = new ArrayList();
    private Date dateCreated = new Date();
    private static final long serialVersionUID = 42L;

    public Order() {
    }

    public Order(int code) {
        this.code = code;
    }

    public Order(int code, Order order) {
        this.code = code;
        for (int i = 0; i < items.size(); i++)
            items.add(new Item(order.items.get(i).name, order.items.get(i).getPriceWithoutTax(), order.items.get(i).id));
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void setItems(List<Item> itemList) {
        this.items.clear();
        this.items.addAll(itemList);
    }

    public double orderValue() {
        double sum = 0;
        for (int i = 0; i < items.size(); i++)
            sum += items.get(i).getPriceWithTax();
        return sum;
    }

    @Override
    public String toString() {
        return MessageFormat.format("Order code: #{0}\n" +
                "Date: {1}\n" +
                "\n" +
                "Items:\n" +
                "{2}\n" +
                "\n" +
                "__________________\n" +
                "\n" +
                "Order Value: {3}", code, dateCreated, items, orderValue());

    }

    public Optional<Order> copy() {
        try (PipedOutputStream pipedOutputStream = new PipedOutputStream();
             PipedInputStream pipedInputStream = new PipedInputStream()) {
            pipedInputStream.connect(pipedOutputStream);
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(pipedOutputStream);
                 ObjectInputStream objectInputStream = new ObjectInputStream(pipedInputStream)) {
                objectOutputStream.writeObject(this);
                Order orderCopy = (Order) objectInputStream.readObject();
                objectOutputStream.close();
                objectInputStream.close();
                return Optional.of(orderCopy);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
