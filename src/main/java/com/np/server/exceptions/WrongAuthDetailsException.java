package com.np.server.exceptions;

public class WrongAuthDetailsException extends RuntimeException {

    public static final int REASON_USERNAME = 1;
    public static final int REASON_PASSWORD = 2;

    public final int reason;

    public WrongAuthDetailsException(int reason) {

        this.reason = reason;
    }

}
