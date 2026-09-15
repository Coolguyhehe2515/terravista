package com.krispy.terravista.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class TerraVistaConfigScreen {

    public static Screen create(Screen parent) {
        TerraVistaConfig config = TerraVistaConfig.get();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("TerraVista Settings"))
                .setSavingRunnable(TerraVistaConfig::save);

        builder.getOrCreateCategory(Text.literal("General"))
                .addEntry(
                        builder.entryBuilder()
                                .startBooleanToggle(
                                        Text.literal("Enable TerraVista"),
                                        config.enabled
                                )
                                .setDefaultValue(true)
                                .setSaveConsumer(value -> config.enabled = value)
                                .build()
                )
                .addEntry(
                        builder.entryBuilder()
                                .startIntSlider(
                                        Text.literal("Near Distance"),
                                        config.nearDistance,
                                        32,
                                        512
                                )
                                .setDefaultValue(128)
                                .setSaveConsumer(value -> config.nearDistance = value)
                                .build()
                )
                .addEntry(
                        builder.entryBuilder()
                                .startIntSlider(
                                        Text.literal("LOD Distance"),
                                        config.lodDistance,
                                        256,
                                        4096
                                )
                                .setDefaultValue(1024)
                                .setSaveConsumer(value -> config.lodDistance = value)
                                .build()
                )
                .addEntry(
                        builder.entryBuilder()
                                .startIntSlider(
                                        Text.literal("LOD Render Distance"),
                                        config.renderDistance,
                                        8,
                                        256
                                )
                                .setDefaultValue(32)
                                .setSaveConsumer(value -> config.renderDistance = value)
                                .build()
                )
                .addEntry(
                        builder.entryBuilder()
                                .startIntSlider(
                                        Text.literal("Sample Step"),
                                        config.sampleStep,
                                        1,
                                        8
                                )
                                .setDefaultValue(4)
                                .setSaveConsumer(value -> config.sampleStep = value)
                                .build()
                )
                .addEntry(
                        builder.entryBuilder()
                                .startIntSlider(
                                        Text.literal("Chunks Generated Per Frame"),
                                        config.maxChunksPerFrame,
                                        1,
                                        16
                                )
                                .setDefaultValue(2)
                                .setSaveConsumer(value -> config.maxChunksPerFrame = value)
                                .build()
                );

        return builder.build();
    }
}
