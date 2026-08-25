package me.sirius.sguilds.listeners;

import me.sirius.sguilds.SGuilds;
import me.sirius.sguilds.managers.BlockRegistry;
import me.sirius.sguilds.managers.BlockRegistry.BlockEntry;
import me.sirius.sguilds.managers.BlockRegistry.Sistema;
import me.sirius.sguilds.managers.ConfigManager;
import me.sirius.sguilds.managers.DataManager;
import me.sirius.sguilds.managers.RewardManager;
import me.sirius.sguilds.models.TierData;
import me.sirius.core.text.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.UUID;

public final class FarmeoListener implements Listener {

    private final ConfigManager config;
    private final DataManager data;
    private final RewardManager rewards;

    public FarmeoListener(SGuilds plugin) {
        this.config  = plugin.getConfigManager();
        this.data    = plugin.getDataManager();
        this.rewards = plugin.getRewardManager();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getBlock().getWorld().getName().equals(config.getMundo())) return;

        BlockEntry entry = BlockRegistry.get(event.getBlock().getType());
        if (entry == null) return;

        Player player = event.getPlayer();
        int tier = entry.tier();
        Sistema sistema = entry.sistema();

        if (tier > 0) {
            TierData tierData = config.getTier(tier);
            if (tierData == null) return;

            if (!tieneAcceso(player, tier, tierData.requiereVip2())) {
                event.setCancelled(true);
                sendMensajeBloqueado(player, sistema, tierData.requiereVip2());
                return;
            }
        }

        UUID uuid = player.getUniqueId();
        TierData tierData = config.getTier(tier);
        if (tierData == null) return;

        switch (sistema) {
            case CULTIVOS  -> {
                data.addCultivo(uuid);
                int cnt = data.getCultivos(uuid);
                if (cnt >= tierData.umbral()) {
                    rewards.evaluar(player, tierData, cnt);
                    data.resetCultivos(uuid);
                }
            }
            case MINERALES -> {
                data.addMineral(uuid);
                int cnt = data.getMinerales(uuid);
                if (cnt >= tierData.umbral()) {
                    rewards.evaluar(player, tierData, cnt);
                    data.resetMinerales(uuid);
                }
            }
            case MADERAS   -> {
                data.addMadera(uuid);
                int cnt = data.getMaderas(uuid);
                if (cnt >= tierData.umbral()) {
                    rewards.evaluar(player, tierData, cnt);
                    data.resetMaderas(uuid);
                }
            }
        }
    }

    private boolean tieneAcceso(Player player, int tier, boolean requiereVip2) {
        if (player.hasPermission("farmeo.tier" + tier)) return true;
        if (player.hasPermission("farmeo.vip2")) return true;
        if (!requiereVip2 && player.hasPermission("farmeo.vip")) return true;
        return false;
    }

    private void sendMensajeBloqueado(Player player, Sistema sistema, boolean requiereVip2) {
        String msgBloqueo = switch (sistema) {
            case CULTIVOS  -> config.getMsgCultivoBloqueado();
            case MINERALES -> config.getMsgMineralBloqueado();
            case MADERAS   -> config.getMsgMaderaBloqueada();
        };
        String msgExtra = requiereVip2
                ? config.getMsgRequiereVip2()
                : config.getMsgRequiereRango();

        Text.send(player, msgBloqueo);
        Text.send(player, msgExtra);
    }
}