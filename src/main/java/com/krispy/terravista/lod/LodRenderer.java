package com.krispy.terravista.lod;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexBuffer;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class LodRenderer {

    private static final Map<Long, LodMesh> meshes = new HashMap<>();

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
        RenderSystem.disableBlend();

        RenderSystem.setShader(
                GameRenderer::getPositionColorProgram
        );

        for (Map.Entry<Long, LodChunk> entry : chunks.entrySet()) {

            long key = entry.getKey();
            LodChunk chunk = entry.getValue();

            LodMesh mesh = meshes.get(key);

            if (mesh == null) {
                mesh = new LodMesh();
                mesh.build(chunk);
                meshes.put(key, mesh);
            }

            mesh.draw();
        }
    }

    public static void remove(long key) {
        LodMesh mesh = meshes.remove(key);

        if (mesh != null) {
            mesh.close();
        }
    }

    public static void clear() {
        for (LodMesh mesh : meshes.values()) {
            mesh.close();
        }

        meshes.clear();
    }
}
