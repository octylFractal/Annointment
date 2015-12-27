package me.kenzierocks.plugins.annointment.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.LocatedSource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.scheduler.SpongeExecutorService.SpongeFuture;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import me.kenzierocks.plugins.annointment.APlugin;
import net.minecraft.entity.item.EntityTNTPrimed;

public class EasterEggCommand implements Command {

    private static final int EXPLOSION_Y_OFFSET = 32;
    private static final int EXPLOSION_TIMER = EXPLOSION_Y_OFFSET * 2;
    private static final String TIMES = "times";
    private static final int DELAY_BETWEEN_EXPLOSIONS = 1000;

    private static final Multiset<Object> GROUP_COUNT_DOWNS =
            HashMultiset.create();
    private static final Map<Object, Boolean> GROUP_OK = new HashMap<>();
    private static final Map<Object, CommandSource> GROUP_SOURCE =
            new HashMap<>();

    private static void startGroup(Object key, int times,
            CommandSource groupSource) {
        GROUP_COUNT_DOWNS.add(key, times);
        GROUP_OK.put(key, Boolean.TRUE);
        GROUP_SOURCE.put(key, groupSource);
    }

    private static void completePartOfGroup(Object key, boolean result) {
        GROUP_COUNT_DOWNS.remove(key);
        // essentially an AND
        GROUP_OK.replace(key, Boolean.TRUE, result);
        if (GROUP_COUNT_DOWNS.count(key) == 0) {
            try {
                if (GROUP_OK.get(key)) {
                    GROUP_SOURCE.get(key).sendMessage(Texts.of("Boom~"));
                } else {
                    GROUP_SOURCE.get(key).sendMessage(
                            Texts.of("Couldn't send all explosions :["));
                }
            } finally {
                GROUP_OK.remove(key);
                GROUP_SOURCE.remove(key);
            }
        }
    }

    private static boolean triggerExplosion(World world, int x, int baseY,
            int z, Cause cause) {
        int y = baseY + EXPLOSION_Y_OFFSET;
        Optional<Entity> opt = world.createEntity(EntityTypes.PRIMED_TNT,
                new Vector3i(x, y, z));
        if (!opt.isPresent()) {
            APlugin.getInstance().getLogger().info("Couldn't spawn PTNT at "
                    + String.format("(%s, %s, %s)", x, y, z));
            return false;
        }
        Entity e = opt.get();
        ((EntityTNTPrimed) e).fuse = EXPLOSION_TIMER;
        // if (!e.offer(Keys.FUSE_DURATION, EXPLOSION_TIMER).isSuccessful()) {
        // APlugin.getInstance().getLogger().info("Couldn't set data on " + e);
        // return false;
        // }
        return world.spawnEntity(e, cause);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args)
            throws CommandException {
        if (!(src instanceof LocatedSource)) {
            src.sendMessage(Texts
                    .of("Must be an in-world source to use this command."));
            return CommandResult.success();
        }
        Cause cause = Cause.of(src,
                // SpawnCause.builder().type(SpawnTypes.PLUGIN).build(),
                APlugin.getInstance());
        Location<World> loc = ((LocatedSource) src).getLocation();
        int count = args.<Integer> getOne(TIMES).orElse(1);
        Object groupKey = new Object();
        startGroup(groupKey, count, src);
        IntStream.range(0, count)
                .forEach(x -> createExplosion(groupKey, x, cause, loc));
        return CommandResult.success();
    }

    private SpongeFuture<Boolean> createExplosion(Object groupKey, int i,
            Cause cause, Location<World> loc) {
        return APlugin.getInstance().getExecutor().schedule(() -> {
            boolean result = false;
            try {
                result = IntStream.range(-5, 5)
                        .allMatch(x -> IntStream.range(-5, 5)
                                .allMatch(z -> triggerExplosion(loc.getExtent(),
                                        loc.getBlockX() + x, loc.getBlockY(),
                                        loc.getBlockZ() + z, cause)));
                return result;
            } finally {
                completePartOfGroup(groupKey, result);
            }
        }, i * DELAY_BETWEEN_EXPLOSIONS, TimeUnit.MILLISECONDS);
    }

    @Override
    public CommandElement getArguments() {
        return GenericArguments
                .optional(GenericArguments.integer(Texts.of(TIMES)), 1);
    }

    @Override
    public String getStringDescription() {
        return "EASTER???? THIS SOON?";
    }

}
