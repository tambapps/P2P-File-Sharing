package com.tambapps.p2p.peer_transfer.android.io;

import android.support.v4.content.FileProvider;

/**
 * Used to provide uri of downloaded files without
 * throwing a FileUriExposedException for sdk >= 24
 */
public class GenericFileProvider extends FileProvider {}
