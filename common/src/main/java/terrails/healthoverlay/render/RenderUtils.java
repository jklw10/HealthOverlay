package terrails.healthoverlay.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import terrails.healthoverlay.Constants;
import terrails.healthoverlay.ModConfiguration;
import terrails.healthoverlay.heart.Heart;
import terrails.healthoverlay.heart.HeartPiece;
import terrails.healthoverlay.heart.HeartType;

import java.util.List;

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
        drawColoredTexturedQuad(poseStack.last().pose(),
                x1, x2,
                y1, y2,
                Minecraft.getInstance().gui.getBlitOffset(),
                (u1) / 256.0F, (u2) / 256.0F,
                (v1) / 256.0F, (v2) / 256.0F,
                red, green, blue, alpha);
    }

    public static void drawColoredTexturedQuad(Matrix4f matrix4f, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, int red, int green, int blue, int alpha) {
        RenderSystem.setShader(() -> alpha != 0 ? GameRenderer.getPositionColorTexShader() : GameRenderer.getPositionTexShader());
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, alpha != 0 ? DefaultVertexFormat.POSITION_COLOR_TEX : DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix4f, (float) x0, (float) y1, (float) z).color(red, green, blue, alpha).uv(u0, v1).endVertex();
        bufferBuilder.vertex(matrix4f, (float) x1, (float) y1, (float) z).color(red, green, blue, alpha).uv(u1, v1).endVertex();
        bufferBuilder.vertex(matrix4f, (float) x1, (float) y0, (float) z).color(red, green, blue, alpha).uv(u1, v0).endVertex();
        bufferBuilder.vertex(matrix4f, (float) x0, (float) y0, (float) z).color(red, green, blue, alpha).uv(u0, v0).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
    }

    public static List<Heart> calculateHearts(int absorption, int health, int maxHealth, HeartType heartType) {
        HeartPiece[] healthColors, absorptionColors; // Indices: 0 == Top row, 1 == Bottom row

        switch (heartType) {
            case NORMAL -> {
                healthColors = ModConfiguration.healthColors;
                absorptionColors = ModConfiguration.absorptionColors;
            }
            case POISONED -> {
                healthColors = ModConfiguration.healthPoisonColors;
                absorptionColors = ModConfiguration.absorptionPoisonColors;
            }
            case WITHERED -> {
                healthColors = ModConfiguration.healthWitherColors;
                absorptionColors = ModConfiguration.absorptionWitherColors;
            }
            case FROZEN -> {
                healthColors = ModConfiguration.healthFrozenColors;
                absorptionColors = ModConfiguration.absorptionFrozenColors;
            }
            default -> {
                Constants.LOGGER.error("Something went very wrong, heart type '{}' does not exist. Using normal heart colors...", heartType);
                healthColors = ModConfiguration.healthColors;
                absorptionColors = ModConfiguration.absorptionColors;
            }
        }
        // Health
        int healthLayer = Math.max((int) ((health ) / 20.0) % healthColors.length -1,0) ;
        
        boolean skipTopHealthHeart = health % 20 == 0 || health < 20;
        healthColors = new HeartPiece[]{
                skipTopHealthHeart ? null : healthColors[healthLayer + 1],
                healthColors[healthLayer]
        };

        // Absorption
        int absorptionLayer = Math.max( (int) ((absorption) / 20.0 % absorptionColors.length)-1,0);
        // One heart color due to health being divisible by 20 or below 20
        boolean skipTopAbsorptionHeart = absorption % 20 == 0 || absorption < 20;
        absorptionColors = new HeartPiece[]{
                skipTopAbsorptionHeart ? null : absorptionColors[absorptionLayer+1],
                absorptionColors[absorptionLayer]
        };
        //how many half harts needed to draw
        int healthRange = Math.min(health, 20);
        int absorptionRange = Math.min(absorption, 20);
        //how many hearts are ontop of layer below
        int topHealthRange = skipTopHealthHeart ? 0 : (health % 20);
        int topAbsorptionRange = skipTopAbsorptionHeart ? 0 : (absorption % 20);
        
        int healthHearts = Mth.ceil(maxHealth / 2.0F);
        int absorptionHearts = Mth.ceil(absorptionRange / 2.0F);
        //previously Mth.ceil(Math.min(maxHealth + absorption, 20) / 2.0F)
        int bothHearts = Math.min(healthHearts+absorptionHearts,10);

        Heart[] hearts = new Heart[bothHearts];

        ModConfiguration.AbsorptionMode mode = ModConfiguration.absorptionOverHealthMode;
        if (health % 20 == 0 || absorption % 20 == 0 || absorption >= 20) {
            mode = ModConfiguration.AbsorptionMode.BEGINNING;
        }

        // Get free space left after the highest health row
        int heartSpace = 20 - (health % 20);
        // Get absorption that is left after free space is filled.
        // This should be added first due to it overflowing to the beginning.
        // Condition is to check if absorption is really overflowing, otherwise it should be skipped
        boolean Wraps = absorption > heartSpace;
        int firstAbsorptionRowStart = Wraps ? absorptionRange - heartSpace : 0;
        // Gets the absorption range after the highest health row
        int overflow = health > 20 ? (topHealthRange + absorptionRange) : (healthRange + absorptionRange);

        HeartPiece firstHalf = null;
        for (int i = 0; i < bothHearts*2; i++) {
            boolean completesHeart = i % 2 != 0;
            HeartPiece currentPiece = healthColors[1];
            if(i >= (health % 20) && !skipTopHealthHeart){
                currentPiece = healthColors[0];
            }
            if (mode == ModConfiguration.AbsorptionMode.BEGINNING)  {
                if(i >= (absorption % 20) && !skipTopAbsorptionHeart){
                    currentPiece = absorptionColors[0];
                }
                if(i >= topAbsorptionRange){
                    currentPiece = absorptionColors[1];
                }
            }
            if (mode == ModConfiguration.AbsorptionMode.AFTER_HEALTH)  {
                if(Wraps){
                    if(i < overflow){
                        currentPiece = absorptionColors[0];
                    }
                    if(i >= firstAbsorptionRowStart){
                        currentPiece = absorptionColors[1];
                    }
                }
                
            }
            
            if(!completesHeart){
                firstHalf = currentPiece;
                continue;
            }
            hearts[i/2] = Heart.full(firstHalf, currentPiece);
        }
    
        return List.ArrayList(hearts);
    }
}
