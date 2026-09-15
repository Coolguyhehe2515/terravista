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
        this.width = Math.max(1, 16 / sampleStep);
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

    public int getMinHeight() {
        int min = Integer.MAX_VALUE;

        for (int height : heights) {
            if (height < min) {
                min = height;
            }
        }

        return min;
    }

    public int getMaxHeight() {
        int max = Integer.MIN_VALUE;

        for (int height : heights) {
            if (height > max) {
                max = height;
            }
        }

        return max;
    }
}
