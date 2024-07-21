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
        if (constraints == null || constraints.isEmpty()) {
            return false;
        }
        for (String constraint : constraints) {
            if (constraint == null) continue;
            return matchesConstraint(material, constraint);
        }
        return false;
    }

    private static boolean matchesConstraint(Material material, String constraint) {
        boolean match = false;
        // No one likes legacy materials >:V
        if (material.isLegacy()) return false;

        if (constraint.startsWith("ENDS_WITH:")) {
            String suffix = constraint.substring("ENDS_WITH:".length());
            if (constraint.contains("&&!")) {
                String[] parts = suffix.split("&&!");
                String positiveMatch = parts[0];
                String negativeMatch = parts[1].substring("STARTS_WITH:".length());
                match = material.name().endsWith(positiveMatch) && !material.name().startsWith(negativeMatch);
            } else match = material.name().endsWith(suffix);
        }
        else if (constraint.startsWith("STARTS_WITH:")) match = material.name().startsWith(constraint.substring("STARTS_WITH:".length()));
        else if (constraint.startsWith("IS:")) match = material.name().equals(constraint.substring("IS:".length()));

        return match;
    }
}
