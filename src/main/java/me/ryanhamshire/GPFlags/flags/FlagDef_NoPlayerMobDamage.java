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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Arrays;
import java.util.List;

public class FlagDef_NoPlayerMobDamage extends FlagDefinition {

    public FlagDef_NoPlayerMobDamage(FlagManager manager, GPFlags plugin) {
        super(manager, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();

        if (target instanceof Player) return;
        if (!(target instanceof LivingEntity)) return;

        Player attacker = getPlayerAttacker(event);
        if (attacker == null) return;

        Flag flag = this.getFlagInstanceAtLocation(target.getLocation(), attacker);
        if (flag == null) return;

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(target.getLocation(), false, null);

        if (Util.shouldBypass(attacker, claim, flag)) return;

        if (claim != null && Util.canBuild(claim, attacker)) return;

        event.setCancelled(true);
        MessagingUtil.sendMessage(attacker, TextMode.Warn + plugin.getFlagsDataStore().getMessage(Messages.NoPlayerMobDamageMessage));
    }

    private Player getPlayerAttacker(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager.getType() == EntityType.PLAYER) {
            return (Player) damager;
        }
        if (damager instanceof Projectile) {
            ProjectileSource source = ((Projectile) damager).getShooter();
            if (source instanceof Player) {
                return (Player) source;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "NoPlayerMobDamage";
    }

    @Override
    public MessageSpecifier getSetMessage(String parameters) {
        return new MessageSpecifier(Messages.EnableNoPlayerMobDamage);
    }

    @Override
    public MessageSpecifier getUnSetMessage() {
        return new MessageSpecifier(Messages.DisableNoPlayerMobDamage);
    }

    @Override
    public List<FlagType> getFlagType() {
        return Arrays.asList(FlagType.CLAIM, FlagType.DEFAULT, FlagType.WORLD, FlagType.SERVER);
    }

}
