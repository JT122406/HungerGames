package tk.shanebee.adapters.v26;

import io.github.rvskele.paperlib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import tk.shanebee.adapters.v1_21.AdapterV1_21;

import java.util.List;
import java.util.Locale;

public class AdapterV26 extends AdapterV1_21 {
    @Override
    public String getWorldId(World world) {
        NamespacedKey k = keyOf(world);
        if (k == null) return super.getWorldId(world);
        return k.getNamespace() + "/" + k.getKey();
    }

    @Override
    public World findWorld(String id) {
        if (id == null || id.isEmpty()) return null;
        List<World> worlds = Bukkit.getWorlds();

        NamespacedKey key = parseKey(id);
        if (key != null) {
            if (PaperLib.isPaper()) {
                World w = Bukkit.getWorld(key);
                if (w != null) return w;
            } else {
                for (World w : worlds) {
                    if (key.equals(keyOf(w))) return w;
                }
            }
        }

        World w = super.findWorld(id);
        if (w != null) return w;

        if (!worlds.isEmpty()) {
            String main = worlds.getFirst().getName();
            NamespacedKey legacy = null;
            if (id.equalsIgnoreCase(main)) legacy = NamespacedKey.minecraft("overworld");
            else if (id.equalsIgnoreCase(main + "_nether")) legacy = NamespacedKey.minecraft("the_nether");
            else if (id.equalsIgnoreCase(main + "_the_end")) legacy = NamespacedKey.minecraft("the_end");
            if (legacy != null) {
                for (World world : worlds) {
                    if (legacy.equals(keyOf(world))) return world;
                }
            }
        }

        String path = id.substring(id.lastIndexOf('/') + 1).toLowerCase(Locale.ROOT);
        for (World world : worlds) {
            NamespacedKey k = keyOf(world);
            if (k != null && k.getKey().equals(path)) return world;
        }
        return null;
    }

    private static NamespacedKey keyOf(World world) {
        return world instanceof Keyed keyed ? keyed.getKey() : null;
    }

    private static NamespacedKey parseKey(String id) {
        int s = id.indexOf('/');
        String raw = s > 0 ? id.substring(0, s) + ":" + id.substring(s + 1) : id;
        if (raw.indexOf(':') <= 0) return null;
        try {
            return NamespacedKey.fromString(raw.toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}