package com.tambapps.file_sharing;

import java.io.File;
import java.io.IOException;

public class TransferInterruptedException extends IOException {
    private final File file;

    public TransferInterruptedException(String message) {
        super(message);
        file = null;
    }

    public TransferInterruptedException(String message, File file) {
        super(message);
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
