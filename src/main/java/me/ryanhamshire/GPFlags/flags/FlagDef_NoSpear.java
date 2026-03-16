package me.ryanhamshire.GPFlags.flags;

import me.ryanhamshire.GPFlags.Flag;
import me.ryanhamshire.GPFlags.FlagManager;
import me.ryanhamshire.GPFlags.GPFlags;
import me.ryanhamshire.GPFlags.MessageSpecifier;
import me.ryanhamshire.GPFlags.Messages;
import me.ryanhamshire.GPFlags.TextMode;
import me.ryanhamshire.GPFlags.util.MessagingUtil;
import me.ryanhamshire.GPFlags.util.Util;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Arrays;
import java.util.List;

public class FlagDef_NoSpear extends FlagDefinition {

    public FlagDef_NoSpear(FlagManager manager, GPFlags plugin) {
        super(manager, plugin);
    }

    /**
     * Check if the entity is a Spear
     */
    private boolean isSpear(Entity entity) {
        String typeName = entity.getType().name().toUpperCase();
        return typeName.contains("SPEAR");
    }

    /**
     * Block spear launching from within flagged areas
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!isSpear(event.getEntity())) return;

        Projectile projectile = event.getEntity();
        if (!(projectile.getShooter() instanceof Player)) return;

        Player player = (Player) projectile.getShooter();

        Flag flag = this.getFlagInstanceAtLocation(player.getLocation(), player);
        if (flag == null) return;

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), false, null);
        if (Util.shouldBypass(player, claim, flag)) return;

        event.setCancelled(true);
        MessagingUtil.sendMessage(player, TextMode.Warn + plugin.getFlagsDataStore().getMessage(Messages.NoSpearInClaim));
    }

    /**
     * Block spears from hitting entities inside flagged claims (protects from external attacks)
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!isSpear(event.getEntity())) return;

        Projectile projectile = event.getEntity();
        ProjectileSource shooter = projectile.getShooter();
        if (!(shooter instanceof Player)) return;

        Player player = (Player) shooter;

        Location hitLocation = null;
        if (event.getHitEntity() != null) {
            hitLocation = event.getHitEntity().getLocation();
        } else if (event.getHitBlock() != null) {
            hitLocation = event.getHitBlock().getLocation();
        }

        if (hitLocation == null) return;

        Flag flag = this.getFlagInstanceAtLocation(hitLocation, player);
        if (flag == null) return;

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(hitLocation, false, null);
        if (claim == null) return;

        if (Util.shouldBypass(player, claim, flag)) return;

        event.setCancelled(true);
        projectile.remove();
        MessagingUtil.sendMessage(player, TextMode.Warn + plugin.getFlagsDataStore().getMessage(Messages.NoSpearIntoClaim));
    }

    @Override
    public String getName() {
        return "NoSpear";
    }

    @Override
    public MessageSpecifier getSetMessage(String parameters) {
        return new MessageSpecifier(Messages.EnableNoSpear);
    }

    @Override
    public MessageSpecifier getUnSetMessage() {
        return new MessageSpecifier(Messages.DisableNoSpear);
    }

    @Override
    public List<FlagType> getFlagType() {
        return Arrays.asList(FlagType.CLAIM, FlagType.DEFAULT, FlagType.WORLD, FlagType.SERVER);
    }

}
