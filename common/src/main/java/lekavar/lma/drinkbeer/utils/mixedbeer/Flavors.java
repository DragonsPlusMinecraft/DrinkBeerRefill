package lekavar.lma.drinkbeer.utils.mixedbeer;

import lekavar.lma.drinkbeer.registries.ParticleTypeRegistry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

import javax.annotation.Nullable;

public enum Flavors {
    SPICY(1, "spicy", null, ParticleTypes.LAVA),
    FIERY(2, "fiery", SPICY, ParticleTypes.LAVA),
    SOOOOO_SPICY(3, "soooo_spicy", null),
    AROMATIC(4, "aromatic", null, ParticleTypeRegistry.MIXED_BEER_DEFAULT.get()),
    AROMATIC1(5, "aromatic1", AROMATIC, ParticleTypeRegistry.MIXED_BEER_DEFAULT.get()),
    REFRESHING(6, "refreshing", null, ParticleTypeRegistry.MIXED_BEER_DEFAULT.get()),
    REFRESHING1(7, "refreshing1", REFRESHING, ParticleTypeRegistry.MIXED_BEER_DEFAULT.get()),
    STORMY(8, "stormy", null, ParticleTypeRegistry.MIXED_BEER_DEFAULT.get()),
    THE_FALL_OF_THE_GIANT(9, "the_fall_of_the_giant", null),
    NUTTY(10, "nutty", null, ParticleTypeRegistry.MIXED_BEER_DEFAULT.get()),
    SWEET(11, "sweet", null, ParticleTypeRegistry.MIXED_BEER_DEFAULT.get()),
    LUSCIOUS(12, "luscious", SWEET, ParticleTypeRegistry.MIXED_BEER_DEFAULT.get()),
    CLOYING(13, "cloying", null),
    NUTTY1(14, "nutty1", NUTTY, ParticleTypeRegistry.MIXED_BEER_DEFAULT.get()),
    MELLOW(15, "mellow", null, ParticleTypeRegistry.MIXED_BEER_DEFAULT.get()),
    DRYING(16, "drying", null, ParticleTypeRegistry.MIXED_BEER_DEFAULT.get());

    private final int id;
    private final String name;
    private final Flavors fatherFlavor;
    private final ParticleType<SimpleParticleType> particle;

    public static final int EMPTY_FLAVOR_ID = 0;
    public static final Flavors DEFAULT_FLAVOR = Flavors.SPICY;
    public static final ParticleType<SimpleParticleType> DEFAULT_PARTICLE = ParticleTypeRegistry.MIXED_BEER_DEFAULT.get();

    /*
    Flavors(int id, String name, Flavors fatherFlavor, SimpleParticleType particle) {
        this.id = id;
        this.name = name;
        this.fatherFlavor = fatherFlavor;
        this.particle = particle;
    }

     */
    Flavors(int id, String name, Flavors fatherFlavor, @Nullable ParticleType<SimpleParticleType> particle) {
        this.id = id;
        this.name = name;
        this.fatherFlavor = fatherFlavor;
        this.particle = particle;
    }

    Flavors(int id, String name, Flavors fatherFlavor) {
        this.id = id;
        this.name = name;
        this.fatherFlavor = fatherFlavor;
        this.particle = null;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Flavors getFatherFlavor() {
        return fatherFlavor;
    }

    public SimpleParticleType getParticle() {
        return (SimpleParticleType) particle;
    }

    public static Flavors byId(int id) {
        Flavors[] flavors = values();
        for (Flavors flavor : flavors) {
            if (flavor.id == id) {
                return flavor;
            }
        }
        return DEFAULT_FLAVOR;
    }
}
