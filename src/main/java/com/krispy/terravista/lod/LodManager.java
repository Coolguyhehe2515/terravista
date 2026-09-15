package com.krispy.terravista.lod;

import com.krispy.terravista.config.TerraVistaConfig;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LodManager {

    private final Map<Long, LodChunk> cache = new HashMap<>();

    private ClientWorld world;
    private int lastSampleStep = -1;

    public void updateWorld(ClientWorld world) {
        if (this.world != world) {
            this.world = world;
            cache.clear();
        }

        int sampleStep = TerraVistaConfig.get().sampleStep;

        if (sampleStep != lastSampleStep) {
            cache.clear();
            lastSampleStep = sampleStep;
        }
    }

    public void updateAround(int centerChunkX, int centerChunkZ) {
        if (world == null) {
            return;
        }

        TerraVistaConfig config = TerraVistaConfig.get();

        int radius = Math.max(1, config.renderDistance);
        int maxPerFrame = Math.max(1, config.maxChunksPerFrame);

        int generated = 0;

        for (int z = -radius; z <= radius && generated < maxPerFrame; z++) {
            for (int x = -radius; x <= radius && generated < maxPerFrame; x++) {

                int chunkX = centerChunkX + x;
                int chunkZ = centerChunkZ + z;

                int dx = x * 16;
                int dz = z * 16;

                double distance = Math.sqrt((double) dx * dx + (double) dz * dz);

                if (distance < config.nearDistance) {
                    continue;
                }

                if (distance > config.lodDistance) {
                    continue;
                }

                ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
                long key = chunkPos.toLong();

                if (cache.containsKey(key)) {
                    continue;
                }

                LodChunk lodChunk = LodGenerator.generate(
                        world,
                        chunkPos,
                        config.sampleStep
                );

                if (lodChunk != null) {
                    cache.put(key, lodChunk);
                    generated++;
                }
            }
        }

        removeFarChunks(centerChunkX, centerChunkZ, radius);
    }

    private void removeFarChunks(int centerChunkX, int centerChunkZ, int radius) {
        int maxDistance = radius + 2;
        int maxDistanceSquared = maxDistance * maxDistance;

        Iterator<Map.Entry<Long, LodChunk>> iterator = cache.entrySet().iterator();

        while (iterator.hasNext()) {
            LodChunk lodChunk = iterator.next().getValue();

            int dx = lodChunk.getChunkPos().x - centerChunkX;
            int dz = lodChunk.getChunkPos().z - centerChunkZ;

            if (dx * dx + dz * dz > maxDistanceSquared) {
                iterator.remove();
            }
        }
    }

    public Map<Long, LodChunk> getCache() {
        return cache;
    }

    public void clear() {
        cache.clear();
        world = null;
        lastSampleStep = -1;
    }
}
