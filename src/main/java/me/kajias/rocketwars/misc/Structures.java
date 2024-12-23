package me.kajias.rocketwars.misc;

import com.github.shynixn.structureblocklib.api.bukkit.StructureBlockLibApi;
import com.github.shynixn.structureblocklib.api.enumeration.StructureRotation;
import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.objects.enums.TeamColor;
import org.bukkit.Location;

import java.nio.file.Path;
import java.util.logging.Level;

public class Structures {
    public static void spawnRocketStructure(Location location, String structureName, TeamColor teamColor) {
        Path path = RocketWars.INSTANCE.getDataFolder().toPath().resolve("structures/" + structureName + ".nbt");
        int offsetZ = 4;

        StructureRotation rotation = StructureRotation.ROTATION_270;
        if (teamColor == TeamColor.RED) {
            rotation = StructureRotation.ROTATION_90;
            offsetZ = -offsetZ;
        }

        StructureBlockLibApi.INSTANCE
                .loadStructure(RocketWars.INSTANCE)
                .at(location.clone().subtract(0, 4, offsetZ))
                .rotation(rotation)
                .loadFromPath(path)
                .onException(e -> RocketWars.INSTANCE.getLogger().log(Level.SEVERE, "Failed to load structure.", e));
    }

    public static void spawnShieldStructure(Location location, String structureName) {
        Path path = RocketWars.INSTANCE.getDataFolder().toPath().resolve("structures/" + structureName + ".nbt");

        StructureRotation rotation = StructureRotation.ROTATION_90;

        StructureBlockLibApi.INSTANCE
                .loadStructure(RocketWars.INSTANCE)
                .at(location)
                .rotation(rotation)
                .loadFromPath(path)
                .onException(e -> RocketWars.INSTANCE.getLogger().log(Level.SEVERE, "Failed to load structure.", e));
    }

    public static void spawnBonusCubeStructure(Location location, String structureName) {
        Path path = RocketWars.INSTANCE.getDataFolder().toPath().resolve("structures/" + structureName + ".nbt");

        StructureBlockLibApi.INSTANCE
                .loadStructure(RocketWars.INSTANCE)
                .at(location)
                .loadFromPath(path)
                .onException(e -> RocketWars.INSTANCE.getLogger().log(Level.SEVERE, "Failed to load structure.", e));
    }

    public static void spawnShipStructure(Location location, String structureName, StructureRotation rotation) {
        Path path = RocketWars.INSTANCE.getDataFolder().toPath().resolve("structures/" + structureName + ".nbt");

        StructureBlockLibApi.INSTANCE
                .loadStructure(RocketWars.INSTANCE)
                .at(location)
                .includeEntities(true)
                .rotation(rotation)
                .loadFromPath(path)
                .onException(e -> RocketWars.INSTANCE.getLogger().log(Level.SEVERE, "Failed to load structure.", e));
    }
}
