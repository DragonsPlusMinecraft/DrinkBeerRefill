package lekavar.lma.drinkbeer.utils.dataComponent;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpiceDataTest {
    @Test
    void acceptsEmptyAndPartialSpiceLists() {
        assertEquals(new SpiceData(0, 0, 0), SpiceData.fromSpiceList(List.of()));
        assertEquals(new SpiceData(4, 0, 0), SpiceData.fromSpiceList(List.of(4)));
        assertEquals(new SpiceData(4, 7, 0), SpiceData.fromSpiceList(List.of(4, 7)));
        assertEquals(new SpiceData(4, 7, 9), SpiceData.fromSpiceList(List.of(4, 7, 9)));
    }

    @Test
    void ignoresExcessAndNormalizesInvalidEntries() {
        assertEquals(new SpiceData(4, 0, 0), SpiceData.fromSpiceList(Arrays.asList(4, null, -3, 9)));
    }

    @Test
    void convertsComponentBackToCompactList() {
        assertEquals(List.of(4, 9), new SpiceData(4, 0, 9).toSpiceList());
    }
}
