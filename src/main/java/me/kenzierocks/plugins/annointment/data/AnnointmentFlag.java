package me.kenzierocks.plugins.annointment.data;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.spongepowered.api.data.DataHolder;

public enum AnnointmentFlag {
    NO_INVENTORIES, LOTS_OF_LIGHTNING, PRACTICAL_PARTICLE_HELL,
    CONTINUOUS_EASTER_EGG;

    private static final Map<String, AnnointmentFlag> map =
            new HashMap<>(AnnointmentFlag.values().length);

    static {
        for (AnnointmentFlag af : values()) {
            map.put(af.name(), af);
        }
    }

    public static Optional<AnnointmentFlag> fromName(String name) {
        return Optional.ofNullable(map.get(name));
    }

    public static Set<AnnointmentFlag> fromNames(Set<String> names) {
        return names.stream().map(AnnointmentFlag::fromName)
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toCollection(
                        () -> EnumSet.noneOf(AnnointmentFlag.class)));
    }

    public void addTo(DataHolder holder) {
        holder.getOrCreate(AnnointmentFlagData.class).get()
                .getValue(AnnointmentDataManager.ANNOINMENT_FLAGS).get()
                .add(this);
    }

    public void removeFrom(DataHolder holder) {
        holder.getOrCreate(AnnointmentFlagData.class).get()
                .getValue(AnnointmentDataManager.ANNOINMENT_FLAGS).get()
                .remove(this);

    }

}
