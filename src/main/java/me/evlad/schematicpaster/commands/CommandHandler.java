package me.evlad.schematicpaster.commands;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.evlad.schematicpaster.SchematicPaster;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {
    private final SchematicPaster plugin;

    public CommandHandler(SchematicPaster plugin) {
        this.plugin = plugin;
    }

    private boolean pasteSchematic(String schematicName, String worldName, int x, int y, int z) {
        var world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World `"+worldName+"` not found!");
            return false;
        }

        Clipboard clipboard;
        File file = new File(plugin.getDataFolder(), "schematics/" + schematicName);
        if (!file.exists()) {
            plugin.getLogger().warning("Could not find schematic `"+schematicName+"`");
            return false;
        }

        var weWorld = BukkitAdapter.adapt(world);
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(x, y, z))
                        .build();
                Operations.complete(operation);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {
            case 0, 1:
                return plugin.schematics;
            case 2:
                return Bukkit.getWorlds().stream().map(WorldInfo::getName).toList();
            case 3:
                return List.of("x");
            case 4:
                return List.of("y");
            case 5:
                return List.of("z");
        }

        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length < 5) {
            sender.sendMessage("§cInsufficient arguments");
            return true;
        }

        var x = Integer.parseInt(args[2]);
        var y = Integer.parseInt(args[3]);
        var z = Integer.parseInt(args[4]);
        if (!pasteSchematic(args[0], args[1], x, y, z)) {
            sender.sendMessage("§cAn error occurred while pasting the schematic, check the console for more details.");
        }

        return true;
    }
}
