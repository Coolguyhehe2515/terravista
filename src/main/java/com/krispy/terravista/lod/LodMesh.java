package com.krispy.terravista.lod;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexBuffer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

public class LodMesh implements AutoCloseable {

    private final VertexBuffer vertexBuffer;
    private boolean uploaded;

    public LodMesh() {
        this.vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.uploaded = false;
    }

    public void build(
            LodChunk chunk,
            double cameraX,
            double cameraY,
            double cameraZ
    ) {
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

                float r = ((chunk.getColor(x, z) >> 16) & 255) / 255.0f;
                float g = ((chunk.getColor(x, z) >> 8) & 255) / 255.0f;
                float b = (chunk.getColor(x, z) & 255) / 255.0f;

                float x1 = (float) (worldX - cameraX);
                float z1 = (float) (worldZ - cameraZ);
                float x2 = (float) (worldX + step - cameraX);
                float z2 = (float) (worldZ + step - cameraZ);
                float y = (float) (height - cameraY);

                buffer.vertex(x1, y, z1)
                        .color(r, g, b, 1.0f);

                buffer.vertex(x1, y, z2)
                        .color(r, g, b, 1.0f);

                buffer.vertex(x2, y, z2)
                        .color(r, g, b, 1.0f);

                buffer.vertex(x2, y, z1)
                        .color(r, g, b, 1.0f);
            }
        }

        BufferBuilder.BuiltBuffer builtBuffer = buffer.end();

        if (builtBuffer != null) {
            vertexBuffer.bind();
            vertexBuffer.upload(builtBuffer);
            VertexBuffer.unbind();
            builtBuffer.close();

            uploaded = true;
        }
    }

    public void draw(Matrix4f matrix) {
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
