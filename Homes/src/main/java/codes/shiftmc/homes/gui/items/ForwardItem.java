package codes.shiftmc.homes.gui.items;

import org.bukkit.Material;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;

public class ForwardItem extends PageItem {

    public ForwardItem() {
        super(true);
    }

    @Override
    public ItemProvider getItemProvider(PagedGui<?> gui) {
        var builder = new ItemBuilder(Material.RED_STAINED_GLASS_PANE);
        builder.setDisplayName("§7Próxima")
                .addLoreLines(gui.hasNextPage() ?
                        "§7Vá para a página §e" + (gui.getCurrentPage() + 1) + "§7/§e" + gui.getPageAmount() :
                        "§cEssa é a primeira Ultima!"
                );
        return builder;
    }
}
