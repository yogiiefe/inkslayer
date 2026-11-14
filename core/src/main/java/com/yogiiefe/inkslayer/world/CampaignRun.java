package com.yogiiefe.inkslayer.world;

import java.util.List;

/** Campaign position and combat rules, independent of input, rendering and persistence. */
public final class CampaignRun {
    public enum Phase { WALK, LESSON, ATTACK, DEFEND, WON, LOST }

    public static final double STEP = 1.0 / 60;
    public static final double ENCOUNTER_X = 530;
    public static final int MAX_HP = 30;
    public final CampaignContent content;
    public int stage, encounter, round, hp, monsterHp;
    public int facing;
    public double x;
    public Phase phase;
    private int attackIndex, openingAttacks;

    public CampaignRun(CampaignContent content) {
        this.content = content;
        restore(0, 0);
    }

    public void restore(int stage, int encounter) {
        this.stage = stage;
        this.encounter = encounter;
        round = 0;
        hp = MAX_HP;
        monsterHp = enemy().health();
        x = 110;
        facing = 1;
        attackIndex = 0;
        openingAttacks = 0;
        phase = Phase.WALK;
    }

    public CampaignContent.Encounter enemy() {
        return content.stages.get(stage).encounters().get(encounter);
    }

    public boolean isBoss() { return enemy().id().equals(CampaignContent.BOSS_ID); }

    public void completeQuizEncounter() {
        if (phase != Phase.LESSON) return;
        monsterHp = 0;
        phase = Phase.WON;
    }
    public boolean isFinalEncounter() { return isBoss() && phase == Phase.WON; }

    public void tick(int direction) {
        if (phase != Phase.WALK) return;
        if (direction != 0) facing = direction;
        x = Math.clamp(x + direction * 155 * STEP, 35, ENCOUNTER_X);
        if (x == ENCOUNTER_X) {
            facing = 1;
            phase = Phase.LESSON;
        }
    }

    public void finishLesson() {
        if (phase != Phase.LESSON) return;
        if (isBoss()) {
            openingAttacks = content.bossAttacks.get(round).size();
            phase = Phase.DEFEND;
        } else {
            phase = Phase.ATTACK;
        }
    }

    public String incoming() {
        if (!isBoss()) {
            var attacks = enemy().attacks();
            return attacks.get(attackIndex % attacks.size());
        }
        List<String> attacks = content.bossAttacks.get(round);
        return attacks.get(Math.min(attackIndex, attacks.size() - 1));
    }

    public static int damage(String attack, String target) {
        if (!CampaignContent.GLYPHS.containsKey(attack) || !CampaignContent.GLYPHS.containsKey(target))
            throw new IllegalArgumentException("Unknown element");
        if (attack.equals(CampaignContent.COUNTERS.get(target))) return 20;
        if (attack.equals(target) || target.equals(CampaignContent.COUNTERS.get(attack))) return 5;
        return 10;
    }

    public int attack(String element) {
        if (phase != Phase.ATTACK) return 0;
        int hit = Math.min(monsterHp, damage(element, enemy().element()));
        monsterHp -= hit;
        if (monsterHp > 0) {
            phase = Phase.DEFEND;
        } else if (isBoss() && round < 2) {
            round++;
            attackIndex = 0;
            monsterHp = enemy().health();
            phase = Phase.LESSON;
        } else {
            phase = Phase.WON;
        }
        return hit;
    }

    /** Null means Guard. Only the announced attack is resolved. */
    public int defend(String element) {
        if (phase != Phase.DEFEND) return 0;
        if (element != null && !CampaignContent.GLYPHS.containsKey(element))
            throw new IllegalArgumentException("Unknown element");
        int damage = enemy().damage();
        if (CampaignContent.COUNTERS.get(incoming()).equals(element)) damage = 0;
        else if (element == null) damage = (damage + 1) / 2;
        hp = Math.max(0, hp - damage);
        attackIndex++;
        if (openingAttacks > 0) openingAttacks--;
        if (hp == 0) phase = Phase.LOST;
        else phase = openingAttacks > 0 ? Phase.DEFEND : Phase.ATTACK;
        return damage;
    }

    public void advance() {
        if (phase != Phase.WON || isBoss()) return;
        if (encounter + 1 < content.stages.get(stage).encounters().size()) {
            restore(stage, encounter + 1);
        } else {
            restore(stage + 1, 0);
        }
    }
}
