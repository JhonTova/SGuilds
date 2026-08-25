package me.sirius.sguilds.managers;

import me.sirius.sguilds.SGuilds;
import me.sirius.sguilds.models.TierData;
import me.sirius.sguilds.models.TierData.Raridad;
import me.sirius.sguilds.models.TierData.RewardEntry;
import me.sirius.core.text.Text;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class RewardManager {

    private final SGuilds plugin;
    private final ConfigManager config;

    public RewardManager(SGuilds plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void evaluar(Player player, TierData tier, int contador) {
        if (contador < tier.umbral()) return;

        List<RewardEntry> recompensas = tier.recompensas();
        if (recompensas.isEmpty()) return;

        double roll = ThreadLocalRandom.current().nextDouble(0, 100);
        double acumulado = 0.0;

        for (RewardEntry entry : recompensas) {
            acumulado += entry.chance();
            if (roll < acumulado) {
                entregar(player, entry.raridad());
                return;
            }
        }
    }

    private void entregar(Player player, Raridad raridad) {
        String[] comandos = config.getComandos(raridad);
        String[] mensajes = config.getMensajes(raridad);

        int idx = ThreadLocalRandom.current().nextInt(3);

        String comando = comandos[idx].replace("{player}", player.getName());
        String mensaje = mensajes[idx];

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                comando
            );
        });

        if (!mensaje.isBlank()) {
            String[] lineas = mensaje.split("\\\\n");
            for (String linea : lineas) {
                String parsed = linea.replace("&l", "<bold>")
                                     .replace("&r", "<reset>")
                                     .replace("&7", "<gray>")
                                     .replace("&c", "<red>")
                                     .replace("&e", "<yellow>")
                                     .replace("&b", "<aqua>")
                                     .replace("&d", "<light_purple>")
                                     .replace("&f", "<white>")
                                     .replace("&a", "<green>");
                Text.send(player, parsed);
            }
        }
    }
}