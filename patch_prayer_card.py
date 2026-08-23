import re

with open('app/src/main/java/com/example/ui/screens/MihrabScreen.kt', 'r') as f:
    content = f.read()

new_func = """
@Composable
private fun PrayerTimesCard(
    prayerTimes: List<PrayerTime>,
    nextPrayerName: String = "",
    nextPrayerCountdown: String = "",
    isStale: Boolean = false,
    isFetching: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val colors = QabasThemeTokens.colors
    QabasCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_mihrab_prayer_times"),
        glowAccent = colors.gold,
        contentPadding = QabasDimens.Space16
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = colors.gold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(QabasDimens.Space8))
                    Text(
                        text = stringResource(id = R.string.prayer_times),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isFetching) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = colors.gold,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(QabasDimens.Space8))
                    }
                    if (isStale) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Stale Data",
                            tint = colors.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(QabasDimens.Space4))
                    }
                    androidx.compose.material3.IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(QabasDimens.Space16))

            if (nextPrayerName.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Next: $nextPrayerName",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.gold
                    )
                    Text(
                        text = nextPrayerCountdown,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(QabasDimens.Space12))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                prayerTimes.forEach { prayer ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = prayer.nameAr,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (prayer.nameAr == nextPrayerName) colors.gold else colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(QabasDimens.Space4))
                        Text(
                            text = prayer.timeStr.split(" ")[0],
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (prayer.nameAr == nextPrayerName) colors.gold else colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}
"""

content = re.sub(r'private fun PrayerTimesCard\(prayerTimes: List<PrayerTime>\) \{.*?(?=\n@Composable|\Z)', new_func, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/MihrabScreen.kt', 'w') as f:
    f.write(content)
