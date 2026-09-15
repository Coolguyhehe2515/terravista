package com.krispy.terravista.lod;

import net.minecraft.util.math.ChunkPos;

public class LodChunk {

    private final ChunkPos chunkPos;
    private final int sampleStep;
    private final int width;
    private final int[] heights;
    private final int[] colors;

    public LodChunk(ChunkPos chunkPos, int sampleStep) {
        this.chunkPos = chunkPos;
        this.sampleStep = sampleStep;
        this.width = 16 / sampleStep;
        this.heights = new int[width * width];
        this.colors = new int[width * width];
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public int getSampleStep() {
        return sampleStep;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight(int x, int z) {
        return heights[z * width + x];
    }

    public void setHeight(int x, int z, int height) {
        heights[z * width + x] = height;
    }

    public int getColor(int x, int z) {
        return colors[z * width + x];
    }

    public void setColor(int x, int z, int color) {
        colors[z * width + x] = color;
    }

    public int getWorldX(int x) {
        return chunkPos.getStartX() + x * sampleStep;
    }

    public int getWorldZ(int z) {
        return chunkPos.getStartZ() + z * sampleStep;
    }
}

"src/main/java/com/krispy/terravista/lod/LodGenerator.java"

package com.krispy.terravista.lod;

import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;

public class LodGenerator {

    public static LodChunk generate(
            ClientWorld world,
            ChunkPos chunkPos,
            int sampleStep
    ) {
        sampleStep = normalizeSampleStep(sampleStep);

        LodChunk lodChunk = new LodChunk(chunkPos, sampleStep);

        int width = lodChunk.getWidth();
        int minY = world.getBottomY();
        int maxY = world.getTopY();

        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int z = 0; z < width; z++) {
            for (int x = 0; x < width; x++) {

                int worldX = chunkPos.getStartX() + x * sampleStep;
                int worldZ = chunkPos.getStartZ() + z * sampleStep;

                int height = world.getTopY(
                        Heightmap.Type.WORLD_SURFACE,
                        worldX,
                        worldZ
                );

                if (height < minY) {
                    height = minY;
                }

                if (height > maxY) {
                    height = maxY;
                }

                mutable.set(worldX, height - 1, worldZ);

                BlockState state = world.getBlockState(mutable);

                int color = state.getMapColor(world, mutable).color;

                lodChunk.setHeight(x, z, height);
                lodChunk.setColor(x, z, color);
            }
        }

        return lodChunk;
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

"src/main/java/com/krispy/terravista/lod/LodManager.java"

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
            lastSampleStep = -1;
        }

        int sampleStep = normalizeSampleStep(
                TerraVistaConfig.get().sampleStep
        );

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
                        (double) x * x * 256.0 +
                        (double) z * z * 256.0
                );

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

                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    continue;
                }

                LodChunk lodChunk = LodGenerator.generate(
                        world,
                        chunkPos,
                        normalizeSampleStep(config.sampleStep)
                );

                if (lodChunk != null) {
                    cache.put(key, lodChunk);
                    generated++;
                }
            }
        }

        removeFarChunks(centerChunkX, centerChunkZ, radius);
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
        lastSampleStep = -1;
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
