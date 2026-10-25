package id.roleplay.ui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Setara WBP_LoadingScreen / DT_Loading: layar hitam + judul + bar progres + tips. */
public class LoadingScreen implements Listener {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final UltimateUIMC plugin;
    private final Map<UUID, BukkitTask> active = new HashMap<>();

    public LoadingScreen(UltimateUIMC plugin) { this.plugin = plugin; }

    public boolean isLoading(UUID id) { return active.containsKey(id); }

    public void show(Player p, int seconds) {
        stop(p, false);
        FileConfiguration cfg = plugin.getConfig();
        int total = Math.max(1, seconds) * 20;
        List<String> tips = cfg.getStringList("loading.tips");

        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, total + 20, 0, false, false, false));
        p.showTitle(Title.title(
                MM.deserialize(cfg.getString("loading.title", "")),
                MM.deserialize(cfg.getString("loading.subtitle", "")),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(seconds), Duration.ofMillis(600))));

        final int[] elapsed = {0};
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!p.isOnline()) { stop(p, false); return; }
            elapsed[0] += 4;
            if (elapsed[0] >= total) { stop(p, true); return; }
            int pct = elapsed[0] * 100 / total;
            int filled = pct / 5;
            String bar = "<gray>[<green>" + "█".repeat(filled) + "<dark_gray>" + "█".repeat(20 - filled)
                    + "<gray>] <white>" + pct + "%";
            String tip = tips.isEmpty() ? "" : "  <dark_gray>|  <gray>" + tips.get((elapsed[0] / 40) % tips.size());
            p.sendActionBar(MM.deserialize(bar + tip));
        }, 0L, 4L);
        active.put(p.getUniqueId(), task);
    }

    public void stop(Player p, boolean finished) {
        BukkitTask t = active.remove(p.getUniqueId());
        if (t != null) t.cancel();
        if (t != null && p.isOnline()) {
            p.removePotionEffect(PotionEffectType.BLINDNESS);
            if (finished) {
                plugin.sounds().play(p, SoundService.Category.SFX, "minecraft:entity.player.levelup", 0.6f, 1.2f);
                p.sendActionBar(net.kyori.adventure.text.Component.empty());
            }
        }
    }

    public void stopAll() {
        for (UUID id : new java.util.ArrayList<>(active.keySet())) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) stop(p, false);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (active.containsKey(e.getPlayer().getUniqueId()) && e.hasChangedPosition()) e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && active.containsKey(p.getUniqueId())) e.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        stop(e.getPlayer(), false);
    }
}
