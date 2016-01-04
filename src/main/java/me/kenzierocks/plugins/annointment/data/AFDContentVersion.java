package me.kenzierocks.plugins.annointment.data;

import org.spongepowered.api.data.DataSerializable;

public interface AFDContentVersion extends DataSerializable {

    int VERSION = 0;

    @Override
    default int getContentVersion() {
        return VERSION;
    }

}
