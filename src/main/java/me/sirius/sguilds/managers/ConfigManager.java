package me.sirius.sguilds.managers;

import me.sirius.sguilds.SGuilds;
import me.sirius.sguilds.models.BlockMatch;
import me.sirius.sguilds.models.Categoria;
import me.sirius.sguilds.models.Recompensa;
import me.sirius.sguilds.models.Recompensa.Opcion;
import me.sirius.sguilds.models.TierData;
import me.sirius.sguilds.models.TierData.RewardEntry;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ConfigManager {

    // Tope de pares mensaje-N/comando-N que se buscan en el formato antiguo de recompensas.
    private static final int MAX_OPCIONES_LEGACY = 50;

    private final SGuilds plugin;

    private boolean todosLosMundos;
    private Set<String> mundos = Set.of();

    private String permisoAdmin;
    private String permisoBypass;
    private String permisoVip;
    private String permisoVip2;
    private String permisoTier;
    private String permisoCategoriaTier;

    private final Map<String, Categoria> categorias = new LinkedHashMap<>();
    private final Map<Material, List<BlockMatch>> indice = new EnumMap<>(Material.class);
    private final Map<String, Recompensa> recompensas = new HashMap<>();
    private final Map<String, String> mensajes = new HashMap<>();

    private String prefijo = "";

    public ConfigManager(SGuilds plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        var cfg = plugin.getConfig();

        cargarMundos(cfg);
        cargarPermisos(cfg.getConfigurationSection("permisos"));
        cargarMensajes(cfg.getConfigurationSection("mensajes"));
        cargarRecompensas(cfg.getConfigurationSection("recompensas"));

        Map<Integer, TierData> tiersGlobales = leerTiers(cfg.getConfigurationSection("tiers"), Map.of());
        cargarCategorias(cfg.getConfigurationSection("categorias"), tiersGlobales);

        if (categorias.isEmpty()) {
            plugin.getLogger().warning("No hay ninguna categoria valida en config.yml: el farmeo queda inactivo.");
        }
    }

    private void cargarMundos(ConfigurationSection cfg) {
        todosLosMundos = cfg.getBoolean("todos-los-mundos", false);

        List<String> lista = new ArrayList<>(cfg.getStringList("mundos"));
        // "mundo: cultivos" era el formato de un solo mundo; se sigue aceptando.
        String unico = cfg.getString("mundo");
        if (unico != null && !unico.isBlank()) lista.add(unico);

        mundos = normalizarMundos(lista);
        if (!todosLosMundos && mundos.isEmpty()) {
            plugin.getLogger().warning("No hay mundos configurados en 'mundos': ninguna categoria global se activara.");
        }
    }

    private static Set<String> normalizarMundos(Collection<String> nombres) {
        Set<String> out = new LinkedHashSet<>();
        for (String nombre : nombres) {
            if (nombre != null && !nombre.isBlank()) out.add(nombre.trim().toLowerCase(Locale.ROOT));
        }
        return out;
    }

    private void cargarPermisos(ConfigurationSection sec) {
        permisoAdmin         = leerPermiso(sec, "admin", "sguilds.admin");
        permisoBypass        = leerPermiso(sec, "bypass", "sguilds.bypass");
        permisoVip           = leerPermiso(sec, "vip", "farmeo.vip");
        permisoVip2          = leerPermiso(sec, "vip2", "farmeo.vip2");
        permisoTier          = leerPermiso(sec, "tier", "farmeo.tier{tier}");
        permisoCategoriaTier = leerPermiso(sec, "categoria-tier", "");
    }

    private static String leerPermiso(ConfigurationSection sec, String key, String def) {
        String valor = sec == null ? def : sec.getString(key, def);
        return valor == null ? "" : valor.trim();
    }

    private void cargarMensajes(ConfigurationSection sec) {
        mensajes.clear();
        if (sec == null) return;

        for (String key : sec.getKeys(false)) {
            String valor = sec.getString(key);
            if (valor != null) mensajes.put(key, valor);
        }
        prefijo = mensajes.getOrDefault("prefijo", "");
    }

    private void cargarRecompensas(ConfigurationSection sec) {
        recompensas.clear();
        if (sec == null) return;

        for (String key : sec.getKeys(false)) {
            var sub = sec.getConfigurationSection(key);
            if (sub == null) continue;

            String id = key.toLowerCase(Locale.ROOT);
            List<Opcion> opciones = leerOpciones(sub, id);
            if (opciones.isEmpty()) {
                plugin.getLogger().warning("La recompensa '" + id + "' no tiene opciones: se ignora.");
                continue;
            }
            recompensas.put(id, new Recompensa(id, List.copyOf(opciones)));
        }
    }

    private List<Opcion> leerOpciones(ConfigurationSection sec, String id) {
        List<Opcion> opciones = new ArrayList<>();

        for (Map<?, ?> item : sec.getMapList("opciones")) {
            String mensaje = item.get("mensaje") == null ? "" : String.valueOf(item.get("mensaje"));
            opciones.add(new Opcion(mensaje, leerComandos(item.get("comandos"), item.get("comando"), id)));
        }
        if (!opciones.isEmpty()) return opciones;

        // Formato antiguo: mensaje-1/comando-1, mensaje-2/comando-2, ...
        for (int i = 1; i <= MAX_OPCIONES_LEGACY; i++) {
            String mensaje = sec.getString("mensaje-" + i);
            String comando = sec.getString("comando-" + i);
            if (mensaje == null && comando == null) continue;

            List<String> comandos = comando == null || comando.isBlank() ? List.of() : List.of(comando);
            opciones.add(new Opcion(mensaje == null ? "" : mensaje, comandos));
        }
        return opciones;
    }

    private List<String> leerComandos(Object lista, Object unico, String id) {
        List<String> out = new ArrayList<>();

        if (lista instanceof Collection<?> coleccion) {
            for (Object o : coleccion) {
                if (o != null && !String.valueOf(o).isBlank()) out.add(String.valueOf(o));
            }
        } else if (lista != null && !String.valueOf(lista).isBlank()) {
            out.add(String.valueOf(lista));
        }
        if (unico != null && !String.valueOf(unico).isBlank()) out.add(String.valueOf(unico));

        if (out.isEmpty()) {
            plugin.getLogger().warning("Una opcion de la recompensa '" + id + "' no ejecuta ningun comando.");
        }
        return List.copyOf(out);
    }

    private Map<Integer, TierData> leerTiers(ConfigurationSection sec, Map<Integer, TierData> fallback) {
        if (sec == null) return fallback;

        Map<Integer, TierData> out = new HashMap<>(fallback);
        for (String key : sec.getKeys(false)) {
            int num;
            try {
                num = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Tier invalido '" + key + "': el nombre debe ser un numero.");
                continue;
            }

            var tierSec = sec.getConfigurationSection(key);
            if (tierSec == null) continue;

            int umbral = Math.max(1, tierSec.getInt("umbral", 64));
            boolean requierePermiso = tierSec.getBoolean("requiere-permiso", num > 0);
            boolean vip2 = tierSec.getBoolean("requiere-vip2", false);

            List<RewardEntry> entries = new ArrayList<>();
            for (Map<?, ?> item : tierSec.getMapList("recompensas")) {
                String raridad = String.valueOf(item.get("raridad")).toLowerCase(Locale.ROOT);
                double chance;
                try {
                    chance = Double.parseDouble(String.valueOf(item.get("chance")));
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Chance invalida en el tier " + num + " (raridad '" + raridad + "').");
                    continue;
                }
                if (!recompensas.containsKey(raridad)) {
                    plugin.getLogger().warning("El tier " + num + " usa la raridad '" + raridad
                            + "', que no esta definida en 'recompensas'.");
                    continue;
                }
                entries.add(new RewardEntry(raridad, chance));
            }

            out.put(num, new TierData(num, umbral, requierePermiso, vip2, List.copyOf(entries)));
        }
        return Map.copyOf(out);
    }

    private void cargarCategorias(ConfigurationSection sec, Map<Integer, TierData> tiersGlobales) {
        categorias.clear();
        indice.clear();
        if (sec == null) return;

        for (String key : sec.getKeys(false)) {
            var catSec = sec.getConfigurationSection(key);
            if (catSec == null) continue;
            if (!catSec.getBoolean("activada", true)) continue;

            String id = key.toLowerCase(Locale.ROOT);
            String nombre = catSec.getString("nombre", key);
            String bloqueado = prefijar(catSec.getString("mensaje-bloqueado", getMensaje("bloque-bloqueado")));

            boolean catTodos = catSec.getBoolean("todos-los-mundos", todosLosMundos);
            Set<String> catMundos = catSec.isSet("mundos")
                    ? normalizarMundos(catSec.getStringList("mundos"))
                    : mundos;

            Map<Integer, TierData> tiers = leerTiers(catSec.getConfigurationSection("tiers"), tiersGlobales);

            Categoria categoria = new Categoria(id, nombre, catTodos, Set.copyOf(catMundos), bloqueado, tiers);
            categorias.put(id, categoria);
            indexarBloques(catSec.getConfigurationSection("bloques"), categoria);
        }
    }

    private void indexarBloques(ConfigurationSection sec, Categoria categoria) {
        if (sec == null) {
            plugin.getLogger().warning("La categoria '" + categoria.id() + "' no define ningun bloque.");
            return;
        }

        for (String key : sec.getKeys(false)) {
            int tier;
            try {
                tier = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Tier invalido '" + key + "' en los bloques de '" + categoria.id() + "'.");
                continue;
            }
            if (categoria.tier(tier) == null) {
                plugin.getLogger().warning("La categoria '" + categoria.id() + "' asigna bloques al tier " + tier
                        + ", que no esta definido en 'tiers'.");
                continue;
            }

            for (String nombre : sec.getStringList(key)) {
                Material material = Material.matchMaterial(nombre.trim().toUpperCase(Locale.ROOT));
                if (material == null) {
                    plugin.getLogger().warning("Material desconocido '" + nombre + "' en '"
                            + categoria.id() + "' tier " + tier + ".");
                    continue;
                }
                indice.computeIfAbsent(material, m -> new ArrayList<>())
                        .add(new BlockMatch(categoria, tier));
            }
        }
    }

    // Categoria y tier del bloque en ese mundo, o null si ahi no cuenta para nada.
    public BlockMatch match(Material material, String mundo) {
        List<BlockMatch> candidatos = indice.get(material);
        if (candidatos == null) return null;

        String normalizado = mundo.toLowerCase(Locale.ROOT);
        for (BlockMatch candidato : candidatos) {
            if (candidato.categoria().aplicaEn(normalizado)) return candidato;
        }
        return null;
    }

    public Recompensa getRecompensa(String raridad) {
        return recompensas.get(raridad);
    }

    public Collection<Categoria> getCategorias() {
        return Collections.unmodifiableCollection(categorias.values());
    }

    public String getMensaje(String key) {
        return prefijar(mensajes.getOrDefault(key, ""));
    }

    private String prefijar(String mensaje) {
        return mensaje.replace("{prefijo}", prefijo);
    }

    public String getPermisoAdmin()  { return permisoAdmin; }
    public String getPermisoBypass() { return permisoBypass; }
    public String getPermisoVip()    { return permisoVip; }
    public String getPermisoVip2()   { return permisoVip2; }

    public String permisoTier(int tier) {
        return permisoTier.replace("{tier}", String.valueOf(tier));
    }

    public String permisoCategoriaTier(String categoria, int tier) {
        if (permisoCategoriaTier.isEmpty()) return "";
        return permisoCategoriaTier.replace("{categoria}", categoria)
                                   .replace("{tier}", String.valueOf(tier));
    }
}
