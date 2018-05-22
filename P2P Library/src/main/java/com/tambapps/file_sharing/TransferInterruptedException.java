package com.tambapps.file_sharing;

import java.io.IOException;

public class TransferInterruptedException extends IOException {

    public TransferInterruptedException(String message) {
        super(message);
    }
}
