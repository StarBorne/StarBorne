package net.starborne.client.render.blocksystem;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.starborne.client.render.blocksystem.chunk.BlockSystemRenderChunk;
import net.starborne.client.render.blocksystem.chunk.RenderChunkFactory;
import net.starborne.server.blocksystem.BlockSystem;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class BlockSystemViewFrustum {
    protected final BlockSystemRenderer renderer;
    protected final BlockSystem blockSystem;
    protected int chunkRangeY;
    protected int chunkRangeX;
    protected int chunkRangeZ;
    protected RenderChunkFactory factory;
    public BlockSystemRenderChunk[] chunks;

    public BlockSystemViewFrustum(BlockSystemRenderer renderer, BlockSystem blockSystem, int renderDistance, RenderChunkFactory factory) {
        this.renderer = renderer;
        this.blockSystem = blockSystem;
        this.factory = factory;
        this.setRenderDistance(renderDistance);
        this.populateChunks();
    }

    protected void populateChunks() {
        int chunkCount = this.chunkRangeX * this.chunkRangeY * this.chunkRangeZ;
        this.chunks = new BlockSystemRenderChunk[chunkCount];
        for (int chunkX = 0; chunkX < this.chunkRangeX; ++chunkX) {
            for (int chunkY = 0; chunkY < this.chunkRangeY; ++chunkY) {
                for (int chunkZ = 0; chunkZ < this.chunkRangeZ; ++chunkZ) {
                    int arrayIndex = (chunkZ * this.chunkRangeY + chunkY) * this.chunkRangeX + chunkX;
                    this.chunks[arrayIndex] = this.factory.create(this.blockSystem, this.renderer);
                    this.chunks[arrayIndex].setPosition(chunkX * 16, chunkY * 16, chunkZ * 16);
                }
            }
        }
    }

    public void delete() {
        for (BlockSystemRenderChunk chunk : this.chunks) {
            chunk.delete();
        }
    }

    protected void setRenderDistance(int renderRange) {
        int horizontalRange = renderRange * 2 + 1;
        this.chunkRangeX = horizontalRange;
        this.chunkRangeY = 16;
        this.chunkRangeZ = horizontalRange;
    }

    public void updateChunkPositions(double viewEntityX, double viewEntityZ) {
        int baseX = MathHelper.floor_double(viewEntityX) - 8;
        int baseZ = MathHelper.floor_double(viewEntityZ) - 8;
        int blockRangeX = this.chunkRangeX * 16;
        for (int chunkX = 0; chunkX < this.chunkRangeX; ++chunkX) {
            int x = this.getBaseCoordinate(baseX, blockRangeX, chunkX);
            for (int chunkZ = 0; chunkZ < this.chunkRangeZ; ++chunkZ) {
                int z = this.getBaseCoordinate(baseZ, blockRangeX, chunkZ);
                for (int chunkY = 0; chunkY < this.chunkRangeY; ++chunkY) {
                    int y = chunkY * 16;
                    BlockSystemRenderChunk chunk = this.chunks[(chunkZ * this.chunkRangeY + chunkY) * this.chunkRangeX + chunkX];
                    chunk.setPosition(x, y, z);
                }
            }
        }
    }

    private int getBaseCoordinate(int base, int range, int chunkCoordinate) {
        int worldCoordinate = chunkCoordinate * 16;
        int center = (worldCoordinate - base) + (range / 2);
        if (center < 0) {
            center -= range - 1;
        }
        return worldCoordinate - ((center / range) * range);
    }

    public void queueRenderUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean requiresUpdate) {
        int chunkMinX = MathHelper.bucketInt(minX, 16);
        int chunkMinY = MathHelper.bucketInt(minY, 16);
        int chunkMinZ = MathHelper.bucketInt(minZ, 16);
        int chunkMaxX = MathHelper.bucketInt(maxX, 16);
        int chunkMaxY = MathHelper.bucketInt(maxY, 16);
        int chunkMaxZ = MathHelper.bucketInt(maxZ, 16);
        for (int worldChunkX = chunkMinX; worldChunkX <= chunkMaxX; ++worldChunkX) {
            int chunkX = worldChunkX % this.chunkRangeX;
            if (chunkX < 0) {
                chunkX += this.chunkRangeX;
            }
            for (int worldChunkY = chunkMinY; worldChunkY <= chunkMaxY; ++worldChunkY) {
                int chunkY = worldChunkY % this.chunkRangeY;
                if (chunkY < 0) {
                    chunkY += this.chunkRangeY;
                }
                for (int worldChunkZ = chunkMinZ; worldChunkZ <= chunkMaxZ; ++worldChunkZ) {
                    int chunkZ = worldChunkZ % this.chunkRangeZ;
                    if (chunkZ < 0) {
                        chunkZ += this.chunkRangeZ;
                    }
                    int chunkIndex = (chunkZ * this.chunkRangeY + chunkY) * this.chunkRangeX + chunkX;
                    BlockSystemRenderChunk chunk = this.chunks[chunkIndex];
                    chunk.setNeedsUpdate(requiresUpdate);
                }
            }
        }
    }

    @Nullable
    protected BlockSystemRenderChunk getChunk(BlockPos pos) {
        int chunkX = MathHelper.bucketInt(pos.getX(), 16);
        int chunkY = MathHelper.bucketInt(pos.getY(), 16);
        int chunkZ = MathHelper.bucketInt(pos.getZ(), 16);
        if (chunkY >= 0 && chunkY < this.chunkRangeY) {
            chunkX = chunkX % this.chunkRangeX;
            if (chunkX < 0) {
                chunkX += this.chunkRangeX;
            }
            chunkZ = chunkZ % this.chunkRangeZ;
            if (chunkZ < 0) {
                chunkZ += this.chunkRangeZ;
            }
            int index = (chunkZ * this.chunkRangeY + chunkY) * this.chunkRangeX + chunkX;
            return this.chunks[index];
        } else {
            return null;
        }
    }
}