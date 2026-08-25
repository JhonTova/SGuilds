package me.sirius.sguilds.commands;

import me.sirius.core.text.Text;
import me.sirius.sguilds.SGuilds;
import me.sirius.sguilds.managers.ConfigManager;
import me.sirius.sguilds.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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

        if (!sender.hasPermission("sguilds.admin")) {
            Text.send(sender, config.getMsgSinPermiso());
            return true;
        }

        if (args.length == 0) {
            Text.send(sender, config.getMsgUsoComando());
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "stats"  -> handleStats(sender, args);
            case "reset"  -> handleReset(sender, args);
            default       -> Text.send(sender, config.getMsgUsoComando());
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        config.reload();
        Text.send(sender, config.getMsgReloadOk());
    }

    private void handleStats(CommandSender sender, String[] args) {
        Player target = resolveTarget(sender, args);
        if (target == null) return;

        UUID uuid = target.getUniqueId();
        Text.send(sender, "<#FF1493><bold>SGuilds</bold> <gray>— Stats de <white>" + target.getName());
        Text.send(sender, "<gray>  Cultivos : <white>" + data.getCultivos(uuid));
        Text.send(sender, "<gray>  Minerales: <white>" + data.getMinerales(uuid));
        Text.send(sender, "<gray>  Maderas  : <white>" + data.getMaderas(uuid));
    }

    private void handleReset(CommandSender sender, String[] args) {
        Player target = resolveTarget(sender, args);
        if (target == null) return;

        data.resetAll(target.getUniqueId());
        Text.send(sender, "<green>Contadores de <white>" + target.getName() + "<green> reseteados.");
    }

    @Nullable
    private Player resolveTarget(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            Player p = Bukkit.getPlayerExact(args[1]);
            if (p == null) {
                Text.send(sender, config.getMsgJugadorNoEncontrado());
                return null;
            }
            return p;
        }
        if (sender instanceof Player p) return p;
        Text.send(sender, "<red>Especifica un jugador: /sguilds stats <jugador>");
        return null;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String[] args) {
        if (!sender.hasPermission("sguilds.admin")) return List.of();

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
