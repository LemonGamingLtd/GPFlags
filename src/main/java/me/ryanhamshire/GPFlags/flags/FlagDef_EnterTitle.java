package me.ryanhamshire.GPFlags.flags;

import me.ryanhamshire.GPFlags.Flag;
import me.ryanhamshire.GPFlags.FlagManager;
import me.ryanhamshire.GPFlags.GPFlags;
import me.ryanhamshire.GPFlags.MessageSpecifier;
import me.ryanhamshire.GPFlags.Messages;
import me.ryanhamshire.GPFlags.SetFlagResult;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class FlagDef_EnterTitle extends PlayerMovementFlagDefinition {

    public FlagDef_EnterTitle(FlagManager manager, GPFlags plugin) {
        super(manager, plugin);
    }

    @Override
    public void onChangeClaim(Player player, Location lastLocation, Location to, Claim claimFrom, Claim claimTo) {
        if (claimTo == null) return;
        Flag flagTo = plugin.getFlagManager().getEffectiveFlag(to, this.getName(), claimTo);
        if (flagTo == null) return;
        Flag flagFrom = plugin.getFlagManager().getEffectiveFlag(lastLocation, this.getName(), claimFrom);
        if (flagFrom == flagTo) return;
        // moving to different claim with the same params
        if (flagFrom != null && flagFrom.parameters.equals(flagTo.parameters)) return;

        final PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
        final String owner = playerData.lastClaim != null ? playerData.lastClaim.getOwnerName() : "N/A";

        final Title title = Title.title(
            Component.text("Entering Claim", NamedTextColor.GREEN),
            Component.text(String.format("Owned by: %s", owner), TextColor.color(204, 204, 204)),
            Title.Times.times(Ticks.duration(10L), Ticks.duration(25L), Ticks.duration(10L))
        );
        GPFlags.getInstance().getAdventure().player(player).showTitle(title);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Flag flag = this.getFlagInstanceAtLocation(player.getLocation(), player);
        if (flag == null) return;
        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
        Claim lastClaim = playerData.lastClaim;

        final String owner = lastClaim != null ? lastClaim.getOwnerName() : "N/A";
        final Title title = Title.title(
            Component.text("Entering Claim", NamedTextColor.GREEN),
            Component.text(String.format("Owned by: %s", owner), TextColor.color(204, 204, 204)),
            Title.Times.times(Ticks.duration(10L), Ticks.duration(25L), Ticks.duration(10L))
        );
        GPFlags.getInstance().getAdventure().player(player).showTitle(title);
    }

    @Override
    public String getName() {
        return "EnterTitle";
    }

    @Override
    public SetFlagResult validateParameters(String parameters, CommandSender sender) {
//        if (parameters.isEmpty()) {
//            return new SetFlagResult(false, new MessageSpecifier(Messages.ActionbarRequired));
//        }
        return new SetFlagResult(true, this.getSetMessage(parameters));
    }

    @Override
    public MessageSpecifier getSetMessage(String parameters) {
        return new MessageSpecifier(Messages.AddedEnterTitle, parameters);
    }

    @Override
    public MessageSpecifier getUnSetMessage() {
        return new MessageSpecifier(Messages.RemovedEnterTitle);
    }

}
