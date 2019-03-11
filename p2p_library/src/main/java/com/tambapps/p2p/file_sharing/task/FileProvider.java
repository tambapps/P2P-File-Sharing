package com.tambapps.p2p.file_sharing.task;

import java.io.File;
import java.io.IOException;

public interface FileProvider {

  File newFile(String name) throws IOException;

}
