package com.np.server.auth;

/**
 * This class contains the credentials data of a user, they are saved on the server side and mapped by a session id that is sent to the client side.
 */
public class SessionServer {
    public int accountType;
    public long userId;
}
