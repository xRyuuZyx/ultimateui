package id.roleplay.ui;

import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

/** Setara Mix_Master / Mix_Music / Mix_SFX / Mix_UI / Mix_Ambient: volume Master x kategori per pemain. */
public class SoundService {
    public enum Category {
        MUSIC(SoundCategory.MUSIC), SFX(SoundCategory.PLAYERS), UI(SoundCategory.PLAYERS), AMBIENT(SoundCategory.AMBIENT);
        final SoundCategory sc;
        Category(SoundCategory sc) { this.sc = sc; }
    }

    private final UltimateUIMC plugin;
    private final Random random = new Random();
    private final Map<UUID, Long> nextMusic = new HashMap<>();
    private final Map<UUID, String> currentTrack = new HashMap<>();
    private final Map<UUID, Long> nextAmbient = new HashMap<>();

    public SoundService(UltimateUIMC plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 40L, 20L);
    }

    private static int vol(PlayerSettings s, Category c) {
        return switch (c) {
            case MUSIC -> s.music;
            case SFX -> s.sfx;
            case UI -> s.ui;
            case AMBIENT -> s.ambient;
        };
    }

    public void play(Player p, Category c, String key, float volume, float pitch) {
        if (key == null || key.isBlank()) return;
        PlayerSettings s = plugin.store().get(p.getUniqueId());
        float m = (s.master / 100f) * (vol(s, c) / 100f);
        if (m <= 0f) return;
        p.playSound(p.getLocation(), key, c.sc, Math.max(0.01f, volume * m), pitch);
    }

    /** Hentikan musik pemain; akan dimulai lagi otomatis dengan volume baru bila > 0. */
    public void refreshMusic(Player p) {
        String cur = currentTrack.remove(p.getUniqueId());
        if (cur != null) p.stopSound(cur, SoundCategory.MUSIC);
        nextMusic.remove(p.getUniqueId());
    }

    public void forget(UUID id) {
        nextMusic.remove(id);
        currentTrack.remove(id);
        nextAmbient.remove(id);
    }

    private void tick() {
        FileConfiguration cfg = plugin.getConfig();
        boolean music = cfg.getBoolean("music.enabled", false);
        boolean ambient = cfg.getBoolean("ambient.enabled", false);
        if (!music && !ambient) return;
        long now = System.currentTimeMillis();

        for (Player p : Bukkit.getOnlinePlayers()) {
            UUID id = p.getUniqueId();
            PlayerSettings s = plugin.store().get(id);

            if (music) {
                if (s.master * s.music <= 0) {
                    if (currentTrack.containsKey(id)) refreshMusic(p);
                } else if (now >= nextMusic.getOrDefault(id, 0L)) {
                    List<Map<?, ?>> list = cfg.getMapList("music.playlist");
                    if (!list.isEmpty()) {
                        Map<?, ?> t = list.get(random.nextInt(list.size()));
                        String snd = String.valueOf(t.get("sound"));
                        int len = t.get("length") instanceof Number n ? n.intValue() : 120;
                        play(p, Category.MUSIC, snd, 1f, 1f);
                        currentTrack.put(id, snd);
                        nextMusic.put(id, now + len * 1000L);
                    }
                }
            }

            if (ambient && now >= nextAmbient.getOrDefault(id, 0L)) {
                List<String> sounds = cfg.getStringList("ambient.sounds");
                if (!sounds.isEmpty()) {
                    play(p, Category.AMBIENT, sounds.get(random.nextInt(sounds.size())), 1f, 1f);
                }
                nextAmbient.put(id, now + cfg.getLong("ambient.interval-seconds", 45) * 1000L);
            }
        }
    }
}
