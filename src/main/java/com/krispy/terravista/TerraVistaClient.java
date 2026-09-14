package com.krispy.terravista;

import com.krispy.terravista.config.TerraVistaConfig;
import net.fabricmc.api.ClientModInitializer;

public class TerraVistaClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TerraVistaConfig.loadConfig();
    }
}
