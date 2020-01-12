package com.tambapps.p2p.fandem.io

import java.io.DataInputStream
import java.io.InputStream

class CustomDataInputStream(stream: InputStream) : DataInputStream(stream) {

  fun readString(): String {
    val chars = CharArray(readInt())
    for (i in chars.indices) {
      chars[i] = readChar()
    }
    return String(chars)
  }

}