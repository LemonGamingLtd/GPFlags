package me.ryanhamshire.GPFlags.commands;

import me.ryanhamshire.GPFlags.Flag;
import me.ryanhamshire.GPFlags.FlagManager;
import me.ryanhamshire.GPFlags.GPFlags;
import me.ryanhamshire.GPFlags.MessageSpecifier;
import me.ryanhamshire.GPFlags.Messages;
import me.ryanhamshire.GPFlags.SetFlagResult;
import me.ryanhamshire.GPFlags.TextMode;
import me.ryanhamshire.GPFlags.flags.FlagDefinition;
import me.ryanhamshire.GPFlags.util.MessagingUtil;
import me.ryanhamshire.GPFlags.util.Util;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandUnsetClaimFlag implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("gpflags.command.setclaimflag")) {
            MessagingUtil.sendMessage(sender, TextMode.Err, Messages.NoCommandPermission, command.toString());
            return true;
        }
        if (args.length < 1) return false;

        Player player = ((Player) sender);

        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), false, playerData.lastClaim);

        if (claim == null) {
            MessagingUtil.sendMessage(player, TextMode.Err, Messages.StandInAClaim);
            return true;
        }

        Long claimID = claim.getID();
        if (claimID == null || claimID == -1) {
            MessagingUtil.sendMessage(player, TextMode.Err, Messages.UpdateGPForSubdivisionFlags);
            return true;
        }
        String flagName = args[0];
        GPFlags plugin = GPFlags.getInstance();
        FlagDefinition def = plugin.getFlagManager().getFlagDefinitionByName(flagName);
        if (def == null) {
            MessagingUtil.sendMessage(player, TextMode.Err, Messages.InvalidFlagDefName, Util.getAvailableFlags(player));
            return true;
        }

        if (!sender.hasPermission("gpflags.flag." + def.getName())) {
            MessagingUtil.sendMessage(player, TextMode.Err, Messages.NoFlagPermission, def.getName());
            return true;
        }

        if (!Util.canConfigureClaimFlags(player, claim, def)) {
            MessagingUtil.sendMessage(player, TextMode.Err, Messages.NotYourClaim);
            return true;
        }

        SetFlagResult result;
        if (def.getName().equals("NoEnterPlayer") && args.length > 1) {
            result = removeBlockedPlayers(plugin.getFlagManager(), claim, def, sender, Arrays.copyOfRange(args, 1, args.length));
        } else {
            result = plugin.getFlagManager().unSetFlag(claimID.toString(), def);
        }
        String color = result.isSuccess() ? TextMode.Success : TextMode.Err;
        MessagingUtil.sendMessage(player, color, result.getMessage().getMessageID(), result.getMessage().getMessageParams());
        if (result.isSuccess()) plugin.getFlagManager().save();

        return true;
    }

    /**
     * Let just the given players back in with /unsetclaimflag NoEnterPlayer &lt;players&gt;, removing the flag once nobody is left
     */
    private SetFlagResult removeBlockedPlayers(FlagManager flagManager, Claim claim, FlagDefinition def, CommandSender sender, String[] players) {
        Set<String> toRemove = new HashSet<>();
        for (String player : players) {
            toRemove.add(player.toLowerCase());
            toRemove.add(FlagManager.resolvePlayerParameter(player).toLowerCase());
        }

        Flag flag = flagManager.getRawClaimFlag(claim, "noenterplayer");
        String[] ids = flag != null && flag.getSet() ? flag.getParametersArray() : new String[0];
        String[] names = flag != null && flag.getSet() ? flag.getFriendlyParameters().split(" ") : new String[0];
        List<String> remaining = new ArrayList<>();
        for (int i = 0; i < ids.length; i++) {
            boolean listed = toRemove.contains(ids[i].toLowerCase()) || (i < names.length && toRemove.contains(names[i].toLowerCase()));
            if (!listed) remaining.add(ids[i]);
        }

        if (remaining.size() == ids.length) {
            return new SetFlagResult(false, new MessageSpecifier(Messages.NoEnterPlayerNotBlocked, String.join(" ", players)));
        }
        if (remaining.isEmpty()) {
            return flagManager.unSetFlag(claim, def);
        }
        SetFlagResult result = flagManager.setFlag(claim.getID().toString(), def, true, sender, remaining.toArray(new String[0]));
        if (!result.isSuccess()) return result;
        return new SetFlagResult(true, def.getSetMessage(flagManager.getRawClaimFlag(claim, "noenterplayer").getFriendlyParameters()));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return Util.flagTab(commandSender, args[0]);
        }
        if (args[0].equalsIgnoreCase("NoEnterPlayer") && commandSender instanceof Player) {
            Player player = (Player) commandSender;
            Flag flag = GPFlags.getInstance().getFlagManager().getFlagDefinitionByName("noenterplayer").getFlagInstanceAtLocation(player.getLocation(), player);
            if (flag == null) return Collections.emptyList();
            return StringUtil.copyPartialMatches(args[args.length - 1], Arrays.asList(flag.getFriendlyParameters().split(" ")), new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
