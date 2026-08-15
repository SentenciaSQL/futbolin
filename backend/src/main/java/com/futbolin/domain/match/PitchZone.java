package com.futbolin.domain.match;

/**
 * Signed field position from the perspective of player A.
 * -2 Goalmouth A (player B attacking)
 * -1 Defense A / Attack B
 *  0 Midfield
 * +1 Attack A / Defense B
 * +2 Goalmouth B (player A attacking)
 */
public enum PitchZone {
    GOAL_A(-2),
    DEFENSE_A(-1),
    MIDFIELD(0),
    DEFENSE_B(1),
    GOAL_B(2);

    private final int position;

    PitchZone(int position) {
        this.position = position;
    }

    public int position() {
        return position;
    }

    public static PitchZone fromPosition(int position) {
        int clamped = Math.max(-2, Math.min(2, position));
        return switch (clamped) {
            case -2 -> GOAL_A;
            case -1 -> DEFENSE_A;
            case 1 -> DEFENSE_B;
            case 2 -> GOAL_B;
            default -> MIDFIELD;
        };
    }
}
