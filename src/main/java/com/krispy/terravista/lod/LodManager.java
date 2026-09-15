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

    public void updateWorld(ClientWorld world) {
        if (this.world != world) {
            this.world = world;
            cache.clear();
        }
    }

    public void updateAround(int centerChunkX, int centerChunkZ) {
        if (world == null) {
            return;
        }

        TerraVistaConfig config = TerraVistaConfig.get();

        if (!config.enabled) {
            return;
        }

        int radius = Math.max(1, config.renderDistance);
        int maxPerFrame = Math.max(1, config.maxChunksPerFrame);
        int generated = 0;

        for (int z = -radius; z <= radius && generated < maxPerFrame; z++) {
            for (int x = -radius; x <= radius && generated < maxPerFrame; x++) {

                int chunkX = centerChunkX + x;
                int chunkZ = centerChunkZ + z;

                double distance = Math.sqrt(
                        (double) x * x * 16.0 * 16.0 +
                        (double) z * z * 16.0 * 16.0
                );

                if (distance < config.nearDistance) {
                    continue;
                }

                if (distance > config.lodDistance) {
                    continue;
                }

                int sampleStep = getSampleStep(
                        distance,
                        config.nearDistance,
                        config.lodDistance,
                        config.sampleStep
                );

                ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
                long key = chunkPos.toLong();

                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    continue;
                }

                LodChunk cached = cache.get(key);

                if (cached != null &&
                        cached.getSampleStep() == sampleStep) {
                    continue;
                }

                LodChunk lodChunk = LodGenerator.generate(
                        world,
                        chunkPos,
                        sampleStep
                );

                if (lodChunk != null) {
                    cache.put(key, lodChunk);
                    generated++;
                }
            }
        }

        removeFarChunks(
                centerChunkX,
                centerChunkZ,
                radius
        );
    }

    private int getSampleStep(
            double distance,
            int nearDistance,
            int lodDistance,
            int baseSampleStep
    ) {
        if (distance <= nearDistance * 2.0) {
            return normalizeSampleStep(baseSampleStep);
        }

        if (distance <= nearDistance * 4.0) {
            return normalizeSampleStep(
                    Math.max(2, baseSampleStep)
            );
        }

        if (distance <= nearDistance * 8.0) {
            return normalizeSampleStep(
                    Math.max(4, baseSampleStep)
            );
        }

        if (distance <= lodDistance) {
            return normalizeSampleStep(
                    Math.max(8, baseSampleStep)
            );
        }

        return normalizeSampleStep(baseSampleStep);
    }

    private void removeFarChunks(
            int centerChunkX,
            int centerChunkZ,
            int radius
    ) {
        int maxDistance = radius + 2;
        int maxDistanceSquared = maxDistance * maxDistance;

        Iterator<Map.Entry<Long, LodChunk>> iterator =
                cache.entrySet().iterator();

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
    }

    private static int normalizeSampleStep(int sampleStep) {
        if (sampleStep <= 1) {
            return 1;
        }

        if (sampleStep <= 2) {
            return 2;
        }

        if (sampleStep <= 4) {
            return 4;
        }

        return 8;
    }
}
