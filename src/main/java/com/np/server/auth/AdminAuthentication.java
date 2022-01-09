package com.np.server.auth;

import com.np.server.exceptions.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This class handles the signing up, signing in and persistence of admin details on disks.
 */
public class AdminAuthentication {


    private List<Admin> adminList;

    private static final String ADMIN_FILE_NAME = "admins.file";

    private static AdminAuthentication adminAuthentication;

    public static AdminAuthentication getInstance() throws IOException {
        if (adminAuthentication == null) adminAuthentication = new AdminAuthentication();
        return adminAuthentication;
    }


    private AdminAuthentication() throws IOException {

        File file = new File(ADMIN_FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
                adminList = new ArrayList<>();
                save();
                return;
            } catch (IOException e) {
                throw new CreateNewFileException(e);
            }
        }

        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file))) {
            adminList = (List<Admin>) objectInputStream.readObject();
        } catch (IOException e) {
            throw new ReadFileException(e);
        } catch (ClassNotFoundException e) {
            throw new ReadFileException(e);
        }

    }

    public long signUp(String username, String password, String privateKey) throws UsernameExistException, WrongPrivateKeyException, IOException {

        validateSecretKey(privateKey);
        validateUsername(username);

        long newAdminId = generateId();
        Admin admin = new Admin();
        admin.adminId = newAdminId;
        admin.password = password;
        admin.userName = username;
        adminList.add(admin);

        save();
        return newAdminId;
    }

    public long authenticate(String username, String password) throws WrongAuthDetailsException {

        Optional<Admin> admin = adminList.stream().filter((ad -> ad.userName.equals(username))).findAny();
        if (admin.isPresent()) {
            if (admin.get().password.equals(password)) {
                return admin.get().adminId;
            } else {
                throw new WrongAuthDetailsException(WrongAuthDetailsException.REASON_PASSWORD);
            }
        }
        throw new WrongAuthDetailsException(WrongAuthDetailsException.REASON_USERNAME);
    }


    private long generateId() {
        boolean unique = false;
        long id = -1;
        while (!unique) {
            id = UUID.randomUUID().getLeastSignificantBits();
            long finalId = id;
            unique = adminList.stream().allMatch(admin -> admin.adminId != finalId);
        }
        return id;
    }

    public void validateSecretKey(String privateKey) throws WrongPrivateKeyException {
        if ("123456789".equals(privateKey)) return;
        throw new WrongPrivateKeyException();
    }

    private void validateUsername(String username) throws UsernameExistException {

        Optional<Admin> admin = adminList.stream().filter(ad -> ad.userName.equals(username)).findAny();
        if (admin.isPresent()) {
            throw new UsernameExistException();
        }
    }

    private void save() throws IOException, IllegalStateException {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(ADMIN_FILE_NAME))) {
            objectOutputStream.writeObject(adminList);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("File must have already been created!");
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

}
