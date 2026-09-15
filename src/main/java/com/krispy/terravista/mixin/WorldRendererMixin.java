package com.krispy.terravista.mixin;

import com.krispy.terravista.config.TerraVistaConfig;
import com.krispy.terravista.lod.LodManager;
import com.krispy.terravista.lod.LodRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ChunkPos;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    private final LodManager terravista$lodManager = new LodManager();

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void terravista$renderLod(
            RenderTickCounter tickCounter,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightmapTextureManager lightmapTextureManager,
            Matrix4f matrix4f,
            Matrix4f matrix4f2,
            CallbackInfo ci
    ) {
        TerraVistaConfig config = TerraVistaConfig.get();

        if (!config.enabled) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;

        if (world == null) {
            terravista$lodManager.clear();
            LodRenderer.clear();
            return;
        }

        terravista$lodManager.updateWorld(world);

        double cameraX = camera.getPos().x;
        double cameraY = camera.getPos().y;
        double cameraZ = camera.getPos().z;

        int cameraChunkX = ChunkPos.getChunkCoord(
                (int) Math.floor(cameraX)
        );

        int cameraChunkZ = ChunkPos.getChunkCoord(
                (int) Math.floor(cameraZ)
        );

        terravista$lodManager.updateAround(
                cameraChunkX,
                cameraChunkZ
        );

        LodRenderer.render(
                matrix4f,
                matrix4f2,
                terravista$lodManager.getCache()
        );
    }
}
