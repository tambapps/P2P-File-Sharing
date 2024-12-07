@file:OptIn(ExperimentalFoundationApi::class)

package com.tambapps.p2p.peer_transfer.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tambapps.p2p.peer_transfer.android.ui.theme.FandemAndroidTheme
import com.tambapps.p2p.peer_transfer.android.ui.theme.FandemSurface
import com.tambapps.p2p.peer_transfer.android.ui.theme.TextColor
import kotlinx.coroutines.launch

class HelpActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      FandemAndroidTheme {
        val tabData = listOf(HelpPageData(R.string.app_name, R.string.help_description), HelpPageData(R.string.tab_text_2, R.string.help_description), HelpPageData(R.string.tab_text_3, R.string.receive_description))
        FandemSurface {
          Column(modifier = Modifier.fillMaxSize()) {
            val pagerState = rememberPagerState(pageCount = tabData::size)
            HelpTab(pagerState, tabData)
            HelpPager(pagerState = pagerState, tabData = tabData)
          }
        }
      }
    }
  }
}

data class HelpPageData(val titleRes: Int, val messageRes: Int)
@Composable
fun HelpTab(pagerState: PagerState, tabData: List<HelpPageData>) {
  TabRow(
    selectedTabIndex = pagerState.currentPage,
    divider = {
      Spacer(modifier =Modifier.height(5.dp))
    },
    containerColor = Color.Transparent,
    indicator = { tabPositions ->
      TabRowDefaults.Indicator(
        modifier =
        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
        height = 5.dp,
        color = Color.White
      )
    },
    modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight()
  ) {
    val scope = rememberCoroutineScope()
    tabData.forEachIndexed { index, data ->
      Tab(selected = pagerState.currentPage == index,
        onClick = {
          scope.launch {
            pagerState.animateScrollToPage(index)
          }
        },
        text = {
          Text(text = stringResource(id = data.titleRes), color = TextColor)
        })
    }
  }
}

@Composable
fun HelpPager(pagerState: PagerState, tabData: List<HelpPageData>) {
  HorizontalPager(state = pagerState, verticalAlignment = Alignment.Top) { index ->
    val state = rememberScrollState()
    // we need 2 columns here. One is to fit the whole screen, the other is to enable scrolling in case of everfitting
    // we can't have both on same column
    Column(modifier = Modifier.fillMaxSize()
      , horizontalAlignment = Alignment.CenterHorizontally) {
      Column(modifier = Modifier.verticalScroll(state).padding(horizontal = 8.dp, vertical = 16.dp)) {
        if (index == 0) {
          HelpPage()
        } else {
          PageText(text = AnnotatedString(stringResource(id = tabData[index].messageRes)))
        }
      }
    }
  }
}

@LayoutScopeMarker
@Preview(showBackground = true)
@Composable
fun ColumnScope.HelpPage() {
  val context = LocalContext.current
  val fandemLinkTag = "Fandem Github Link"
  val annotatedString = buildAnnotatedString {
    append(context.getString(R.string.help_description))
    append(" ")

    pushStringAnnotation(tag = fandemLinkTag, annotation = "https://github.com/tambapps/P2P-File-Sharing")
    withStyle(style = SpanStyle(color = Color.Cyan, textDecoration = TextDecoration.Underline)) {
      append(context.getString(R.string.here))
    }
    pop()
  }
  val uriHandler = LocalUriHandler.current
  Text(text = stringResource(id = R.string.welcome_to_fandem), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextColor, modifier = Modifier.align(CenterHorizontally))
  Spacer(modifier = Modifier.size(width = 1.dp, height = 16.dp))
  PageText(text = annotatedString, onClick = { offset ->
    annotatedString.getStringAnnotations(tag = fandemLinkTag, start = offset, end = offset).firstOrNull()?.let {
      uriHandler.openUri(it.item)
    }
  })
  Spacer(modifier = Modifier.size(width = 1.dp, height = 32.dp))
  /*
  Button(onClick = { context.startActivity(Intent(context, OnBoardingActivity::class.java)) }, modifier = Modifier.align(CenterHorizontally)) {
    Text(text = stringResource(id = R.string.rewatch_intro).uppercase(), color = TextColor)
  }
   */
}

@Composable
fun PageText(text: AnnotatedString, onClick: (Int) -> Unit = {}) {
  ClickableText(text = text, onClick = onClick, style = TextStyle.Default.copy(fontSize = 16.sp, color = TextColor))
}
