package com.tambapps.p2p.peer_transfer.android

import android.Manifest.permission.POST_NOTIFICATIONS
import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.toArgb
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.tambapps.p2p.peer_transfer.android.databinding.ActivityOnBoardingBinding
import com.tambapps.p2p.peer_transfer.android.ui.theme.BlueOcean
import com.tambapps.p2p.peer_transfer.android.ui.theme.Cyan
import com.tambapps.p2p.peer_transfer.android.util.hasPermission
import java.util.function.Consumer

class OnBoardingActivity : ComponentActivity() {

  private lateinit var binding: ActivityOnBoardingBinding
  private val pages = listOf(
    PageData(R.string.welcome_to_fandem, R.string.welcome_des, R.drawable.appicon),
    PageData(R.string.p2p, R.string.p2p_des, R.drawable.transfer),
    PageData(R.string.same_wifi, R.string.same_wifi_des, R.drawable.wifi),
    PageData(R.string.hotspot, R.string.hotspot_des, R.drawable.hotspot),
    PageData(R.string.transfer_followup, R.string.transfer_followup_des, R.drawable.notification),
    PageData(R.string.lets_get_started, R.string.lets_get_started_des, R.drawable.appicon),
  )
  private val needsNotificationPermission
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission(permission = POST_NOTIFICATIONS)
  private val notificationPageIndex = pages.indexOfFirst { it.imageRes == R.drawable.notification }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityOnBoardingBinding.inflate(layoutInflater)
    setContentView(binding.root)
    binding.apply {
      val pushNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (viewPager.currentItem < viewPager.adapter!!.itemCount - 1) {
          viewPager.currentItem = viewPager.currentItem + 1
        }
      }
      viewPager.adapter = OnBoardingAdapter()
      indicator.setViewPager(viewPager)
      nextButton.setOnClickListener {
        if (viewPager.currentItem == notificationPageIndex && needsNotificationPermission) {
          pushNotificationPermissionLauncher.launch(POST_NOTIFICATIONS)
        } else if (viewPager.currentItem < viewPager.adapter!!.itemCount - 1) {
          viewPager.currentItem = viewPager.currentItem + 1
        } else {
          finish()
        }
      }

      val root: View = findViewById(R.id.root)

      viewPager.registerOnPageChangeCallback(OnPageChangeListener(nextButton, pages.size) { color ->
        window.navigationBarColor = color
        root.setBackgroundColor(color)
      })
    }
  }

  private inner class OnBoardingAdapter : RecyclerView.Adapter<OnBoardingViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnBoardingViewHolder {
      val view =
        LayoutInflater.from(parent.context).inflate(R.layout.onboarding_page, parent, false)
      return OnBoardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnBoardingViewHolder, position: Int) {
      val data = pages[position]
      holder.imageView.setImageResource(data.imageRes)
      holder.titleView.text = getString(data.titleRes)
      holder.descriptionView.text = getString(data.messageRes)
    }

    override fun getItemCount() = pages.size
  }


  private class OnBoardingViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView
    val titleView: TextView
    val descriptionView: TextView

    init {
      imageView = itemView.findViewById(R.id.imageview)
      titleView = itemView.findViewById(R.id.title)
      descriptionView = itemView.findViewById(R.id.description)
    }
  }

  @SuppressLint("MissingSuperCall")
  override fun onBackPressed() {
    // prevent user from quiting this activity
  }

  private inner class OnPageChangeListener constructor(
    private val button: Button,
    itemCount: Int,
    private val backgroundColorUpdater: Consumer<Int>
  ) :
    ViewPager2.OnPageChangeCallback() {
    private val argbEvaluator: ArgbEvaluator = ArgbEvaluator()
    private val transitionCount: Int
    private val startColor: Int
    private val endColor: Int

    init {
      transitionCount = itemCount - 1 // YES, this is normal. THINK!
      startColor = BlueOcean.toArgb()

      endColor = Cyan.toArgb()
    }

    override fun onPageSelected(position: Int) {
      if (position == pages.size - 1) {
        button.setText(R.string.onboarding_end)
      } else {
        button.setText(R.string.next)
      }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
      backgroundColorUpdater.accept(
        argbEvaluator.evaluate(
          (positionOffset + toFloat(position)) / toFloat(transitionCount),
          startColor, endColor
        ) as Int
      )
    }

    private fun toFloat(i: Int): Float {
      return i.toFloat()
    }
  }
}


data class PageData(val titleRes: Int, val messageRes: Int, val imageRes: Int)
