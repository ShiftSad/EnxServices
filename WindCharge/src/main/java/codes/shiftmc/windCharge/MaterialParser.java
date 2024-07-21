package codes.shiftmc.windCharge;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MaterialParser {

    public static List<Material> parseMaterials(List<String> constraints) {
        return Arrays.stream(Material.values())
                .filter(material -> matchesConstraints(material, constraints))
                .collect(Collectors.toList());
    }

    public static boolean matchesConstraints(Material material, List<String> constraints) {
        for (String constraint : constraints) {
            if (matchesConstraint(material, constraint)) return true;
        }
        return false;
    }

    private static boolean matchesConstraint(Material material, String constraint) {
        if (constraint.startsWith("ENDS_WITH:")) {
            String suffix = constraint.substring("ENDS_WITH:".length());
            if (constraint.contains("&&!")) {
                String[] parts = suffix.split("&&!");
                String notPrefix = parts[1].substring("STARTS_WITH:".length());
                return material.name().endsWith(parts[0]) && !material.name().startsWith(notPrefix);
            }
            return material.name().endsWith(suffix);
        } else if (constraint.startsWith("STARTS_WITH:")) {
            return material.name().startsWith(constraint.substring("STARTS_WITH:".length()));
        } else if (constraint.startsWith("IS:")) {
            return material.name().equals(constraint.substring("IS:".length()));
        }
        return false;
    }

}
