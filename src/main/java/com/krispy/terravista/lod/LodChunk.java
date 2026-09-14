package com.krispy.terravista.lod;

import net.minecraft.util.math.ChunkPos;

public final class LodChunk {
    private final ChunkPos chunkPos;
    private final int sampleStep;
    private final int[] heights;
    private final int[] colors;

    public LodChunk(ChunkPos chunkPos, int sampleStep) {
        if (sampleStep < 1 || 16 % sampleStep != 0) {
            throw new IllegalArgumentException("sampleStep must divide 16");
        }

        this.chunkPos = chunkPos;
        this.sampleStep = sampleStep;

        int samplesPerSide = 16 / sampleStep;
        this.heights = new int[samplesPerSide * samplesPerSide];
        this.colors = new int[samplesPerSide * samplesPerSide];
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public int getSampleStep() {
        return sampleStep;
    }

    public int getSamplesPerSide() {
        return 16 / sampleStep;
    }

    public int getHeight(int x, int z) {
        return heights[z * getSamplesPerSide() + x];
    }

    public void setHeight(int x, int z, int height) {
        heights[z * getSamplesPerSide() + x] = height;
    }

    public int getColor(int x, int z) {
        return colors[z * getSamplesPerSide() + x];
    }

    public void setColor(int x, int z, int color) {
        colors[z * getSamplesPerSide() + x] = color;
    }

    public int getWorldX(int sampleX) {
        return chunkPos.getStartX() + sampleX * sampleStep;
    }

    public int getWorldZ(int sampleZ) {
        return chunkPos.getStartZ() + sampleZ * sampleStep;
    }
}
