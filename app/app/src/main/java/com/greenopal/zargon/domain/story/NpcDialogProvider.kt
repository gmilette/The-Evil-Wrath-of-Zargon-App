package com.greenopal.zargon.domain.story

import com.greenopal.zargon.data.models.Dialog
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.data.models.NpcType
import com.greenopal.zargon.data.models.StoryAction
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides NPC dialogs based on story progression
 * Based on QBASIC hut interaction code (ZARGON.BAS:1860-1960)
 */
@Singleton
class NpcDialogProvider @Inject constructor() {

    /**
     * Get dialog for an NPC based on current game state
     */
    fun getDialog(npcType: NpcType, gameState: GameState): Dialog {
        return when (npcType) {
            NpcType.BOATMAN -> getBoatmanDialog(gameState)
            NpcType.SANDMAN -> getSandmanDialog(gameState)
            NpcType.NECROMANCER -> getNecromancerDialog(gameState)
            NpcType.MOUNTAIN_JACK -> getMountainJackDialog(gameState)
            NpcType.OLD_MAN -> getOldManDialog(gameState)
            NpcType.FOUNTAIN -> getFountainDialog()
            else -> Dialog("", "No one is here", "", "", "", "")
        }
    }

    /**
     * Boatman dialog (ZARGON.BAS:1863-1904)
     */
    private fun getBoatmanDialog(gameState: GameState): Dialog {
        val status = gameState.storyStatus
        val hasWood = gameState.hasItem("wood")

        return when {
            // Stage 1: Trapped boatman
            status < 2f -> {
                if (status == 1.5f) {
                    // Player has dynamite - show option to rescue him
                    Dialog(
                        question1 = "what happened??",
                        answer1 = "monsters attacked me, but their mage blasted down these rocks...now i am stuck, please help!!!",
                        question2 = "how can i help?",
                        answer2 = "blast me out of here!!.. use the dynamite, the sandman knows where it is, i think..",
                        question3 = "(use the dynamite)",
                        answer3 = "**BOOM!** The rocks explode! The boatman is free! 'Thank you!' he says. 'I can help you escape this land - I'm a boat master!'",
                        storyAction = StoryAction.MultiAction(listOf(
                            StoryAction.TakeItem("dynomite"),
                            StoryAction.AdvanceStory(2.0f)
                        ))
                    )
                } else {
                    // Player doesn't have dynamite yet
                    Dialog(
                        question1 = "what happened??",
                        answer1 = "monsters attacked me, but their mage blasted down these rocks...now i am stuck, please help!!!",
                        question2 = "how can i help?",
                        answer2 = "blast me out of here!!.. use the dynamite, the sandman knows where it is, i think..",
                        question3 = "why should i help you?",
                        answer3 = "because i am a boat master.. i can build you a boat!!!! ooo ouch! help!"
                    )
                }
            }

            // Stage 2: Freed boatman, needs materials
            status >= 2f && status < 3f -> {
                if (status == 2.5f && hasWood) {
                    Dialog(
                        question1 = "how can we get out of here?",
                        answer1 = "well there used to be a warper, in the old castle, but it is currently overrun by monsters, the island is the only way out",
                        question2 = "how can we get to this island?",
                        answer2 = "duh? i am a boatmaker, i guess i could make you a boat seeing that you saved my life and all.. i'll need some materials though..",
                        question3 = "(give him the wood)",
                        answer3 = "ahh yes thankyou, i will build you the boat soon enough, but i am tired, and i need to get some rest, so please excuse me, oh here is what i need (he gives you the boat plans)",
                        storyAction = StoryAction.GiveItem("boat plans")
                    )
                } else {
                    Dialog(
                        question1 = "how can we get out of here?",
                        answer1 = "well there used to be a warper, in the old castle, but it is currently overrun by monsters, the island is the only way out",
                        question2 = "how can we get to this island?",
                        answer2 = "duh? i am a boatmaker, i guess i could make you a boat seeing that you saved my life and all.. i'll need some materials though..",
                        question3 = "what do you need and where do i find it?",
                        answer3 = "i'll need wood first, but not any old wood..it has to be nearly dead and dried. Almost as if the sand had blown its strength into it"
                    )
                }
            }

            // Stage 3-4: Dead boatman
            status >= 3f && status < 5f -> {
                Dialog(
                    question1 = "what happened!?!, I got you your stinkin' items!!! I WANT MY SHIP!!!",
                    answer1 = "he isnt moving",
                    question2 = "(poke at him/wake him up)",
                    answer2 = "you poke him, but he doesnt move, you push him over and a drop of blood escapes from his mouth, he's dead",
                    question3 = if (status >= 4f) "kick dirt in the deadman's face" else "search him",
                    answer3 = if (status >= 4f) "nope, he still does not move" else "in his left hand, you find the plans for the ship...you pick them up and decide to read them later (hit I then choose them)"
                )
            }

            // Stage 5+: Resurrected boatman
            status >= 5f && status < 6f -> {
                if (status == 5.5f) {
                    Dialog(
                        question1 = "hello?",
                        answer1 = "yes? what do you want?",
                        question2 = "cool, how do i use it?",
                        answer2 = "just walk into the ship, and you will be able to travel the river",
                        question3 = "are you ok now",
                        answer3 = "yes i am fine"
                    )
                } else {
                    Dialog(
                        question1 = "hello?",
                        answer1 = "yes? what do you want?",
                        question2 = "now, can you build the ship",
                        answer2 = "yes..give me the plans. i will build your ship",
                        question3 = "are you ok now",
                        answer3 = "yes i am fine",
                        storyAction = if (gameState.hasItem("boat plans")) StoryAction.BuildBoat else null
                    )
                }
            }

            else -> Dialog("", "No one is here", "", "", "", "")
        }
    }

    /**
     * Sandman dialog (ZARGON.BAS:1906-1936)
     */
    private fun getSandmanDialog(gameState: GameState): Dialog {
        val status = gameState.storyStatus

        return when {
            // Early game
            status < 3f -> {
                if (status == 1f) {
                    // Beginning - player can ask about dynamite
                    Dialog(
                        question1 = "how can i escape?",
                        answer1 = "the only way i know of is through the river",
                        question2 = "what's up with this place?",
                        answer2 = "This is the land of Gef. The great river splits the forest of the land from the rocks and mountains. In the middle of the island is the castle where the elves used to live, i have no knowledge of what is going on there now",
                        question3 = "(ask him about the dynamite)",
                        answer3 = "the dynamite? hmm.. it lies on the east side of the TWO GREAT ROCKS OF THE UNIVERSE, but beware, monsters tend to spurt out, be careful, may the sands be with u",
                        storyAction = StoryAction.AdvanceStory(1.5f)
                    )
                } else {
                    // After learning about dynamite
                    Dialog(
                        question1 = "how can i escape?",
                        answer1 = "the only way i know of is through the river",
                        question2 = "what's up with this place?",
                        answer2 = "This is the land of Gef. The great river splits the forest of the land from the rocks and mountains. In the middle of the island is the castle where the elves used to live, i have no knowledge of what is going on there now",
                        question3 = "do you dig the sand?",
                        answer3 = "yea it rules man."
                    )
                }
            }

            // Mid game: collecting boat materials
            status >= 3f && status < 4f -> {
                Dialog(
                    question1 = "where is the cloth, oh wisest man of the sands?",
                    answer1 = "it is, my friend, where the lion slashed the trees, and left the man's cloth",
                    question2 = "where is the wood so that we may construct a ship",
                    answer2 = "the wood lies, in between the two great forests, their strong limbs will help you along",
                    question3 = "where is the rutter?",
                    answer3 = "i am afraid it is too obvious, it is right under you're nose..where the rocks of the east meet with the trees of the west"
                )
            }

            // Late game: need necromancer
            status >= 4f && status < 5f -> {
                Dialog(
                    question1 = "how can i get off this darn island?",
                    answer1 = "well, the only way is to go to the castle by boat..",
                    question2 = "the boatman is dead! WHAT DO I DO NOW?",
                    answer2 = "bring him back from the dead!",
                    question3 = "HUH?",
                    answer3 = "i know of a man, who can help you. He lives in the northeast. Its dangerous though, he lives in a graveyard, this man is a NECROMANCER!!!!"
                )
            }

            else -> {
                Dialog(
                    question1 = "how can i escape?",
                    answer1 = "the only way i know of is through the river",
                    question2 = "what's up with this place?",
                    answer2 = "This is the land of Gef. The great river splits the forest of the land from the rocks and mountains.",
                    question3 = "do you dig the sand?",
                    answer3 = "yea it rules man."
                )
            }
        }
    }

    /**
     * Necromancer dialog (ZARGON.BAS:1938-1959)
     */
    private fun getNecromancerDialog(gameState: GameState): Dialog {
        val status = gameState.storyStatus
        val hasSoul = gameState.hasItem("soul")

        return when {
            status < 4f || status >= 5f -> {
                Dialog(
                    question1 = "",
                    answer1 = "there is no one here",
                    question2 = "",
                    answer2 = "there is no one here",
                    question3 = "",
                    answer3 = "there is no one here"
                )
            }

            status >= 4f && status < 5f -> {
                if (status == 4.3f && hasSoul) {
                    Dialog(
                        question1 = "hello mr. necromancer, i have to ask you for something",
                        answer1 = "WHAT DO YOU WANT? YOU PATHETIC MORTALS?",
                        question2 = "well sir(gulp) the boatman died and i was told you could bring him back.",
                        answer2 = "WHAT?.. you are asking quite a favor, you know how many pathetic creatures come to me and ask me that same question? Well i am a nice guy, so OCCASIONALLY i will do it, but ya see i aint gunna do it no more, it has made me too weak. NO.. the answer is NO!",
                        question3 = "(give him the soul)",
                        answer3 = "ahhhhh yes, fresh blood...there, i will restore your friend..hopefully he'll do the same for you when you die MAHAHAHAHAAA!!",
                        storyAction = StoryAction.ResurrectBoatman
                    )
                } else {
                    Dialog(
                        question1 = "hello mr. necromancer, i have to ask you for something",
                        answer1 = "WHAT DO YOU WANT? YOU PATHETIC MORTALS?",
                        question2 = "well sir(gulp) the boatman died and i was told you could bring him back.",
                        answer2 = "WHAT?.. you are asking quite a favor, you know how many pathetic creatures come to me and ask me that same question? Well i am a nice guy, so OCCASIONALLY i will do it, but ya see i aint gunna do it no more, it has made me too weak. NO.. the answer is NO!",
                        question3 = "well, perhaps we can offer you something",
                        answer3 = "well i need a soul it takes a toll on my body's whole. you see everytime i use my black arts. i grow weaker, my soul is drained, if you could get me another soul....perhaps we could talk again"
                    )
                }
            }

            else -> Dialog("", "No one is here", "", "", "", "")
        }
    }

    /**
     * Mountain Jack dialog (ZARGON.BAS:1962-1980)
     */
    private fun getMountainJackDialog(gameState: GameState): Dialog {
        val status = gameState.storyStatus

        return when {
            status < 4f -> {
                Dialog(
                    question1 = "",
                    answer1 = "there is no one here",
                    question2 = "",
                    answer2 = "",
                    question3 = "",
                    answer3 = ""
                )
            }
            status >= 4f -> {
                Dialog(
                    question1 = "hey old man",
                    answer1 = "wha?... who you callin old man, get away from me",
                    question2 = "please help me, i need a trapped soul",
                    answer2 = "a trapped soul!?! you're crazy kid...ok fine, i do know where one is, if you just get away from me. travel to the north east, there is a graveyard, an evil graveyard. There you will find what you seek",
                    question3 = "that's it?",
                    answer3 = "yep."
                )
            }
            else -> Dialog("", "No one is here", "", "", "", "")
        }
    }

    /**
     * Old Man dialog (ZARGON.BAS:1998-2004)
     */
    private fun getOldManDialog(gameState: GameState): Dialog {
        return Dialog(
            question1 = "hello old man",
            answer1 = "hello traveler, how may i help you?",
            question2 = "where am i?",
            answer2 = "this is the land of gef. you seek the castle i presume?",
            question3 = "yes, how do i get there?",
            answer3 = "well, you can take the long way, down by the river, or you can take my airship, but first you must play me in a game (play the old man's color matching game for the airship)"
        )
    }

    /**
     * Fountain dialog (ZARGON.BAS:1487-1540)
     */
    private fun getFountainDialog(): Dialog {
        return Dialog(
            question1 = "drink from fountain",
            answer1 = "You drink the cool refreshing water. You feel revitalized! HP and MP restored!",
            action1 = StoryAction.HealPlayer,
            question2 = "bathe in fountain",
            answer2 = "You bathe in the fountain. The water cleanses your wounds! HP and MP restored!",
            action2 = StoryAction.HealPlayer,
            question3 = "leave",
            answer3 = "You walk away from the fountain feeling refreshed."
        )
    }
}
