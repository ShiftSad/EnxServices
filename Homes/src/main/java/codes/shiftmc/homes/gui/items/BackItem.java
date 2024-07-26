package codes.shiftmc.homes.gui.items;

import org.bukkit.Material;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;

public class BackItem extends PageItem {

    public BackItem() {
        super(false);
    }

    @Override
    public ItemProvider getItemProvider(PagedGui<?> gui) {
        var builder = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE);
        builder.setDisplayName("§7Anterior")
                .addLoreLines(gui.hasPreviousPage() ?
                        "§7Vá para a página §e" + (gui.getCurrentPage()) + "§7/§e" + gui.getPageAmount() :
                        "§cEssa é a primeira página!"
                );
        return builder;
    }
}
