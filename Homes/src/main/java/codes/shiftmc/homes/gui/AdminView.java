package codes.shiftmc.homes.gui;

import codes.shiftmc.homes.UserController;
import codes.shiftmc.homes.gui.items.BackItem;
import codes.shiftmc.homes.gui.items.ForwardItem;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.builder.SkullBuilder;
import xyz.xenondevs.invui.item.impl.AsyncItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.AnvilWindow;

import java.util.ArrayList;
import java.util.List;

public class AdminView {

    public static void show(Player player) {
        var empty = new SimpleItem(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName("§7"));
        List<AsyncItem> items = UserController.getUsers().stream().map((user) -> {
            var headTexture = HeadTextureCacheHandler.getTexture(user.user().uuid());
            return new AsyncItem(new ItemBuilder(Material.STRUCTURE_VOID), () -> new SkullBuilder(headTexture)
                    .setDisplayName("§7" + user.user().username())
                    .addLoreLines("§7Clique para ver as homes"));
        }).toList();

        var playerGui = PagedGui.items()
                .setStructure(
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "# # # < # > # # #"
                )
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('#', empty)
                .addIngredient('<', new BackItem())
                .addIngredient('>', new ForwardItem())
                .setContent(new ArrayList<>(items))
                .build();

        var anvilGui = Gui.normal()
                .setStructure("x x x")
                .addIngredient('x', empty)
                .build();

        var anvilWindow = AnvilWindow.split()
                .setViewer(player)
                .setUpperGui(anvilGui)
                .setLowerGui(playerGui)
                .setTitle("§7Home Admin")
                .addRenameHandler((word) -> {
                    // Remove all items that don't match the search
                    playerGui.setContent(new ArrayList<>(items.stream().filter((item) -> {
                        var name = PlainTextComponentSerializer.plainText().serialize(item.getItemProvider().get().displayName()).substring(1);
                        System.out.println(name);
                        return name.toLowerCase().contains(word.toLowerCase());
                    }).toList()));
                })
                .build();

        anvilWindow.open();
    }
}
