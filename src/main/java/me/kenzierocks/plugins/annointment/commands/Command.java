package me.kenzierocks.plugins.annointment.commands;

import java.util.Optional;

import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;

public interface Command extends CommandExecutor {

    CommandElement getArguments();

    String getStringDescription();

    default Text getDescription() {
        return Texts.of(getStringDescription());
    }

    default CommandSpec.Builder
            doExtraConfiguration(CommandSpec.Builder builder) {
        return builder;
    }

    default Optional<String> getPermissions() {
        return Optional.empty();
    }

    default CommandSpec.Builder appendPermissions(CommandSpec.Builder builder) {
        getPermissions().ifPresent(builder::permission);
        return builder;
    }

    default CommandSpec getSpec() {
        return doExtraConfiguration(
                appendPermissions(CommandSpec.builder().arguments(getArguments())
                        .executor(this).description(getDescription()))).build();
    }

}
