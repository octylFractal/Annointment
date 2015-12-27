package me.kenzierocks.plugins.annointment.data;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.KeyFactory;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.util.persistence.InvalidDataException;

import com.google.common.collect.Sets;

public final class AnnointmentDataManager {

    public static final Key<SetValue<AnnointmentFlag>> ANNOINMENT_FLAGS =
            KeyFactory.makeSetKey(AnnointmentFlag.class,
                    DataQuery.of("annointmentflags"));

    private static transient UserStorageService cachedService;
    private static transient Collection<User> cachedAnnointedUsers =
            Sets.newConcurrentHashSet();
    private static final AtomicBoolean cachedUserListInvalid =
            new AtomicBoolean(true);
    /**
     * This optional allows for those silly cases where caching might break.
     * It's off by default because caching should work, but in case of breakage
     * it can be turned on by the server owner.
     */
    // TODO: config option
    public static final AtomicBoolean ignoreCachedUserListAlways =
            new AtomicBoolean();
    // TODO - when data api is better switch
    private static final AnnointmentDataAccess DATA_ACCESS =
            new SavedMapAnnointmentDataAccess();

    public static void registerAll() {
        // AnnointmentFlagData
        Sponge.getDataManager().register(AnnointmentFlagData.class,
                ImmutableAnnointmentFlagData.class,
                new DataManipulatorBuilder<AnnointmentFlagData, ImmutableAnnointmentFlagData>() {

                    @Override
                    public Optional<AnnointmentFlagData> build(
                            DataView container) throws InvalidDataException {
                        return AnnointmentFlagData.from(container);
                    }

                    @Override
                    public AnnointmentFlagData create() {
                        return new AnnointmentFlagData(
                                EnumSet.noneOf(AnnointmentFlag.class));
                    }

                    @Override
                    public Optional<AnnointmentFlagData>
                            createFrom(DataHolder dataHolder) {
                        return AnnointmentFlagData.from(dataHolder);
                    }
                });
        // END AnnointmentFlagData
    }

    public static Stream<User> getAllAnnointedUsers() {
        if (cachedService == null) {
            Sponge.getServiceManager().provide(UserStorageService.class)
                    .ifPresent(x -> cachedService = x);
            if (cachedService == null) {
                // We can't tell who's annointed yet - no one is!
                return Stream.empty();
            }
        }
        if (cachedUserListInvalid.get() || ignoreCachedUserListAlways.get()) {
            cachedAnnointedUsers.clear();
            cachedAnnointedUsers.addAll(generateAnnointedUsersCollection());
            cachedUserListInvalid.set(false);
        }
        return cachedAnnointedUsers.stream();
    }

    public static boolean isUserAnnointed(User user) {
        return !getAnnointmentFlags(user).isEmpty();
    }

    public static Set<AnnointmentFlag> getAnnointmentFlags(User user) {
        return DATA_ACCESS.get(user);
    }

    public static void addAnnointmentFlag(User user, AnnointmentFlag flag) {
        doSetOperation(user, set -> set.add(flag));
    }

    public static void removeAnnointmentFlag(User user, AnnointmentFlag flag) {
        doSetOperation(user, set -> set.remove(flag));
    }

    private static void doSetOperation(User user,
            Consumer<Set<AnnointmentFlag>> op) {
        Set<AnnointmentFlag> flagSet = DATA_ACCESS.get(user);
        op.accept(flagSet);
        if (!DATA_ACCESS.set(user, flagSet)) {
            throw new IllegalStateException("OFFER FLAILED");
        }
        cachedUserListInvalid.set(true);
    }

    private static Collection<User> generateAnnointedUsersCollection() {
        return cachedService.getAll().stream().map(cachedService::get)
                .filter(Optional::isPresent).map(Optional::get)
                .filter(AnnointmentDataManager::isUserAnnointed)
                .collect(Collectors.toSet());
    }

    private AnnointmentDataManager() {
    }

}
