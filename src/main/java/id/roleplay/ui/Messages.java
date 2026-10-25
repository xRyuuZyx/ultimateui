package id.roleplay.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.Duration;

/** Setara WBP_Message / FMessage: pesan singkat dan dialog konfirmasi. */
public class Messages {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final UltimateUIMC plugin;

    public Messages(UltimateUIMC plugin) { this.plugin = plugin; }

    /** Tampilkan pesan sesuai gaya pesan pilihan pemain (chat / action bar / judul). */
    public void show(Player p, String mini) {
        PlayerSettings s = plugin.store().get(p.getUniqueId());
        Component msg = MM.deserialize(mini);
        switch (s.msgStyle) {
            case CHAT -> p.sendMessage(MM.deserialize(plugin.getConfig().getString("prefix", "")).append(msg));
            case ACTIONBAR -> p.sendActionBar(msg);
            case TITLE -> p.showTitle(Title.title(Component.empty(), msg,
                    Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(400))));
        }
    }

    /** Petunjuk: hanya tampil kalau pemain mengaktifkan petunjuk. */
    public void hint(Player p, String mini) {
        if (plugin.store().get(p.getUniqueId()).showHints) show(p, mini);
    }

    public void confirm(Player p, String title, String description, Runnable yes, Runnable no) {
        Menu m = new Menu(MM.deserialize("<dark_gray>" + title), 3);
        m.fill(Material.GRAY_STAINED_GLASS_PANE);
        m.set(13, Items.make(Material.PAPER, "<white>" + title, "<gray>" + description), null);
        m.set(11, Items.make(Material.LIME_CONCRETE, "<green>✔ Ya"), pl -> {
            pl.closeInventory();
            plugin.sounds().play(pl, SoundService.Category.UI, plugin.getConfig().getString("ui-sound.confirm"), 1f, 1f);
            if (yes != null) yes.run();
        });
        m.set(15, Items.make(Material.RED_CONCRETE, "<red>✘ Batal"), pl -> {
            pl.closeInventory();
            plugin.sounds().play(pl, SoundService.Category.UI, plugin.getConfig().getString("ui-sound.deny"), 1f, 0.8f);
            if (no != null) no.run();
        });
        m.open(p);
    }
}
