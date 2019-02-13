package at.jellybit.mc;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.Plugin;

import java.util.logging.Level;

@Plugin(name="MysteryPack", version="1.0")
public class MysteryPack extends JavaPlugin {

    @Override
    public void onEnable() {
        super.onEnable();

        getLogger().log(Level.SEVERE, "PLUGIN ENABLED");
    }
}
