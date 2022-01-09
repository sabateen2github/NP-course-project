package com.np.server.auth;

import java.io.Serializable;

public class Customer implements Serializable {
    public String userName;
    public String password;
    public Long customerId;

    private static final long serialVersionUID = 42L;

}
