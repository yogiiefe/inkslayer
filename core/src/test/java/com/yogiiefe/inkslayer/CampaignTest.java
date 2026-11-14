package com.yogiiefe.inkslayer;

import com.yogiiefe.inkslayer.world.CampaignContent;
import com.yogiiefe.inkslayer.world.CampaignRun;
import com.yogiiefe.inkslayer.world.CampaignRun.Phase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CampaignTest {
    private static CampaignContent content;

    @BeforeAll static void load() throws Exception {
        content = new CampaignContent(Files.readString(Path.of(System.getProperty("inkslayer.assets"), "data/campaign.json")));
    }

    @Test void allTwentyFiveDamagePairsFollowTheReferenceCycle() {
        var elements = List.of("WATER", "FIRE", "METAL", "WOOD", "EARTH");
        for (int attack = 0; attack < 5; attack++) {
            for (int target = 0; target < 5; target++) {
                int distance = Math.floorMod(target - attack, 5);
                int expected = switch (distance) {
                    case 1 -> 20;
                    case 0, 4 -> 5;
                    default -> 10;
                };
                assertEquals(expected, CampaignRun.damage(elements.get(attack), elements.get(target)));
            }
        }
    }

    @Test void defenseUsesIncomingAttackAndOnlyResolvesOnce() {
        var run = encounter(0, 5);
        run.finishLesson();
        assertEquals(20, run.attack("WATER"));
        assertEquals(0, run.attack("WATER"));
        assertEquals("FIRE", run.incoming());
        assertEquals((run.enemy().damage() + 1) / 2, run.defend(null));
        assertEquals(0, run.defend(null));
        run.attack("FIRE");
        assertEquals("WOOD", run.incoming());
        int hp = run.hp;
        assertEquals(0, run.defend("METAL"));
        assertEquals(hp, run.hp);
        assertEquals(Phase.ATTACK, run.phase);
    }

    @Test void kotobaRequiresThreeRoundsAndAnnouncesAllFiveElements() {
        var run = encounter(3, 9);
        for (int round = 0; round < 3; round++) {
            assertEquals(Phase.LESSON, run.phase);
            assertEquals(0, run.attack("FIRE"));
            run.finishLesson();
            for (String element : content.bossAttacks.get(round)) {
                assertEquals(Phase.DEFEND, run.phase);
                assertEquals(element, run.incoming());
                assertEquals(0, run.defend(CampaignContent.COUNTERS.get(element)));
            }
            assertEquals(20, run.attack("FIRE"));
        }
        assertTrue(run.isFinalEncounter());
        assertEquals(CampaignRun.MAX_HP, run.hp);
    }

    private CampaignRun encounter(int stage, int position) {
        var run = new CampaignRun(content);
        run.restore(stage, position);
        for (int i = 0; i < 200 && run.phase == Phase.WALK; i++) run.tick(1);
        assertEquals(Phase.LESSON, run.phase);
        return run;
    }
}
