package io.github.spaicygaming.anticommandsuggestions.util;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.RegionResultSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class RegionsUtil {

    private RegionsUtil() {
    }

    public static List<String> getRegionsNames(Location location) {
        return getWGRegions(location).getRegions().stream().map(ProtectedRegion::getId).collect(Collectors.toList());
    }

    public static ApplicableRegionSet getWGRegions(Location loc) {
        RegionManager manager = getWGRegionManager(loc.getWorld());
        try {
            Vector ve = new Vector(loc.getX(), loc.getY(), loc.getZ());
            return manager.getApplicableRegions(ve);
        }
        // For post 7 beta 2 WorldEdit/WorldGuard support
        catch (NoClassDefFoundError post7Beta2version) {
            BlockVector3 blockVector = BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
            try {
                @SuppressWarnings("JavaReflectionMemberAccess") Method method = manager.getClass().getMethod("getApplicableRegions", BlockVector3.class);
                Object invokeValue = method.invoke(manager, blockVector);
                return (ApplicableRegionSet) invokeValue;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                Bukkit.getServer().getLogger().severe("Your WorldGuard version seems to be incompatible! Please, use a supported version");
                return new RegionResultSet(Collections.emptyList(), null);
            }
        }
    }

    public static RegionManager getWGRegionManager(World world) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(world));
    }


    /**
     * Get, if present, the name of the {@link ClaimedResidence} at the given location
     *
     * @param loc The bukkit location
     * @return null if there isn't any residence at the given location
     */
    public static String getResidenceRegionName(Location loc) {
        ClaimedResidence claimedResidence = Residence.getInstance().getResidenceManager().getByLoc(loc);
        return claimedResidence == null ? null : claimedResidence.getName();
    }


}
