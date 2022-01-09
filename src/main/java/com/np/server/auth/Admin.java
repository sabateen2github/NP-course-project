package com.np.server.auth;

import java.io.Serializable;

public class Admin implements Serializable {
    public String userName;
    public String password;
    public Long adminId;

    private static final long serialVersionUID = 42L;

}
