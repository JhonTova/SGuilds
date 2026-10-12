package me.sirius.sguilds.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataManagerTest {

    private static final Logger LOG = Logger.getLogger("test");

    @TempDir
    File carpeta;

    @Test
    void salirYVolverAEntrarConservaLosContadores() {
        UUID uuid = UUID.randomUUID();
        DataManager datos = new DataManager(carpeta, LOG);

        datos.load(uuid);
        for (int i = 0; i < 7; i++) {
            datos.incrementar(uuid, "minerales");
        }
        datos.save(uuid);
        // Vuelve a entrar en el acto, antes de que nadie espere al disco.
        datos.load(uuid);
        datos.incrementar(uuid, "minerales");
        datos.saveAll();

        DataManager reabierto = new DataManager(carpeta, LOG);
        reabierto.load(uuid);
        reabierto.saveAll();
        assertEquals(8, leer(uuid, "minerales"));
    }

    @Test
    void muchasSalidasALaVezNoSePisan() {
        DataManager datos = new DataManager(carpeta, LOG);
        List<UUID> jugadores = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            UUID uuid = UUID.randomUUID();
            jugadores.add(uuid);
            datos.load(uuid);
            for (int j = 0; j <= i % 5; j++) {
                datos.incrementar(uuid, "cultivos");
            }
        }
        jugadores.forEach(datos::save);
        datos.saveAll();

        for (int i = 0; i < jugadores.size(); i++) {
            assertEquals(i % 5 + 1, leer(jugadores.get(i), "cultivos"));
        }
    }

    @Test
    void loQueSeRompeMientrasCargaSeSumaALoGuardado() {
        UUID uuid = UUID.randomUUID();
        DataManager primera = new DataManager(carpeta, LOG);
        primera.load(uuid);
        primera.incrementar(uuid, "maderas");
        primera.incrementar(uuid, "maderas");
        primera.saveAll();

        DataManager segunda = new DataManager(carpeta, LOG);
        segunda.load(uuid);
        segunda.incrementar(uuid, "maderas");
        segunda.saveAll();

        assertEquals(3, leer(uuid, "maderas"));
    }

    @Test
    void migraElFicheroAntiguo() throws Exception {
        UUID nuevo = UUID.randomUUID();
        UUID viejo = UUID.randomUUID();
        File antiguo = new File(carpeta, "data/player_data.yml");
        antiguo.getParentFile().mkdirs();
        YamlConfiguration yml = new YamlConfiguration();
        yml.set(nuevo + ".contadores.minerales", 12);
        yml.set(viejo + ".cultivos", 40);
        yml.save(antiguo);

        DataManager datos = new DataManager(carpeta, LOG);
        datos.load(nuevo);
        datos.load(viejo);
        datos.saveAll();

        assertEquals(12, leer(nuevo, "minerales"));
        assertEquals(40, leer(viejo, "cultivos"));
        assertFalse(antiguo.exists());
        assertTrue(new File(carpeta, "data/player_data.yml.migrado").exists());
    }

    @Test
    void unJugadorSinProgresoNoDejaFichero() {
        UUID uuid = UUID.randomUUID();
        DataManager datos = new DataManager(carpeta, LOG);
        datos.load(uuid);
        datos.save(uuid);
        datos.saveAll();
        assertFalse(new File(carpeta, "data/jugadores/" + uuid + ".yml").exists());
    }

    private int leer(UUID uuid, String categoria) {
        File fichero = new File(carpeta, "data/jugadores/" + uuid + ".yml");
        return YamlConfiguration.loadConfiguration(fichero).getInt("contadores." + categoria);
    }
}
