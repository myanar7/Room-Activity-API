public final class Util {
    public static boolean isBetween(String x1, String y1, String z1) {
        int x = Integer.parseInt(x1);
        int y = Integer.parseInt(y1);
        int z = Integer.parseInt(z1);
        return x >= y && x <= y + z;
    }

    public static boolean isBetweenWithDuration(String x1, String x2, String y1, String z1) {
        int x = Integer.parseInt(x1) + Integer.parseInt(x2);
        int y = Integer.parseInt(y1);
        int z = Integer.parseInt(z1);
        return x >= y && x <= y + z;
    }
}
