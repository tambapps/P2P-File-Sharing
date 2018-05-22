package com.tambapps.p2p.file_sharing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtils {

    private IOUtils() {}

    public static boolean contentEquals(File f1, File f2) throws IOException {
        InputStream is1 = new FileInputStream(f1);
        InputStream is2 = new FileInputStream(f2);

        final int EOF = -1;
        int i1 = is1.read();
        while (i1 != EOF) {
            int i2 = is2.read();
            if (i2 != i1) {
                return false;
            }
            i1 = is1.read();
        }

        return is2.read() == EOF;
    }
}
