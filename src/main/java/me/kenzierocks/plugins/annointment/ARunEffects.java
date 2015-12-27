package me.kenzierocks.plugins.annointment;

import java.util.Random;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;

import me.kenzierocks.plugins.annointment.data.AnnointmentDataManager;
import me.kenzierocks.plugins.annointment.data.AnnointmentFlag;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity;
import net.minecraft.world.World;

public class ARunEffects implements Runnable {

    private Random rand = new Random();

    @Override
    public void run() {
        try {
            doRun();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doRun() throws Exception {
        for (Player p : Sponge.getGame().getServer().getOnlinePlayers()) {
            AnnointmentDataManager.getAnnointmentFlags(p)
                    .forEach(x -> applyEffects(p, x));
        }
    }

    private void applyEffects(Player user, AnnointmentFlag flag) {
        switch (flag) {
            case CONTINUOUS_EASTER_EGG:
                // TODO
                break;
            case LOTS_OF_LIGHTNING:
                World world = (World) user.getWorld();
                Location<org.spongepowered.api.world.World> loc =
                        user.getLocation();
                double x = loc.getX();
                double y = loc.getY();
                double z = loc.getZ();
                for (int dx = -5; dx < 5; dx++) {
                    for (int dz = -5; dz < 5; dz++) {
                        doTheLightningThing((EntityPlayerMP) user, world,
                                x + dx, y, z + dz);
                    }
                }
                break;
            case NO_INVENTORIES:
                // TODO
                break;
            case PRACTICAL_PARTICLE_HELL:
                // TODO
                break;
            default:
        }
    }

    private void doTheLightningThing(EntityPlayerMP user, World world, double x,
            double y, double z) {
        user.playerNetServerHandler.sendPacket(new S2CPacketSpawnGlobalEntity(
                new EntityLightningBolt(world, x, y, z)));
        user.playerNetServerHandler.sendPacket(
                new S29PacketSoundEffect("ambient.weather.thunder", x, y, z,
                        10000.0F, 0.8F + this.rand.nextFloat() * 0.2F));
        user.playerNetServerHandler.sendPacket(
                new S29PacketSoundEffect("random.explode", x, y, z,
                        20.0F, 0.5F + this.rand.nextFloat() * 0.2F));
    }

}
