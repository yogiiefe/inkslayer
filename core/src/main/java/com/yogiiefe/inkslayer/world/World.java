package com.yogiiefe.inkslayer.world;

/** Horizontal exploration and a single fixed-tick, turn-based encounter. */
public final class World {
    public static final double STEP = 1.0 / 60;
    public static final int MAX_HEALTH = 3;
    public enum Phase { WALK, PLAYER_TURN, MONSTER_TURN, WON, LOST }

    public final GameContent content;
    public double x;
    public int facing, health, enemyHealth;
    public Phase phase;
    private int counterTicks;

    public World(GameContent content) {
        this.content = content;
        reset();
    }

    public void reset() {
        x = content.spawnX;
        facing = 1;
        health = MAX_HEALTH;
        enemyHealth = content.enemyHealth;
        phase = Phase.WALK;
        counterTicks = 0;
    }

    public void tick(int move) {
        switch (phase) {
            case WALK -> {
                if (move != 0) facing = move;
                x = Math.clamp(x + move * 155 * STEP, 0, content.enemyX - 180);
                if (x >= content.enemyX - 180) {
                    facing = 1;
                    phase = Phase.PLAYER_TURN;
                }
            }
            case MONSTER_TURN -> {
                if (--counterTicks == 0) {
                    health--;
                    phase = health == 0 ? Phase.LOST : Phase.PLAYER_TURN;
                }
            }
            default -> {}
        }
    }

    public boolean cast() {
        if (phase != Phase.PLAYER_TURN) return false;
        enemyHealth = Math.max(0, enemyHealth - content.damage("WATER", "FIRE"));
        if (enemyHealth == 0) {
            phase = Phase.WON;
        } else {
            counterTicks = 45;
            phase = Phase.MONSTER_TURN;
        }
        return true;
    }
}
