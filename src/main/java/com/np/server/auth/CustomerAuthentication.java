package com.np.server.auth;

import com.np.server.exceptions.CreateNewFileException;
import com.np.server.exceptions.ReadFileException;
import com.np.server.exceptions.UsernameExistException;
import com.np.server.exceptions.WrongAuthDetailsException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This class handles the signing up, signing in and persistence of customers details on disks.
 */
public class CustomerAuthentication {

    private List<Customer> customerList;
    private static final String CUSTOMERS_FILE_NAME = "customers.file";
    private static CustomerAuthentication customerAuthentication;

    public static CustomerAuthentication getInstance() throws IOException {
        if (customerAuthentication == null) customerAuthentication = new CustomerAuthentication();
        return customerAuthentication;
    }

    private CustomerAuthentication() throws IOException {
        File file = new File(CUSTOMERS_FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
                customerList = new ArrayList<>();
                save();
                return;
            } catch (IOException e) {
                throw new CreateNewFileException(e);
            }
        }

        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file))) {
            customerList = (List<Customer>) objectInputStream.readObject();
        } catch (IOException e) {
            throw new ReadFileException(e);
        } catch (ClassNotFoundException e) {
            throw new ReadFileException(e);
        }
    }

    public long signUp(String username, String password) throws UsernameExistException, IOException {

        validateUsername(username);

        long newUserId = generateId();
        Customer customer = new Customer();
        customer.customerId = newUserId;
        customer.password = password;
        customer.userName = username;
        customerList.add(customer);

        save();
        return newUserId;
    }

    public long authenticate(String username, String password) {
        Optional<Customer> customer = customerList.stream().filter((ad -> ad.userName.equals(username))).findAny();
        if (customer.isPresent()) {
            if (customer.get().password.equals(password)) {
                return customer.get().customerId;
            } else {
                throw new WrongAuthDetailsException(WrongAuthDetailsException.REASON_PASSWORD);
            }
        }
        throw new WrongAuthDetailsException(WrongAuthDetailsException.REASON_USERNAME);
    }

    private void validateUsername(String username) throws UsernameExistException {

        Optional<Customer> customer = customerList.stream().filter(ad -> ad.userName.equals(username)).findAny();
        if (customer.isPresent()) {
            throw new UsernameExistException();
        }
    }

    private void save() throws IOException, IllegalStateException {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(CUSTOMERS_FILE_NAME))) {
            objectOutputStream.writeObject(customerList);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("File must have already been created!");
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    private long generateId() {
        boolean unique = false;
        long id = -1;
        while (!unique) {
            id = UUID.randomUUID().getLeastSignificantBits();
            long finalId = id;
            unique = customerList.stream().allMatch(customer -> customer.customerId != finalId);
        }
        return id;
    }
}
