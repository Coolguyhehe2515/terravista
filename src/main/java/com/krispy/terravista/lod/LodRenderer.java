package com.krispy.terravista.lod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.joml.Matrix4f;

public class LodRenderer {

    private static final Map<Long, LodMesh> meshes = new HashMap<>();

    public static void render(
            Matrix4f viewMatrix,
            Matrix4f projectionMatrix,
            Map<Long, LodChunk> chunks
    ) {
        if (chunks.isEmpty()) {
            cleanupMissing(chunks);
            return;
        }

        for (Map.Entry<Long, LodChunk> entry : chunks.entrySet()) {

            long key = entry.getKey();
            LodChunk chunk = entry.getValue();

            LodMesh mesh = meshes.get(key);

            if (mesh == null) {
                mesh = new LodMesh();
                mesh.build(chunk);
                meshes.put(key, mesh);
            } else if (!mesh.matches(chunk)) {
                mesh.close();
                mesh = new LodMesh();
                mesh.build(chunk);
                meshes.put(key, mesh);
            }

            mesh.draw(viewMatrix, projectionMatrix);
        }

        cleanupMissing(chunks);
    }

    private static void cleanupMissing(
            Map<Long, LodChunk> chunks
    ) {
        Iterator<Map.Entry<Long, LodMesh>> iterator =
                meshes.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Long, LodMesh> entry = iterator.next();

            if (!chunks.containsKey(entry.getKey())) {
                entry.getValue().close();
                iterator.remove();
            }
        }
    }

    public static void clear() {
        for (LodMesh mesh : meshes.values()) {
            mesh.close();
        }

        meshes.clear();
    }
}
