package me.kenzierocks.plugins.annointment.commands;

import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;

import me.kenzierocks.plugins.annointment.data.AnnointmentDataManager;
import me.kenzierocks.plugins.annointment.data.AnnointmentFlag;

public class AddCommand implements Command {

    private static final String ADD = "add";

    @Override
    public CommandResult execute(CommandSource src, CommandContext args)
            throws CommandException {
        Object potentialUser =
                args.getOne(ADD).orElseThrow(() -> new CommandException(
                        Texts.of("Need a user to apply to!")));
        User user = null;
        if (potentialUser instanceof User) {
            user = (User) potentialUser;
        } else if (potentialUser instanceof String) {
            try {
                user = Sponge.getServiceManager()
                        .provideUnchecked(UserStorageService.class)
                        .get(UUID.fromString((String) potentialUser))
                        .orElseThrow(() -> new CommandException(
                                Texts.of("User " + potentialUser
                                        + " has never played on this server")));
            } catch (CommandException e) {
                throw e;
            } catch (IllegalArgumentException e) {
                src.sendMessage(Texts.of(TextColors.RED,
                        "Invalid UUID " + potentialUser));
            } catch (Exception e) {
                e.printStackTrace();
                src.sendMessage(Texts
                        .of("Couldn't attach the flag: " + e.getMessage()));
            }
        }
        if (user == null) {
            return CommandResult.empty();
        }
        try {
            AnnointmentDataManager.addAnnointmentFlag(user,
                    /* TODO just for testing */ AnnointmentFlag.LOTS_OF_LIGHTNING);
            src.sendMessage(Texts.of("Attached the flag to ",
                    (user.isOnline()
                            ? user.get(Keys.DISPLAY_NAME)
                                    .orElse(Texts.of(user.getName()))
                            : user.getUniqueId()),
                    " successfully."));
            return CommandResult.success();
        } catch (Exception e) {
            e.printStackTrace();
            src.sendMessage(
                    Texts.of("Couldn't attach the flag: " + e.getMessage()));
        }
        return CommandResult.empty();
    }

    @Override
    public CommandElement getArguments() {
        return GenericArguments.flags()
                .buildWith(GenericArguments.firstParsing(
                        GenericArguments.player(Texts.of(ADD)),
                        GenericArguments.string(Texts.of(ADD))));
    }

    @Override
    public String getStringDescription() {
        return "Adds a user to the annointed list.";
    }

}
