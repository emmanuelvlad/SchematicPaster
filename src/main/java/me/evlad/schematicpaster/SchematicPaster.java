package me.evlad.schematicpaster;

import me.evlad.schematicpaster.commands.CommandHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class SchematicPaster extends JavaPlugin {
    private static SchematicPaster instance;
    public List<String> schematics = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;
        // Creates the schematics folder
        File schematicsFolder = new File(getDataFolder(), "schematics");
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }
        var files = schematicsFolder.listFiles();
        if (files != null) {
            for (var file : files) {
                schematics.add(file.getName());
            }
        }

        var commandHandler = new CommandHandler(this);
        getCommand("schempaster").setExecutor(commandHandler);
        getCommand("schempaster").setTabCompleter(commandHandler);
    }

    @Override
    public void onDisable() {}

    public static SchematicPaster getInstance() { return instance; }
}
