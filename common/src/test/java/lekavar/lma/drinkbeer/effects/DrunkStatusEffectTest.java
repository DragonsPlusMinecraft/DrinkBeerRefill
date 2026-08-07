package lekavar.lma.drinkbeer.effects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DrunkStatusEffectTest {
    @Test
    void exposesEverySupportedStageDuration() {
        assertAll(
                () -> assertEquals(3600, DrunkStatusEffect.getDrunkDuration(0)),
                () -> assertEquals(3000, DrunkStatusEffect.getDrunkDuration(1)),
                () -> assertEquals(2400, DrunkStatusEffect.getDrunkDuration(2)),
                () -> assertEquals(1800, DrunkStatusEffect.getDrunkDuration(3)),
                () -> assertEquals(1200, DrunkStatusEffect.getDrunkDuration(4))
        );
    }

    @Test
    void invalidAmplifiersFallBackWithoutThrowing() {
        assertAll(
                () -> assertEquals(1200, DrunkStatusEffect.getDrunkDuration(-1)),
                () -> assertEquals(1200, DrunkStatusEffect.getDrunkDuration(5)),
                () -> assertEquals(1200, DrunkStatusEffect.getDrunkDuration(Integer.MAX_VALUE))
        );
    }
}
