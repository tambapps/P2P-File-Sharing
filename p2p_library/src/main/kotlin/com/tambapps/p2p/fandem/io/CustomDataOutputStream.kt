package com.tambapps.p2p.fandem.io

import java.io.DataOutputStream
import java.io.OutputStream

class CustomDataOutputStream(stream: OutputStream) : DataOutputStream(stream) {

  fun writeString(s: String) {
    writeInt(s.length)
    writeChars(s)
  }

}