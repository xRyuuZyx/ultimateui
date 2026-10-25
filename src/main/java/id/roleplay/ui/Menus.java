package id.roleplay.ui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Menu utama (WBP_Menu_Main) dan menu jeda (WBP_Menu_Pause) yang dibaca dari config. */
public final class Menus {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private Menus() {}

    public static void openMain(UltimateUIMC plugin, Player p) { open(plugin, p, "main-menu"); }

    public static void openPause(UltimateUIMC plugin, Player p) { open(plugin, p, "pause-menu"); }

    private static void open(UltimateUIMC plugin, Player p, String path) {
        var cfg = plugin.getConfig();
        Menu menu = new Menu(MM.deserialize(cfg.getString(path + ".title", "Menu")), Math.max(1, Math.min(6, cfg.getInt(path + ".rows", 3))));
        Material filler = Items.material(cfg.getString(path + ".filler"), Material.BLACK_STAINED_GLASS_PANE);
        menu.fill(filler);

        for (Map<?, ?> b : cfg.getMapList(path + ".buttons")) {
            int slot = b.get("slot") instanceof Number n ? n.intValue() : 0;
            Material mat = Items.material(String.valueOf(b.get("material")), Material.STONE);
            String name = String.valueOf(b.get("name")).replace("%player%", p.getName());
            List<String> lore = new ArrayList<>();
            if (b.get("lore") instanceof List<?> l) for (Object o : l) lore.add(String.valueOf(o).replace("%player%", p.getName()));

            ItemStack item = Items.make(mat, name, lore);
            if (mat == Material.PLAYER_HEAD && item.getItemMeta() instanceof SkullMeta sm) {
                sm.setOwningPlayer(p);
                item.setItemMeta(sm);
            }
            menu.set(slot, item, pl -> run(plugin, pl, b, path));
        }
        menu.open(p);
    }

    private static void run(UltimateUIMC plugin, Player p, Map<?, ?> b, String path) {
        String action = b.get("action") == null ? "" : String.valueOf(b.get("action")).toLowerCase();
        switch (action) {
            case "settings" -> SettingsMenu.open(plugin, p, SettingsMenu.Tab.AUDIO);
            case "pause" -> openPause(plugin, p);
            case "menu" -> openMain(plugin, p);
            case "close" -> p.closeInventory();
            case "quit" -> plugin.messages().confirm(p, "Keluar dari server?", "Kamu akan terputus dari server.",
                    () -> p.kick(MM.deserialize(plugin.getConfig().getString("pause-menu.quit-message", "Sampai jumpa!"))),
                    () -> openPause(plugin, p));
            default -> {
                Object cmd = b.get("command");
                if (cmd == null) return;
                p.closeInventory();
                String line = String.valueOf(cmd).replace("%player%", p.getName());
                if ("console".equalsIgnoreCase(String.valueOf(b.get("as")))) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), line);
                } else {
                    p.performCommand(line);
                }
            }
        }
    }
}
