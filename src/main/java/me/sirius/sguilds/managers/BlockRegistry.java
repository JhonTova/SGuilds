package me.sirius.sguilds.managers;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public final class BlockRegistry {

    public enum Sistema { CULTIVOS, MINERALES, MADERAS }

    public record BlockEntry(Sistema sistema, int tier) {}

    private static final Map<Material, BlockEntry> REGISTRY = new HashMap<>();

    static {
        reg(Material.WHEAT,             Sistema.CULTIVOS, 0);
        reg(Material.CARROTS,           Sistema.CULTIVOS, 1);
        reg(Material.POTATOES,          Sistema.CULTIVOS, 2);
        reg(Material.BEETROOTS,         Sistema.CULTIVOS, 3);
        reg(Material.PUMPKIN,           Sistema.CULTIVOS, 4);
        reg(Material.MELON,             Sistema.CULTIVOS, 5);
        reg(Material.NETHER_WART,       Sistema.CULTIVOS, 6);
        reg(Material.COCOA,             Sistema.CULTIVOS, 7);
        reg(Material.SUGAR_CANE,        Sistema.CULTIVOS, 8);
        reg(Material.CACTUS,            Sistema.CULTIVOS, 9);
        reg(Material.BAMBOO,            Sistema.CULTIVOS, 10);
        reg(Material.KELP,              Sistema.CULTIVOS, 10);
        reg(Material.KELP_PLANT,        Sistema.CULTIVOS, 10);
        reg(Material.SWEET_BERRY_BUSH,  Sistema.CULTIVOS, 10);
        reg(Material.BROWN_MUSHROOM,    Sistema.CULTIVOS, 10);
        reg(Material.RED_MUSHROOM,      Sistema.CULTIVOS, 10);

        reg(Material.COAL_ORE,                  Sistema.MINERALES, 0);
        reg(Material.DEEPSLATE_COAL_ORE,        Sistema.MINERALES, 0);
        reg(Material.COAL_BLOCK,                Sistema.MINERALES, 0);
        reg(Material.COPPER_ORE,                Sistema.MINERALES, 1);
        reg(Material.DEEPSLATE_COPPER_ORE,      Sistema.MINERALES, 1);
        reg(Material.COPPER_BLOCK,              Sistema.MINERALES, 1);
        reg(Material.IRON_ORE,                  Sistema.MINERALES, 2);
        reg(Material.DEEPSLATE_IRON_ORE,        Sistema.MINERALES, 2);
        reg(Material.IRON_BLOCK,                Sistema.MINERALES, 2);
        reg(Material.NETHER_QUARTZ_ORE,         Sistema.MINERALES, 3);
        reg(Material.GOLD_ORE,                  Sistema.MINERALES, 4);
        reg(Material.DEEPSLATE_GOLD_ORE,        Sistema.MINERALES, 4);
        reg(Material.NETHER_GOLD_ORE,           Sistema.MINERALES, 4);
        reg(Material.GOLD_BLOCK,                Sistema.MINERALES, 4);
        reg(Material.REDSTONE_ORE,              Sistema.MINERALES, 5);
        reg(Material.DEEPSLATE_REDSTONE_ORE,    Sistema.MINERALES, 5);
        reg(Material.REDSTONE_BLOCK,            Sistema.MINERALES, 5);
        reg(Material.LAPIS_ORE,                 Sistema.MINERALES, 6);
        reg(Material.DEEPSLATE_LAPIS_ORE,       Sistema.MINERALES, 6);
        reg(Material.LAPIS_BLOCK,               Sistema.MINERALES, 6);
        reg(Material.AMETHYST_CLUSTER,          Sistema.MINERALES, 7);
        reg(Material.AMETHYST_BLOCK,            Sistema.MINERALES, 7);
        reg(Material.DIAMOND_ORE,               Sistema.MINERALES, 8);
        reg(Material.DEEPSLATE_DIAMOND_ORE,     Sistema.MINERALES, 8);
        reg(Material.DIAMOND_BLOCK,             Sistema.MINERALES, 8);
        reg(Material.EMERALD_ORE,               Sistema.MINERALES, 9);
        reg(Material.DEEPSLATE_EMERALD_ORE,     Sistema.MINERALES, 9);
        reg(Material.EMERALD_BLOCK,             Sistema.MINERALES, 9);
        reg(Material.ANCIENT_DEBRIS,            Sistema.MINERALES, 10);
        reg(Material.NETHERITE_BLOCK,           Sistema.MINERALES, 10);

        reg(Material.OAK_LOG,       Sistema.MADERAS, 0);
        reg(Material.SPRUCE_LOG,    Sistema.MADERAS, 1);
        reg(Material.BIRCH_LOG,     Sistema.MADERAS, 2);
        reg(Material.JUNGLE_LOG,    Sistema.MADERAS, 3);
        reg(Material.ACACIA_LOG,    Sistema.MADERAS, 4);
        reg(Material.DARK_OAK_LOG,  Sistema.MADERAS, 5);
        reg(Material.MANGROVE_LOG,  Sistema.MADERAS, 6);
        reg(Material.CHERRY_LOG,    Sistema.MADERAS, 7);
        reg(Material.CRIMSON_STEM,  Sistema.MADERAS, 8);
        reg(Material.WARPED_STEM,   Sistema.MADERAS, 9);
    }

    private BlockRegistry() {}

    private static void reg(Material mat, Sistema sistema, int tier) {
        REGISTRY.put(mat, new BlockEntry(sistema, tier));
    }

    public static BlockEntry get(Material material) {
        return REGISTRY.get(material);
    }
}