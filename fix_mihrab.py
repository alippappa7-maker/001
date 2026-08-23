import re

with open('app/src/main/java/com/example/ui/screens/MihrabScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    if "@Composable" in line and i < len(lines)-1 and "@Composable" in lines[i+1]:
        continue # Skip repeated annotation
    new_lines.append(line)

content = "".join(new_lines)

imports_to_add = """
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import com.example.domain.model.DailyPrayerTimes
"""
if "import androidx.compose.foundation.layout.width" not in content:
    content = content.replace("import androidx.compose.foundation.layout.height", "import androidx.compose.foundation.layout.height\n" + imports_to_add)

content = content.replace("R.string.prayer_times", "R.string.feature_mihrab_title") # Temporary workaround for string resource
content = content.replace("prayerTimes = uiState.prayerTimes", "prayerTimes = uiState.dailyPrayerTimes?.toList() ?: emptyList()")

with open('app/src/main/java/com/example/ui/screens/MihrabScreen.kt', 'w') as f:
    f.write(content)
