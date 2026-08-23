import re

with open('app/src/main/java/com/example/ui/screens/MihrabScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("colors.error", "androidx.compose.ui.graphics.Color.Red")
content = re.sub(r'(@Composable\s*)+private fun PrayerTimesCard', '@Composable\nprivate fun PrayerTimesCard', content)

with open('app/src/main/java/com/example/ui/screens/MihrabScreen.kt', 'w') as f:
    f.write(content)
