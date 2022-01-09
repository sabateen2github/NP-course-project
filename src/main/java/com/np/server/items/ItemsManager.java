package com.np.server.items;

import com.np.common.Item;
import com.np.server.exceptions.CreateNewFileException;
import com.np.server.exceptions.ItemAlreadyExistException;
import com.np.server.exceptions.ItemNotFoundException;
import com.np.server.exceptions.ReadFileException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This class handles the addition, retrieval, removal, modification, reviewing and persisting items details on disks.
 */
public class ItemsManager {


    private static ItemsManager instance;

    public static ItemsManager getInstance() throws CreateNewFileException, ReadFileException {
        if (instance == null) instance = new ItemsManager();
        return instance;
    }

    private static final String ITEMS_FILE_NAME = "items.out";
    private List<Item> itemList;

    private ItemsManager() throws CreateNewFileException, ReadFileException {
        File file = new File(ITEMS_FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
                itemList = new ArrayList<>();
                save();
                return;
            } catch (IOException e) {
                throw new CreateNewFileException(e);
            }
        }

        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file))) {
            itemList = (List<Item>) objectInputStream.readObject();
        } catch (IOException e) {
            throw new ReadFileException(e);
        } catch (ClassNotFoundException e) {
            throw new ReadFileException(e);
        }
    }

    public Stream<Item> getAllItems() {
        return itemList.stream().filter(it -> !it.isUpdating());
    }

    public synchronized void addItem(Item item) throws ItemAlreadyExistException, IOException {
        Optional<Item> conflictedItem = itemList.stream().filter(it -> it.id == item.id).findAny();
        if (conflictedItem.isPresent()) throw new ItemAlreadyExistException();
        item.setUpdating(true);
        itemList.add(item);
        save();
        item.setUpdating(false);
    }

    public synchronized void removeItem(int id) throws IOException {
        Optional<Item> item = itemList.stream().filter(it -> it.id == id).findAny();
        if (item.isEmpty()) throw new ItemNotFoundException();
        item.get().setUpdating(true);
        itemList.remove(item.get());
        save();
    }

    public void modifyItem(int oldId, Item newItem) throws ItemNotFoundException, IOException {
        Optional<Item> item = itemList.stream().filter(it -> it.id == oldId).findAny();
        if (item.isEmpty()) throw new ItemNotFoundException();
        synchronized (item.get()) {
            item.get().setUpdating(true);
            itemList.set(itemList.indexOf(item.get()), newItem);
            save();
        }

    }


    private void save() throws IOException, IllegalStateException {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(ITEMS_FILE_NAME))) {
            objectOutputStream.writeObject(itemList);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("File must have already been created!");
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

}
