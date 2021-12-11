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
        List<Heart> hearts = Lists.newArrayList();
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
        if (health > 20) {
            if ((health % 20) == 0) {
                int var = (int) ((health - 1) / 20.0);
                int index = var % healthColors.length;
                healthColors = new HeartPiece[]{
                        null,
                        healthColors[index]
                };
            } else {
                int var = (int) (health / 20.0);
                healthColors = new HeartPiece[]{
                        healthColors[var % healthColors.length],
                        healthColors[(var - 1) % healthColors.length]
                };
            }
        } else {
            healthColors = new HeartPiece[]{
                    null,
                    healthColors[0]
            };
        }

        // Absorption
        if (absorption > 20) {
            if ((absorption % 20) == 0) {
                // One heart color due to health being divisible by 20
                int var = (int) ((absorption - 1) / 20.0);
                int index = var % absorptionColors.length;
                absorptionColors = new HeartPiece[]{
                        null,
                        absorptionColors[index]
                };
            } else {
                // Two different heart colors due to health not being divisible by 20
                int var = (int) (absorption / 20.0);
                absorptionColors = new HeartPiece[]{
                        absorptionColors[var % absorptionColors.length],
                        absorptionColors[(var - 1) % absorptionColors.length]
                };
            }
        } else if (absorption > 0) {
            absorptionColors = new HeartPiece[]{
                    null,
                    absorptionColors[0]
            };
        }

        int healthRange = Math.min(health, 20);
        int topHealthRange = health > 20 && (health % 20 != 0) ? (health % 20) : 0;

        int absorptionRange = Math.min(absorption, 20);
        int topAbsorptionRange = absorption > 20 && (absorption % 20 != 0) ? (absorption % 20) : 0;

        if (ModConfiguration.absorptionOverHealth && absorption > 0) {

            ModConfiguration.AbsorptionMode mode = ModConfiguration.absorptionOverHealthMode;
            if (health % 20 == 0 || absorption % 20 == 0 || absorption >= 20) {
                mode = ModConfiguration.AbsorptionMode.BEGINNING;
            }

            int[] offsets = null;
            if (mode == ModConfiguration.AbsorptionMode.AFTER_HEALTH) {
                    // Get free space left after the highest health row
                    int temp = 20 - (health % 20);

                    // Get absorption that is left after free space is filled.
                    // This should be added first due to it overflowing to the beginning.
                    // Condition is to check if absorption is really overflowing, otherwise it should be skipped
                    int var1 = absorption > temp ? absorptionRange - temp : 0;
                    // Gets the absorption range after the highest health row
                    int var2 = health > 20 ? (topHealthRange + absorptionRange) : (healthRange + absorptionRange);
                    offsets = new int[]{var1, var2};
                }

            for (int i = 0; i < Mth.ceil(Math.min(maxHealth + absorption, 20) / 2.0F); i++) {
                int value = i * 2 + 1;

                // If value is higher than absorption or current health add heart containers to get to max health
                if (value > (absorption + health)) {
                    if (value < maxHealth) {
                        hearts.add(Heart.CONTAINER_FULL);
                    } else if (value == maxHealth) {
                        hearts.add(Heart.CONTAINER_HALF);
                    }
                } else switch (mode) {
                    case BEGINNING -> {
                        if (value < topAbsorptionRange) {
                            hearts.add(Heart.full(absorptionColors[0]));
                        } else if (value == topAbsorptionRange) {
                            hearts.add(Heart.full(absorptionColors[0], absorptionColors[1]));
                        } else if (value < absorptionRange) {
                            hearts.add(Heart.full(absorptionColors[1]));
                        } else if (value == absorptionRange) {
                            hearts.add(Heart.full(absorptionColors[1], healthColors[value + 1 <= topHealthRange ? 0 : 1]));
                        } else if (value < topHealthRange) {
                            hearts.add(Heart.full(healthColors[0]));
                        } else if (value == topHealthRange) {
                            hearts.add(Heart.full(healthColors[0], healthColors[1]));
                        } else if (value < healthRange) {
                            hearts.add(Heart.full(healthColors[1]));
                        } else if (value == healthRange) {
                            hearts.add(Heart.half(healthColors[1], value != maxHealth));
                        }
                    }
                    case AFTER_HEALTH -> {
                        if (value < topAbsorptionRange) {
                            hearts.add(Heart.full(absorptionColors[0]));
                        } else if (value == topAbsorptionRange) {
                            hearts.add(Heart.full(absorptionColors[0], absorptionColors[1]));
                        } else if (value < offsets[0]) {
                            hearts.add(Heart.full(absorptionColors[1]));
                        } else if (value == offsets[0]) {
                            if (absorption >= 20) {
                                hearts.add(Heart.full(absorptionColors[1]));
                            } else hearts.add(Heart.full(absorptionColors[1], healthColors[value + 1 < topHealthRange ? 0 : 1]));
                        } else if (value < topHealthRange) {
                            hearts.add(Heart.full(healthColors[0]));
                        } else if (value == topHealthRange) {
                            if (value + 1 <= offsets[1]) {
                                hearts.add(Heart.full(healthColors[0], absorptionColors[1]));
                            } else hearts.add(Heart.full(healthColors[0], healthColors[1]));
                        } else if (health > 20) {
                            if (value < offsets[1]) {
                                hearts.add(Heart.full(absorptionColors[1]));
                            } else if (value == offsets[1]) {
                                hearts.add(Heart.full(absorptionColors[1], healthColors[1]));
                            } else if (value < healthRange) {
                                hearts.add(Heart.full(healthColors[1]));
                            } else if (value == healthRange) {
                                hearts.add(Heart.full(healthColors[1], absorptionColors[1]));
                            }
                        } else {
                            if (value < healthRange) {
                                hearts.add(Heart.full(healthColors[1]));
                            } else if (value == healthRange) {
                                hearts.add(Heart.full(healthColors[1], absorptionColors[1]));
                            } else if (value < offsets[1]) {
                                hearts.add(Heart.full(absorptionColors[1]));
                            } else if (value == offsets[1]) {
                                hearts.add(Heart.half(absorptionColors[1], false));
                            }
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < Mth.ceil(Math.min(maxHealth, 20) / 2.0F); i++) {
                int value = i * 2 + 1;

                if (value < topHealthRange) {
                    hearts.add(Heart.full(healthColors[0]));
                } else if (value == topHealthRange) {
                    hearts.add(Heart.full(healthColors[0], healthColors[1]));
                } else if (value < healthRange) {
                    hearts.add(Heart.full(healthColors[1]));
                } else if (value == healthRange) {
                    if (maxHealth > value) {
                        hearts.add(Heart.half(healthColors[1], true));
                    } else {
                        hearts.add(Heart.half(healthColors[1], false));
                    }
                } else if (value < maxHealth) {
                    hearts.add(Heart.CONTAINER_FULL);
                } else if (value == maxHealth) {
                    hearts.add(Heart.CONTAINER_HALF);
                }
            }

            if (absorption > 0) {
                for (int i = 0; i < Mth.ceil(absorptionRange / 2.0F); i++) {
                    int value = i * 2 + 1;

                    if (absorptionColors == null) {
                        if (value < topAbsorptionRange || value < absorptionRange) {
                            hearts.add(Heart.CONTAINER_FULL);
                        } else if (value == topAbsorptionRange || value == absorptionRange) {
                            hearts.add(Heart.CONTAINER_HALF);
                        }
                    } else if (value < topAbsorptionRange) {
                        hearts.add(Heart.full(absorptionColors[0]));
                    } else if (value == topAbsorptionRange) {
                        hearts.add(Heart.full(absorptionColors[0], absorptionColors[1]));
                    } else if (value < absorptionRange) {
                        hearts.add(Heart.full(absorptionColors[1]));
                    } else if (value == absorptionRange) {
                        hearts.add(Heart.half(absorptionColors[1], false));
                    }
                }
            }
        }
        return hearts;
    }
}
