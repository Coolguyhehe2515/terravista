package com.krispy.terravista.lod;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

public class LodMesh implements AutoCloseable {

    private final VertexBuffer vertexBuffer;
    private boolean uploaded;
    private int sampleStep = -1;

    public LodMesh() {
        vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
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

                buffer.vertex(worldX, height, worldZ + step)
                        .color(r, g, b, 1.0f);

                buffer.vertex(worldX + step, height, worldZ + step)
                        .color(r, g, b, 1.0f);

                buffer.vertex(worldX + step, height, worldZ)
                        .color(r, g, b, 1.0f);
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
        sampleStep = step;
    }

    public void draw(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        if (!uploaded) {
            return;
        }

        vertexBuffer.bind();

        vertexBuffer.draw(
                viewMatrix,
                projectionMatrix,
                net.minecraft.client.render.GameRenderer
                        .getPositionColorProgram()
        );

        VertexBuffer.unbind();
    }

    public boolean matches(LodChunk chunk) {
        return sampleStep == chunk.getSampleStep();
    }

    @Override
    public void close() {
        vertexBuffer.close();
        uploaded = false;
        sampleStep = -1;
    }
}
