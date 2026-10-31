package net.smok.om.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.smok.om.entity.SpellProjectile;

public class SpellProjectileRenderer extends EntityRenderer<SpellProjectile, SpellProjectileRenderer.SpellRenderState> {

    private final ItemModelResolver itemModelResolver;

    public SpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public SpellRenderState createRenderState() {
        return new SpellRenderState();
    }

    @Override
    public void extractRenderState(SpellProjectile entity, SpellRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        this.itemModelResolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.GROUND, entity);
        state.scale = entity.getItemRenderScale();
    }

    @Override
    public void submit(final SpellRenderState state, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(state.scale, state.scale, state.scale);
        poseStack.mulPose(camera.orientation);
        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    public static class SpellRenderState extends EntityRenderState {
        public ItemStackRenderState item = new ItemStackRenderState();
        public float scale = 1;
    }
}
