package com.tambapps.p2p.peer_transfer.android.service

import android.net.wifi.WifiManager
import android.util.Log
import com.tambapps.p2p.fandem.Fandem
import com.tambapps.p2p.fandem.SenderPeer
import com.tambapps.p2p.peer_transfer.android.util.NetworkUtils
import com.tambapps.p2p.speer.datagram.MulticastDatagramPeer
import com.tambapps.p2p.speer.datagram.service.MulticastReceiverService
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// not using hilt/dagger for this one because it needs a listener which is the activity
/**
 * Class that uses a MulticastLock because it is recommended
 */
class AndroidSenderPeersReceiverService private constructor(
  private val wifiManager: WifiManager,
  private val executorService: ExecutorService,
  listener: DiscoveryListener<List<SenderPeer>>
) : MulticastReceiverService<List<SenderPeer>>(
  executorService,
  Fandem.PEER_DISCOVERY_MULTICAST_ADDRESS,
  Fandem.PEER_DISCOVERY_PORT,
  Fandem.senderPeersDeserializer(),
  listener
) {

  constructor(wifiManager: WifiManager, listener: DiscoveryListener<List<SenderPeer>>): this(wifiManager, Executors.newSingleThreadExecutor(), listener)

  @Throws(IOException::class)
  override fun start(datagramPeer: MulticastDatagramPeer) {
    // this is needed for multicast to work on hotspot (AKA mobile data sharing)
    val wifiNetworkInterface = NetworkUtils.findWifiNetworkInterface()
    if (wifiNetworkInterface != null) {
      datagramPeer.socket.networkInterface = wifiNetworkInterface
    }
    super.start(datagramPeer)
  }

  override fun listen(datagramPeer: MulticastDatagramPeer) {
    val lock = wifiManager.createMulticastLock(LOCK_TAG)
    try {
      lock.acquire()
      lock.setReferenceCounted(true)
      super.listen(datagramPeer)
      lock.release()
    } catch (e: Exception) {
      Log.e("AndroidSenderPeersReceiverService", "Multicast receive error", e)
    }
  }

  fun stopWithExecutor() {
    if (isRunning) stop()
    executorService.shutdown()
  }

  companion object {
    private const val LOCK_TAG = "SenderPeersReceiverService"
  }
}