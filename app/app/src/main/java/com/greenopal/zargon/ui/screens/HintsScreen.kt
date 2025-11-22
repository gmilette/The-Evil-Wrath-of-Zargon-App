package com.greenopal.zargon.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Hints screen showing map guide and quest information
 */
@Composable
fun HintsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Handle back button
    BackHandler {
        onBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "MAP GUIDE & HINTS",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Scrollable content card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    MapGuideContent()
                }
            }

            // Back button
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("BACK TO TITLE")
            }
        }
    }
}

@Composable
private fun MapGuideContent() {
    Column {
        SectionHeader("WORLD LAYOUT")
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

        SectionHeader("QUEST ITEMS")
        QuestItem("DYNAMITE", "Map 44 (4,4)", "(14, 6)", "Blast rocks to free Boatman")
        QuestItem("RUTTER", "Map 24 (2,4)", "(9, 1)", "Navigation tool for ship")
        QuestItem("WOOD", "Map 22 (2,2)", "(10, 4)", "Ship hull material")
        QuestItem("DEAD WOOD", "Map 14 (1,4)", "(3, 8)", "Boat construction material")
        QuestItem("CLOTH", "Map 13 (1,3)", "(7, 6)", "Sail material")

        SectionHeader("KEY LOCATIONS")
        LocationItem("Healer", "Map 24 (2,4)", "Paid healing + Save")
        LocationItem("Weapon Shop", "Map 24 (2,4) at (2,7)", "Weapons & Armor")
        LocationItem("Fountain", "Map 11 (1,1)", "Free healing + Save")
        LocationItem("Fountain", "Map 32 (3,2)", "Free healing + Save")
        LocationItem("Castle", "Map 32 (3,2)", "Final area - requires ship")

        SectionHeader("NPCS")
        NpcItem("Sandman", "Map 14 (1,4)", "Tells about dynamite", "From start")
        NpcItem("Boatman", "Map 24 (2,4)", "Trapped under rocks", "From start")
        NpcItem("Necromancer", "Map 41 (4,1)", "Resurrects Boatman", "Story 4.0+")
        NpcItem("Mountain Jack", "Map 43 (4,3)", "Tells about soul", "Story 4.0+")
        NpcItem("Old Man", "Map 44 (4,4)", "Airship mini-game", "From start")

        SectionHeader("QUEST PROGRESSION")
        HintText(
            """
            1. Talk to Sandman (Map 14) - Learn about dynamite
            2. Find Dynamite (Map 44 at 14,6)
            3. Free Boatman (Map 24) - Use dynamite
            4. Collect boat materials:
               • Rutter (Map 24 at 9,1)
               • Wood (Map 22 at 10,4)
               • Dead Wood (Map 14 at 3,8)
               • Cloth (Map 13 at 7,6)
            5. Give materials to Boatman → Get boat plans
            6. Boatman dies → Collect trapped soul
            7. Visit Mountain Jack (Map 43) → Learn about Necromancer
            8. Visit Necromancer (Map 41) → Give soul
            9. Return to Boatman → Get ship
            10. Travel river to Castle Island (Map 32)
            11. Defeat ZARGON!
            """.trimIndent()
        )

        SectionHeader("TERRAIN LEGEND")
        HintText(
            """
            T/t = Trees (not walkable)
            R/r = Rocks (not walkable)
            w = Deep Water (blocked even with ship)
            4 = Shallow Water (requires ship)
            1 = Grass (walkable, encounters)
            2 = Sand (walkable, encounters)
            0 = Floor (walkable, fewer encounters)

            h = Hut (NPC location)
            H = Healer
            W = Weapon Shop
            C = Castle
            """.trimIndent()
        )

        SectionHeader("TIPS")
        HintText(
            """
            • Use SEARCH command when near item locations
            • Save at Healers or Fountains frequently
            • Ship only works on shallow water (4), not deep (w)
            • Level up before exploring dangerous areas
            • Stock up on potions from Weapon Shop
            • Check Quest Progress in menu to track items
            """.trimIndent()
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = Color(0xFF4CAF50),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun HintText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White,
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun QuestItem(name: String, map: String, coords: String, description: String) {
    Text(
        text = "• $name",
        style = MaterialTheme.typography.bodyLarge,
        color = Color(0xFFFFD700),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
    Text(
        text = "  Location: $map at $coords",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFFCCCCCC),
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp
    )
    Text(
        text = "  $description",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF999999),
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun LocationItem(name: String, location: String, notes: String) {
    Text(
        text = "• $name - $location",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF2196F3),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp)
    )
    Text(
        text = "  $notes",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF999999)
    )
}

@Composable
private fun NpcItem(name: String, location: String, info: String, requirement: String) {
    Text(
        text = "• $name - $location",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFFFF9800),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp)
    )
    Text(
        text = "  $info",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFFCCCCCC)
    )
    Text(
        text = "  Available: $requirement",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF999999)
    )
}
