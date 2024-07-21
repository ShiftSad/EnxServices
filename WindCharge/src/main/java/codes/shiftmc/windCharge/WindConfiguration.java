package codes.shiftmc.windCharge;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;

import java.util.Arrays;
import java.util.List;

public class WindConfiguration {

    private static WindConfiguration instance;

    boolean explodesBlocks;
    boolean damagesEntities;
    float knockbackMultiplier;
    List<Material> vulnerableBlocks;
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
        vulnerableBlocks = List.of();
    }

    public static WindConfiguration getInstance() {
        if (instance == null) {
            instance = new WindConfiguration();
        }
        return instance;
    }

    public void load(Configuration config) {
        explodesBlocks = config.getBoolean("config.afetar-blocos", explodesBlocks);
        damagesEntities = config.getBoolean("config.machuca-entidades", damagesEntities);
        knockbackMultiplier = (float) config.getDouble("config.multiplicador-knockback", knockbackMultiplier);
        radius = (float) config.getDouble("config.distancia-explosao", radius);

        var constrains = config.getStringList("config.blocos-vulneraveis");
        vulnerableBlocks = MaterialParser.parseMaterials(constrains);
    }
}
