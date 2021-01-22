package com.tambapps.p2p.fandem

import com.tambapps.p2p.fandem.exception.TransferCanceledException
import com.tambapps.p2p.fandem.task.ReceivingTask
import com.tambapps.p2p.fandem.task.SendingTask
import com.tambapps.p2p.fandem.util.FileUtils
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.UnknownHostException
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class FileTransferTest {
  companion object {
    private const val FILE_PATH = "./file.txt"
    private val FILE = File("./file.txt")
    private var ORIGIN_FILE: File = try {
      File(FileUtils.decodePath(FileTransferTest::class.java.classLoader
          .getResource(FILE_PATH)!!
          .file))
    } catch (e: UnsupportedEncodingException) {
      throw RuntimeException("Couldn't start tests")
    }

    private var SENDER_PEER: Peer = try {
      Peer.of(InetAddress.getLocalHost(), 8081)
    } catch (e: UnknownHostException) {
      throw RuntimeException("Couldn't start tests")
    }

    init {
      FILE.deleteOnExit()
    }
  }

  private var sender: SendingTask? = null
  private var receiver: ReceivingTask? = null
  private val completionService = ExecutorCompletionService<Boolean>(Executors.newFixedThreadPool(2))
  @Before
  @Throws(IOException::class)
  fun init() {
    sender = SendingTask(SENDER_PEER, 1000)
    receiver = ReceivingTask(FILE)
  }

  @Test
  @Throws(IOException::class, InterruptedException::class, ExecutionException::class)
  fun transferTest() {
    completionService.submit {
      sender!!.send(ORIGIN_FILE)
      true
    }
    Thread.sleep(250)
    completionService.submit {
      receiver!!.receiveFrom(SENDER_PEER)
      true
    }
    //check if tasks finished without throwing an exception
    for (i in 0..1) {
      val success = completionService.poll(2, TimeUnit.SECONDS).get()
      Assert.assertEquals("A task didn't ended well", java.lang.Boolean.TRUE, success)
    }
    val file = receiver!!.outputFile
    Assert.assertNotNull("Shouldn't be null", file)
    Assert.assertTrue("Didn't correctly downloaded file", file!!.exists())
    Assert.assertTrue("Content of received file differs from origin file",
        contentEquals(ORIGIN_FILE, file))
  }

  @Test
  @Throws(Exception::class)
  fun cancelReceiver() {
    completionService.submit {
      //fake file sender
      val serverSocket = ServerSocket(SENDER_PEER.port, 0, SENDER_PEER.ip)
      serverSocket.accept()
      true
    }
    Thread.sleep(250)
    completionService.submit {
      receiver!!.receiveFrom(SENDER_PEER.ip, SENDER_PEER.port)
      true
    }
    receiver!!.cancel()
    Thread.sleep(250)
    for (i in 0..1) {
      val success = completionService.poll(10, TimeUnit.SECONDS).get()
      Assert.assertEquals("A task didn't ended well", java.lang.Boolean.TRUE, success)
    }
    Assert.assertTrue("Should be true", receiver!!.isCanceled)
  }

  @Test
  @Throws(Exception::class)
  fun cancelSenderWhileWaiting() {
    val sendingFuture = completionService.submit {
      sender!!.send(ORIGIN_FILE)
      true
    }
    Thread.sleep(250)
    sender!!.cancel()
    try {
      sendingFuture.get()
      fail("A TransferCanceledException should have been thrown")
    }  catch (e: ExecutionException) {
      assertTrue(e.cause is TransferCanceledException)
    }
    assertTrue("Should be true", sender!!.isCanceled)
  }

  @Test
  @Throws(Exception::class)
  fun cancelSenderWhileSending() { //closing without
    val sendingFuture = completionService.submit {
      sender!!.send(ORIGIN_FILE)
      true
    }
    Thread.sleep(250)
    completionService.submit {
      val socket = Socket(SENDER_PEER.ip, SENDER_PEER.port)
      socket.close()
      // sleep to make this callable end after the sending callable
      Thread.sleep(4000)
      true
    }
    sender!!.cancel()

    try {
      // sender should have thrown TransferCanceledException in first runnable submitted to executor
      sendingFuture.get()
      fail("A TransferCanceledException should have been thrown")
    }  catch (e: ExecutionException) {
      assertTrue(e.cause is TransferCanceledException)
    }
    assertTrue("Should be true", sender!!.isCanceled)

  }

  @Throws(IOException::class)
  private fun contentEquals(f1: File, f2: File): Boolean {
    val is1: InputStream = FileInputStream(f1)
    val is2: InputStream = FileInputStream(f2)
    val EOF = -1
    var i1 = is1.read()
    while (i1 != EOF) {
      val i2 = is2.read()
      if (i2 != i1) {
        return false
      }
      i1 = is1.read()
    }
    return is2.read() == EOF
  }
}