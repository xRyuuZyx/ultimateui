package id.roleplay.ui;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor, TabCompleter {
    private final UltimateUIMC plugin;

    public Commands(UltimateUIMC plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender s, Command c, String label, String[] a) {
        String name = c.getName().toLowerCase();
        if (name.equals("uiadmin")) { admin(s, a); return true; }

        if (!(s instanceof Player p)) { plugin.send(s, "<red>Hanya untuk pemain."); return true; }
        switch (name) {
            case "menu" -> Menus.openMain(plugin, p);
            case "pause" -> Menus.openPause(plugin, p);
            case "settings" -> {
                SettingsMenu.Tab tab = SettingsMenu.Tab.AUDIO;
                if (a.length > 0) {
                    for (SettingsMenu.Tab t : SettingsMenu.Tab.values()) if (t.name().equalsIgnoreCase(a[0])) tab = t;
                }
                SettingsMenu.open(plugin, p, tab);
            }
        }
        return true;
    }

    private void admin(CommandSender s, String[] a) {
        if (!s.hasPermission("ultimateui.admin")) { plugin.send(s, "<red>Tidak punya izin."); return; }
        String sub = a.length > 0 ? a[0].toLowerCase() : "";
        switch (sub) {
            case "reload" -> {
                plugin.reloadConfig();
                plugin.send(s, "<green>Config dimuat ulang.");
            }
            case "loading" -> {
                int seconds = 5;
                if (a.length > 1) {
                    try { seconds = Math.max(1, Math.min(60, Integer.parseInt(a[1]))); }
                    catch (NumberFormatException e) { plugin.send(s, "<red>Detik harus angka."); return; }
                }
                Player target = a.length > 2 ? Bukkit.getPlayerExact(a[2]) : (s instanceof Player pl ? pl : null);
                if (target == null) { plugin.send(s, "<red>Pemain tidak ditemukan."); return; }
                plugin.loading().show(target, seconds);
                plugin.send(s, "<green>Loading screen ditampilkan ke <yellow>" + target.getName() + "</yellow>.");
            }
            default -> plugin.send(s, "<gray>/uiadmin <reload|loading [detik] [pemain]>");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String label, String[] a) {
        List<String> out = new ArrayList<>();
        String name = c.getName().toLowerCase();
        if (name.equals("uiadmin") && a.length == 1) out.addAll(List.of("reload", "loading"));
        if (name.equals("uiadmin") && a.length == 3) Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
        if (name.equals("settings") && a.length == 1) for (SettingsMenu.Tab t : SettingsMenu.Tab.values()) out.add(t.name().toLowerCase());
        String last = a.length > 0 ? a[a.length - 1].toLowerCase() : "";
        out.removeIf(x -> !x.toLowerCase().startsWith(last));
        return out;
    }
}
