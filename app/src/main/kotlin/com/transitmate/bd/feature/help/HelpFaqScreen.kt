package com.transitmate.bd.feature.help

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.EmptyStateView
import com.transitmate.bd.core.designsystem.component.ResponsiveScreenContainer
import com.transitmate.bd.core.designsystem.component.SecondaryTonalButton
import com.transitmate.bd.core.designsystem.component.SurfaceSectionCard
import com.transitmate.bd.core.designsystem.component.TransitTopBar
import com.transitmate.bd.domain.model.AppLanguage

private data class FaqEntry(
    val qEn: String,
    val aEn: String,
    val qBn: String,
    val aBn: String
)

private val FaqEntries = listOf(
    FaqEntry(
        qEn = "Why does balance say Not Available?",
        aEn = "TransitMate reads verified FeliCa purse balance and trip history on Rapid Pass cards. For pending cards (such as unvalidated MRT Pass formats) or protected blocks, TransitMate honestly displays Not Available instead of showing guessed numbers.",
        qBn = "ব্যালেন্স 'পাওয়া যায়নি' দেখায় কেন?",
        aBn = "TransitMate র‍্যাপিড পাস কার্ডের যাচাইকৃত ব্যালেন্স ও ভ্রমণ ইতিহাস পড়ে। তবে অযাচাইকৃত ফরম্যাট (যেমন এমআরটি পাস) বা সুরক্ষিত ব্লকের ক্ষেত্রে অনুমানভিত্তিক সংখ্যা না দেখিয়ে 'পাওয়া যায়নি' দেখায়।"
    ),
    FaqEntry(
        qEn = "How do I scan MRT Pass or Rapid Pass?",
        aEn = "Open the Scan tab, enable NFC on your phone, and hold the flat side of your card behind the upper-center back of your phone for 1–2 seconds without moving.",
        qBn = "এমআরটি পাস বা র‍্যাপিড পাস কীভাবে স্ক্যান করব?",
        aBn = "স্ক্যান ট্যাব খুলে ফোনের NFC চালু করুন এবং ফোনের পেছনের উপরের অংশে কার্ডটি ১–২ সেকেন্ড স্থিরভাবে ধরে রাখুন।"
    ),
    FaqEntry(
        qEn = "Can I top up or recharge my card using TransitMate?",
        aEn = "No. TransitMate is strictly read-only. Recharging requires official station Add Value Machines (AVMs) or official ticket counters.",
        qBn = "এই অ্যাপ দিয়ে কি কার্ড রিচার্জ করা যায়?",
        aBn = "না। TransitMate সম্পূর্ণ রিড-অনলি অ্যাপ। রিচার্জের জন্য মেট্রো স্টেশনের অফিসিয়াল কাউন্টার বা অ্যাড ভ্যালু মেশিন (AVM) ব্যবহার করতে হবে।"
    ),
    FaqEntry(
        qEn = "Can I use the app if my phone does not have NFC?",
        aEn = "Yes. The Fare Calculator and 16-station MRT Line 6 Station Explorer work completely offline on both NFC and non-NFC devices.",
        qBn = "আমার ফোনে NFC না থাকলে কি অ্যাপ ব্যবহার করতে পারব?",
        aBn = "হ্যাঁ। মেট্রোর ভাড়া হিসাব এবং ১৬টি স্টেশনের তালিকা ইন্টারনেট ও NFC ছাড়াই অফলাইনে ব্যবহার করা যাবে।"
    ),
    FaqEntry(
        qEn = "How is my card IDm & privacy protected?",
        aEn = "Your full Card IDm is a persistent hardware identifier stored strictly offline on your device inside local database storage—zero data leaves your phone. Avoid sharing public screenshots or reports that expose your raw Card IDm.",
        qBn = "আমার কার্ড IDm ও গোপনীয়তা কীভাবে সুরক্ষিত থাকে?",
        aBn = "কার্ড IDm একটি স্থায়ী পরিচয় নম্বর যা কেবল এই ডিভাইসেই সম্পূর্ণ অফলাইনে সংরক্ষিত থাকে—কোনো তথ্য বাইরে পাঠানো হয় না। পাবলিক স্ক্রিনশট বা শেয়ার করা রিপোর্টে আপনার মূল IDm প্রকাশ করা থেকে বিরত থাকুন।"
    )
)

@Composable
fun HelpFaqScreen(
    language: AppLanguage,
    onReplayOnboarding: () -> Unit,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var expandedIdx by remember { mutableStateOf<Int?>(0) }

    val filtered = remember(query) {
        val q = query.trim()
        if (q.isEmpty()) FaqEntries
        else FaqEntries.filter {
            it.qEn.contains(q, ignoreCase = true) ||
                it.aEn.contains(q, ignoreCase = true) ||
                it.qBn.contains(q, ignoreCase = true) ||
                it.aBn.contains(q, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TransitTopBar(
                title = stringResource(R.string.help_title),
                showBack = true,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        ResponsiveScreenContainer(
            modifier = Modifier
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.help_search_hint)) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cd_clear_search))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                SurfaceSectionCard {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Contactless,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.help_guide_title),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.help_guide_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    SurfaceSectionCard {
                        EmptyStateView(
                            icon = Icons.Rounded.Search,
                            title = stringResource(R.string.help_empty_title),
                            message = stringResource(R.string.help_empty_body, query),
                            actionLabel = stringResource(R.string.action_clear_search),
                            actionIcon = Icons.Rounded.Close,
                            onAction = { query = "" }
                        )
                    }
                }
            } else {
                itemsIndexed(filtered) { index, faq ->
                    val isExpanded = expandedIdx == index
                    val question = if (language == AppLanguage.BANGLA) faq.qBn else faq.qEn
                    val answer = if (language == AppLanguage.BANGLA) faq.aBn else faq.aEn

                    SurfaceSectionCard(
                        modifier = Modifier.animateContentSize(),
                        onClick = { expandedIdx = if (isExpanded) null else index }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = question,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isExpanded) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = answer,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                SecondaryTonalButton(
                    text = stringResource(R.string.help_replay_onboarding),
                    onClick = onReplayOnboarding
                )
            }
        }
        }
    }
}
