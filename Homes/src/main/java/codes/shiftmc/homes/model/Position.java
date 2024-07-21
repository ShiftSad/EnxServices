package codes.shiftmc.homes.model;

public record Position(
        String world,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {

    public static Position fromString(String string) {
        String[] split = string.split(";");
        return new Position(
                split[0],
                Double.parseDouble(split[1]),
                Double.parseDouble(split[2]),
                Double.parseDouble(split[3]),
                Float.parseFloat(split[4]),
                Float.parseFloat(split[5])
        );
    }

    @Override
    public String toString() {
        return world + ";" + x + ";" + y + ";" + z + ";" + yaw + ";" + pitch;
    }
}
