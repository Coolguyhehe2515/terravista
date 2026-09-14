package com.krispy.terravista.lod;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ChunkPos;
import org.joml.Matrix4f;

import java.util.Collection;

public final class LodRenderer {
    private LodRenderer() {
    }

    public static void render(
            MatrixStack matrices,
            double cameraX,
            double cameraY,
            double cameraZ,
            Collection<LodChunk> chunks
    ) {
        if (chunks.isEmpty()) {
            return;
        }

        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();

        BufferBuilder buffer = tessellator.begin(
                VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION_COLOR
        );

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        for (LodChunk lodChunk : chunks) {
            ChunkPos chunkPos = lodChunk.getChunkPos();

            double baseX = chunkPos.getStartX() - cameraX;
            double baseZ = chunkPos.getStartZ() - cameraZ;

            int samples = lodChunk.getSamplesPerSide();
            int step = lodChunk.getSampleStep();

            for (int z = 0; z < samples; z++) {
                for (int x = 0; x < samples; x++) {
                    int height = lodChunk.getHeight(x, z);
                    int color = lodChunk.getColor(x, z);

                    float red = ((color >> 16) & 255) / 255.0f;
                    float green = ((color >> 8) & 255) / 255.0f;
                    float blue = (color & 255) / 255.0f;

                    float x0 = (float) (baseX + x * step);
                    float z0 = (float) (baseZ + z * step);
                    float x1 = x0 + step;
                    float z1 = z0 + step;
                    float y = (float) (height + 1 - cameraY);

                    buffer.vertex(matrix, x0, y, z0)
                            .color(red, green, blue, 1.0f);

                    buffer.vertex(matrix, x1, y, z0)
                            .color(red, green, blue, 1.0f);

                    buffer.vertex(matrix, x1, y, z1)
                            .color(red, green, blue, 1.0f);

                    buffer.vertex(matrix, x0, y, z1)
                            .color(red, green, blue, 1.0f);
                }
            }
        }

        BuiltBuffer builtBuffer = buffer.end();

        BufferRenderer.drawWithGlobalProgram(builtBuffer);

        builtBuffer.close();
    }
}
