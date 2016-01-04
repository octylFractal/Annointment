package me.kenzierocks.plugins.annointment.data;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.KeyFactory;
import org.spongepowered.api.data.manipulator.mutable.common.collection.AbstractSingleSetData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.mutable.SetValue;

public class AnnointmentFlagData extends
        AbstractSingleSetData<AnnointmentFlag, AnnointmentFlagData, ImmutableAnnointmentFlagData>
        implements AFDContentVersion {

    private static final Key<SetValue<AnnointmentFlag>> KEY =
            AnnointmentDataManager.ANNOINMENT_FLAGS;
    private static final Key<SetValue<String>> ENUM_SERIALIZED_KEY =
            KeyFactory.makeSetKey(String.class,
                    DataQuery.of("annointmentflags_serialized"));

    private static int compareBitSets(BitSet lhs, BitSet rhs) {
        if (lhs.equals(rhs))
            return 0;
        BitSet xor = (BitSet) lhs.clone();
        xor.xor(rhs);
        int firstDifferent = xor.length() - 1;
        return rhs.get(firstDifferent) ? 1 : -1;
    }

    private static Optional<Set<AnnointmentFlag>> getFlagsHelper(
            boolean supports,
            Supplier<Optional<Set<AnnointmentFlag>>> keyDataSupplier,
            Supplier<Optional<Set<String>>> serKeyDataSupplier) {
        if (!supports) {
            return Optional.empty();
        }
        Optional<Set<AnnointmentFlag>> keyData = keyDataSupplier.get();
        if (keyData.isPresent()) {
            return keyData;
        } else {
            return serKeyDataSupplier.get().map(AnnointmentFlag::fromNames);
        }
    }

    private static Optional<Set<AnnointmentFlag>>
            getFlags(ValueContainer<?> c) {
        return getFlagsHelper(
                c.supports(KEY) || c.supports(ENUM_SERIALIZED_KEY),
                () -> c.get(KEY), () -> c.get(ENUM_SERIALIZED_KEY));
    }

    @SuppressWarnings("unchecked")
    private static Optional<Set<AnnointmentFlag>> getFlags(DataView c) {
        return getFlagsHelper(true,
                () -> c.get(KEY.getQuery()).map(x -> (Set<AnnointmentFlag>) x),
                () -> c.get(ENUM_SERIALIZED_KEY.getQuery())
                        .map(x -> (Set<String>) x));
    }

    static Optional<AnnointmentFlagData> from(DataView container) {
        return getFlags(container).map(AnnointmentFlagData::new);
    }

    static Optional<AnnointmentFlagData> from(DataHolder dataHolder) {
        return getFlags(dataHolder).map(AnnointmentFlagData::new);
    }

    public AnnointmentFlagData(Set<AnnointmentFlag> value) {
        super(EnumSet.copyOf(value), KEY);
    }

    @Override
    public Optional<AnnointmentFlagData> fill(DataHolder dataHolder,
            MergeFunction overlap) {
        AnnointmentFlagData dataHolderAsAFD =
                getFlags(dataHolder).map(AnnointmentFlagData::new).orElse(null);
        return Optional.of(overlap.merge(this, dataHolderAsAFD));
    }

    @Override
    public Optional<AnnointmentFlagData> from(DataContainer container) {
        Optional<Set<AnnointmentFlag>> flags = getFlags(container);
        if (flags.isPresent()) {
            setValue(flags.get());
            return Optional.of(this);
        }
        return Optional.empty();
    }

    @Override
    public AnnointmentFlagData copy() {
        return new AnnointmentFlagData(getValue());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(KEY, getValue());
    }

    @Override
    public ImmutableAnnointmentFlagData asImmutable() {
        return new ImmutableAnnointmentFlagData(getValue());
    }

    @Override
    public int compareTo(AnnointmentFlagData o) {
        BitSet ourSet = getValue().stream().collect(BitSet::new,
                (set, val) -> set.set(val.ordinal()), BitSet::and);
        BitSet thrSet = o.getValue().stream().collect(BitSet::new,
                (set, val) -> set.set(val.ordinal()), BitSet::and);
        return compareBitSets(ourSet, thrSet);
    }

}
