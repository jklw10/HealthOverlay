package terrails.healthoverlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

public class RenderUtils {

    public static void drawTexture(PoseStack poseStack, int x, int y, int u, int v) {
        drawTexture(poseStack, x, y, u, v, 0);
    }

    public static void drawTexture(PoseStack poseStack, int x, int y, int u, int v, int alpha) {
        drawTexture(poseStack, x, x + 9, y, y + 9, u, u + 9, v, v + 9, alpha);
    }

    public static void drawTexture(PoseStack poseStack, int x, int y, int u, int v, int color, int alpha) {
        drawTexture(poseStack, x, x + 9, y, y + 9, u, u + 9, v, v + 9, color, alpha);
    }

    public static void drawTexture(PoseStack poseStack, int x1, int x2, int y1, int y2, int u1, int u2, int v1, int v2) {
        drawTexture(poseStack, x1, x2, y1, y2, u1, u2, v1, v2, 0, 0, 0, 0);
    }

    public static void drawTexture(PoseStack poseStack, int x1, int x2, int y1, int y2, int u1, int u2, int v1, int v2, int alpha) {
        drawTexture(poseStack, x1, x2, y1, y2, u1, u2, v1, v2, 255, 255, 255, alpha);
    }

    public static void drawTexture(PoseStack poseStack, int x1, int x2, int y1, int y2, int u1, int u2, int v1, int v2, int color, int alpha) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        drawTexture(poseStack, x1, x2, y1, y2, u1, u2, v1, v2, r, g, b, alpha);
    }

    public static void drawTexture(PoseStack poseStack, int x1, int x2, int y1, int y2, int u1, int u2, int v1, int v2, int red, int green, int blue, int alpha) {
        RenderUtils.drawColoredTexturedQuad(poseStack.last().pose(),
                x1, x2,
                y1, y2,
                Minecraft.getInstance().gui.getBlitOffset(),
                (u1) / 256.0F, (u2) / 256.0F,
                (v1) / 256.0F, (v2) / 256.0F,
                red, green, blue, alpha);
    }

    public static void drawColoredTexturedQuad(Matrix4f matrices, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, int red, int green, int blue, int alpha) {
        RenderSystem.setShader(() -> alpha != 0 ? GameRenderer.getPositionColorTexShader() : GameRenderer.getPositionTexShader());
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, alpha != 0 ? DefaultVertexFormat.POSITION_COLOR_TEX : DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrices, (float) x0, (float) y1, (float) z).color(red, green, blue, alpha).uv(u0, v1).endVertex();
        bufferBuilder.vertex(matrices, (float) x1, (float) y1, (float) z).color(red, green, blue, alpha).uv(u1, v1).endVertex();
        bufferBuilder.vertex(matrices, (float) x1, (float) y0, (float) z).color(red, green, blue, alpha).uv(u1, v0).endVertex();
        bufferBuilder.vertex(matrices, (float) x0, (float) y0, (float) z).color(red, green, blue, alpha).uv(u0, v0).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
    }
}
