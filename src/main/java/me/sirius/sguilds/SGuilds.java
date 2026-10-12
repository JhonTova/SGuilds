package me.sirius.sguilds;

import me.sirius.sguilds.commands.FarmeoCommand;
import me.sirius.sguilds.listeners.FarmeoListener;
import me.sirius.sguilds.listeners.PlayerListener;
import me.sirius.sguilds.managers.ConfigManager;
import me.sirius.sguilds.managers.DataManager;
import me.sirius.sguilds.managers.RewardManager;
import me.sirius.core.SiriusPlugin;

public final class SGuilds extends SiriusPlugin {

    private ConfigManager configManager;
    private DataManager dataManager;
    private RewardManager rewardManager;

    @Override
    protected void onStart() {
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        dataManager   = new DataManager(getDataFolder(), getLogger());
        rewardManager = new RewardManager(this, configManager);

        listeners(new FarmeoListener(this), new PlayerListener(this));
        command("sguilds", new FarmeoCommand(this));

        // Un /reload en caliente deja jugadores ya conectados a los que nadie
        // cargaría el perfil si solo escucháramos el evento de entrada.
        getServer().getOnlinePlayers().forEach(dataManager::load);

        // Sin esto los contadores solo llegaban al disco al salir: un cierre en seco
        // perdia todo lo de la sesion.
        long cincoMinutos = 5 * 60 * 20L;
        scheduler().asyncTimer(dataManager::autosave, cincoMinutos, cincoMinutos);
    }

    @Override
    protected void onStop() {
        if (dataManager != null) dataManager.saveAll();
    }

    public ConfigManager getConfigManager() { return configManager; }
    public DataManager   getDataManager()   { return dataManager; }
    public RewardManager getRewardManager() { return rewardManager; }
}