package me.sirius.sguilds.listeners;

import me.sirius.core.text.Text;
import me.sirius.sguilds.SGuilds;
import me.sirius.sguilds.managers.ConfigManager;
import me.sirius.sguilds.managers.DataManager;
import me.sirius.sguilds.managers.RewardManager;
import me.sirius.sguilds.models.BlockMatch;
import me.sirius.sguilds.models.Categoria;
import me.sirius.sguilds.models.TierData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public final class FarmeoListener implements Listener {

    private final ConfigManager config;
    private final DataManager data;
    private final RewardManager rewards;

    public FarmeoListener(SGuilds plugin) {
        this.config  = plugin.getConfigManager();
        this.data    = plugin.getDataManager();
        this.rewards = plugin.getRewardManager();
    }

    // Bloqueo por tier. Va en LOW para cancelar antes de que otros plugins
    // hagan trabajo inútil sobre una rotura que no debe ocurrir.
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreakGate(BlockBreakEvent event) {
        BlockMatch match = resolver(event);
        if (match == null) return;

        TierData tier = match.categoria().tier(match.tier());
        if (tier == null || tieneAcceso(event.getPlayer(), match.categoria(), tier)) return;

        event.setCancelled(true);
        Text.send(event.getPlayer(), match.categoria().mensajeBloqueado());
        Text.send(event.getPlayer(), tier.requiereVip2()
                ? config.getMensaje("requiere-vip2")
                : config.getMensaje("requiere-rango"));
    }

    // Conteo y recompensa. Va en MONITOR con ignoreCancelled para que solo sume
    // cuando el bloque se rompe de verdad: si una protección (WorldGuard, claims,
    // etc.) cancela el evento en HIGH/HIGHEST, el jugador ve el bloque romperse en
    // su cliente pero el servidor lo repone, y ahí no debe contar ni dar premio.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakCount(BlockBreakEvent event) {
        BlockMatch match = resolver(event);
        if (match == null) return;

        Categoria categoria = match.categoria();
        TierData tier = categoria.tier(match.tier());
        if (tier == null) return;

        Player player = event.getPlayer();
        if (!tieneAcceso(player, categoria, tier)) return;

        int contador = data.incrementar(player.getUniqueId(), categoria.id());
        if (contador < tier.umbral()) return;

        data.reset(player.getUniqueId(), categoria.id());
        rewards.evaluar(player, categoria, tier);
    }

    private BlockMatch resolver(BlockBreakEvent event) {
        return config.match(event.getBlock().getType(), event.getBlock().getWorld().getName());
    }

    private boolean tieneAcceso(Player player, Categoria categoria, TierData tier) {
        if (!tier.requierePermiso()) return true;
        if (tienePermiso(player, config.getPermisoBypass())) return true;
        if (tienePermiso(player, config.permisoTier(tier.tier()))) return true;
        if (tienePermiso(player, config.permisoCategoriaTier(categoria.id(), tier.tier()))) return true;
        if (tienePermiso(player, config.getPermisoVip2())) return true;
        return !tier.requiereVip2() && tienePermiso(player, config.getPermisoVip());
    }

    private static boolean tienePermiso(Player player, String permiso) {
        return !permiso.isEmpty() && player.hasPermission(permiso);
    }
}
