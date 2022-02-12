package com.tambapps.p2p.fandem.util;

import java.io.IOException;

public interface IoSupplier<T> {

  T get() throws IOException;

}
