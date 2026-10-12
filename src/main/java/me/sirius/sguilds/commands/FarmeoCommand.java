package me.sirius.sguilds.commands;

import me.sirius.core.text.Text;
import me.sirius.sguilds.SGuilds;
import me.sirius.sguilds.managers.ConfigManager;
import me.sirius.sguilds.managers.DataManager;
import me.sirius.sguilds.models.Categoria;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class FarmeoCommand implements CommandExecutor, TabCompleter {

    private final ConfigManager config;
    private final DataManager data;

    public FarmeoCommand(SGuilds plugin) {
        this.config = plugin.getConfigManager();
        this.data   = plugin.getDataManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!sender.hasPermission(config.getPermisoAdmin())) {
            Text.send(sender, config.getMensaje("sin-permiso"));
            return true;
        }

        if (args.length == 0) {
            Text.send(sender, config.getMensaje("uso-comando"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "stats"  -> handleStats(sender, args);
            case "reset"  -> handleReset(sender, args);
            default       -> Text.send(sender, config.getMensaje("uso-comando"));
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        config.reload();
        Text.send(sender, config.getMensaje("reload-ok"));
    }

    private void handleStats(CommandSender sender, String[] args) {
        Player target = resolveTarget(sender, args);
        if (target == null) return;

        UUID uuid = target.getUniqueId();
        Map<String, Integer> contadores = data.getContadores(uuid);

        Text.send(sender, config.getMensaje("stats-titulo").replace("{player}", target.getName()));
        String formato = config.getMensaje("stats-linea");
        for (Categoria categoria : config.getCategorias()) {
            Text.send(sender, formato
                    .replace("{categoria}", categoria.nombre())
                    .replace("{valor}", String.valueOf(contadores.getOrDefault(categoria.id(), 0))));
        }
    }

    private void handleReset(CommandSender sender, String[] args) {
        Player target = resolveTarget(sender, args);
        if (target == null) return;

        data.resetAll(target.getUniqueId());
        Text.send(sender, config.getMensaje("reset-ok").replace("{player}", target.getName()));
    }

    @Nullable
    private Player resolveTarget(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            Player p = Bukkit.getPlayerExact(args[1]);
            if (p == null) {
                Text.send(sender, config.getMensaje("jugador-no-encontrado"));
                return null;
            }
            return p;
        }
        if (sender instanceof Player p) return p;
        Text.send(sender, config.getMensaje("especifica-jugador"));
        return null;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String[] args) {
        if (!sender.hasPermission(config.getPermisoAdmin())) return List.of();

        if (args.length == 1) {
            return List.of("reload", "stats", "reset").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("reset"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
