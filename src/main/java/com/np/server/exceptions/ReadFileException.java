package com.np.server.exceptions;

import java.io.IOException;

public class ReadFileException extends IOException {
    public ReadFileException(Exception e) {
        super(e);
    }
}
