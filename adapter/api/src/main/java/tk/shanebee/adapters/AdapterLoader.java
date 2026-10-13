package tk.shanebee.adapters;

import org.bukkit.Bukkit;

public final class AdapterLoader {
    public static VersionAdapter load() {
        String raw = Bukkit.getBukkitVersion().split("-")[0];
        String[] p = raw.split("\\.");
        int first = Integer.parseInt(p[0]);
        int minor = first == 1 ? Integer.parseInt(p[1]) : 1000 + first;

        String className;
        if (minor < 17) className = "tk.shanebee.adapters.legacy.AdapterLegacy";
        else {
            if (minor >= 1000) className = "tk.shanebee.adapters.v26.AdapterV26";
            else if (minor >= 21) className = "tk.shanebee.adapters.v1_21.AdapterV1_21";
            else className = "tk.shanebee.adapters.v1_17.AdapterV1_17";
        }

        try {
            return (VersionAdapter) Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to load " + className, e);
        }
    }
}