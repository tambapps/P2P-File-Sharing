package tambapps.com.a2sfile_sharing.io;

import android.support.v4.content.FileProvider;

/**
 * Used to provide uri of downloaded files without
 * throwing a FileUriExposedException for sdk >= 24
 */
public class GenericFileProvider extends FileProvider {}
