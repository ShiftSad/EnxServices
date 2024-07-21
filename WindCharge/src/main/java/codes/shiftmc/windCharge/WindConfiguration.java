package codes.shiftmc.windCharge;

import org.bukkit.configuration.Configuration;

import java.util.List;

public class WindConfiguration {

    private static WindConfiguration instance;

    boolean explodesBlocks;
    boolean damagesEntities;
    float knockbackMultiplier;
    List<String> vulnerableBlocks;
    float radius;

    private WindConfiguration() {
        explodesBlocks = true;
        damagesEntities = false;
        knockbackMultiplier = 1.22f;
        radius = 1.2F;
        /*
        Lista baseada no código fonte do WindCharge.
        Infelizmente dentro da API do PaperMC, não
        temos acesso ao Registro. :(
        */
        vulnerableBlocks = List.of(
                "ENDS_WITH:DOOR&&!STARTS_WITH:IRON",
                "ENDS_WITH:TRAPDOOR&&!STARTS_WITH:IRON",
                "ENDS_WITH:FENCE_GATE",
                "ENDS_WITH:BUTTON",
                "IS:LEVER",
                "IS:BELL",
                "IS:CHORUS_FLOWER",
                "IS:POINTED_DRIPSTONE",
                "IS:DECORATED_POT",
                "ENDS_WITH:CANDLE"
        );
    }

    public static WindConfiguration getInstance() {
        if (instance == null) {
            instance = new WindConfiguration();
        }
        return instance;
    }

    public void load(Configuration config) {

    }
}
