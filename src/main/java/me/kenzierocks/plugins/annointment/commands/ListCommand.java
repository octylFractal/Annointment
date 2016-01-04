package me.kenzierocks.plugins.annointment.commands;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationBuilder;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import com.google.common.base.Strings;

import me.kenzierocks.plugins.annointment.data.AnnointmentDataManager;

public class ListCommand implements Command {

    private static final String FOUR_SPACES = Strings.repeat(" ", 4);

    @Override
    public CommandResult execute(CommandSource src, CommandContext args)
            throws CommandException {
        List<User> annointedUsers = AnnointmentDataManager
                .getAllAnnointedUsers().collect(Collectors.toList());
        if (annointedUsers.isEmpty()) {
            src.sendMessage(
                    Texts.of(TextColors.RED, "No one has been annointed."));
        } else {
            PaginationBuilder builder = Sponge.getServiceManager()
                    .provideUnchecked(PaginationService.class).builder();
            builder.title(Texts.of(TextColors.GREEN,
                    "Annointed Users [Name (UUID) [Flags]]"));
            builder.contents(annointedUsersToContents(annointedUsers));
            builder.sendTo(src);
        }
        return CommandResult.success();
    }

    private List<Text> annointedUsersToContents(List<User> annointedUsers) {
        return annointedUsers.stream().flatMap(this::userToContents)
                .collect(Collectors.toList());
    }

    private Stream<Text> userToContents(User user) {
        Stream<Text> basicData = Stream.of(getUserName(user),
                Texts.of(getColorForUserOnlineStatus(user),
                        " (" + user.getUniqueId() + ")"));
        Stream<Text> flags = AnnointmentDataManager.getAnnointmentFlags(user)
                .stream().map(flag -> Texts.of(FOUR_SPACES + flag.toString()));
        return Stream.concat(basicData, flags);
    }

    private TextColor getColorForUserOnlineStatus(User user) {
        return user.isOnline() ? TextColors.GREEN : TextColors.RED;
    }

    private Text getUserName(User user) {
        // online -> green name/uuid, offline -> red name/uuid
        return user.getPlayer().flatMap(x -> x.get(DisplayNameData.class))
                .flatMap(x -> x.displayName().getDirect())
                .orElse(Texts.of(user.getName())).toBuilder()
                .color(getColorForUserOnlineStatus(user)).style(TextStyles.BOLD)
                .build();
    }

    @Override
    public CommandElement getArguments() {
        return GenericArguments.none();
    }

    @Override
    public String getStringDescription() {
        return "Lists all annointed players";
    }

}
