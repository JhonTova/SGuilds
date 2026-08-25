package me.sirius.sguilds.managers;

import me.sirius.sguilds.SGuilds;
import me.sirius.sguilds.models.TierData;
import me.sirius.sguilds.models.TierData.Raridad;
import me.sirius.sguilds.models.TierData.RewardEntry;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class ConfigManager {

    private final SGuilds plugin;

    private String mundo;
    private final Map<Integer, TierData> tiers = new HashMap<>();

    private String msgCultivoBloqueado;
    private String msgMineralBloqueado;
    private String msgMaderaBloqueada;
    private String msgRequiereVip2;
    private String msgRequiereRango;
    private String msgReloadOk;
    private String msgSinPermiso;
    private String msgUsoComando;
    private String msgJugadorNoEncontrado;

    private final Map<Raridad, String[]> comandosRecompensa = new EnumMap<>(Raridad.class);
    private final Map<Raridad, String[]> mensajesRecompensa = new EnumMap<>(Raridad.class);

    public ConfigManager(SGuilds plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        var cfg = plugin.getConfig();

        mundo = cfg.getString("mundo", "gremio");

        var msgs = cfg.getConfigurationSection("mensajes");
        if (msgs != null) {
            msgCultivoBloqueado   = msgs.getString("cultivo-bloqueado", "&cCultivo bloqueado");
            msgMineralBloqueado   = msgs.getString("mineral-bloqueado", "&cMineral bloqueado");
            msgMaderaBloqueada    = msgs.getString("madera-bloqueada",  "&cMadera bloqueada");
            msgRequiereVip2       = msgs.getString("requiere-vip2",     "&cRequieres VIP2");
            msgRequiereRango      = msgs.getString("requiere-rango",    "&cRequieres un rango mayor");
            msgReloadOk           = msgs.getString("reload-ok",         "&aConfiguración recargada.");
            msgSinPermiso         = msgs.getString("sin-permiso",       "&cSin permiso.");
            msgUsoComando         = msgs.getString("uso-comando",        "&7/sguilds <reload|stats|reset>");
            msgJugadorNoEncontrado = msgs.getString("jugador-no-encontrado", "&cJugador no encontrado.");
        }

        loadRecompensas(cfg.getConfigurationSection("recompensas"));

        tiers.clear();
        var tiersSection = cfg.getConfigurationSection("tiers");
        if (tiersSection != null) {
            for (String key : tiersSection.getKeys(false)) {
                try {
                    int num = Integer.parseInt(key);
                    var sec = tiersSection.getConfigurationSection(key);
                    if (sec == null) continue;

                    int umbral = sec.getInt("umbral", 64);
                    boolean vip2 = sec.getBoolean("requiere-vip2", false);

                    List<RewardEntry> entries = new ArrayList<>();
                    var lista = sec.getMapList("recompensas");
                    for (var item : lista) {
                        String rarStr = String.valueOf(item.get("raridad"));
                        double chance = Double.parseDouble(String.valueOf(item.get("chance")));
                        try {
                            entries.add(new RewardEntry(Raridad.valueOf(rarStr), chance));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Raridad inválida '" + rarStr + "' en tier " + num);
                        }
                    }

                    tiers.put(num, new TierData(num, umbral, vip2, entries));
                } catch (NumberFormatException e) {
                    plugin.getLogger().log(Level.WARNING, "Tier inválido: " + key, e);
                }
            }
        }
    }

    private void loadRecompensas(ConfigurationSection sec) {
        if (sec == null) return;
        comandosRecompensa.clear();
        mensajesRecompensa.clear();

        for (Raridad r : Raridad.values()) {
            String key = r.name().toLowerCase();
            var sub = sec.getConfigurationSection(key);
            if (sub == null) continue;

            comandosRecompensa.put(r, new String[]{
                sub.getString("comando-1", ""),
                sub.getString("comando-2", ""),
                sub.getString("comando-3", "")
            });
            mensajesRecompensa.put(r, new String[]{
                sub.getString("mensaje-1", ""),
                sub.getString("mensaje-2", ""),
                sub.getString("mensaje-3", "")
            });
        }
    }

    public String getMundo() { return mundo; }

    public TierData getTier(int num) { return tiers.get(num); }

    public String[] getComandos(Raridad r) {
        return comandosRecompensa.getOrDefault(r, new String[]{"", "", ""});
    }

    public String[] getMensajes(Raridad r) {
        return mensajesRecompensa.getOrDefault(r, new String[]{"", "", ""});
    }

    public String getMsgCultivoBloqueado()    { return msgCultivoBloqueado; }
    public String getMsgMineralBloqueado()    { return msgMineralBloqueado; }
    public String getMsgMaderaBloqueada()     { return msgMaderaBloqueada; }
    public String getMsgRequiereVip2()        { return msgRequiereVip2; }
    public String getMsgRequiereRango()       { return msgRequiereRango; }
    public String getMsgReloadOk()            { return msgReloadOk; }
    public String getMsgSinPermiso()          { return msgSinPermiso; }
    public String getMsgUsoComando()          { return msgUsoComando; }
    public String getMsgJugadorNoEncontrado() { return msgJugadorNoEncontrado; }
}