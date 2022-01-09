package com.np.server.exceptions;

import java.io.IOException;

public class CreateNewFileException extends IOException {
    public CreateNewFileException(IOException e) {
        super(e);
    }
}
