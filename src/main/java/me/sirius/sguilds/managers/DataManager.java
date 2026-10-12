package me.sirius.sguilds.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

// Un fichero por jugador y un unico hilo de disco. Antes todo vivia en un solo
// player_data.yml que se parseaba entero en el hilo principal en cada entrada y
// se reescribia entero en cada salida: con dos salidas a la vez, la ultima
// escritura pisaba a la otra.
// Al ser un solo hilo, las tareas salen en el orden en que se encolan: la carga
// de quien vuelve a entrar siempre lee lo que dejo escrito su salida anterior.
public final class DataManager {

    private static final String[] CATEGORIAS_ANTIGUAS = {"cultivos", "minerales", "maderas"};

    private final Logger logger;
    private final File carpeta;
    private final File antiguo;
    private final Map<UUID, Map<String, Integer>> cache = new ConcurrentHashMap<>();
    private final ExecutorService disco = Executors.newSingleThreadExecutor(tarea -> {
        Thread hilo = new Thread(tarea, "SGuilds-datos");
        hilo.setDaemon(true);
        return hilo;
    });

    public DataManager(File dataFolder, Logger logger) {
        this.logger = logger;
        this.carpeta = new File(dataFolder, "data/jugadores");
        this.antiguo = new File(dataFolder, "data/player_data.yml");
        carpeta.mkdirs();
        // Primera tarea de la cola: ninguna carga puede adelantarse a la migracion.
        encolar(this::migrar);
    }

    // El mapa del jugador existe desde el primer instante para que los bloques que
    // rompa mientras se lee el disco cuenten igual; lo leido se suma encima.
    public void load(Player player) {
        load(player.getUniqueId());
    }

    void load(UUID uuid) {
        Map<String, Integer> contadores = getOrCreate(uuid);
        encolar(() -> leer(uuid).forEach((categoria, valor) -> contadores.merge(categoria, valor, Integer::sum)));
    }

    public void save(Player player) {
        save(player.getUniqueId());
    }

    // Se le pasa el mapa vivo y no una copia: si su carga aun no termino, la copia
    // hecha aqui no tendria lo leido del disco y lo borraria al guardar. En la
    // cola la carga va antes, asi que al llegar este turno el mapa ya esta completo.
    void save(UUID uuid) {
        Map<String, Integer> contadores = cache.remove(uuid);
        if (contadores != null) {
            encolar(() -> escribir(uuid, Map.copyOf(contadores)));
        }
    }

    public void autosave() {
        encolar(() -> cache.forEach((uuid, contadores) -> escribir(uuid, Map.copyOf(contadores))));
    }

    // Al apagar: primero se deja terminar lo que hay en cola y despues se escribe
    // lo que sigue en memoria, en este mismo hilo, porque ya no habra otro.
    public void saveAll() {
        disco.shutdown();
        try {
            if (!disco.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.warning("El disco no termino a tiempo; se guarda lo que hay en memoria.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        cache.forEach((uuid, contadores) -> escribir(uuid, Map.copyOf(contadores)));
        cache.clear();
    }

    private void encolar(Runnable tarea) {
        try {
            disco.execute(() -> {
                try {
                    tarea.run();
                } catch (RuntimeException e) {
                    logger.log(Level.SEVERE, "Error en los datos de farmeo", e);
                }
            });
        } catch (RejectedExecutionException apagado) {
            tarea.run();
        }
    }

    private File fichero(UUID uuid) {
        return new File(carpeta, uuid + ".yml");
    }

    private Map<String, Integer> leer(UUID uuid) {
        File fichero = fichero(uuid);
        if (!fichero.isFile()) {
            return Map.of();
        }
        ConfigurationSection sec = YamlConfiguration.loadConfiguration(fichero).getConfigurationSection("contadores");
        if (sec == null) {
            return Map.of();
        }
        Map<String, Integer> out = new HashMap<>();
        for (String categoria : sec.getKeys(false)) {
            out.put(categoria, sec.getInt(categoria, 0));
        }
        return out;
    }

    private void escribir(UUID uuid, Map<String, Integer> contadores) {
        File fichero = fichero(uuid);
        if (contadores.values().stream().allMatch(valor -> valor == 0)) {
            if (fichero.exists() && !fichero.delete()) {
                logger.warning("No se pudo borrar " + fichero.getName());
            }
            return;
        }
        YamlConfiguration yml = new YamlConfiguration();
        contadores.forEach((categoria, valor) -> yml.set("contadores." + categoria, valor));
        try {
            yml.save(fichero);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error al guardar los datos de farmeo de " + uuid, e);
        }
    }

    // Parte el player_data.yml de versiones anteriores en un fichero por jugador.
    private void migrar() {
        if (!antiguo.isFile()) {
            return;
        }
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(antiguo);
        int migrados = 0;
        for (String clave : yml.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(clave);
            } catch (IllegalArgumentException e) {
                continue;
            }
            if (fichero(uuid).exists()) {
                continue;
            }
            Map<String, Integer> contadores = new HashMap<>();
            ConfigurationSection sec = yml.getConfigurationSection(clave + ".contadores");
            if (sec != null) {
                for (String categoria : sec.getKeys(false)) {
                    contadores.put(categoria, sec.getInt(categoria, 0));
                }
            }
            for (String categoria : CATEGORIAS_ANTIGUAS) {
                String path = clave + "." + categoria;
                if (yml.isInt(path) && yml.getInt(path) > 0) {
                    contadores.putIfAbsent(categoria, yml.getInt(path));
                }
            }
            escribir(uuid, contadores);
            migrados++;
        }
        File hecho = new File(antiguo.getParentFile(), "player_data.yml.migrado");
        if (!antiguo.renameTo(hecho)) {
            logger.warning("No se pudo renombrar player_data.yml tras migrarlo; se volvera a intentar.");
        }
        logger.info("Migrados " + migrados + " jugador(es) al formato de un fichero por jugador.");
    }

    public int get(UUID uuid, String categoria) {
        Map<String, Integer> contadores = cache.get(uuid);
        return contadores == null ? 0 : contadores.getOrDefault(categoria, 0);
    }

    public int incrementar(UUID uuid, String categoria) {
        return getOrCreate(uuid).merge(categoria, 1, Integer::sum);
    }

    public void reset(UUID uuid, String categoria) {
        getOrCreate(uuid).put(categoria, 0);
    }

    public void resetAll(UUID uuid) {
        Map<String, Integer> contadores = cache.get(uuid);
        if (contadores != null) contadores.clear();
    }

    public Map<String, Integer> getContadores(UUID uuid) {
        return Collections.unmodifiableMap(cache.getOrDefault(uuid, Map.of()));
    }

    private Map<String, Integer> getOrCreate(UUID uuid) {
        return cache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
    }
}
