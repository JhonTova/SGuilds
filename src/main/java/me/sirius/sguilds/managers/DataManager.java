package me.sirius.sguilds.managers;

import me.sirius.sguilds.SGuilds;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class DataManager {

    private static final int CULTIVOS  = 0;
    private static final int MINERALES = 1;
    private static final int MADERAS   = 2;

    private final SGuilds plugin;
    private final File dataFile;
    private final Map<UUID, int[]> cache = new ConcurrentHashMap<>();

    public DataManager(SGuilds plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data/player_data.yml");
        dataFile.getParentFile().mkdirs();
    }

    public void load(Player player) {
        UUID uuid = player.getUniqueId();
        if (cache.containsKey(uuid)) return;

        YamlConfiguration yml = YamlConfiguration.loadConfiguration(dataFile);
        String path = uuid.toString();

        int[] counters = new int[]{
            yml.getInt(path + ".cultivos",  0),
            yml.getInt(path + ".minerales", 0),
            yml.getInt(path + ".maderas",   0)
        };
        cache.put(uuid, counters);
    }

    public void save(Player player) {
        UUID uuid = player.getUniqueId();
        int[] counters = cache.remove(uuid);
        if (counters == null) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(dataFile);
            String path = uuid.toString();
            yml.set(path + ".cultivos",  counters[CULTIVOS]);
            yml.set(path + ".minerales", counters[MINERALES]);
            yml.set(path + ".maderas",   counters[MADERAS]);
            try {
                yml.save(dataFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Error al guardar datos de " + player.getName(), e);
            }
        });
    }

    public void saveAll() {
        if (cache.isEmpty()) return;
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(dataFile);
        cache.forEach((uuid, counters) -> {
            String path = uuid.toString();
            yml.set(path + ".cultivos",  counters[CULTIVOS]);
            yml.set(path + ".minerales", counters[MINERALES]);
            yml.set(path + ".maderas",   counters[MADERAS]);
        });
        try {
            yml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al guardar todos los datos de farmeo", e);
        }
    }

    public int getCultivos(UUID uuid)  { return get(uuid, CULTIVOS); }
    public int getMinerales(UUID uuid) { return get(uuid, MINERALES); }
    public int getMaderas(UUID uuid)   { return get(uuid, MADERAS); }

    public void addCultivo(UUID uuid)  { add(uuid, CULTIVOS); }
    public void addMineral(UUID uuid)  { add(uuid, MINERALES); }
    public void addMadera(UUID uuid)   { add(uuid, MADERAS); }

    public void resetCultivos(UUID uuid)  { set(uuid, CULTIVOS, 0); }
    public void resetMinerales(UUID uuid) { set(uuid, MINERALES, 0); }
    public void resetMaderas(UUID uuid)   { set(uuid, MADERAS, 0); }

    public void resetAll(UUID uuid) {
        int[] c = cache.get(uuid);
        if (c != null) { c[0] = 0; c[1] = 0; c[2] = 0; }
    }

    private int[] getOrCreate(UUID uuid) {
        return cache.computeIfAbsent(uuid, k -> new int[3]);
    }

    private int get(UUID uuid, int idx) {
        int[] c = cache.get(uuid);
        return c != null ? c[idx] : 0;
    }

    private void add(UUID uuid, int idx) {
        getOrCreate(uuid)[idx]++;
    }

    private void set(UUID uuid, int idx, int val) {
        getOrCreate(uuid)[idx] = val;
    }
}