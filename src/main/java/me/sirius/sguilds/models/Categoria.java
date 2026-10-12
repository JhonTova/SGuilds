package me.sirius.sguilds.models;

import java.util.Map;
import java.util.Set;

public record Categoria(
        String id,
        String nombre,
        boolean todosLosMundos,
        Set<String> mundos,
        String mensajeBloqueado,
        Map<Integer, TierData> tiers
) {

    // Los nombres de mundo llegan ya normalizados a minúsculas desde el ConfigManager.
    public boolean aplicaEn(String mundo) {
        return todosLosMundos || mundos.contains(mundo);
    }

    public TierData tier(int num) {
        return tiers.get(num);
    }
}
