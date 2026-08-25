package `fun`.kirari.hanako.feature.history.ui

import android.graphics.Rect
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.lazy.rememberLazyListState
import `fun`.kirari.hanako.core.model.ProcessingResult
import `fun`.kirari.hanako.core.model.QuotedFragment
import `fun`.kirari.hanako.core.model.decodeHistoryBitmap
import `fun`.kirari.hanako.core.model.displayedAnswerVersions
import `fun`.kirari.hanako.core.model.latestAnswerText
import `fun`.kirari.hanako.core.model.loadHistoryBitmap
import `fun`.kirari.hanako.core.ui.components.AnswerSwitchDirection
import `fun`.kirari.hanako.core.ui.richtext.MarkdownLatexText
import `fun`.kirari.hanako.core.ui.image.ImagePreviewOverlay
import `fun`.kirari.hanako.feature.home.presentation.RegisterScrollToTopHandler
import kotlinx.coroutines.launch

@Composable
fun HistoryDetailScreen(
    scrollRoute: String,
    result: ProcessingResult?,
    regenerating: Boolean = false,
    chatSending: Boolean = false,
    runningAnswerVersionIndex: Int? = null,
    conversationModelLabel: String = "选择模型",
    onRegenerate: ((ProcessingResult) -> Unit)? = null,
    onSelectConversationModel: (() -> Unit)? = null,
    onSendFollowUp: ((String, List<QuotedFragment>) -> Unit)? = null,
    onRetryFollowUp: (() -> Unit)? = null
) {
    if (result == null) {
        MissingHistoryDetail()
        return
    }

    val answerVersions = remember(result.id, result.answerVersions, result.answer) {
        result.displayedAnswerVersions()
    }
    val screenshots = remember(result.allScreenshotPaths, result.screenshotBase64) {
        result.allScreenshotPaths.mapNotNull { it.loadHistoryBitmap() }.toMutableList().apply {
            if (isEmpty()) result.screenshotBase64?.decodeHistoryBitmap()?.let(::add)
        }
    }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var currentVersionIndex by remember(result.id, answerVersions.size) {
        mutableStateOf((answerVersions.size - 1).coerceAtLeast(0))
    }
    var switchDirection by remember(result.id) { mutableStateOf(AnswerSwitchDirection.NONE) }
    var followUpDraft by remember(result.id) { mutableStateOf("") }
    var confirmOriginalRegeneration by remember { mutableStateOf(false) }
    var rawTextForSelection by remember(result.id) { mutableStateOf<String?>(null) }
    var previewImageIndex by remember { mutableStateOf(-1) }
    var imageBounds by remember { mutableStateOf<Rect?>(null) }
    var draftQuotes by remember(result.id) { mutableStateOf(emptyList<QuotedFragment>()) }
    var focusedBlock by remember(result.id) { mutableStateOf<HistoryRenderedBlock?>(null) }
    var blockRects by remember(result.id) { mutableStateOf<Map<String, HistoryRenderedBlock>>(emptyMap()) }
    var menuBlock by remember(result.id) { mutableStateOf<HistoryRenderedBlock?>(null) }
    var viewerQuote by remember(result.id) { mutableStateOf<QuotedFragment?>(null) }

    LaunchedEffect(regenerating, runningAnswerVersionIndex, answerVersions.size) {
        if (regenerating && answerVersions.isNotEmpty()) {
            currentVersionIndex = runningAnswerVersionIndex
                ?.coerceIn(0, answerVersions.lastIndex)
                ?: answerVersions.lastIndex
        }
    }
    RegisterScrollToTopHandler(route = scrollRoute) {
        coroutineScope.launch { listState.animateScrollToItem(0) }
    }

    val displayedAnswer = answerVersions.getOrNull(currentVersionIndex)?.text ?: result.latestAnswerText()
    val density = LocalDensity.current
    val bottomInset = with(density) {
        WindowInsets.navigationBars.union(WindowInsets.ime).getBottom(this).toDp()
    }
    val requestOriginalRegeneration = onRegenerate?.let { regenerate ->
        {
            if (result.followUpTurns.isEmpty()) regenerate(result)
            else confirmOriginalRegeneration = true
        }
    }

    HistoryDetailContent(
        result = result,
        screenshots = screenshots,
        answerVersions = answerVersions,
        displayedAnswer = displayedAnswer,
        currentVersionIndex = currentVersionIndex,
        switchDirection = switchDirection,
        regenerating = regenerating,
        chatSending = chatSending,
        bottomInset = bottomInset,
        listState = listState,
        followUpDraft = followUpDraft,
        conversationModelLabel = conversationModelLabel,
        onPreviousAnswerVersion = {
            if (currentVersionIndex > 0) {
                switchDirection = AnswerSwitchDirection.PREVIOUS
                currentVersionIndex -= 1
            }
        },
        onNextAnswerVersion = {
            if (currentVersionIndex < answerVersions.lastIndex) {
                switchDirection = AnswerSwitchDirection.NEXT
                currentVersionIndex += 1
            }
        },
        onRegenerate = requestOriginalRegeneration,
        onSelectRawText = { rawTextForSelection = it },
        onSingleImagePositioned = { imageBounds = it },
        onPreviewImage = { previewImageIndex = it },
        onSelectConversationModel = onSelectConversationModel,
        onFollowUpDraftChange = { followUpDraft = it },
        draftQuotes = draftQuotes,
        highlightedBlockId = focusedBlock?.anchor?.blockId,
        underlinedBlockIds = result.followUpTurns.flatMap { it.quotedFragments }.map { it.anchor.blockId }.toSet(),
        onBlockFocused = {
            val positioned = blockRects[it.anchor.blockId] ?: it
            focusedBlock = positioned
            menuBlock = positioned
        },
        onBlockPositioned = { positioned -> blockRects = blockRects + (positioned.anchor.blockId to positioned) },
        onRemoveDraftQuote = { id -> draftQuotes = draftQuotes.filterNot { it.id == id } },
        onQuoteClick = { quote ->
            val positioned = blockRects[quote.anchor.blockId] ?: HistoryRenderedBlock(quote.anchor, Rect())
            focusedBlock = positioned
            menuBlock = positioned
        },
        onSendFollowUp = onSendFollowUp?.let { send ->
            {
                val prompt = followUpDraft.trim()
                if (prompt.isNotBlank()) {
                    followUpDraft = ""
                    val quotes = draftQuotes
                    draftQuotes = emptyList()
                    send(prompt, quotes)
                }
            }
        },
        onRetryFollowUp = onRetryFollowUp
    )

    menuBlock?.let { block ->
        val density = LocalDensity.current
        val configuration = LocalConfiguration.current
        val menuWidthPx = with(density) { 180.dp.roundToPx() }
        val menuHeightPx = with(density) { 104.dp.roundToPx() }
        val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }
        val screenHeightPx = with(density) { configuration.screenHeightDp.dp.roundToPx() }
        val target = block.anchor
        val quoteCount = result.followUpTurns.sumOf { turn ->
            turn.quotedFragments.count { it.anchor.blockId == target.blockId }
        }
        Popup(
            alignment = androidx.compose.ui.Alignment.TopStart,
            offset = IntOffset(
                block.rect.left.coerceIn(8, (screenWidthPx - menuWidthPx - 8).coerceAtLeast(8)),
                (block.rect.bottom + 6).coerceIn(8, (screenHeightPx - menuHeightPx - 8).coerceAtLeast(8))
            ),
            onDismissRequest = { menuBlock = null },
            properties = PopupProperties(focusable = true)
        ) {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                shape = androidx.compose.material3.MaterialTheme.shapes.medium
            ) {
                Column {
                    TextButton(onClick = {
                        block.toQuotedFragment().let { quote ->
                            if (draftQuotes.none { it.anchor.blockId == quote.anchor.blockId }) {
                                draftQuotes = draftQuotes + quote
                            }
                        }
                        menuBlock = null
                    }) { Text("引用") }
                    if (quoteCount > 0) {
                        TextButton(onClick = {
                            viewerQuote = QuotedFragment(anchor = target)
                            menuBlock = null
                        }) { Text("查看引用（$quoteCount）") }
                    }
                }
            }
        }
    }

    if (confirmOriginalRegeneration) {
        AlertDialog(
            onDismissRequest = { confirmOriginalRegeneration = false },
            title = { Text("重新生成首轮回答？") },
            text = { Text("这会删除全部后续对话，然后使用当前模型配置重新生成首轮答案。") },
            confirmButton = {
                TextButton(onClick = {
                    confirmOriginalRegeneration = false
                    onRegenerate?.invoke(result)
                }) { Text("删除并重试") }
            },
            dismissButton = {
                TextButton(onClick = { confirmOriginalRegeneration = false }) { Text("取消") }
            }
        )
    }
    rawTextForSelection?.let {
        RawTextSelectionSheet(rawText = it, onDismiss = { rawTextForSelection = null })
    }
    if (previewImageIndex in screenshots.indices) {
        ImagePreviewOverlay(
            visible = true,
            bitmap = screenshots[previewImageIndex],
            fileName = "hanako_history_${result.id}_$previewImageIndex",
            onDismiss = { previewImageIndex = -1 },
            sourceBounds = imageBounds
        )
    }
    viewerQuote?.let { quote ->
        AlertDialog(
            onDismissRequest = { viewerQuote = null },
            confirmButton = {
                TextButton(onClick = { viewerQuote = null }) { Text("关闭") }
            },
            title = { Text("引用内容") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("问题", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    MarkdownLatexText(
                        result.extractedText.ifBlank { "图片题目" },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text("回答", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 14.dp))
                    MarkdownLatexText(
                        displayedAnswer.ifBlank { "暂无回答" },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text("引用片段", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 14.dp))
                    MarkdownLatexText(quote.anchor.rawMarkdown, modifier = Modifier.padding(top = 8.dp))
                }
            }
        )
    }
}
