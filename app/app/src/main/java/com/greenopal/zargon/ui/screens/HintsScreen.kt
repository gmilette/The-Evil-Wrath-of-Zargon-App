package com.greenopal.zargon.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenopal.zargon.ui.components.DungeonBackground
import com.greenopal.zargon.ui.components.MedievalButton
import com.greenopal.zargon.ui.components.MedievalPanel
import com.greenopal.zargon.ui.components.OrnateSeparator
import com.greenopal.zargon.ui.components.ScreenHeading
import com.greenopal.zargon.ui.components.ScrollBanner
import com.greenopal.zargon.ui.theme.EmberBright
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.GoldBright
import com.greenopal.zargon.ui.theme.MpBlueBright
import com.greenopal.zargon.ui.theme.Parchment
import com.greenopal.zargon.ui.theme.ParchmentDim
import com.greenopal.zargon.ui.theme.XpGreenBright

@Composable
fun HintsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    DungeonBackground {
        LazyColumn(
            modifier            = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding      = PaddingValues(vertical = 24.dp),
        ) {
            item { ScreenHeading("Map Guide & Hints") }

            item {
                MedievalPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ScrollBanner("World Layout")
                        HintText(
                            """
The game world is a 4×4 grid of maps (16 total).
Each map is 20×10 tiles. Maps are numbered:

  Row 1: Map 11, 21, 31, 41
  Row 2: Map 12, 22, 32, 42
  Row 3: Map 13, 23, 33, 43
  Row 4: Map 14, 24, 34, 44
                            """.trimIndent()
                        )
                    }
                }
            }

            item {
                MedievalPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        ScrollBanner("Quest Items")
                        Spacer(Modifier.height(4.dp))
                        QuestHint("DYNAMITE", "Map 44 (4,4)", "(14, 6)", "Blast rocks to free Boatman")
                        OrnateSeparator()
                        QuestHint("RUTTER", "Map 24 (2,4)", "(9, 1)", "Navigation tool for ship")
                        OrnateSeparator()
                        QuestHint("WOOD", "Map 22 (2,2)", "(10, 4)", "Ship hull material")
                        OrnateSeparator()
                        QuestHint("DEAD WOOD", "Map 14 (1,4)", "(3, 8)", "Boat construction material")
                        OrnateSeparator()
                        QuestHint("CLOTH", "Map 13 (1,3)", "(7, 6)", "Sail material")
                        OrnateSeparator()
                        QuestHint("TRAPPED SOUL", "Battle drop", "Defeat Necromancer", "Random drop from Necromancer monster")
                    }
                }
            }

            item {
                MedievalPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        ScrollBanner("Key Locations")
                        Spacer(Modifier.height(4.dp))
                        LocationHint("Healer", "Map 24 (2,4)", "Paid healing + Save")
                        OrnateSeparator()
                        LocationHint("Weapon Shop", "Map 24 (2,4) at (2,7)", "Weapons & Armor")
                        OrnateSeparator()
                        LocationHint("Fountain", "Map 11 (1,1)", "Free healing + Save")
                        OrnateSeparator()
                        LocationHint("Fountain", "Map 32 (3,2)", "Free healing + Save")
                        OrnateSeparator()
                        LocationHint("Castle", "Map 32 (3,2)", "Final area — requires ship")
                    }
                }
            }

            item {
                MedievalPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        ScrollBanner("Quest Progression")
                        Spacer(Modifier.height(4.dp))
                        HintText(
                            """
1. Talk to Sandman (Map 14) — Learn about dynamite
2. Find Dynamite (Map 44 at 14,6)
3. Free Boatman (Map 24) — Use dynamite
4. Collect boat materials:
   • Rutter (Map 24 at 9,1)
   • Wood (Map 22 at 10,4)
   • Dead Wood (Map 14 at 3,8)
   • Cloth (Map 13 at 7,6)
5. Give materials to Boatman → Get boat plans
6. Boatman dies! → Battle Necromancer monsters to get "trapped soul"
7. Visit Mountain Jack (Map 43) → Learn about Necromancer NPC
8. Visit Necromancer NPC (Map 41) → Give soul to resurrect Boatman
9. Return to Boatman (Map 24) → Get ship!
10. Travel river to Castle Island (Map 32)
11. Defeat ZARGON!
                            """.trimIndent()
                        )
                    }
                }
            }

            item {
                MedievalPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        ScrollBanner("Tips")
                        Spacer(Modifier.height(4.dp))
                        HintText(
                            """
• Use SEARCH command when near item locations
• Save at Healers or Fountains frequently
• Ship only works on shallow water (4), not deep (w)
• Level up before exploring dangerous areas
• Stock up on potions from Weapon Shop
• Check Quest Progress in menu to track items
• IMPORTANT: "Trapped soul" drops from Necromancer
  monsters in battle — you may need several fights
                            """.trimIndent()
                        )
                    }
                }
            }

            item {
                MedievalButton(onClick = onBack) {
                    Text("Back to Menu", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun HintText(text: String) {
    Text(
        text  = text,
        style = MaterialTheme.typography.bodyMedium.copy(color = ParchmentDim),
    )
}

@Composable
private fun QuestHint(name: String, map: String, coords: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text  = "• $name",
            style = MaterialTheme.typography.bodyLarge.copy(color = GoldBright),
        )
        Text(
            text  = "  $map at $coords",
            style = MaterialTheme.typography.bodySmall.copy(color = Parchment),
        )
        Text(
            text  = "  $description",
            style = MaterialTheme.typography.bodySmall.copy(color = ParchmentDim),
        )
    }
}

@Composable
private fun LocationHint(name: String, location: String, notes: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text  = "• $name — $location",
            style = MaterialTheme.typography.bodyLarge.copy(color = MpBlueBright),
        )
        Text(
            text  = "  $notes",
            style = MaterialTheme.typography.bodySmall.copy(color = ParchmentDim),
        )
    }
}
