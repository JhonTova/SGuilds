package me.sirius.sguilds.models;

import java.util.List;

public record TierData(
        int tier,
        int umbral,
        boolean requierePermiso,
        boolean requiereVip2,
        List<RewardEntry> recompensas
) {

    public record RewardEntry(String raridad, double chance) {}
}
