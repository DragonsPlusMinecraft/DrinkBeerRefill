package lekavar.lma.drinkbeer.utils.mixedbeer;

import lekavar.lma.drinkbeer.registries.ItemRegistry;
import net.minecraft.world.item.Item;

public enum Spices {
    BLAZE_PAPRIKA(1, ItemRegistry.SPICE_BLAZE_PAPRIKA.get(), Flavors.FIERY),
    DRIED_EGLIA_BUD(2, ItemRegistry.SPICE_DRIED_EGLIA_BUD.get(), Flavors.SPICY),
    SMOKED_EGLIA_BUD(3, ItemRegistry.SPICE_SMOKED_EGLIA_BUD.get(), Flavors.FIERY),
    AMETHYST_NIGELLA_SEEDS(4, ItemRegistry.SPICE_AMETHYST_NIGELLA_SEEDS.get(), Flavors.AROMATIC),
    CITRINE_NIGELLA_SEEDS(5, ItemRegistry.SPICE_CITRINE_NIGELLA_SEEDS.get(), Flavors.AROMATIC1),
    ICE_MINT(6, ItemRegistry.SPICE_ICE_MINT.get(), Flavors.REFRESHING),
    ICE_PATCHOULI(7, ItemRegistry.SPICE_ICE_PATCHOULI.get(), Flavors.REFRESHING1),
    STORM_SHARDS(8, ItemRegistry.SPICE_STORM_SHARDS.get(), Flavors.STORMY),
    ROASTED_RED_PINE_NUTS(9, ItemRegistry.SPICE_ROASTED_RED_PINE_NUTS.get(), Flavors.NUTTY),
    GLACE_GOJI_BERRIES(10, ItemRegistry.SPICE_GLACE_GOJI_BERRIES.get(), Flavors.SWEET),
    FROZEN_PERSIMMON(11, ItemRegistry.SPICE_FROZEN_PERSIMMON.get(), Flavors.LUSCIOUS),
    ROASTED_PECANS(12, ItemRegistry.SPICE_ROASTED_PECANS.get(), Flavors.NUTTY1),
    SILVER_NEEDLE_WHITE_TEA(13, ItemRegistry.SPICE_SILVER_NEEDLE_WHITE_TEA.get(), Flavors.MELLOW),
    GOLDEN_CINNAMON_POWDER(14, ItemRegistry.SPICE_GOLDEN_CINNAMON_POWDER.get(), Flavors.SWEET),
    DRIED_SELAGINELLA(15, ItemRegistry.SPICE_DRIED_SELAGINELLA.get(), Flavors.DRYING);

    private final int id;
    private final Item spiceItem;
    private final Flavors flavor;

    public static final int EMPTY_SPICE_ID = 0;
    public static final Spices DEFAULT_SPICE = Spices.BLAZE_PAPRIKA;

    Spices(int id, Item spiceItem, Flavors flavor) {
        this.id = id;
        this.spiceItem = spiceItem;
        this.flavor = flavor;
    }

    public int getId() {
        return this.id;
    }

    public Item getSpiceItem() {
        return spiceItem;
    }

    public Flavors getFlavor() {
        return flavor;
    }

    public static Spices byId(int id) {
        Spices[] spices = values();
        for (Spices spice : spices) {
            if (spice.id == id) {
                return spice;
            }
        }
        return DEFAULT_SPICE;
    }

    public static Spices byItem(Item spiceItem) {
        Spices[] spices = values();
        for (Spices spice : spices) {
            if (spice.spiceItem.equals(spiceItem)) {
                return spice;
            }
        }
        return DEFAULT_SPICE;
    }

    public static int size() {
        return values().length;
    }
}
