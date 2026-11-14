package com.yogiiefe.inkslayer.screens;

import java.util.List;

/** Linear scenes anchored to campaign checkpoints. */
enum StoryBeat {
    ELEMENTS_START("The city gate", "The inspection queue stretches past the workshops."),
    FIRST_MASK("The fallen mask",
        "Player: You were on the list.",
        "Inspector: So were you. Almost."),
    ELEMENTS_EXIT("Beyond the gate", "The inspector's mask and the list of punishments carry the same number."),
    PETS_START("The sealed district", "A dog waits beside a sealed door. Its bowl still has its owner's name."),
    PETS_MIDDLE("A familiar sound", "The dog barks. The examiner turns toward the house, then lowers its head and reads the order again."),
    PETS_EXIT("The road uphill", "The dog follows the examiner home. You take the road toward the center of the city."),
    WEATHER_START("The storm road", "Rain cuts across the queue. The notice tells everyone to stay in line."),
    WEATHER_MIDDLE("Shelter", "One examiner looks at the crowd, unlocks the shelter, and pockets the key."),
    WEATHER_EXIT("Down to the kitchens", "The shelter keeper points toward the kitchens. Kotoba is checking the food line himself."),
    FOOD_START("The ration hall", "The kitchen is hot. Plates wait behind an inspection desk."),
    FOOD_MIDDLE("The ledger", "Orders, rations, sentences. The same names run down all three columns."),
    KOTOBA("The unchecked name",
        "Kotoba opens the ledger. Your name is there. The inspection box is still empty.",
        "Kotoba: Name?",
        "Player: You had your chance.");

    final String title;
    final List<String> lines;

    StoryBeat(String title, String... lines) {
        this.title = title;
        this.lines = List.of(lines);
    }

    static StoryBeat arrival(int stage, int encounter) {
        if (encounter == 0)
            return switch (stage) {
                case 0 -> ELEMENTS_START;
                case 1 -> PETS_START;
                case 2 -> WEATHER_START;
                case 3 -> FOOD_START;
                default -> throw new IllegalArgumentException("Invalid stage");
            };
        if (encounter != 5) return null;
        return switch (stage) {
            case 1 -> PETS_MIDDLE;
            case 2 -> WEATHER_MIDDLE;
            case 3 -> FOOD_MIDDLE;
            default -> null;
        };
    }

    static StoryBeat victory(int stage, int encounter) {
        if (stage == 0 && encounter == 0) return FIRST_MASK;
        if (encounter != 9) return null;
        return switch (stage) {
            case 0 -> ELEMENTS_EXIT;
            case 1 -> PETS_EXIT;
            case 2 -> WEATHER_EXIT;
            default -> null;
        };
    }
}
