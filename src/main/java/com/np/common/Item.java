package com.np.common;

import javax.swing.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Optional;

public class Item implements java.io.Serializable {
    public String name;
    public int id;
    private ArrayList<String> reviews = new ArrayList();
    private double priceWithoutTax;

    public boolean isUpdating() {
        return isUpdating;
    }

    public void setUpdating(boolean updating) {
        isUpdating = updating;
    }

    private transient boolean isUpdating;
    private static double salesTax = 0.16;

    public Item() {
        name = JOptionPane.showInputDialog("Please enter item name: ");
        priceWithoutTax = Double.parseDouble(JOptionPane.showInputDialog("Please enter item price: "));
        while (priceWithoutTax <= 0) {
            priceWithoutTax = Double.parseDouble(JOptionPane.showInputDialog("Please enter positive non-zero value for the price: "));
        }
        id = Integer.parseInt(JOptionPane.showInputDialog("Please enter item ID: "));

    }

    public Item(String name, double priceWithoutTax, int id) {
        this.name = name;
        this.priceWithoutTax = priceWithoutTax;
        this.id = id;
    }

    public void addReview() {
        reviews.add(JOptionPane.showInputDialog("Please enter your review for " + name));
    }

    public void addReview(String review) {
        reviews.add(review);
    }

    public ArrayList<String> getReviews() {
        return reviews;
    }

    public double getPriceWithoutTax() {
        return priceWithoutTax;
    }

    public void setPriceWithoutTax(double priceWithoutTax) {
        if (priceWithoutTax > 0) this.priceWithoutTax = priceWithoutTax;
    }

    public static double getSalesTax() {
        return salesTax;
    }

    public static void setSalesTax(double tax) {
        salesTax = tax;
    }

    public double getPriceWithTax() {
        return priceWithoutTax + priceWithoutTax * salesTax;
    }

    public void printReviews() {
        String s = "";
        for (int i = 0; i < reviews.size(); i++)
            s += reviews.get(i) + "\n";
        JOptionPane.showMessageDialog(null, s);
    }

    @Override
    public String toString() {
        return id + "-" + name + "-" + getPriceWithTax();
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Item)) return false;
        return id == ((Item) obj).id;
    }

    public Optional<Item> copy() {
        try (PipedOutputStream pipedOutputStream = new PipedOutputStream();
             PipedInputStream pipedInputStream = new PipedInputStream()) {
            pipedInputStream.connect(pipedOutputStream);
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(pipedOutputStream);
                 ObjectInputStream objectInputStream = new ObjectInputStream(pipedInputStream)) {
                objectOutputStream.writeObject(this);
                Item orderCopy = (Item) objectInputStream.readObject();
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
