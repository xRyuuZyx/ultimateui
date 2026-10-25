package id.roleplay.ui;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Setara SaveGame_Settings: simpan pengaturan tiap pemain ke settings.yml. */
public class SettingsStore {
    private final UltimateUIMC plugin;
    private final File file;
    private final Map<UUID, PlayerSettings> data = new HashMap<>();
    private boolean dirty;

    public SettingsStore(UltimateUIMC plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "settings.yml");
        load();
    }

    private void load() {
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = y.getConfigurationSection("players");
        if (root == null) return;
        for (String key : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(key);
            if (s == null) continue;
            try {
                PlayerSettings p = PlayerSettings.defaults(plugin.getConfig());
                p.master = s.getInt("master", p.master);
                p.music = s.getInt("music", p.music);
                p.sfx = s.getInt("sfx", p.sfx);
                p.ui = s.getInt("ui", p.ui);
                p.ambient = s.getInt("ambient", p.ambient);
                p.viewDistance = s.getInt("view", p.viewDistance);
                p.simDistance = s.getInt("sim", p.simDistance);
                p.showStats = s.getBoolean("stats", p.showStats);
                p.showHints = s.getBoolean("hints", p.showHints);
                p.msgStyle = PlayerSettings.parse(PlayerSettings.MsgStyle.class, s.getString("msg"), p.msgStyle);
                p.menuKey = PlayerSettings.parse(PlayerSettings.Hotkey.class, s.getString("menukey"), p.menuKey);
                p.pauseKey = PlayerSettings.parse(PlayerSettings.Hotkey.class, s.getString("pausekey"), p.pauseKey);
                data.put(UUID.fromString(key), p);
            } catch (IllegalArgumentException ignored) { }
        }
    }

    public PlayerSettings get(UUID id) {
        return data.computeIfAbsent(id, k -> PlayerSettings.defaults(plugin.getConfig()));
    }

    public void reset(UUID id) {
        data.put(id, PlayerSettings.defaults(plugin.getConfig()));
        dirty = true;
    }

    public void markDirty() { dirty = true; }

    public void saveIfDirty() { if (dirty) save(); }

    public void save() {
        YamlConfiguration y = new YamlConfiguration();
        for (Map.Entry<UUID, PlayerSettings> e : data.entrySet()) {
            String p = "players." + e.getKey() + ".";
            PlayerSettings s = e.getValue();
            y.set(p + "master", s.master);
            y.set(p + "music", s.music);
            y.set(p + "sfx", s.sfx);
            y.set(p + "ui", s.ui);
            y.set(p + "ambient", s.ambient);
            y.set(p + "view", s.viewDistance);
            y.set(p + "sim", s.simDistance);
            y.set(p + "stats", s.showStats);
            y.set(p + "hints", s.showHints);
            y.set(p + "msg", s.msgStyle.name());
            y.set(p + "menukey", s.menuKey.name());
            y.set(p + "pausekey", s.pauseKey.name());
        }
        try {
            plugin.getDataFolder().mkdirs();
            y.save(file);
            dirty = false;
        } catch (IOException ex) {
            plugin.getLogger().warning("Gagal menyimpan settings.yml: " + ex.getMessage());
        }
    }
}
