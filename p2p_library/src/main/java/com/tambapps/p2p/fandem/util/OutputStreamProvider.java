package com.tambapps.p2p.fandem.util;

import java.io.IOException;
import java.io.OutputStream;

public interface OutputStreamProvider {

  OutputStream newOutputStream(String fileName) throws IOException;

}
