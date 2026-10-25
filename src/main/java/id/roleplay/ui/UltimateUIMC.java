package id.roleplay.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public class UltimateUIMC extends JavaPlugin implements Listener {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private SettingsStore store;
    private SoundService sounds;
    private Messages messages;
    private LoadingScreen loading;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        store = new SettingsStore(this);
        sounds = new SoundService(this);
        messages = new Messages(this);
        loading = new LoadingScreen(this);

        Commands cmds = new Commands(this);
        for (String name : new String[]{"menu", "pause", "settings", "uiadmin"}) {
            getCommand(name).setExecutor(cmds);
            getCommand(name).setTabCompleter(cmds);
        }
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(loading, this);

        // Penghitung ping & TPS (setara WBP_FrameRate) + simpan berkala
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (store.get(p.getUniqueId()).showStats) updateStats(p);
            }
        }, 40L, 20L);
        Bukkit.getScheduler().runTaskTimer(this, store::saveIfDirty, 1200L, 1200L);
    }

    @Override
    public void onDisable() {
        if (loading != null) loading.stopAll();
        if (store != null) store.save();
    }

    // ---------------- Event ----------------

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!p.isOnline()) return;
            applyGraphics(p);
            updateStats(p);
            if (getConfig().getBoolean("loading.on-join", true)) {
                loading.show(p, getConfig().getInt("loading.seconds", 5));
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    if (p.isOnline()) messages.hint(p, "<gray>Tekan <white>"
                            + store.get(p.getUniqueId()).menuKey.label() + "</white> atau ketik <white>/menu</white> untuk membuka menu.");
                }, getConfig().getInt("loading.seconds", 5) * 20L + 20L);
            }
        }, 10L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        sounds.forget(e.getPlayer().getUniqueId());
    }

    /** Tombol pintas (setara Key Selector): F / Shift+F membuka menu. */
    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent e) {
        Player p = e.getPlayer();
        if (loading.isLoading(p.getUniqueId())) return;
        PlayerSettings s = store.get(p.getUniqueId());
        PlayerSettings.Hotkey pressed = p.isSneaking() ? PlayerSettings.Hotkey.SHIFT_F : PlayerSettings.Hotkey.F;
        if (s.menuKey == pressed) {
            e.setCancelled(true);
            Menus.openMain(this, p);
        } else if (s.pauseKey == pressed) {
            e.setCancelled(true);
            Menus.openPause(this, p);
        }
    }

    // ---------------- Pengaturan yang diterapkan ----------------

    public void applyGraphics(Player p) {
        PlayerSettings s = store.get(p.getUniqueId());
        int vMin = getConfig().getInt("limits.view-distance.min", 4);
        int vMax = getConfig().getInt("limits.view-distance.max", 16);
        int sMin = getConfig().getInt("limits.simulation-distance.min", 3);
        int sMax = getConfig().getInt("limits.simulation-distance.max", 10);
        p.setViewDistance(Math.max(vMin, Math.min(vMax, s.viewDistance)));
        p.setSimulationDistance(Math.max(sMin, Math.min(sMax, s.simDistance)));
    }

    public void updateStats(Player p) {
        PlayerSettings s = store.get(p.getUniqueId());
        if (!s.showStats) {
            p.sendPlayerListFooter(Component.empty());
            return;
        }
        int ping = p.getPing();
        double tps = Math.min(20.0, Bukkit.getTPS()[0]);
        String pc = ping < 100 ? "<green>" : ping < 200 ? "<yellow>" : "<red>";
        String tc = tps >= 18 ? "<green>" : tps >= 15 ? "<yellow>" : "<red>";
        p.sendPlayerListFooter(MM.deserialize("<gray>Ping: " + pc + ping + " ms <dark_gray>| <gray>TPS: " + tc
                + String.format(Locale.US, "%.1f", tps)));
    }

    // ---------------- Akses ----------------

    public SettingsStore store() { return store; }
    public SoundService sounds() { return sounds; }
    public Messages messages() { return messages; }
    public LoadingScreen loading() { return loading; }

    public void send(CommandSender to, String miniMessage) {
        to.sendMessage(MM.deserialize(getConfig().getString("prefix", "") + miniMessage));
    }
}
