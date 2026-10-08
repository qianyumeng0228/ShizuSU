package com.sukisu.ultra.ui.screen.modulerepo

import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.theme.LocalEnableBlur
import com.sukisu.ultra.ui.util.BlurredBar
import com.sukisu.ultra.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

@Composable
fun ModuleRepoUploadScreenMiuix(
    state: ModuleRepoUploadUiState,
    actions: ModuleRepoUploadActions,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val enableBlur = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlur)
    val barColor = if (backdrop != null) Color.Transparent else colorScheme.surface

    Scaffold(
        topBar = {
            BlurredBar(backdrop) {
                TopAppBar(
                    color = barColor,
                    title = stringResource(R.string.module_repo_upload_title),
                    navigationIcon = {
                        IconButton(onClick = actions.onBack) {
                            val layoutDirection = LocalLayoutDirection.current
                            Icon(
                                modifier = Modifier.graphicsLayer {
                                    if (layoutDirection == LayoutDirection.Rtl) scaleX = -1f
                                },
                                imageVector = MiuixIcons.Back,
                                contentDescription = null,
                                tint = colorScheme.onSurface
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .scrollEndHaptic()
                .overScrollVertical(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
                start = 12.dp, end = 12.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SmallTitle(text = stringResource(R.string.module_repo_upload_source))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SourceChip(
                            label = stringResource(R.string.module_repo_upload_source_installed),
                            selected = state.source == ModuleUploadSource.INSTALLED,
                            onClick = { actions.onSelectSource(ModuleUploadSource.INSTALLED) },
                            modifier = Modifier.weight(1f),
                        )
                        SourceChip(
                            label = stringResource(R.string.module_repo_upload_source_local_zip),
                            selected = state.source == ModuleUploadSource.LOCAL_ZIP,
                            onClick = { actions.onSelectSource(ModuleUploadSource.LOCAL_ZIP) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            if (state.source == ModuleUploadSource.INSTALLED) {
                item {
                    SmallTitle(text = stringResource(R.string.module_repo_upload_installed_pick))
                }
                if (state.installedLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            InfiniteProgressIndicator()
                        }
                    }
                } else if (state.installedModules.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.module_repo_upload_no_installed),
                                modifier = Modifier.padding(16.dp),
                                color = colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                } else {
                    items(state.installedModules, key = { it.id }) { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { actions.onSelectInstalled(entry.id) },
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Text(
                                    text = entry.name.ifBlank { entry.id },
                                    color = colorScheme.onSurface,
                                )
                                Text(
                                    text = "ID: ${entry.id}",
                                    color = colorScheme.onSurfaceVariantSummary,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = actions.onPickLocalZip,
                    ) {
                        Text(
                            text = state.localZipName ?: stringResource(R.string.module_repo_upload_pick_zip),
                            modifier = Modifier.padding(16.dp),
                            color = if (state.localZipName != null) colorScheme.onSurface else colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }

            item { SmallTitle(text = stringResource(R.string.module_repo_upload_field_name)) }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = state.moduleId,
                            onValueChange = actions.onModuleIdChange,
                            label = stringResource(R.string.module_repo_upload_field_id),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        TextField(
                            value = state.moduleName,
                            onValueChange = actions.onModuleNameChange,
                            label = stringResource(R.string.module_repo_upload_field_name),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        TextField(
                            value = state.author,
                            onValueChange = actions.onAuthorChange,
                            label = stringResource(R.string.module_repo_upload_field_author),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        TextField(
                            value = state.versionName,
                            onValueChange = actions.onVersionNameChange,
                            label = stringResource(R.string.module_repo_upload_field_version),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        TextField(
                            value = state.versionCode,
                            onValueChange = actions.onVersionCodeChange,
                            label = stringResource(R.string.module_repo_upload_field_version_code),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        TextField(
                            value = state.summaryZh,
                            onValueChange = actions.onSummaryZhChange,
                            label = stringResource(R.string.module_repo_upload_field_summary),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(stringResource(R.string.module_repo_upload_zygisk), color = colorScheme.onSurface)
                            Switch(checked = state.zygisk, onCheckedChange = actions.onZygiskChange)
                        }
                    }
                }
            }

            item { SmallTitle(text = stringResource(R.string.module_repo_upload_token)) }
            item {
                // [ShizuSU] 方案 C：令牌框换原生 EditText，importantForAutofill=NO 从根源禁掉 autofill 气泡。
                TokenEditTextMiuix(
                    value = state.token,
                    onValueChange = actions.onTokenChange,
                    hint = stringResource(R.string.module_repo_upload_token_hint),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                when (val s = state.status) {
                    is ModuleUploadStatus.Failed -> StatusText(
                        stringResource(R.string.module_repo_upload_failed, s.message),
                        color = colorScheme.error,
                    )
                    is ModuleUploadStatus.Success -> StatusText(
                        stringResource(R.string.module_repo_upload_success, s.issueNumber),
                        color = colorScheme.primary,
                    )
                    ModuleUploadStatus.Packaging -> StatusText(stringResource(R.string.module_repo_upload_preparing))
                    is ModuleUploadStatus.UploadingGist -> StatusText(
                        stringResource(R.string.module_repo_upload_gist_progress, s.done, s.total),
                    )
                    ModuleUploadStatus.SubmittingIssue -> StatusText(stringResource(R.string.module_repo_upload_issue_progress))
                    ModuleUploadStatus.Idle -> Spacer(Modifier.height(1.dp))
                }
            }

            item {
                Button(
                    onClick = actions.onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canSubmit,
                ) {
                    Text(stringResource(R.string.module_repo_upload_submit))
                }
            }
        }
    }
}

@Composable
private fun TokenEditTextMiuix(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val textColor = android.graphics.Color.parseColor("#FF1C1B1F")
    val hintColor = android.graphics.Color.parseColor("#49454F")
    val bg = remember {
        GradientDrawable().apply {
            cornerRadius = 12f * context.resources.displayMetrics.density
            setColor(android.graphics.Color.parseColor("#FFF7F6F0"))
            setStroke(1, android.graphics.Color.parseColor("#CAC4D0"))
        }
    }
    AndroidView(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        factory = { ctx ->
            EditText(ctx).apply {
                this.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
                this.background = bg
                this.setTextColor(textColor)
                this.setHintTextColor(hintColor)
                this.hint = hint
                this.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                this.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (56 * context.resources.displayMetrics.density).toInt(),
                )
                val pad = (16 * context.resources.displayMetrics.density).toInt()
                setPadding(pad, 0, pad, 0)
                isSingleLine = true
                addTextChangedListener(object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: android.text.Editable?) {
                        if (s?.toString() != value) onValueChange(s?.toString().orEmpty())
                    }
                })
            }
        },
        update = { et ->
            if (et.text.toString() != value) et.setText(value)
        },
    )
}

@Composable
private fun SourceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = if (selected) colorScheme.primary else colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun StatusText(text: String, color: Color = colorScheme.onSurface) {
    Text(
        text = text,
        color = color,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}
