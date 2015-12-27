package me.kenzierocks.plugins.annointment.data;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import me.kenzierocks.plugins.annointment.APlugin;

public class SavedMapAnnointmentDataAccess implements AnnointmentDataAccess {

    private static final Gson JSON_TRANSFERRENCE =
            new GsonBuilder().setPrettyPrinting().create();
    private static final Path STORAGE_FILE = APlugin.getInstance()
            .getConfigDir().resolve("annointment_map.json");
    @SuppressWarnings("serial")
    private static final Type MAP_TYPE =
            new TypeToken<Map<UUID, Collection<AnnointmentFlag>>>() {
            }.getType();
    private static final Multimap<UUID, AnnointmentFlag> dataStore =
            HashMultimap.create();

    static {
        // Initialize the dataStore from disk
        try (
                Reader reader = Files.newBufferedReader(STORAGE_FILE)) {
            if (Files.exists(STORAGE_FILE)) {
                Map<UUID, Collection<AnnointmentFlag>> jsonMap =
                        JSON_TRANSFERRENCE.fromJson(reader, MAP_TYPE);
                if (jsonMap != null) {
                    for (Entry<UUID, Collection<AnnointmentFlag>> e : jsonMap
                            .entrySet()) {
                        dataStore.putAll(e.getKey(), e.getValue());
                    }
                }
            }
        } catch (JsonSyntaxException | JsonIOException | IOException e) {
            e.printStackTrace();
            // lol whatever.
        }
    }

    {
        // Add ourselves for shutdown so we can save
        Sponge.getEventManager().registerListeners(APlugin.getInstance(), this);
        // Also save every minute in case of server shutdown.
        APlugin.getInstance().getExecutor().scheduleAtFixedRate(this::save, 0,
                1, TimeUnit.MINUTES);
    }

    @Listener
    public void doSaveOnShutdown(GameStoppingServerEvent e) {
        save();
    }

    private void save() {
        Map<UUID, Collection<AnnointmentFlag>> dataMap =
                new HashMap<>(dataStore.asMap());
        try {
            try (
                    Writer writer = Files.newBufferedWriter(STORAGE_FILE)) {
                JSON_TRANSFERRENCE.toJson(dataMap, MAP_TYPE, writer);
            } catch (JsonIOException | IOException e) {
                e.printStackTrace();
                // again, lol whatever.
            }
        } catch (Exception e) {
            // we want to save repeatedly still :P
            APlugin.getInstance().getLogger()
                    .error("logging potentially fatal error", e);
        }
    }

    @Override
    public boolean set(User user, Set<AnnointmentFlag> flags) {
        dataStore.get(user.getUniqueId()).clear();
        dataStore.get(user.getUniqueId()).addAll(flags);
        return true;
    }

    @Override
    public Set<AnnointmentFlag> get(User user) {
        Collection<AnnointmentFlag> flags = dataStore.get(user.getUniqueId());
        return flags.isEmpty() ? EnumSet.noneOf(AnnointmentFlag.class)
                : EnumSet.copyOf(flags);
    }

}
