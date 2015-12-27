package me.kenzierocks.plugins.annointment.data;

import java.util.Set;

import org.spongepowered.api.entity.living.player.User;

/**
 * For the sole purpose of making it easier to switch when the Data API is
 * better.
 */
public interface AnnointmentDataAccess {
    
    boolean set(User user, Set<AnnointmentFlag> flags);
    
    Set<AnnointmentFlag> get(User user);

}
