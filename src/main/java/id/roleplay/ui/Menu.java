package id.roleplay.ui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/** Dasar semua menu (setara WBP_Base): chest GUI dengan tombol berisi aksi. */
public class Menu implements InventoryHolder {
    private final Inventory inv;
    private final Map<Integer, Consumer<Player>> actions = new HashMap<>();

    public Menu(Component title, int rows) {
        this.inv = Bukkit.createInventory(this, rows * 9, title);
    }

    @Override public Inventory getInventory() { return inv; }

    public void clear() {
        inv.clear();
        actions.clear();
    }

    public void fill(Material pane) {
        ItemStack filler = Items.make(pane, " ");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);
    }

    public void set(int slot, ItemStack item, Consumer<Player> onClick) {
        if (slot < 0 || slot >= inv.getSize()) return;
        inv.setItem(slot, item);
        if (onClick != null) actions.put(slot, onClick); else actions.remove(slot);
    }

    public void open(Player p) { p.openInventory(inv); }

    public boolean click(int rawSlot, Player p) {
        Consumer<Player> a = actions.get(rawSlot);
        if (a == null) return false;
        a.accept(p);
        return true;
    }
}
