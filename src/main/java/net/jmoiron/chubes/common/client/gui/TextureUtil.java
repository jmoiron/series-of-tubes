package net.jmoiron.chubes.common.client.gui;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.jmoiron.chubes.ChubesMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;

public class TextureUtil {

    public static IGuiTexture getTextureForItem(String key) {
        var res = new ResourceLocation(key);
        var item = ForgeRegistries.ITEMS.getValue(res);
        if (item == null) {
            return null;
        }
        return new ItemStackTexture(item);
    }

    public static IGuiTexture getSilkScreenTextureForItem(String key) {
        var itemStack = getItemStack(key);
        if (itemStack == null) {
            return null;
        }

        var itemName = key.split(":")[1];
        var newLoc = new ResourceLocation(ChubesMod.MOD_ID, itemName+"_silk");

        var silk = applySilkscreenEffect(itemStack);
        if (silk == null) {
            return null;
        }

        System.out.println("Generated new silkScreen texture for key="+key+" at loc="+newLoc);
        registerTexture(silk, newLoc);

        // return getTextureForItem(key);
        return new ResourceTexture(newLoc);

    }

    // getItemStack returns an ItemStack associated with the key
    private static ItemStack getItemStack(String key) {
        return getItemStack(new ResourceLocation(key));
    }

    // getItemStack returns an ItemStack associated with the key
    private static ItemStack getItemStack(ResourceLocation key) {
        Item item = ForgeRegistries.ITEMS.getValue(key);
        if (item == null) {
            return null;
        }
        return new ItemStack(item);
    }

    /**
     * Registers a BufferedImage as a texture with a specific ResourceLocation.
     *
     * @param image          The BufferedImage to register.
     * @param loc The ResourceLocation to associate with the texture.
     */
    public static void registerTexture(BufferedImage image, ResourceLocation loc) {
        // Convert BufferedImage to NativeImage
        NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), false);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgba = image.getRGB(x, y);
                nativeImage.setPixelRGBA(x, y, rgba);
            }
        }

        System.out.println(nativeImage.toString());

        // Register the texture with the TextureManager
        Minecraft.getInstance().submit(() -> {
            Minecraft.getInstance().getTextureManager()
                .register(loc, new DynamicTexture(nativeImage));
        });

    }

    /**
     * Applies a silkscreen effect to the texture of an ItemStack.
     *
     * @param itemStack The ItemStack to process.
     * @return A BufferedImage with the silkscreen effect applied.
     */
    public static BufferedImage applySilkscreenEffect(ItemStack itemStack) {
        // Get the item's texture
        NativeImage texture = renderItemTexture(itemStack);
        if (texture == null) {
            return null; // Texture not found
        }

        // Convert NativeImage to BufferedImage for easier processing
        BufferedImage image = new BufferedImage(texture.getWidth(), texture.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < texture.getWidth(); x++) {
            for (int y = 0; y < texture.getHeight(); y++) {
                int rgba = texture.getPixelRGBA(x, y);
                image.setRGB(x, y, rgba);
            }
        }

        return applySilkscreenEffect(image);
    }


    public static NativeImage renderItemTexture(ItemStack itemStack) {
        if (RenderSystem.isOnRenderThread()) {
            return _drawItemStack(itemStack);
        }

        // Use a CountDownLatch to block until the rendering is complete
        CountDownLatch latch = new CountDownLatch(1);

        // Use an AtomicReference to store the result
        AtomicReference<NativeImage> result = new AtomicReference<>();

        RenderSystem.recordRenderCall(() -> {
            result.set(_drawItemStack(itemStack));
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted while waiting for rendering to complete: " + e.getMessage());
            return null;
        }

        // Return the result
        return result.get();
    }

    private static final int TEXTURE_SIZE = 16;

    private static NativeImage _drawItemStack(ItemStack itemStack) {
        var mc = Minecraft.getInstance();
        var graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
        NativeImage nativeImage = new NativeImage(TEXTURE_SIZE, TEXTURE_SIZE, false);
        RenderTarget frameBuffer = new MainTarget(TEXTURE_SIZE, TEXTURE_SIZE);

        frameBuffer.setClearColor(0, 0, 0, 0);
        frameBuffer.clear(Minecraft.ON_OSX);

        frameBuffer.bindWrite(true);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);


        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 232);
        graphics.renderItem(itemStack, 0, 0);
        // graphics.renderItemDecorations(mc.font, itemStack, 0, 0, null);
        graphics.pose().popPose();

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();

        frameBuffer.bindRead();
        nativeImage.downloadTexture(0, false);
        frameBuffer.unbindRead();
        frameBuffer.unbindWrite();
        frameBuffer.destroyBuffers();

        return nativeImage;
    }

    private static NativeImage _renderItemTexture(ItemStack itemStack) {

        NativeImage nativeImage = new NativeImage(TEXTURE_SIZE, TEXTURE_SIZE, false);
        Minecraft minecraft = Minecraft.getInstance();
        // MainTarget frameBuffer = new MainTarget(TEXTURE_SIZE, TEXTURE_SIZE);


        RenderTarget frameBuffer = new TextureTarget(TEXTURE_SIZE, TEXTURE_SIZE, false, Minecraft.ON_OSX);
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        // poseStack.mulPoseMatrix((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
		poseStack.scale(16.0F, -16.0F, 16.0F);


        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        BakedModel bakedModel = itemRenderer.getModel(itemStack, null, null, 0);

        GuiGraphics guiGraphics = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());

        frameBuffer.bindWrite(true);
        guiGraphics.renderFakeItem(itemStack, 0, 0);

        /*

        // Render the item as in the inventory
        itemRenderer.render(
            itemStack,
            ItemDisplayContext.GUI,
            false,
            poseStack,
            guiGraphics.bufferSource(),
            0xf000f0,
            OverlayTexture.NO_OVERLAY,
            bakedModel
        );
        poseStack.popPose();
        bufferSource.endBatch(); // Flush the render buffer
        */

        frameBuffer.bindRead();
        nativeImage.downloadTexture(0, false);
        frameBuffer.unbindRead();
        frameBuffer.unbindWrite();
        frameBuffer.destroyBuffers();

        return nativeImage;

    }

    /**
     * Retrieves the texture of an ItemStack as a NativeImage.
     *
     * @param itemStack The ItemStack to get the texture for.
     * @return The texture as a NativeImage, or null if not found.
     */
    private static NativeImage getItemTexture(ItemStack itemStack) {
        try {
            return Minecraft.getInstance()
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

    private static BufferedImage applySilkscreenEffect(BufferedImage image) {
        return applySilkscreenEffect(image, 128);
    }

    /**
     * Applies the silkscreen effect to a BufferedImage.
     *
     * @param image The input image.
     * @return The processed image with the silkscreen effect.
     */
    private static BufferedImage applySilkscreenEffect(BufferedImage image, int threshold) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Create a new image for the result
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgba = image.getRGB(x, y);

                int alpha = (rgba >> 24) & 0xFF;
                // retain alpha transparency
                if (alpha == 0) {
                    result.setRGB(x, y, 0x00FFFFFF);
                    continue;
                }

                int red = (rgba >> 16) & 0xFF;
                int green = (rgba >> 8) & 0xFF;
                int blue = rgba & 0xFF;

                // Convert to grayscale
                int gray = (red + green + blue) / 3;

                // Apply threshold to create a 2-color image
                // if the color "should" be black, then make it light gray
                // otherwise, make it invisible..  lower is darker!
                int color = (gray > threshold) ? 0x00FFFFFF : 0xFFDDDDDD;

                // Set the pixel in the result image
                result.setRGB(x, y, color);
            }
        }

        return result;
    }

}
