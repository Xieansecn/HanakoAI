package `fun`.kirari.hanako.feature.history.ui

import android.graphics.Rect
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import `fun`.kirari.hanako.core.model.ContentAnchor
import `fun`.kirari.hanako.core.model.RichTextBlockKind
import `fun`.kirari.hanako.core.model.QuotedFragment
import `fun`.kirari.hanako.core.ui.richtext.MarkdownLatexText
import `fun`.kirari.hanako.core.ui.richtext.RichTextBlock
import `fun`.kirari.hanako.core.ui.richtext.extractRichTextBlocks
import java.nio.charset.StandardCharsets
import java.util.UUID

internal data class HistoryRenderedBlock(
    val anchor: ContentAnchor,
    val rect: Rect
)

internal fun HistoryRenderedBlock.toQuotedFragment(): QuotedFragment = QuotedFragment(anchor = anchor)

internal fun quotePreview(anchor: ContentAnchor): String {
    if (anchor.blockKind == RichTextBlockKind.DISPLAY_MATH) return "[公式]"
    val codePoints = anchor.rawMarkdown
        .replace(Regex("[`*_#~]"), "")
        .replace(Regex("\\s+"), " ")
        .trim()
        .codePoints()
        .limit(3)
        .toArray()
    return buildString { codePoints.forEach(::appendCodePoint) }.ifBlank { "文本" }
}

@Composable
internal fun HistoryInteractiveMarkdown(
    content: String,
    historyId: String,
    messageId: String,
    answerVersionId: String?,
    sourceRevision: Long,
    highlightedBlockId: String?,
    underlinedBlockIds: Set<String>,
    onBlockFocused: (HistoryRenderedBlock) -> Unit,
    onBlockPositioned: (HistoryRenderedBlock) -> Unit,
    modifier: Modifier = Modifier
) {
    val blocks = remember(content) { extractRichTextBlocks(content) }
    Column(modifier = modifier, verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)) {
        blocks.forEach { block ->
            val anchor = remember(historyId, messageId, answerVersionId, sourceRevision, block) {
                val kind = block.kind
                val blockId = UUID.nameUUIDFromBytes(
                    "hanako-block:$historyId:$messageId:${answerVersionId.orEmpty()}:$sourceRevision:${block.ordinal}:${block.rawMarkdown}"
                        .toByteArray(StandardCharsets.UTF_8)
                ).toString()
                ContentAnchor(
                    historyId = historyId,
                    messageId = messageId,
                    answerVersionId = answerVersionId,
                    blockId = blockId,
                    blockKind = kind,
                    sourceRevision = sourceRevision,
                    rawMarkdown = block.rawMarkdown,
                    previewLabel = quotePreview(
                        ContentAnchor(
                            historyId = historyId,
                            messageId = messageId,
                            answerVersionId = answerVersionId,
                            blockId = blockId,
                            blockKind = kind,
                            sourceRevision = sourceRevision,
                            rawMarkdown = block.rawMarkdown,
                            previewLabel = ""
                        )
                    )
                )
            }
            var currentRect by remember(anchor) { mutableStateOf(Rect()) }
            val focused = highlightedBlockId == anchor.blockId
            val underlined = anchor.blockId in underlinedBlockIds
            val primaryColor = MaterialTheme.colorScheme.primary
            val blockModifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .padding(vertical = if (block.kind == RichTextBlockKind.DISPLAY_MATH) 4.dp else 1.dp)
                .then(
                    if (underlined) Modifier.drawBehind {
                        drawLine(
                            color = primaryColor.copy(alpha = 0.45f),
                            start = Offset(0f, size.height - 2.dp.toPx()),
                            end = Offset(size.width, size.height - 2.dp.toPx()),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                        )
                    } else Modifier
                )
                .then(
                    if (focused) Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(primaryColor.copy(alpha = 0.12f))
                    else Modifier
                )
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInRoot()
                    val size = coordinates.size
                    val positioned = HistoryRenderedBlock(
                        anchor,
                        Rect(
                            position.x.toInt(), position.y.toInt(),
                            (position.x + size.width).toInt(), (position.y + size.height).toInt()
                        )
                    )
                    currentRect = positioned.rect
                    onBlockPositioned(positioned)
                }
                .combinedClickable(
                    onClick = {
                        if (underlined) onBlockFocused(HistoryRenderedBlock(anchor, currentRect))
                    },
                    onLongClick = {
                        onBlockFocused(HistoryRenderedBlock(anchor, currentRect))
                    }
                )
            Box(modifier = blockModifier) {
                if (block.kind == RichTextBlockKind.DISPLAY_MATH) {
                    MarkdownLatexText(block.rawMarkdown, modifier = Modifier.fillMaxWidth())
                } else {
                    MarkdownLatexText(block.rawMarkdown, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
