package com.np.common;

import java.io.Serializable;

/**
 * This class used to communicate the session details of the user (admin or customer).
 */
public class Session implements Serializable {
    private static final long serialVersionUID = 42L;

    public static final int TYPES_CUSTOMER = 1;
    public static final int TYPES_ADMIN = 2;

    public String sessionID;
    public int accountType;
}
