package codes.shiftmc.homes.model;

import codes.shiftmc.homes.Homes;
import org.bukkit.Location;

import java.util.List;

public record UserData(
        User user,
        List<Home> homes
) {
}
