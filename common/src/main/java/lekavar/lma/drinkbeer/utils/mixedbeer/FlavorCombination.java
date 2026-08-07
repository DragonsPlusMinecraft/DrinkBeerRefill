package lekavar.lma.drinkbeer.utils.mixedbeer;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class FlavorCombination {
    private List<Pair<List<Flavors>, Boolean>> flavorCombinationList;

    public FlavorCombination() {
        this.flavorCombinationList = new ArrayList<>();
    }

    public List<Pair<List<Flavors>, Boolean>> getFlavorCombinationList() {
        return flavorCombinationList;
    }

    public FlavorCombination addFlavorCombination(Flavors... flavors) {
        return addFlavorCombination(false, flavors);
    }

    /**
     * @param ordered If check flavors in order
     * @param flavors
     * @return
     */
    public FlavorCombination addFlavorCombination(boolean ordered, Flavors... flavors) {
        List<Flavors> flavorList = new ArrayList<>();
        for (Flavors flavor : flavors) {
            flavorList.add(flavor);
        }
        Pair<List<Flavors>, Boolean> flavorCombinationPair = Pair.of(flavorList, ordered);
        this.flavorCombinationList.add(flavorCombinationPair);
        return this;
    }
}
