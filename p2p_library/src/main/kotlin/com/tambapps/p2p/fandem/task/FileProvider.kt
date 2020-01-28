package com.tambapps.p2p.fandem.task

import java.io.File
import java.io.IOException

@FunctionalInterface
interface FileProvider {

    @Throws(IOException::class) fun newFile(name: String): File

}