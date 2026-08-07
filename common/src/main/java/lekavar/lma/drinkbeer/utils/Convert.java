package lekavar.lma.drinkbeer.utils;

public class Convert {
    public static String tickToTime(int tick) {
        String result;
        if (tick > 0) {
            double time = (double) tick / 20;
            int m = (int) (time / 60);
            int s = (int) (time % 60);
            result = m + ":" + s;
        } else result = "";
        return result;
    }
}
