package me.kenzierocks.plugins.annointment.data;

import java.util.EnumSet;
import java.util.Set;

import org.spongepowered.api.entity.living.player.User;

public class DataAPIAnnointmentDataAccess implements AnnointmentDataAccess {

    @Override
    public boolean set(User user, Set<AnnointmentFlag> flags) {
        return user.offer(AnnointmentDataManager.ANNOINMENT_FLAGS, flags)
                .isSuccessful();
    }

    @Override
    public Set<AnnointmentFlag> get(User user) {
        return user.get(AnnointmentDataManager.ANNOINMENT_FLAGS)
                .orElseGet(() -> EnumSet.noneOf(AnnointmentFlag.class));
    }

}
