package me.ryanhamshire.GPFlags.flags;

import me.ryanhamshire.GPFlags.*;

/**
 * Stops wild Pokémon spawning in an area. Cobblemon is a mod, not a plugin, so GPFlags can't hook its spawn event;
 * LGCobblemonHunts cancels the spawns by looking this flag up with {@link #getFlagInstanceAtLocation}.
 */
public class FlagDef_NoPokemonSpawns extends FlagDefinition {

    public FlagDef_NoPokemonSpawns(FlagManager manager, GPFlags plugin) {
        super(manager, plugin);
    }

    @Override
    public String getName() {
        return "NoPokemonSpawns";
    }

    @Override
    public MessageSpecifier getSetMessage(String parameters) {
        return new MessageSpecifier(Messages.EnableNoPokemonSpawns);
    }

    @Override
    public MessageSpecifier getUnSetMessage() {
        return new MessageSpecifier(Messages.DisableNoPokemonSpawns);
    }

}
