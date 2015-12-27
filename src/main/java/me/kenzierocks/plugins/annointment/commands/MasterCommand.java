package me.kenzierocks.plugins.annointment.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;

import com.google.common.collect.ImmutableList;

public final class MasterCommand implements Command {

    private static final Text LEFTOVERS_KEY = Texts.of("other");
    private static final Map<List<String>, CommandCallable> CHILDREN =
            new HashMap<>();
    private static final Map<String, CommandCallable> HIDDEN_CHILDREN =
            new HashMap<>();

    private static void addChild(Command command, String... aliases) {
        CHILDREN.put(ImmutableList.copyOf(aliases), command.getSpec());
    }

    private static void addHiddenChild(Command command, String... aliases) {
        CommandCallable spec = command.getSpec();
        Stream.of(aliases).forEach(x -> HIDDEN_CHILDREN.put(x, spec));
    }

    static {
        addChild(new ListCommand(), "list", "l");
        addChild(new AddCommand(), "add", "a");
        addChild(new RemoveCommand(), "remove", "r");
        addHiddenChild(new EasterEggCommand(), "easteregg");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args)
            throws CommandException {
        Optional<String> subcmd =
                args.<String> getOne(Texts.toPlain(LEFTOVERS_KEY));
        if (subcmd.isPresent()) {
            String[] parts = subcmd.get().split(" ", 2);
            Optional<CommandCallable> callable =
                    Optional.ofNullable(HIDDEN_CHILDREN.get(parts[0]));
            if (callable.isPresent()) {
                return callable.get().process(src, parts.length > 1 ? parts[1] : "");
            }
        }
        src.sendMessage(Texts
                .of("Not a command: " + subcmd.orElse("literally nothing")));
        return CommandResult.success();
    }

    @Override
    public CommandSpec.Builder
            doExtraConfiguration(CommandSpec.Builder builder) {
        return builder.children(CHILDREN);
    }

    @Override
    public CommandElement getArguments() {
        return GenericArguments.remainingJoinedStrings(LEFTOVERS_KEY);
    }

    @Override
    public String getStringDescription() {
        return "Annointment master command";
    }

}
