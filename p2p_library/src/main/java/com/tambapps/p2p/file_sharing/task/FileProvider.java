package com.tambapps.p2p.file_sharing.task;

import java.io.File;
import java.io.IOException;

/**
 * Interface allowing to get a file given a provided name
 */
public interface FileProvider {

  File newFile(String name) throws IOException;

}
