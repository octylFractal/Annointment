package me.kenzierocks.plugins.annointment.data;

import java.util.Optional;
import java.util.Set;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.immutable.common.collection.AbstractImmutableSingleSetData;
import org.spongepowered.api.data.value.BaseValue;

import com.google.common.collect.Sets;

public class ImmutableAnnointmentFlagData extends
        AbstractImmutableSingleSetData<AnnointmentFlag, ImmutableAnnointmentFlagData, AnnointmentFlagData>
        implements AFDContentVersion {

    public ImmutableAnnointmentFlagData(Set<AnnointmentFlag> value) {
        super(Sets.immutableEnumSet(value),
                AnnointmentDataManager.ANNOINMENT_FLAGS);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Optional<ImmutableAnnointmentFlagData>
            with(Key<? extends BaseValue<E>> key, E value) {
        if (!AnnointmentDataManager.ANNOINMENT_FLAGS.equals(key)) {
            return Optional.empty();
        }
        return Optional.of(
                new ImmutableAnnointmentFlagData((Set<AnnointmentFlag>) value));
    }

    @Override
    public AnnointmentFlagData asMutable() {
        return new AnnointmentFlagData(this.value);
    }

}
