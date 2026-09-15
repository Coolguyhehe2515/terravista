package com.krispy.terravista.lod;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

import java.util.Map;

public class LodRenderer {

    private static final float SKIRT_DEPTH = 4.0f;

    public static void render(
            Matrix4f positionMatrix,
            Map<Long, LodChunk> chunks,
            double cameraX,
            double cameraY,
            double cameraZ
    ) {
        if (chunks.isEmpty()) {
            return;
        }

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();

        BufferBuilder buffer = tessellator.begin(
                VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION_COLOR
        );

        for (LodChunk chunk : chunks.values()) {
            renderChunk(
                    buffer,
                    positionMatrix,
                    chunk,
                    cameraX,
                    cameraY,
                    cameraZ
            );
        }

        var builtBuffer = buffer.end();

        if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram(builtBuffer);
        }

        RenderSystem.disableBlend();
    }

    private static void renderChunk(
            BufferBuilder buffer,
            Matrix4f matrix,
            LodChunk chunk,
            double cameraX,
            double cameraY,
            double cameraZ
    ) {
        int width = chunk.getWidth();
        int step = chunk.getSampleStep();

        renderTop(
                buffer,
                matrix,
                chunk,
                cameraX,
                cameraY,
                cameraZ
        );

        renderNorthSkirt(
                buffer,
                matrix,
                chunk,
                cameraX,
                cameraY,
                cameraZ,
                width,
                step
        );

        renderSouthSkirt(
                buffer,
                matrix,
                chunk,
                cameraX,
                cameraY,
                cameraZ,
                width,
                step
        );

        renderWestSkirt(
                buffer,
                matrix,
                chunk,
                cameraX,
                cameraY,
                cameraZ,
                width,
                step
        );

        renderEastSkirt(
                buffer,
                matrix,
                chunk,
                cameraX,
                cameraY,
                cameraZ,
                width,
                step
        );
    }

    private static void renderTop(
            BufferBuilder buffer,
            Matrix4f matrix,
            LodChunk chunk,
            double cameraX,
            double cameraY,
            double cameraZ
    ) {
        int width = chunk.getWidth();
        int step = chunk.getSampleStep();

        for (int z = 0; z < width; z++) {
            for (int x = 0; x < width; x++) {

                int worldX = chunk.getWorldX(x);
                int worldZ = chunk.getWorldZ(z);
                int height = chunk.getHeight(x, z);

                float[] color = unpackColor(
                        chunk.getColor(x, z)
                );

                float x1 = (float) (worldX - cameraX);
                float z1 = (float) (worldZ - cameraZ);

                float x2 = (float) (worldX + step - cameraX);
                float z2 = (float) (worldZ + step - cameraZ);

                float y = (float) (height - cameraY);

                quad(
                        buffer,
                        matrix,
                        x1, y, z1,
                        x1, y, z2,
                        x2, y, z2,
                        x2, y, z1,
                        color
                );
            }
        }
    }

    private static void renderNorthSkirt(
            BufferBuilder buffer,
            Matrix4f matrix,
            LodChunk chunk,
            double cameraX,
            double cameraY,
            double cameraZ,
            int width,
            int step
    ) {
        int z = 0;

        for (int x = 0; x < width; x++) {
            renderSkirtQuad(
                    buffer,
                    matrix,
                    chunk,
                    x,
                    z,
                    x,
                    z,
                    x + 1,
                    z,
                    cameraX,
                    cameraY,
                    cameraZ,
                    step
            );
        }
    }

    private static void renderSouthSkirt(
            BufferBuilder buffer,
            Matrix4f matrix,
            LodChunk chunk,
            double cameraX,
            double cameraY,
            double cameraZ,
            int width,
            int step
    ) {
        int z = width - 1;

        for (int x = 0; x < width; x++) {
            renderSkirtQuad(
                    buffer,
                    matrix,
                    chunk,
                    x,
                    z,
                    x + 1,
                    z,
                    x,
                    z,
                    cameraX,
                    cameraY,
                    cameraZ,
                    step
            );
        }
    }

    private static void renderWestSkirt(
            BufferBuilder buffer,
            Matrix4f matrix,
            LodChunk chunk,
            double cameraX,
            double cameraY,
            double cameraZ,
            int width,
            int step
    ) {
        int x = 0;

        for (int z = 0; z < width; z++) {
            renderSkirtQuad(
                    buffer,
                    matrix,
                    chunk,
                    x,
                    z,
                    x,
                    z + 1,
                    x,
                    z,
                    cameraX,
                    cameraY,
                    cameraZ,
                    step
            );
        }
    }

    private static void renderEastSkirt(
            BufferBuilder buffer,
            Matrix4f matrix,
            LodChunk chunk,
            double cameraX,
            double cameraY,
            double cameraZ,
            int width,
            int step
    ) {
        int x = width - 1;

        for (int z = 0; z < width; z++) {
            renderSkirtQuad(
                    buffer,
                    matrix,
                    chunk,
                    x,
                    z,
                    x,
                    z,
                    x,
                    z + 1,
                    cameraX,
                    cameraY,
                    cameraZ,
                    step
            );
        }
    }

    private static void renderSkirtQuad(
            BufferBuilder buffer,
            Matrix4f matrix,
            LodChunk chunk,
            int x1Index,
            int z1Index,
            int x2Index,
            int z2Index,
            int x3Index,
            int z3Index,
            double cameraX,
            double cameraY,
            double cameraZ,
            int step
    ) {
        if (x1Index >= chunk.getWidth() ||
                z1Index >= chunk.getWidth() ||
                x2Index >= chunk.getWidth() ||
                z2Index >= chunk.getWidth() ||
                x3Index >= chunk.getWidth() ||
                z3Index >= chunk.getWidth()) {
            return;
        }

        int x1 = chunk.getWorldX(x1Index);
        int z1 = chunk.getWorldZ(z1Index);
        int x2 = chunk.getWorldX(x2Index);
        int z2 = chunk.getWorldZ(z2Index);
        int x3 = chunk.getWorldX(x3Index);
        int z3 = chunk.getWorldZ(z3Index);

        int h1 = chunk.getHeight(x1Index, z1Index);
        int h2 = chunk.getHeight(x2Index, z2Index);
        int h3 = chunk.getHeight(x3Index, z3Index);

        float[] color = unpackColor(
                chunk.getColor(x1Index, z1Index)
        );

        float ax = (float) (x1 - cameraX);
        float ay = (float) (h1 - cameraY);
        float az = (float) (z1 - cameraZ);

        float bx = (float) (x2 - cameraX);
        float by = (float) (h2 - cameraY);
        float bz = (float) (z2 - cameraZ);

        float cx = (float) (x3 - cameraX);
        float cy = (float) (h3 - cameraY);
        float cz = (float) (z3 - cameraZ);

        float dx = cx;
        float dy = cy - SKIRT_DEPTH;
        float dz = cz;

        float ex = ax;
        float ey = ay - SKIRT_DEPTH;
        float ez = az;

        quad(
                buffer,
                matrix,
                ax, ay, az,
                bx, by, bz,
                dx, dy, dz,
                ex, ey, ez,
                color
        );
    }

    private static void quad(
            BufferBuilder buffer,
            Matrix4f matrix,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,
            float[] color
    ) {
        buffer.vertex(matrix, x1, y1, z1)
                .color(color[0], color[1], color[2], 1.0f);

        buffer.vertex(matrix, x2, y2, z2)
                .color(color[0], color[1], color[2], 1.0f);

        buffer.vertex(matrix, x3, y3, z3)
                .color(color[0], color[1], color[2], 1.0f);

        buffer.vertex(matrix, x4, y4, z4)
                .color(color[0], color[1], color[2], 1.0f);
    }

    private static float[] unpackColor(int color) {
        float r = ((color >> 16) & 255) / 255.0f;
        float g = ((color >> 8) & 255) / 255.0f;
        float b = (color & 255) / 255.0f;

        return new float[]{r, g, b};
    }
}
