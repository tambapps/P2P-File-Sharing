package com.tambapps.p2p.fandem.exception

import java.io.IOException

class TransferCanceledException(throwable: Throwable) : IOException(throwable)