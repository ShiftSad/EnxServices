package codes.shiftmc.homes.model;

import java.util.List;

public record UserData(
        User user,
        List<Home> homes
) {

    public UserData addHome(Home home) {
        homes.add(home);
        return this;
    }

    public UserData removeHome(Home home) {
        homes.remove(home);
        return this;
    }

    public Home getHome(String name) {
        return homes.stream().filter(h -> h.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
