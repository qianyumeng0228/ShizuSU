package com.sukisu.ultra.ui.screen.modulerepo

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sukisu.ultra.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleRepoUploadScreenMaterial(
    state: ModuleRepoUploadUiState,
    actions: ModuleRepoUploadActions,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.module_repo_upload_title)) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.module_repo_upload_source),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { actions.onSelectSource(ModuleUploadSource.INSTALLED) },
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.module_repo_upload_source_installed)) }
                    OutlinedButton(
                        onClick = { actions.onSelectSource(ModuleUploadSource.LOCAL_ZIP) },
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.module_repo_upload_source_local_zip)) }
                }
            }

            if (state.source == ModuleUploadSource.INSTALLED) {
                item {
                    Text(
                        text = stringResource(R.string.module_repo_upload_installed_pick),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (state.installedLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (state.installedModules.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.module_repo_upload_no_installed),
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                } else {
                    items(state.installedModules, key = { it.id }) { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            onClick = { actions.onSelectInstalled(entry.id) },
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = entry.name.ifBlank { entry.id }, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "ID: ${entry.id}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        )
                    }
                }
            }

            item { HorizontalDivider() }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.moduleId,
                        onValueChange = actions.onModuleIdChange,
                        label = { Text(stringResource(R.string.module_repo_upload_field_id)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.moduleName,
                        onValueChange = actions.onModuleNameChange,
                        label = { Text(stringResource(R.string.module_repo_upload_field_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.author,
                        onValueChange = actions.onAuthorChange,
                        label = { Text(stringResource(R.string.module_repo_upload_field_author)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.versionName,
                        onValueChange = actions.onVersionNameChange,
                        label = { Text(stringResource(R.string.module_repo_upload_field_version)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.versionCode,
                        onValueChange = actions.onVersionCodeChange,
                        label = { Text(stringResource(R.string.module_repo_upload_field_version_code)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.summaryZh,
                        onValueChange = actions.onSummaryZhChange,
                        label = { Text(stringResource(R.string.module_repo_upload_field_summary)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(stringResource(R.string.module_repo_upload_zygisk))
                        Switch(checked = state.zygisk, onCheckedChange = actions.onZygiskChange)
                    }
                }
            }

            item { HorizontalDivider() }

            item {
                OutlinedTextField(
                    value = state.token,
                    onValueChange = actions.onTokenChange,
                    label = { Text(stringResource(R.string.module_repo_upload_token_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                when (val s = state.status) {
                    is ModuleUploadStatus.Failed -> Text(
                        stringResource(R.string.module_repo_upload_failed, s.message),
                        color = MaterialTheme.colorScheme.error,
                    )
                    is ModuleUploadStatus.Success -> Text(
                        stringResource(R.string.module_repo_upload_success, s.issueNumber),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    ModuleUploadStatus.Packaging -> Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                        Text(stringResource(R.string.module_repo_upload_preparing))
                    }
                    is ModuleUploadStatus.UploadingGist -> Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                        Text(stringResource(R.string.module_repo_upload_gist_progress, s.done, s.total))
                    }
                    ModuleUploadStatus.SubmittingIssue -> Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                        Text(stringResource(R.string.module_repo_upload_issue_progress))
                    }
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
