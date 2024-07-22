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
}
