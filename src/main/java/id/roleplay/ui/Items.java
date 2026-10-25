package id.roleplay.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public final class Items {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private Items() {}

    public static Component c(String s) {
        return MM.deserialize(s).decoration(TextDecoration.ITALIC, false);
    }

    public static ItemStack make(Material m, String name, String... lore) {
        return make(m, name, Arrays.asList(lore));
    }

    public static ItemStack make(Material m, String name, List<String> lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(c(name));
        if (!lore.isEmpty()) meta.lore(lore.stream().map(Items::c).toList());
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        it.setItemMeta(meta);
        return it;
    }

    public static ItemStack glow(ItemStack it) {
        ItemMeta meta = it.getItemMeta();
        meta.setEnchantmentGlintOverride(true);
        it.setItemMeta(meta);
        return it;
    }

    public static Material material(String name, Material fallback) {
        Material m = name == null ? null : Material.matchMaterial(name);
        return m == null ? fallback : m;
    }
}
