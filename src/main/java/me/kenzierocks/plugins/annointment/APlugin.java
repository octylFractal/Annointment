package me.kenzierocks.plugins.annointment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.SpongeExecutorService;

import com.google.inject.Inject;

import me.kenzierocks.plugins.annointment.commands.ACommandManager;
import me.kenzierocks.plugins.annointment.data.AnnointmentDataManager;

@Plugin(id = APlugin.ID, name = APlugin.NAME, version = APlugin.VERSION)
public class APlugin {

    public static final String ID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VERSION@";
    private static APlugin INSTANCE;

    public static APlugin getInstance() {
        return INSTANCE;
    }

    @Inject
    private Logger logger;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    {
        INSTANCE = this;
    }

    private SpongeExecutorService executor;

    public Logger getLogger() {
        return this.logger;
    }

    public SpongeExecutorService getExecutor() {
        if (this.executor == null) {
            this.executor = Sponge.getScheduler().createSyncExecutor(this);
        }
        return this.executor;
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        this.logger.info("Loading " + NAME + " v" + VERSION);
        try {
            Files.createDirectories(this.configDir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot use the plugin with no configs!",
                    e);
        }
        AnnointmentDataManager.registerAll();
        ACommandManager.addCommands(this);
        getExecutor().scheduleAtFixedRate(new ARunEffects(), 0, 1000 / 20,
                TimeUnit.MILLISECONDS);
        this.logger.info("Loaded " + NAME + " v" + VERSION);
    }

    public Path getConfigDir() {
        return this.configDir;
    }

}
