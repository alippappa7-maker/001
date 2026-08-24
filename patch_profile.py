import sys

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'r') as f:
    content = f.read()

target = """                // Full Settings Button
                QabasButton(
                    text = stringResource(id = R.string.settings_title),
                    onClick = onNavigateToSettings,
                    variant = QabasButtonVariant.OutlineGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_profile_settings")
                )
            }
        }
    }"""

replacement = """                // Full Settings Button
                QabasButton(
                    text = stringResource(id = R.string.settings_title),
                    onClick = onNavigateToSettings,
                    variant = QabasButtonVariant.OutlineGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_profile_settings")
                )

                Spacer(modifier = Modifier.height(QabasDimens.Space10))

                // Legal & Privacy Section (Security Review Requirement)
                SectionTitle(
                    title = "الخصوصية والشروط",
                    showAccentDot = true
                )
                
                QabasCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = QabasDimens.Space14
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(QabasDimens.Space12)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space10)
                        ) {
                            Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = colors.gold, modifier = Modifier.size(20.dp))
                            Column {
                                Text("سياسة الخصوصية", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                Text("غير متاح حاليًا", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            }
                        }
                        
                        androidx.compose.material3.HorizontalDivider(color = colors.surfaceBorder, thickness = 1.dp)
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(QabasDimens.Space10)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = colors.gold, modifier = Modifier.size(20.dp))
                            Column {
                                Text("شروط الاستخدام", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                Text("غير متاح حاليًا", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'w') as f:
        f.write(content)
    print("Patched successfully")
else:
    print("Target not found")
