package me.kenzierocks.plugins.annointment.commands;

import org.spongepowered.api.Sponge;

import me.kenzierocks.plugins.annointment.APlugin;

public final class ACommandManager {

    public static void addCommands(APlugin plugin) {
        Sponge.getCommandManager().register(plugin,
                new MasterCommand().getSpec(), "annointment", "an");
    }

    private ACommandManager() {
    }

}
