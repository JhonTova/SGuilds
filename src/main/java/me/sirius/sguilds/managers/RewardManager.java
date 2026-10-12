package me.sirius.sguilds.managers;

import me.sirius.core.text.Text;
import me.sirius.sguilds.SGuilds;
import me.sirius.sguilds.models.Categoria;
import me.sirius.sguilds.models.Recompensa;
import me.sirius.sguilds.models.Recompensa.Opcion;
import me.sirius.sguilds.models.TierData;
import me.sirius.sguilds.models.TierData.RewardEntry;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class RewardManager {

    private final SGuilds plugin;
    private final ConfigManager config;

    public RewardManager(SGuilds plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void evaluar(Player player, Categoria categoria, TierData tier) {
        List<RewardEntry> entradas = tier.recompensas();
        if (entradas.isEmpty()) return;

        double roll = ThreadLocalRandom.current().nextDouble(100.0);
        double acumulado = 0.0;

        for (RewardEntry entrada : entradas) {
            acumulado += entrada.chance();
            if (roll < acumulado) {
                entregar(player, categoria, tier, entrada.raridad());
                return;
            }
        }
    }

    private void entregar(Player player, Categoria categoria, TierData tier, String raridad) {
        Recompensa recompensa = config.getRecompensa(raridad);
        if (recompensa == null || recompensa.opciones().isEmpty()) return;

        List<Opcion> opciones = recompensa.opciones();
        Opcion opcion = opciones.get(ThreadLocalRandom.current().nextInt(opciones.size()));

        Map<String, String> vars = Map.of(
                "player",    player.getName(),
                "categoria", categoria.nombre(),
                "tier",      String.valueOf(tier.tier()),
                "raridad",   raridad
        );

        List<String> comandos = opcion.comandos().stream()
                .map(cmd -> reemplazar(cmd, vars))
                .toList();

        if (!comandos.isEmpty()) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                var consola = plugin.getServer().getConsoleSender();
                comandos.forEach(cmd -> plugin.getServer().dispatchCommand(consola, cmd));
            });
        }

        enviarMensaje(player, reemplazar(opcion.mensaje(), vars));
    }

    // El YAML puede traer saltos reales (comillas dobles) o el literal \n; se aceptan ambos.
    private void enviarMensaje(Player player, String mensaje) {
        if (mensaje.isBlank()) return;
        for (String linea : mensaje.split("\\\\n|\\R")) {
            Text.send(player, linea);
        }
    }

    private static String reemplazar(String texto, Map<String, String> vars) {
        String out = texto;
        for (var entrada : vars.entrySet()) {
            out = out.replace("{" + entrada.getKey() + "}", entrada.getValue());
        }
        return out;
    }
}
