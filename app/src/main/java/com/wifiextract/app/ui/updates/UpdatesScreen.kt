package com.wifiextract.app.ui.updates

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifiextract.app.R
import com.wifiextract.app.core.Formatters
import com.wifiextract.app.data.InstalledVersion
import com.wifiextract.app.data.ReleaseInfo
import com.wifiextract.app.ui.components.Eyebrow
import com.wifiextract.app.ui.components.InfoRow
import com.wifiextract.app.ui.components.MnaCard
import com.wifiextract.app.ui.components.Note
import com.wifiextract.app.ui.components.PrimaryButton
import com.wifiextract.app.ui.components.ScreenHeader
import com.wifiextract.app.ui.components.SecondaryButton
import com.wifiextract.app.ui.components.SkeletonBox
import com.wifiextract.app.ui.components.SkeletonLines
import com.wifiextract.app.ui.components.StatusChip
import com.wifiextract.app.ui.theme.MnaColors

@Composable
fun UpdatesScreen(
    state: UpdateUiState,
    installed: InstalledVersion,
    onCheck: () -> Unit,
    onDownload: (ReleaseInfo) -> Unit,
    onCancel: () -> Unit,
    onInstall: () -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.nav_updates), onBack = onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MnaCard {
                InfoRow(stringResource(R.string.lbl_installed_version), "${installed.name} (${installed.code})")
                when (state) {
                    UpdateUiState.Idle -> InfoRow(stringResource(R.string.lbl_latest_version), "—")
                    UpdateUiState.Checking -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.lbl_latest_version), color = MnaColors.TextSecondary, fontSize = 13.sp)
                        SkeletonBox(Modifier.padding(top = 2.dp).fillMaxWidth(0.3f))
                    }
                    is UpdateUiState.UpToDate -> InfoRow(stringResource(R.string.lbl_latest_version), state.latest?.tag ?: "—")
                    is UpdateUiState.Available -> InfoRow(stringResource(R.string.lbl_latest_version), state.info.tag)
                    is UpdateUiState.Downloading -> InfoRow(stringResource(R.string.lbl_latest_version), state.info.tag)
                    is UpdateUiState.Ready -> InfoRow(stringResource(R.string.lbl_latest_version), state.info.tag)
                    is UpdateUiState.Failed -> InfoRow(stringResource(R.string.lbl_latest_version), "—")
                }
                Note(stringResource(R.string.source_github_releases))
            }

            when (state) {
                UpdateUiState.Idle -> {
                    Note(stringResource(R.string.updates_idle_prompt))
                    SecondaryButton(stringResource(R.string.btn_check), onCheck, Modifier.fillMaxWidth())
                }
                UpdateUiState.Checking -> {
                    MnaCard {
                        Eyebrow(stringResource(R.string.eyebrow_release_notes))
                        SkeletonLines(lines = 4)
                    }
                    SecondaryButton(stringResource(R.string.btn_checking), {}, Modifier.fillMaxWidth(), enabled = false)
                }
                is UpdateUiState.UpToDate -> {
                    MnaCard {
                        StatusChip(stringResource(R.string.chip_up_to_date), MnaColors.Success)
                        Note(stringResource(R.string.msg_up_to_date))
                    }
                    SecondaryButton(stringResource(R.string.btn_check_again), onCheck, Modifier.fillMaxWidth())
                    state.latest?.pageUrl?.let { url ->
                        SecondaryButton(stringResource(R.string.btn_view_github), {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }, Modifier.fillMaxWidth())
                    }
                }
                is UpdateUiState.Available -> {
                    ReleaseNotes(state.info)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PrimaryButton(stringResource(R.string.btn_download), { onDownload(state.info) }, Modifier.weight(1f))
                        SecondaryButton(stringResource(R.string.btn_check), onCheck, Modifier.weight(1f))
                    }
                    SecondaryButton(stringResource(R.string.btn_view_github), {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(state.info.pageUrl)))
                    }, Modifier.fillMaxWidth())
                    Note(stringResource(R.string.msg_verification_notice))
                }
                is UpdateUiState.Downloading -> {
                    MnaCard {
                        Eyebrow(if (state.percent == 100) stringResource(R.string.eyebrow_verifying_integrity) else stringResource(R.string.eyebrow_downloading_tag, state.info.tag))
                        if (state.percent != null) {
                            LinearProgressIndicator(
                                progress = { state.percent / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = MnaColors.Accent,
                                trackColor = MnaColors.SurfaceRaised
                            )
                            Note("${state.percent} %")
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = MnaColors.Accent,
                                trackColor = MnaColors.SurfaceRaised
                            )
                        }
                    }
                    SecondaryButton(stringResource(R.string.btn_cancel), onCancel, Modifier.fillMaxWidth())
                }
                is UpdateUiState.Ready -> {
                    MnaCard {
                        StatusChip(stringResource(R.string.chip_verified), MnaColors.Success)
                        Note(stringResource(R.string.msg_verified))
                    }
                    PrimaryButton(stringResource(R.string.btn_install_tag, state.info.tag), onInstall, Modifier.fillMaxWidth())
                }
                is UpdateUiState.Failed -> {
                    MnaCard {
                        Eyebrow(stringResource(R.string.eyebrow_error))
                        Text(state.message, color = MnaColors.AccentText, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                    SecondaryButton(
                        stringResource(R.string.btn_retry),
                        { if (state.retry != null) onDownload(state.retry) else onCheck() },
                        Modifier.fillMaxWidth()
                    )
                }
            }
            Note(stringResource(R.string.updates_install_notice))
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ReleaseNotes(info: ReleaseInfo) {
    MnaCard {
        Eyebrow(stringResource(R.string.eyebrow_notes_title, info.title.uppercase()))
        val notes = info.notes.lines()
            .map { it.trim().trimStart('#', '*', '-', ' ') }
            .filter { it.isNotBlank() }
            .take(8)
        if (notes.isEmpty()) Note(stringResource(R.string.notes_empty))
        else notes.forEach { Text("· $it", color = MnaColors.TextPrimary, fontSize = 13.sp, lineHeight = 19.sp) }
        if (info.apkSize > 0) Note(stringResource(R.string.label_size, Formatters.formatBytes(info.apkSize)))
    }
}
