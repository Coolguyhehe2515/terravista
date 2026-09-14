package com.krispy.terravista.mixin;

import com.krispy.terravista.lod.LodChunk;
import com.krispy.terravista.lod.LodGenerator;
import com.krispy.terravista.lod.LodRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ChunkPos;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(
        method = "render",
        at = @At("HEAD")
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
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;

        if (world == null) {
            return;
        }

        double cameraX = camera.getPos().x;
        double cameraY = camera.getPos().y;
        double cameraZ = camera.getPos().z;

        int cameraChunkX = ChunkPos.getChunkCoord((int) Math.floor(cameraX));
        int cameraChunkZ = ChunkPos.getChunkCoord((int) Math.floor(cameraZ));

        List<LodChunk> lodChunks = new ArrayList<>();

        int radius = 4;

        for (int z = -radius; z <= radius; z++) {
            for (int x = -radius; x <= radius; x++) {
                int chunkX = cameraChunkX + x;
                int chunkZ = cameraChunkZ + z;

                ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

                LodChunk lodChunk = LodGenerator.generate(
                    world,
                    chunkPos,
                    4
                );

                lodChunks.add(lodChunk);
            }
        }

        LodRenderer.render(
            camera.getRotation(),
            cameraX,
            cameraY,
            cameraZ,
            lodChunks
        );
    }
}
