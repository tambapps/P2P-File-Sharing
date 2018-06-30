package com.tambapps.p2p.file_sharing;

import java.util.Locale;

public interface TransferListener {
    void onConnected(String remoteAddress, int remotePort,String fileName, long fileSize);
    void onProgressUpdate(int progress, long byteProcessed, long totalBytes);

    static String bytesToString(long bytes) {
        String units = "kMG";
        long denominator = 1;
        int i = 0;

        while (bytes / (denominator * 1024) > 0 && i < units.length()) {
            denominator *= 1024;
            i++;
        }
        return String.format(Locale.US, "%.1f %sB", ((float)bytes)/((float)denominator),
                i == 0 ? "" : units.charAt(i - 1));
    }
}
