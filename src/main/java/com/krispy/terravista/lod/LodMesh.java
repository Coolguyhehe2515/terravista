package com.krispy.terravista.lod;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public class LodMesh implements AutoCloseable {

    private final VertexBuffer vertexBuffer;
    private boolean uploaded;

    public LodMesh() {
        vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        uploaded = false;
    }

    public void build(LodChunk chunk) {
        Tessellator tessellator = Tessellator.getInstance();

        BufferBuilder buffer = tessellator.begin(
                VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION_COLOR
        );

        int width = chunk.getWidth();
        int step = chunk.getSampleStep();

        for (int z = 0; z < width; z++) {
            for (int x = 0; x < width; x++) {

                int worldX = chunk.getWorldX(x);
                int worldZ = chunk.getWorldZ(z);
                int height = chunk.getHeight(x, z);

                int color = chunk.getColor(x, z);

                float r = ((color >> 16) & 255) / 255.0f;
                float g = ((color >> 8) & 255) / 255.0f;
                float b = (color & 255) / 255.0f;

                buffer.vertex(worldX, height, worldZ)
                        .color(r, g, b, 1.0f);

                buffer.vertex(
                        worldX,
                        height,
                        worldZ + step
                ).color(r, g, b, 1.0f);

                buffer.vertex(
                        worldX + step,
                        height,
                        worldZ + step
                ).color(r, g, b, 1.0f);

                buffer.vertex(
                        worldX + step,
                        height,
                        worldZ
                ).color(r, g, b, 1.0f);
            }
        }

        BufferBuilder.BuiltBuffer builtBuffer = buffer.end();

        if (builtBuffer == null) {
            return;
        }

        vertexBuffer.bind();
        vertexBuffer.upload(builtBuffer);
        VertexBuffer.unbind();

        builtBuffer.close();

        uploaded = true;
    }

    public void draw() {
        if (!uploaded) {
            return;
        }

        vertexBuffer.bind();
        vertexBuffer.draw();
        VertexBuffer.unbind();
    }

    public boolean isUploaded() {
        return uploaded;
    }

    @Override
    public void close() {
        vertexBuffer.close();
        uploaded = false;
    }
}
