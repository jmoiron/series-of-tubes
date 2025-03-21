package net.jmoiron.chubes.common.client.gui;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Utilities for rendering ItemStacks to textures.
 */
public class ItemStackRenderer {
    private static Minecraft mc = Minecraft.getInstance();

    private static String getName(ItemStack itemStack) {
        return itemStack.getDisplayName().getString();
    }

    public static NativeImage getGuiTexture(ItemStack itemStack) {
        if (RenderSystem.isOnRenderThread()) {
            return getGuiTextureUnsafe(itemStack);
        }

        CompletableFuture<NativeImage> future = new CompletableFuture<>();

        // wait for the result in this thread but submit to the render thread
        //CountDownLatch latch = new CountDownLatch(1);
        //AtomicReference<NativeImage> result = new AtomicReference<>();

        RenderSystem.recordRenderCall(() -> {
            future.complete(getGuiTextureUnsafe(itemStack));
        });

        try {
            return future.get();
        } catch (Exception e) {
            System.err.println("Thread interrupted while waiting for rendering to complete: " + e.getMessage());
            return null;
        }

    }

    private static NativeImage getGuiTextureUnsafe(ItemStack itemStack) {
        var renderer = mc.getItemRenderer();
        var model = renderer.getModel(itemStack, mc.level, null, 0);

        if (model.isCustomRenderer()) {
            return null;
        } else if (model.usesBlockLight()) {
            return getBlockLightGuiTexture(itemStack);
        } else if (!model.isGui3d()) {
            // the model is a 3d block so we can use our simpler routine
            return getFlatItemGuiTexture(itemStack);
        } else {
            // System.out.println(name+" no block light");
        }
        return null;
    }

    /**
     * Returns the sprite texture for the itemStack as a NativeImage.
     * This method is only suitable for items whose gui textures are
     * sprites and not rendered versions of their world models.
     *
     * @param itemStack The ItemStack to get the GUI texture for
     * @return NativeImage representing the sprite texture, or null if an error occurs
     */
    @SuppressWarnings("deprecation")
    private static NativeImage getFlatItemGuiTexture(ItemStack itemStack) {
        try {
            return mc
                .getItemRenderer()
                .getItemModelShaper()
                .getItemModel(itemStack)
                .getParticleIcon()
                .contents()
                .getOriginalImage();

        } catch (NullPointerException e) {
            return null;
        }
    }

    private static final int SIZE = 16;

    private static NativeImage getBlockLightGuiTexture(ItemStack itemStack) {
        Lighting.setupFor3DItems();

        var renderer = mc.getItemRenderer();
        var model = renderer.getModel(itemStack, mc.level, null, 0);
        var fb = new MainTarget(SIZE, SIZE);

        //fb.setClearColor(0, 0, 0, 0);
        //fb.clear(Minecraft.ON_OSX);
        fb.bindWrite(true);

        RenderSystem.clearColor(0, 0, 0, 0);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        // RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        PoseStack poseStack = new PoseStack();
        var buffer = mc.renderBuffers().bufferSource();

        poseStack.pushPose();
		poseStack.translate(SIZE/2.0, SIZE/2.0, 150f);
		poseStack.scale(1f, -1f, 1f);

        renderer.render(
            itemStack,
            ItemDisplayContext.GUI,
            false,
            poseStack,
            buffer,
            0xF000F0,
            OverlayTexture.NO_OVERLAY,
            model);

        buffer.endBatch();
        poseStack.popPose();

        fb.bindRead();

        var img = new NativeImage(SIZE, SIZE, false);
        RenderSystem.bindTexture(0);
        System.out.println("texture id=" + fb.getColorTextureId());
        img.downloadTexture(0, false);

        fb.unbindRead();
        fb.unbindWrite();
        fb.destroyBuffers();

        return img;
    }
}
