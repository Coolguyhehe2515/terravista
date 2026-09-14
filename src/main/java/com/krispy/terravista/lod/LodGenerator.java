package com.krispy.terravista.lod;

import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public final class LodGenerator {
    private LodGenerator() {
    }

    public static LodChunk generate(ClientWorld world, ChunkPos chunkPos, int sampleStep) {
        LodChunk lodChunk = new LodChunk(chunkPos, sampleStep);
        int samples = lodChunk.getSamplesPerSide();

        for (int z = 0; z < samples; z++) {
            for (int x = 0; x < samples; x++) {
                int worldX = chunkPos.getStartX() + x * sampleStep;
                int worldZ = chunkPos.getStartZ() + z * sampleStep;

                int height = findSurface(world, worldX, worldZ);
                int color = getSurfaceColor(world, worldX, height, worldZ);

                lodChunk.setHeight(x, z, height);
                lodChunk.setColor(x, z, color);
            }
        }

        return lodChunk;
    }

    private static int findSurface(ClientWorld world, int x, int z) {
        int top = world.getTopYInclusive();
        int bottom = world.getBottomY();

        BlockPos.Mutable pos = new BlockPos.Mutable(x, top, z);

        for (int y = top; y >= bottom; y--) {
            pos.setY(y);

            BlockState state = world.getBlockState(pos);

            if (!state.isAir() && !state.getCollisionShape(world, pos).isEmpty()) {
                return y;
            }
        }

        return bottom;
    }

    private static int getSurfaceColor(ClientWorld world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = world.getBlockState(pos);
        MapColor mapColor = state.getMapColor(world, pos);

        if (mapColor == MapColor.CLEAR) {
            return 0xFF808080;
        }

        return mapColor.color | 0xFF000000;
    }
}
