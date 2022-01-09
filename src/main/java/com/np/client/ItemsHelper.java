package com.np.client;

import com.np.common.Item;
import com.np.common.opcodes.OpCodes;
import com.np.common.responsecodes.ResponseCodes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import static com.np.client.TCPClient.SERVICE_HOST;
import static com.np.client.TCPClient.SERVICE_PORT;

public class ItemsHelper {
    /**
     * This method communicates with the server to get all the items available on the system that are not being modified at the moment.
     *
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static List<Item> getAllItems() throws IOException, ClassNotFoundException {

        try (Socket server = new Socket(SERVICE_HOST, SERVICE_PORT)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(server.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(server.getInputStream());

            objectOutputStream.writeInt(OpCodes.OP_GET_ITEMS);
            objectOutputStream.flush();

            int response = objectInputStream.readInt();
            if (response == ResponseCodes.RESPONSE_GET_ITEMS_SUCCESSFULLY) {
                List<Item> itemList = (List<Item>) objectInputStream.readObject();
                return itemList;
            }
            throw new RuntimeException("Failed to get items from server");
        } catch (UnknownHostException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (ClassNotFoundException e) {
            throw e;
        }

    }
}
