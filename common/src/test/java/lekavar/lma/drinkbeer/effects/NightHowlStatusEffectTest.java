package lekavar.lma.drinkbeer.effects;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NightHowlStatusEffectTest {
    @Test
    void allMoonPhasesMatchUpstreamDurations() {
        assertEquals(
                List.of(8400, 6000, 4800, 3600, 2400, 3600, 4800, 6000),
                IntStream.range(0, 8).map(NightHowlStatusEffect::getNightVisionTime).boxed().toList()
        );
    }

    @Test
    void invalidMoonPhasesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> NightHowlStatusEffect.getNightVisionTime(-1));
        assertThrows(IllegalArgumentException.class, () -> NightHowlStatusEffect.getNightVisionTime(8));
    }
}
