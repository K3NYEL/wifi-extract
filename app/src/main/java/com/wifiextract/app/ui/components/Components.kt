package com.wifiextract.app.ui.components

import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.wifiextract.app.R
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifiextract.app.ui.theme.MnaColors
import com.wifiextract.app.ui.theme.MnaShapes
import androidx.compose.runtime.produceState
import coil.compose.AsyncImage as CoilAsyncImage
import com.wifiextract.app.data.GithubUser
import com.wifiextract.app.data.fetchGithubUser

private val SkeletonColor = Color(0xFF2A2A30)

@Composable
fun ScreenHeader(title: String, modifier: Modifier = Modifier, onBack: (() -> Unit)? = null, trailing: @Composable () -> Unit = {}) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MnaColors.TextPrimary)
            }
        }
        Text(title, modifier = Modifier.weight(1f), color = MnaColors.TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        trailing()
    }
}

@Composable
fun DeveloperImage(modifier: Modifier = Modifier, painter: Painter? = null, initials: String = "", content: @Composable () -> Unit = {},) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DeveloperAvatar(painter = painter, initials = initials)
        content()
    }
}

@Composable
fun GithubProfileCard(username: String, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val result by produceState<Result<GithubUser>?>(initialValue = null, key1 = username) {
        value = fetchGithubUser(username)
    }
    val user = result?.getOrNull()
    val cardShape = RoundedCornerShape(24.dp)
    val accent = Color(0xFFD7263D)
    val colors = MaterialTheme.colorScheme

    if (result == null) {
        SkeletonCard(rows = 3, modifier = modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(colors.surface)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Developer",
            modifier = Modifier
                .background(colors.surfaceVariant, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            color = colors.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .border(3.dp, accent, CircleShape)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    username.take(1).uppercase(),
                    color = colors.onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                CoilAsyncImage(
                    model = user?.avatarUrl,
                    contentDescription = "Avatar de @$username",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = user?.name?.takeIf { it.isNotBlank() } ?: username,
                        color = colors.onSurface,
                        fontSize = 30.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        "GITHUB",
                        modifier = Modifier
                            .background(colors.surfaceVariant, CircleShape)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        color = colors.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                }
                Text(
                    "@${user?.login ?: username}",
                    color = colors.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }

        androidx.compose.material3.Button(
            onClick = { uriHandler.openUri(user?.htmlUrl ?: "https://github.com/$username") },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = accent,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                stringResource(R.string.view_on_github),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
@Composable
private fun DeveloperAvatar(painter: Painter?, initials: String) {
    val shape = CircleShape
    if (painter != null ) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(44.dp).clip(shape),
        )
    } else {
        Box(
            modifier = Modifier.size(44.dp).clip(shape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
            )
        }

    }
}
@Composable
fun MnaCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().background(MnaColors.Surface, MnaShapes.Card).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, color = MnaColors.TextSecondary, fontSize = 12.sp, letterSpacing = 0.8.sp, fontWeight = FontWeight.Medium)
}

@Composable
fun Note(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, color = MnaColors.TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
}

@Composable
fun StatusChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier.background(color.copy(alpha = 0.14f), MnaShapes.Chip).padding(horizontal = 8.dp, vertical = 3.dp),
        color = color,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun ComingSoonTag(modifier: Modifier = Modifier) {
    Text(
        stringResource(R.string.coming_soon).uppercase(),
        modifier = modifier.background(Color(0xFF3B1210), MnaShapes.Chip).padding(horizontal = 6.dp, vertical = 2.dp),
        color = Color(0xFFFF8A85),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun InfoRow(label: String, value: String, modifier: Modifier = Modifier, mono: Boolean = true) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, color = MnaColors.TextSecondary, fontSize = 13.sp)
        Text(
            value,
            modifier = Modifier.weight(1f),
            color = MnaColors.TextPrimary,
            fontSize = 13.sp,
            textAlign = TextAlign.End,
            fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default
        )
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        shape = MnaShapes.Button,
        colors = ButtonDefaults.buttonColors(
            containerColor = MnaColors.AccentButton,
            contentColor = Color.White,
            disabledContainerColor = MnaColors.SurfaceRaised,
            disabledContentColor = MnaColors.TextSecondary
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) { Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        shape = MnaShapes.Button,
        colors = ButtonDefaults.buttonColors(
            containerColor = MnaColors.SurfaceRaised,
            contentColor = MnaColors.TextPrimary,
            disabledContainerColor = MnaColors.SurfaceRaised,
            disabledContentColor = MnaColors.TextSecondary
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) { Text(text, fontSize = 14.sp, maxLines = 1) }
}

@Composable
fun MnaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Uri,
    onDone: () -> Unit = {}
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(label) },
        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
        shape = MnaShapes.Button,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MnaColors.SurfaceFocus,
            unfocusedContainerColor = MnaColors.SurfaceRaised,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = MnaColors.TextPrimary,
            unfocusedTextColor = MnaColors.TextPrimary,
            focusedLabelColor = MnaColors.TextSecondary,
            unfocusedLabelColor = MnaColors.TextSecondary,
            cursorColor = MnaColors.Accent
        )
    )
}

/** Placeholder block shown while real data loads. Pulses; every block is hidden from screen readers. */
@Composable
fun SkeletonBox(modifier: Modifier = Modifier, height: Dp = 14.dp) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(850, easing = LinearEasing), RepeatMode.Reverse),
        label = "skeleton-alpha"
    )
    Box(modifier.height(height).background(SkeletonColor.copy(alpha = alpha), MnaShapes.Card))
}

/** A card of skeleton rows (label on the left, value on the right). */
@Composable
fun SkeletonCard(rows: Int, modifier: Modifier = Modifier) {
    val loadingText = stringResource(R.string.loading)
    MnaCard(modifier.semantics { contentDescription = loadingText }) {
        repeat(rows) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SkeletonBox(Modifier.width(96.dp))
                SkeletonBox(Modifier.width(130.dp))
            }
        }
    }
}

/** Skeleton shaped like a device row: two text lines and a button. */
@Composable
fun SkeletonListRow(modifier: Modifier = Modifier) {
    val loadingText = stringResource(R.string.loading)
    MnaCard(modifier.semantics { contentDescription = loadingText }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBox(Modifier.width(120.dp))
                SkeletonBox(Modifier.width(180.dp), height = 12.dp)
            }
            SkeletonBox(Modifier.width(64.dp), height = 40.dp)
        }
    }
}

/** Skeleton for free text (release notes, results). */
@Composable
fun SkeletonLines(lines: Int, modifier: Modifier = Modifier) {
    val loadingText = stringResource(R.string.loading)
    Column(modifier.fillMaxWidth().semantics { contentDescription = loadingText }, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(lines) { index ->
            SkeletonBox(Modifier.fillMaxWidth(if (index == lines - 1) 0.6f else 1f), height = 12.dp)
        }
    }
}

/** Label for a group of content placed outside a card. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), modifier = modifier, color = MnaColors.TextSecondary, fontSize = 12.sp, letterSpacing = 0.7.sp, fontWeight = FontWeight.SemiBold)
}

/** Replaces rows of toggle-like buttons: one option selected at a time. [disabled] indexes cannot be chosen. */
@Composable
fun SegmentedControl(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    disabled: Set<Int> = emptySet()
) {
    Row(
        modifier.fillMaxWidth().background(MnaColors.Surface, MnaShapes.Button).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val on = index == selected
            val off = index in disabled
            Box(
                Modifier.weight(1f).heightIn(min = 44.dp)
                    .background(if (on) MnaColors.SurfaceFocus else Color.Transparent, MnaShapes.Button)
                    .selectable(selected = on, enabled = !off, role = Role.Tab, onClick = { onSelect(index) }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (on) MnaColors.TextPrimary else MnaColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

/** Compact label + large value, used in rows of three. */
@Composable
fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier.background(MnaColors.Surface, MnaShapes.Card).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, color = MnaColors.TextSecondary, fontSize = 12.sp, maxLines = 1)
        Text(value, color = MnaColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, fontFamily = FontFamily.Monospace)
    }
}

/** Vertical icon + text action. Only one tile per row should be [primary]. */
@Composable
fun ActionTile(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false) {
    Column(
        modifier.heightIn(min = 76.dp)
            .background(if (primary) MnaColors.AccentButton else MnaColors.Surface, MnaShapes.Card)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
    ) {
        Icon(icon, contentDescription = null, tint = if (primary) Color.White else MnaColors.TextPrimary)
        Text(
            text,
            color = if (primary) Color.White else MnaColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

/** Settings-style row: title, optional subtitle, optional trailing content and a chevron. */
@Composable
fun SettingsRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    chevron: ImageVector = Icons.Default.ChevronRight,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = MnaColors.TextPrimary, fontSize = 15.sp)
            if (subtitle != null) Text(subtitle, color = MnaColors.TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        }
        trailing()
        Icon(chevron, contentDescription = null, tint = MnaColors.TextSecondary)
    }
}

/** Monospaced output block for diagnostic results. */
@Composable
fun TerminalBlock(lines: List<String>, modifier: Modifier = Modifier, footer: @Composable ColumnScope.() -> Unit = {}) {
    Column(
        modifier.fillMaxWidth().background(MnaColors.NavBar, MnaShapes.Card).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        lines.forEach { Text(it, color = MnaColors.TextPrimary, fontSize = 12.sp, lineHeight = 19.sp, fontFamily = FontFamily.Monospace) }
        footer()
    }
}
