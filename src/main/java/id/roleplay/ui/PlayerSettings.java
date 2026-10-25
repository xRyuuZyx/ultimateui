package id.roleplay.ui;

import org.bukkit.configuration.file.FileConfiguration;

/** Setara FSettingsAudio / Graphics / Controls / UI pada Ultimate UI. */
public class PlayerSettings {
    public enum Hotkey {
        OFF("Mati"), F("F"), SHIFT_F("Shift + F");
        private final String label;
        Hotkey(String l) { label = l; }
        public String label() { return label; }
        public Hotkey next() { return values()[(ordinal() + 1) % values().length]; }
    }

    public enum MsgStyle {
        CHAT("Chat"), ACTIONBAR("Action bar"), TITLE("Judul layar");
        private final String label;
        MsgStyle(String l) { label = l; }
        public String label() { return label; }
        public MsgStyle next() { return values()[(ordinal() + 1) % values().length]; }
    }

    // Audio
    public int master, music, sfx, ui, ambient;
    // Grafis
    public int viewDistance, simDistance;
    public boolean showStats;
    // Kontrol
    public Hotkey menuKey, pauseKey;
    // UI
    public boolean showHints;
    public MsgStyle msgStyle;

    public static <E extends Enum<E>> E parse(Class<E> type, String s, E fallback) {
        if (s == null) return fallback;
        try { return Enum.valueOf(type, s.toUpperCase()); } catch (IllegalArgumentException e) { return fallback; }
    }

    public static PlayerSettings defaults(FileConfiguration c) {
        PlayerSettings s = new PlayerSettings();
        s.master = c.getInt("defaults.audio.master", 100);
        s.music = c.getInt("defaults.audio.music", 60);
        s.sfx = c.getInt("defaults.audio.sfx", 100);
        s.ui = c.getInt("defaults.audio.ui", 100);
        s.ambient = c.getInt("defaults.audio.ambient", 100);
        s.viewDistance = c.getInt("defaults.view-distance", 10);
        s.simDistance = c.getInt("defaults.simulation-distance", 8);
        s.showStats = c.getBoolean("defaults.show-stats", false);
        s.showHints = c.getBoolean("defaults.show-hints", true);
        s.msgStyle = parse(MsgStyle.class, c.getString("defaults.message-style"), MsgStyle.ACTIONBAR);
        s.menuKey = parse(Hotkey.class, c.getString("defaults.menu-hotkey"), Hotkey.SHIFT_F);
        s.pauseKey = parse(Hotkey.class, c.getString("defaults.pause-hotkey"), Hotkey.OFF);
        return s;
    }
}
