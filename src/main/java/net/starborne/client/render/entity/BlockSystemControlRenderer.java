package net.starborne.client.render.entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.starborne.server.entity.BlockSystemControlEntity;

public class BlockSystemControlRenderer extends Render<BlockSystemControlEntity> {
    public BlockSystemControlRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(BlockSystemControlEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(BlockSystemControlEntity entity) {
        return null;
    }
}
