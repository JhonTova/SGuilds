package me.sirius.sguilds.models;

import java.util.List;

public record TierData(
        int tier,
        int umbral,
        boolean requiereVip2,
        List<RewardEntry> recompensas
) {

    public record RewardEntry(Raridad raridad, double chance) {}

    public enum Raridad {
        COMUN, RARO, ULTRARARO, LEGENDARIO, ULTRALEGENDARIO
    }
}