package me.sirius.sguilds.listeners;

import me.sirius.sguilds.SGuilds;
import me.sirius.sguilds.managers.DataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {

    private final DataManager data;

    public PlayerListener(SGuilds plugin) {
        this.data = plugin.getDataManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        data.load(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        data.save(event.getPlayer());
    }
}