package com.transitmate.bd.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Terminal
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import java.nio.charset.StandardCharsets
import com.transitmate.bd.BuildConfig
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.InfoKeyValueRow
import com.transitmate.bd.core.designsystem.component.PrimaryActionButton
import com.transitmate.bd.core.designsystem.component.SecondaryOutlinedButton
import com.transitmate.bd.core.designsystem.component.SurfaceSectionCard
import com.transitmate.bd.core.designsystem.component.TransitTopBar
import com.transitmate.bd.core.designsystem.theme.TransitMateThemeTokens
import com.transitmate.bd.domain.model.AppLanguage
import com.transitmate.bd.domain.model.AppThemeMode
import com.transitmate.bd.domain.repository.UserPreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onSelectTheme: (AppThemeMode) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onOpenDataManagement: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenDiagnostics: () -> Unit
) {
    val semantic = TransitMateThemeTokens.semantic
    var showThemeSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showPrivacySheet by remember { mutableStateOf(false) }

    val themeLabel = when (preferences.themeMode) {
        AppThemeMode.SYSTEM -> stringResource(R.string.theme_system)
        AppThemeMode.LIGHT -> stringResource(R.string.theme_light)
        AppThemeMode.DARK -> stringResource(R.string.theme_dark)
    }
    val languageLabel = when (preferences.language) {
        AppLanguage.ENGLISH -> stringResource(R.string.lang_english)
        AppLanguage.BANGLA -> stringResource(R.string.lang_bangla)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TransitTopBar(title = stringResource(R.string.settings_title), showBack = false)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionLabel(stringResource(R.string.settings_group_appearance))
            }
            item {
                SurfaceSectionCard(contentPadding = 4.dp) {
                    SettingsRow(
                        icon = Icons.Rounded.Palette,
                        title = stringResource(R.string.settings_theme),
                        subtitle = themeLabel,
                        badgeBg = MaterialTheme.colorScheme.primaryContainer,
                        badgeFg = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = { showThemeSheet = true }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SettingsRow(
                        icon = Icons.Rounded.Language,
                        title = stringResource(R.string.settings_language),
                        subtitle = languageLabel,
                        badgeBg = MaterialTheme.colorScheme.secondaryContainer,
                        badgeFg = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = { showLanguageSheet = true }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(2.dp))
                SectionLabel(stringResource(R.string.settings_group_privacy))
            }
            item {
                SurfaceSectionCard(contentPadding = 4.dp) {
                    SettingsRow(
                        icon = Icons.Rounded.Storage,
                        title = stringResource(R.string.settings_data_mgmt),
                        subtitle = stringResource(R.string.settings_data_mgmt_sub),
                        badgeBg = MaterialTheme.colorScheme.surfaceVariant,
                        badgeFg = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onOpenDataManagement
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SettingsRow(
                        icon = Icons.Rounded.Shield,
                        title = stringResource(R.string.settings_privacy),
                        subtitle = stringResource(R.string.settings_privacy_sub),
                        badgeBg = semantic.successContainer,
                        badgeFg = semantic.onSuccessContainer,
                        onClick = { showPrivacySheet = true }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(2.dp))
                SectionLabel(stringResource(R.string.settings_group_support))
            }
            item {
                SurfaceSectionCard(contentPadding = 4.dp) {
                    SettingsRow(
                        icon = Icons.AutoMirrored.Rounded.HelpOutline,
                        title = stringResource(R.string.settings_help),
                        subtitle = stringResource(R.string.settings_help_sub),
                        badgeBg = MaterialTheme.colorScheme.surfaceVariant,
                        badgeFg = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onOpenHelp
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SettingsRow(
                        icon = Icons.Rounded.Info,
                        title = stringResource(R.string.settings_about),
                        subtitle = stringResource(R.string.settings_about_sub, BuildConfig.VERSION_NAME),
                        badgeBg = MaterialTheme.colorScheme.surfaceVariant,
                        badgeFg = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onOpenAbout
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SettingsRow(
                        icon = Icons.Rounded.Terminal,
                        title = stringResource(R.string.settings_diagnostics),
                        subtitle = stringResource(R.string.settings_diagnostics_sub),
                        badgeBg = semantic.warningContainer,
                        badgeFg = semantic.onWarningContainer,
                        onClick = onOpenDiagnostics
                    )
                }
            }
        }
    }

    if (showThemeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showThemeSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = stringResource(R.string.settings_theme), style = MaterialTheme.typography.titleLarge)
                listOf(
                    AppThemeMode.SYSTEM to stringResource(R.string.theme_system),
                    AppThemeMode.LIGHT to stringResource(R.string.theme_light),
                    AppThemeMode.DARK to stringResource(R.string.theme_dark)
                ).forEach { (mode, label) ->
                    SheetRadioOption(
                        label = label,
                        selected = preferences.themeMode == mode,
                        onClick = {
                            onSelectTheme(mode)
                            showThemeSheet = false
                        }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                SecondaryOutlinedButton(
                    text = stringResource(R.string.action_close),
                    onClick = { showThemeSheet = false }
                )
            }
        }
    }

    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = stringResource(R.string.settings_language), style = MaterialTheme.typography.titleLarge)
                listOf(
                    AppLanguage.ENGLISH to stringResource(R.string.lang_english),
                    AppLanguage.BANGLA to stringResource(R.string.lang_bangla)
                ).forEach { (lang, label) ->
                    SheetRadioOption(
                        label = label,
                        selected = preferences.language == lang,
                        onClick = {
                            onSelectLanguage(lang)
                            showLanguageSheet = false
                        }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                SecondaryOutlinedButton(
                    text = stringResource(R.string.action_done),
                    onClick = { showLanguageSheet = false }
                )
            }
        }
    }

    if (showPrivacySheet) {
        ModalBottomSheet(
            onDismissRequest = { showPrivacySheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            PrivacyInfoSheetContent(onClose = { showPrivacySheet = false })
        }
    }
}

@Composable
fun PrivacyInfoSheetContent(onClose: () -> Unit) {
    val semantic = TransitMateThemeTokens.semantic
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(semantic.successContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Shield, contentDescription = null, tint = semantic.onSuccessContainer)
            }
            Text(text = stringResource(R.string.sheet_privacy_title), style = MaterialTheme.typography.titleLarge)
        }
        SurfaceSectionCard(contentPadding = 14.dp) {
            InfoKeyValueRow(
                label = stringResource(R.string.data_mgmt_cloud_backups),
                value = stringResource(R.string.data_mgmt_cloud_disabled)
            )
            InfoKeyValueRow(
                label = stringResource(R.string.about_operating_mode),
                value = stringResource(R.string.about_operating_val)
            )
            InfoKeyValueRow(
                label = stringResource(R.string.about_storage_label),
                value = stringResource(R.string.about_storage_val),
                showDivider = false
            )
        }
        PrimaryActionButton(
            text = stringResource(R.string.action_close),
            onClick = onClose
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    savedCardCount: Int,
    historyCount: Int,
    isBangla: Boolean,
    onLoadExportPreview: suspend () -> String,
    onBuildCsvExport: suspend (Boolean) -> String,
    onClearHistory: () -> Unit,
    onDeleteAllData: () -> Unit,
    onBack: () -> Unit
) {
    val semantic = TransitMateThemeTokens.semantic
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var exportPreviewJson by remember { mutableStateOf<String?>(null) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }

    val csvSavedMsg = stringResource(R.string.data_mgmt_csv_saved, historyCount)
    val csvEmptyMsg = stringResource(R.string.data_mgmt_csv_empty)
    val historyClearedMsg = stringResource(R.string.data_mgmt_history_cleared)
    val allDeletedMsg = stringResource(R.string.data_mgmt_all_deleted)

    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val csvText = onBuildCsvExport(isBangla)
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(csvText.toByteArray(StandardCharsets.UTF_8))
                    }
                }.onSuccess {
                    snackbarHostState.showSnackbar(csvSavedMsg)
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TransitTopBar(
                title = stringResource(R.string.settings_data_mgmt),
                showBack = true,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SurfaceSectionCard {
                Text(
                    text = stringResource(R.string.data_mgmt_storage_header),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                InfoKeyValueRow(label = stringResource(R.string.data_mgmt_saved_cards), value = "$savedCardCount")
                InfoKeyValueRow(label = stringResource(R.string.data_mgmt_history_records), value = "$historyCount")
                InfoKeyValueRow(
                    label = stringResource(R.string.data_mgmt_cloud_backups),
                    value = stringResource(R.string.data_mgmt_cloud_disabled),
                    showDivider = false
                )
            }

            SurfaceSectionCard(contentPadding = 4.dp) {
                SettingsRow(
                    icon = Icons.Rounded.Download,
                    title = stringResource(R.string.data_mgmt_export_csv_title),
                    subtitle = stringResource(R.string.data_mgmt_export_csv_sub),
                    badgeBg = semantic.successContainer,
                    badgeFg = semantic.onSuccessContainer,
                    onClick = {
                        if (historyCount == 0) {
                            scope.launch { snackbarHostState.showSnackbar(csvEmptyMsg) }
                        } else {
                            createCsvLauncher.launch("transitmate_history.csv")
                        }
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsRow(
                    icon = Icons.Rounded.Download,
                    title = stringResource(R.string.data_mgmt_export_title),
                    subtitle = stringResource(R.string.data_mgmt_export_sub),
                    badgeBg = MaterialTheme.colorScheme.primaryContainer,
                    badgeFg = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = {
                        scope.launch { exportPreviewJson = onLoadExportPreview() }
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsRow(
                    icon = Icons.Rounded.History,
                    title = stringResource(R.string.data_mgmt_clear_history_title),
                    subtitle = stringResource(R.string.data_mgmt_clear_history_sub),
                    badgeBg = semantic.warningContainer,
                    badgeFg = semantic.onWarningContainer,
                    onClick = {
                        onClearHistory()
                        scope.launch { snackbarHostState.showSnackbar(historyClearedMsg) }
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { showDeleteAllConfirm = true },
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.data_mgmt_delete_all),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    exportPreviewJson?.let { json ->
        ModalBottomSheet(
            onDismissRequest = { exportPreviewJson = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = stringResource(R.string.sheet_export_title), style = MaterialTheme.typography.titleLarge)
                Text(
                    text = stringResource(R.string.sheet_export_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = json,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        modifier = Modifier.padding(14.dp)
                    )
                }
                PrimaryActionButton(
                    text = stringResource(R.string.sheet_export_copy),
                    icon = Icons.Rounded.ContentCopy,
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        cm?.setPrimaryClip(ClipData.newPlainText("TransitMate Export", json))
                        exportPreviewJson = null
                    }
                )
                SecondaryOutlinedButton(
                    text = stringResource(R.string.action_cancel),
                    onClick = { exportPreviewJson = null }
                )
            }
        }
    }

    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text(stringResource(R.string.data_mgmt_delete_all)) },
            text = { Text(stringResource(R.string.dialog_delete_card_message, stringResource(R.string.data_mgmt_saved_cards))) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAllConfirm = false
                        onDeleteAllData()
                        scope.launch { snackbarHostState.showSnackbar(allDeletedMsg) }
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) { Text(stringResource(R.string.action_remove)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirm = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeBg: Color,
    badgeFg: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = badgeFg, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SheetRadioOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            RadioButton(selected = selected, onClick = null)
        }
    }
}
