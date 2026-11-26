<div align="center">

# SGuilds

### Farmeo competitivo por gremios, con recompensas por tiers.

Los jugadores acumulan progreso recolectando, y suben de tier para desbloquear premios.

[![build](https://github.com/JhonTova/SGuilds/actions/workflows/build.yml/badge.svg)](https://github.com/JhonTova/SGuilds/actions/workflows/build.yml) [![release](https://img.shields.io/github/v/release/JhonTova/SGuilds?label=release)](https://github.com/JhonTova/SGuilds/releases/latest) ![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11%2B-62B47A) ![Java](https://img.shields.io/badge/Java-21-E76F00)

</div>

---

## Características

- **Contadores de farmeo** por jugador: cultivos, minerales y maderas.
- **Sistema de tiers** — al alcanzar cada umbral, el jugador sube y recibe su recompensa.
- **Recompensas configurables** por tier (comandos, ítems, lo que quieras).
- **Por mundo** — activa el farmeo solo donde tenga sentido.
- **Estadísticas** consultables con un comando.
- Persistencia de datos: el progreso no se pierde al reiniciar.

## Comandos

| Comando | Acción |
|---|---|
| `/sguilds stats [jugador]` | Muestra el progreso de farmeo. |
| `/sguilds reset <jugador>` | Reinicia el progreso de un jugador. |
| `/sguilds reload` | Recarga la configuración. |

Permiso: `sguilds.admin`

## Requisitos

Requiere **[SiriusCore](https://github.com/JhonTova/SiriusCore)**.

## Instalación

`SiriusCore.jar` + `SGuilds.jar` en `plugins/`. Spigot / Paper / Purpur 1.21.11+.

## Descarga

El `.jar` de cada versión está en [Releases](https://github.com/JhonTova/SGuilds/releases/latest).

## Compilar desde el código

Requiere Java 21 y Maven. SiriusCore no está en un repositorio Maven público, así que
primero se instala en local la versión que declara el `pom.xml`:

```bash
git clone --branch v1.1.1 https://github.com/JhonTova/SiriusCore.git
mvn -f SiriusCore/pom.xml install -DskipTests
mvn package        # en este repositorio; el .jar queda en target/
```

Cada push y pull request se compila en GitHub Actions. Al subir una tag `v*`, el `.jar`
se adjunta solo a la release.
