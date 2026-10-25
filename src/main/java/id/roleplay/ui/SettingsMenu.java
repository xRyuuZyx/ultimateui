package id.roleplay.ui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

/** Setara WBP_Menu_Settings + tab Audio / Grafis / Kontrol / UI, lengkap dengan slider (WBP_Slider). */
public class SettingsMenu {
    public enum Tab {
        AUDIO("Audio", Material.NOTE_BLOCK), GRAFIS("Grafis", Material.SPYGLASS),
        KONTROL("Kontrol", Material.LEVER), UI("UI", Material.OAK_SIGN);
        final String label;
        final Material icon;
        Tab(String l, Material i) { label = l; icon = i; }
    }

    private static final int BAR = 6;
    private final UltimateUIMC plugin;
    private final Player p;
    private final Menu menu;
    private Tab tab;

    private SettingsMenu(UltimateUIMC plugin, Player p, Tab tab) {
        this.plugin = plugin;
        this.p = p;
        this.tab = tab;
        this.menu = new Menu(MiniMessage.miniMessage().deserialize("<dark_gray>Pengaturan"), 6);
    }

    public static void open(UltimateUIMC plugin, Player p, Tab tab) {
        SettingsMenu sm = new SettingsMenu(plugin, p, tab);
        sm.render();
        sm.menu.open(p);
    }

    private PlayerSettings s() { return plugin.store().get(p.getUniqueId()); }

    private void render() {
        menu.clear();
        menu.fill(Material.BLACK_STAINED_GLASS_PANE);

        menu.set(0, Items.make(Material.ARROW, "<yellow>← Kembali"), pl -> Menus.openMain(plugin, pl));
        menu.set(8, Items.make(Material.BARRIER, "<red>Reset ke bawaan", "<gray>Kembalikan semua pengaturan"), pl ->
                plugin.messages().confirm(pl, "Reset pengaturan?", "Semua pengaturanmu kembali ke bawaan.", () -> {
                    plugin.store().reset(pl.getUniqueId());
                    plugin.applyGraphics(pl);
                    plugin.updateStats(pl);
                    plugin.sounds().refreshMusic(pl);
                    open(plugin, pl, tab);
                }, () -> open(plugin, pl, tab)));

        int[] slots = {2, 3, 4, 5};
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            Tab t = tabs[i];
            var item = Items.make(t.icon, (t == tab ? "<gold><bold>" : "<white>") + t.label);
            if (t == tab) Items.glow(item);
            menu.set(slots[i], item, pl -> { tab = t; render(); });
        }

        switch (tab) {
            case AUDIO -> audio();
            case GRAFIS -> graphics();
            case KONTROL -> controls();
            case UI -> ui();
        }
    }

    // ---------------- Tab ----------------

    private void audio() {
        PlayerSettings s = s();
        var UI = SoundService.Category.UI;
        slider(0, Material.JUKEBOX, "Volume Utama", s.master, 0, 100, 10, v -> s.master = v, "%", this::audioChanged,
                () -> sample(UI));
        slider(1, Material.NOTE_BLOCK, "Musik", s.music, 0, 100, 10, v -> s.music = v, "%", this::audioChanged,
                () -> plugin.messages().show(p, "<gray>Musik memakai playlist di config.yml"));
        slider(2, Material.BELL, "Efek Suara (SFX)", s.sfx, 0, 100, 10, v -> s.sfx = v, "%", this::audioChanged,
                () -> plugin.sounds().play(p, SoundService.Category.SFX, "minecraft:entity.experience_orb.pickup", 1f, 1f));
        slider(3, Material.OAK_BUTTON, "Suara UI", s.ui, 0, 100, 10, v -> s.ui = v, "%", this::audioChanged,
                () -> sample(UI));
        slider(4, Material.FEATHER, "Suara Lingkungan", s.ambient, 0, 100, 10, v -> s.ambient = v, "%", this::audioChanged,
                () -> plugin.sounds().play(p, SoundService.Category.AMBIENT, "minecraft:ambient.cave", 1f, 1f));
    }

    private void sample(SoundService.Category c) {
        plugin.sounds().play(p, c, plugin.getConfig().getString("ui-sound.click"), 1f, 1f);
    }

    private void audioChanged() {
        plugin.sounds().refreshMusic(p);
    }

    private void graphics() {
        PlayerSettings s = s();
        int vMin = plugin.getConfig().getInt("limits.view-distance.min", 4);
        int vMax = plugin.getConfig().getInt("limits.view-distance.max", 16);
        int sMin = plugin.getConfig().getInt("limits.simulation-distance.min", 3);
        int sMax = plugin.getConfig().getInt("limits.simulation-distance.max", 10);

        slider(0, Material.SPYGLASS, "Jarak Pandang", clamp(s.viewDistance, vMin, vMax), vMin, vMax, 1,
                v -> s.viewDistance = v, " chunk", () -> plugin.applyGraphics(p), null);
        slider(1, Material.REPEATER, "Jarak Simulasi", clamp(s.simDistance, sMin, sMax), sMin, sMax, 1,
                v -> s.simDistance = v, " chunk", () -> plugin.applyGraphics(p), null);
        toggle(2, Material.CLOCK, "Tampilkan Ping & TPS", s.showStats, v -> s.showStats = v,
                () -> plugin.updateStats(p), "<gray>Tampil di bagian bawah tab list.");
    }

    private void controls() {
        PlayerSettings s = s();
        cycle(0, Material.COMPASS, "Tombol Menu Utama", s.menuKey.label(), () -> s.menuKey = s.menuKey.next(),
                "<gray>Klik untuk ganti. Tombol F = tukar tangan.");
        cycle(1, Material.CLOCK, "Tombol Menu Jeda", s.pauseKey.label(), () -> s.pauseKey = s.pauseKey.next(),
                "<gray>Esc tidak bisa dipakai, jadi pilih tombol lain.");
        menu.set(3 * 9, Items.make(Material.BOOK, "<gold>Info", "<gray>Perintah: <white>/menu</white>, <white>/pause</white>, <white>/settings</white>"), null);
    }

    private void ui() {
        PlayerSettings s = s();
        toggle(0, Material.OAK_SIGN, "Tampilkan Petunjuk", s.showHints, v -> s.showHints = v, null,
                "<gray>Tips singkat saat bermain.");
        cycle(1, Material.WRITABLE_BOOK, "Gaya Pesan", s.msgStyle.label(), () -> s.msgStyle = s.msgStyle.next(),
                "<gray>Chat / action bar / judul layar.");
        menu.set(2 * 9, Items.make(Material.NAME_TAG, "<gold>HUD", "<gray>Nyalakan / matikan HUD (butuh RoleplayHUD)"),
                pl -> { pl.closeInventory(); pl.performCommand("hud toggle"); });
        menu.set(2 * 9 + 4, Items.make(Material.LIME_DYE, "<green>Klik untuk toggle HUD"),
                pl -> { pl.closeInventory(); pl.performCommand("hud toggle"); });
    }

    // ---------------- Komponen (slider / toggle / cycle) ----------------

    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }

    private void changed(Runnable after) {
        plugin.store().markDirty();
        if (after != null) after.run();
        render();
    }

    private void slider(int row, Material icon, String label, int value, int min, int max, int step,
                        IntConsumer setter, String unit, Runnable after, Runnable sample) {
        int base = (1 + row) * 9;
        String val = "<white>" + value + unit;

        menu.set(base, Items.make(icon, "<gold>" + label, "<gray>Nilai: " + val, sample != null ? "<dark_gray>Klik untuk tes" : ""),
                pl -> { if (sample != null) sample.run(); });
        menu.set(base + 1, Items.make(Material.ARROW, "<red>− " + step + unit),
                pl -> { setter.accept(clamp(value - step, min, max)); changed(after); });
        menu.set(base + 8, Items.make(Material.ARROW, "<green>+ " + step + unit),
                pl -> { setter.accept(clamp(value + step, min, max)); changed(after); });

        int filled = (int) Math.round((value - min) * (double) BAR / (max - min));
        for (int i = 0; i < BAR; i++) {
            final int idx = i;
            Material m = i < filled ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
            menu.set(base + 2 + i, Items.make(m, "<gold>" + label + ": " + val), pl -> {
                int target = min + (int) Math.round((idx + 1) * (max - min) / (double) BAR);
                target = min + Math.round((float) (target - min) / step) * step;
                setter.accept(clamp(target, min, max));
                changed(after);
            });
        }
    }

    private void toggle(int row, Material icon, String label, boolean value, Consumer<Boolean> setter,
                        Runnable after, String desc) {
        int base = (1 + row) * 9;
        Runnable flip = () -> { setter.accept(!value); changed(after); };
        menu.set(base, Items.make(icon, "<gold>" + label, desc), pl -> flip.run());
        menu.set(base + 4, value
                        ? Items.make(Material.LIME_DYE, "<green>AKTIF", "<gray>Klik untuk mematikan")
                        : Items.make(Material.GRAY_DYE, "<red>MATI", "<gray>Klik untuk menyalakan"),
                pl -> flip.run());
    }

    private void cycle(int row, Material icon, String label, String current, Runnable next, String desc) {
        int base = (1 + row) * 9;
        Runnable go = () -> { next.run(); changed(null); };
        menu.set(base, Items.make(icon, "<gold>" + label, desc), pl -> go.run());
        menu.set(base + 4, Items.make(Material.PAPER, "<white>" + current, "<gray>Klik untuk ganti"), pl -> go.run());
    }
}
