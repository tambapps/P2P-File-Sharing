package com.tambapps.p2p.fandem.util;

import java.io.File;
import java.io.IOException;

public interface FileProvider {

  File newFile(String name) throws IOException;

}
