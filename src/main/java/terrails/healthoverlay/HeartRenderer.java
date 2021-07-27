package terrails.healthoverlay;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import terrails.healthoverlay.heart.ColoredHeart;
import terrails.healthoverlay.heart.HeartIcon;

import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class HeartRenderer {

    public static HeartRenderer INSTANCE = new HeartRenderer();

    private static final Random random = new Random();

    private static int previousHealth, previousMaxHealth, previousAbsorption, previousEffect;
    private static List<HeartIcon> hearts = Lists.newArrayList();

    private static int previousHealthHud;
    private static List<HeartIcon> tabHearts = Lists.newArrayList();

    public void renderHeartBar(MatrixStack matrixStack, PlayerEntity player, int xPos, int yPos, int regeneratingHeartIndex, float maxHealth, int currentHealth, int absorption, boolean blinking) {
        if (HealthOverlay.healthColors.length == 0 || HealthOverlay.absorptionColors.length == 0) {
            return;
        }

        int currentEffect = 0;
        if (player.hasStatusEffect(StatusEffects.POISON)) {
            currentEffect = 1;
        } else if (player.hasStatusEffect(StatusEffects.WITHER)) {
            currentEffect = 2;
        } else if (player.isFreezing()) {
            currentEffect = 3;
        }

        int ticks = MinecraftClient.getInstance().inGameHud.getTicks();
        HeartRenderer.random.setSeed(ticks * 312871L);

        if (HeartRenderer.previousHealth != currentHealth || HeartRenderer.previousMaxHealth != (int) maxHealth
                || HeartRenderer.previousAbsorption != absorption || HeartRenderer.previousEffect != currentEffect) {
            HeartRenderer.hearts = calculateHearts(absorption, currentHealth, (int) maxHealth, currentEffect);
            HeartRenderer.previousHealth = currentHealth;
            HeartRenderer.previousMaxHealth = (int) maxHealth;
            HeartRenderer.previousAbsorption = absorption;
            HeartRenderer.previousEffect = currentEffect;
        }

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
        for (int i = 0; i < HeartRenderer.hearts.size(); i++) {
            HeartIcon heart = HeartRenderer.hearts.get(i);

            int regenOffset = i < 10 && i == regeneratingHeartIndex ? -2 : 0;
            int absorptionOffset = i > 9 ? -10 : 0;

            int yPosition = yPos + regenOffset + absorptionOffset;
            int xPosition = xPos + i % 10 * 8;

            if (HealthOverlay.absorptionOverHealth || i < 10) {

                if (currentHealth + absorption <= 4) {
                    yPosition += HeartRenderer.random.nextInt(2);
                }

                if (i == regeneratingHeartIndex) {
                    yPosition -= 2;
                }
            }

            heart.render(matrixStack, xPosition, yPosition, blinking, currentEffect);
        }
        RenderSystem.disableBlend();

    }

    public void renderPlayerListHud(ScoreboardObjective objective, int yPos, String string, int xPos, int xOffset, PlayerListEntry playerEntry, MatrixStack matrixStack, long showTime) {
        int scoreValue = objective.getScoreboard().getPlayerScore(string, objective).getScore();

        MinecraftClient.getInstance().getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);
        long m = Util.getMeasuringTimeMs();
        if (showTime == playerEntry.getShowTime()) {
            if (scoreValue < playerEntry.getLastHealth()) {
                playerEntry.setLastHealthTime(m);
                playerEntry.setBlinkingHeartTime(MinecraftClient.getInstance().inGameHud.getTicks() + 20);
            } else if (scoreValue > playerEntry.getLastHealth()) {
                playerEntry.setLastHealthTime(m);
                playerEntry.setBlinkingHeartTime(MinecraftClient.getInstance().inGameHud.getTicks() + 10);
            }
        }

        if (m - playerEntry.getLastHealthTime() > 1000L || showTime != playerEntry.getShowTime()) {
            playerEntry.setLastHealth(scoreValue);
            playerEntry.setHealth(scoreValue);
            playerEntry.setLastHealthTime(m);
        }

        playerEntry.setShowTime(showTime); // method_2964 = setRenderVisibilityId
        playerEntry.setLastHealth(scoreValue); // method_2972 = setLastHealth

        int health = playerEntry.getHealth(); // method_2960 = getDisplayHealth

        if (MathHelper.ceil((float) Math.max(scoreValue, health) / 2.0F) > 0) {

            if (scoreValue != HeartRenderer.previousHealthHud) {
                    HeartRenderer.tabHearts = calculateHearts(0, scoreValue, 20, 0);
                // Fixed maxHealth value as its not possible to attain it via the leaderboard, might be possible by requesting PlayerEntity via the UUID, but it would be a better idea to avoid that
                HeartRenderer.previousHealthHud = scoreValue;
            }

            // Limit to 20 health
            health = Math.min(health, 20);
            scoreValue = Math.min(scoreValue, 20);

            int heartCount = Math.max(MathHelper.ceil((float) (scoreValue / 2)), Math.max(MathHelper.ceil((float) (health / 2)), 10));
            int spacingMultiplier = MathHelper.floor(Math.min((float) (xOffset - xPos - 4) / (float) heartCount, 9.0F));

            int ticks = MinecraftClient.getInstance().inGameHud.getTicks();
            boolean highlight = playerEntry.getBlinkingHeartTime() > (long) ticks && (playerEntry.getBlinkingHeartTime() - (long) ticks) / 3L % 2L == 1L;

            RenderSystem.enableBlend();
            RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
            for (int i = 0; i < HeartRenderer.tabHearts.size(); i++) {
                HeartIcon heart = HeartRenderer.tabHearts.get(i);

                int xPosition = xPos + i % 10 * spacingMultiplier;

                heart.render(matrixStack, xPosition, yPos, highlight, 0);
            }
            RenderSystem.disableBlend();
        }
    }

    private static List<HeartIcon> calculateHearts(int absorption, int health, int maxHealth, int effects) {
        if (effects < 0 || effects > 3) {
            HealthOverlay.LOGGER.error("Something went very wrong. Effects value is different than what it is supposed to be.");
            throw new IllegalArgumentException(String.valueOf(effects));
        }

        if (HealthOverlay.absorptionOverHealth && HealthOverlay.absorptionOverHealthMode == HealthOverlay.AbsorptionMode.AS_HEALTH) {
            health += absorption;
        }

        List<HeartIcon> hearts = Lists.newArrayList();
        ColoredHeart[] healthColors = null, absorptionColors = null;

        if (effects == 0) {
            boolean flag = HealthOverlay.absorptionOverHealth && HealthOverlay.absorptionOverHealthMode == HealthOverlay.AbsorptionMode.AFTER_HEALTH_ADVANCED && health % 20 != 0;
            boolean asHealth = HealthOverlay.absorptionOverHealth && HealthOverlay.absorptionOverHealthMode == HealthOverlay.AbsorptionMode.AS_HEALTH;

            // Health
            if (!HealthOverlay.absorptionOverHealth || absorption < 20 || flag || asHealth) {
                if (health > 20) {
                    healthColors = new ColoredHeart[2];
                    double var = health - (health % 20 == 0 ? 1 : 0);
                    healthColors[1] = HealthOverlay.healthColors[(int) (var / 20.0) % HealthOverlay.healthColors.length];

                    if (health % 20 != 0 && (health > 40 || !HealthOverlay.healthVanilla)) {
                        var = var - 20.0;
                        healthColors[0] = HealthOverlay.healthColors[(int) (var / 20.0) % HealthOverlay.healthColors.length];
                    } else {
                        healthColors[0] = HealthOverlay.healthColors[0];
                    }
                } else {
                    healthColors = new ColoredHeart[1];
                    healthColors[0] = HealthOverlay.healthColors[0];
                }
            }

            // Absorption
            if (absorption > 0 && !asHealth) {
                double var1 = flag ? 20 - (health % 20) : 20;
                if (absorption > var1) {
                    absorptionColors = new ColoredHeart[2];
                    double var2 = absorption - (absorption % var1 == 0 ? 1 : 0);
                    absorptionColors[1] = HealthOverlay.absorptionColors[(int) (var2 / var1) % HealthOverlay.absorptionColors.length];

                    if (absorption % var1 != 0 && (absorption > (2 * var1) || !HealthOverlay.absorptionVanilla)) {
                        var2 = var2 - var1;
                        absorptionColors[0] = HealthOverlay.absorptionColors[(int) (var2 / var1) % HealthOverlay.absorptionColors.length];
                    } else {
                        absorptionColors[0] = HealthOverlay.absorptionColors[0];
                    }
                } else {
                    absorptionColors = new ColoredHeart[1];
                    absorptionColors[0] = HealthOverlay.absorptionColors[0];
                }
            }

        } else if (effects == 1){
            healthColors = HealthOverlay.healthPoisonColors;
            absorptionColors = HealthOverlay.absorptionPoisonColors;
            assert absorptionColors != null;
        } else if (effects == 2) {
            healthColors = HealthOverlay.healthWitherColors;
            absorptionColors = HealthOverlay.absorptionWitherColors;
            assert absorptionColors != null;
        } else {
            healthColors = HealthOverlay.healthFrozenColors;
            absorptionColors = HealthOverlay.absorptionFrozenColors;
            assert absorptionColors != null;
        }

        int healthRange = Math.min(health, 20);
        int topHealthRange = health > 20 ? (health % 20 == 0 ? 20 : (health % 20)) : 0;
        int absorptionRange = Math.min(absorption, 20);
        int topAbsorptionRange = absorption > 20 ? (absorption % 20 == 0 ? 20 : (absorption % 20)) : 0;

        if (HealthOverlay.absorptionOverHealth) {

            HealthOverlay.AbsorptionMode mode = HealthOverlay.absorptionOverHealthMode;
            int[] offsets = null;

            if (mode != HealthOverlay.AbsorptionMode.AS_HEALTH) {
                if (absorption == 0 || health % 20 == 0 || (absorption >= 20 && mode != HealthOverlay.AbsorptionMode.AFTER_HEALTH_ADVANCED)) {
                    // If health is divisible by 20 render via the simpler BEGINNING mode
                    mode = HealthOverlay.AbsorptionMode.BEGINNING;
                } else if (HealthOverlay.absorptionOverHealthMode == HealthOverlay.AbsorptionMode.AFTER_HEALTH) {
                    // Get free space left after highest health row
                    int var1 = 20 - (health % 20);
                    // Get absorption that is left after free space is filled, aka "overflown" absorption
                    int var2 = absorptionRange - var1;
                    // If value is lower than or equal to this, render absorption that comes after highest health row
                    int var3 = health > 20 ? (topHealthRange + absorptionRange) : (healthRange + absorptionRange);

                    offsets = new int[]{var1, var2, var3};

                } else if (HealthOverlay.absorptionOverHealthMode == HealthOverlay.AbsorptionMode.AFTER_HEALTH_ADVANCED) {
                    // Get free space left after highest health row
                    // health % 20 is never 0 because of the first if statement
                    int var = 20 - (health % 20);
                    // Get actual topAbsorptionRange as this mode keeps highest health row always visible unless (health % 20 == 0)
                    topAbsorptionRange = absorption > var ? (absorption % var == 0 ? var : (absorption % var)) : 0;

                    int temp = health < 20 ? healthRange : topHealthRange;
                    // If value is lower than or equal to this, render highest absorption that comes after the highest health row
                    int var1 = topAbsorptionRange + temp;
                    // If value is lower than or equal to this, render lower absorption that comes after the highest health row
                    int var2 = absorptionRange + temp;

                    offsets = new int[]{var1, var2};
                }
            }

            for (int i = 0; i < MathHelper.ceil(Math.min(maxHealth + absorption, 20) / 2.0F); i++) {
                int value = i * 2 + 1;

                if (value > (absorption + health)) {
                    if (value < maxHealth) {
                        hearts.add(HeartIcon.background(true));
                    } else if (value == maxHealth) {
                        hearts.add(HeartIcon.background(false));
                    }
                } else switch (mode) {
                    case BEGINNING:
                        if (value < topAbsorptionRange) {
                            hearts.add(HeartIcon.heart(true, true, absorptionColors[1]));
                        } else if (value == topAbsorptionRange) {
                            hearts.add(HeartIcon.heart(true, true, absorptionColors[1], absorptionColors[0]));
                        } else if (value < absorptionRange) {
                            hearts.add(HeartIcon.heart(true, true, absorptionColors[0]));
                        } else if (value == absorptionRange) {
                            hearts.add(HeartIcon.heart(true, true, absorptionColors[0], healthColors[0]));
                        } else if (value < topHealthRange) {
                            hearts.add(HeartIcon.heart(true, true, healthColors[1]));
                        } else if (value == topHealthRange) {
                            hearts.add(HeartIcon.heart(true, true, healthColors[1], healthColors[0]));
                        } else if (value < healthRange) {
                            hearts.add(HeartIcon.heart(true, true, healthColors[0]));
                        } else if (value == healthRange) {
                            hearts.add(HeartIcon.heart(value != maxHealth, false, healthColors[0]));
                        }
                        break;
                    case AFTER_HEALTH:
                        if (absorption > offsets[0] && value < offsets[1]) {
                            hearts.add(HeartIcon.heart(true, true, absorptionColors[0]));
                        } else if (absorption > offsets[0] && value == offsets[1]) {
                            hearts.add(HeartIcon.heart(true, true, absorptionColors[0], healthColors[value < topHealthRange ? 1 : 0]));
                        } else if (value < topHealthRange) {
                            hearts.add(HeartIcon.heart(true, true, healthColors[1]));
                        } else if (value == topHealthRange) {
                            hearts.add(HeartIcon.heart(true, true, healthColors[1], absorptionColors[0]));
                        } else if (health > 20) {
                            if (value < offsets[2]) {
                                hearts.add(HeartIcon.heart(true, true, absorptionColors[0]));
                            } else if (value == offsets[2]) {
                                hearts.add(HeartIcon.heart(true, true, absorptionColors[0], healthColors[0]));
                            } else if (value < healthRange) {
                                hearts.add(HeartIcon.heart(true, true, healthColors[0]));
                            } else if (value == healthRange) {
                                if (absorption > 0) {
                                    hearts.add(HeartIcon.heart(true, true, healthColors[0], absorptionColors[0]));
                                } else hearts.add(HeartIcon.heart(value != maxHealth, false, healthColors[0]));
                            }
                        } else {
                            if (value < healthRange) {
                                hearts.add(HeartIcon.heart(true, true, healthColors[0]));
                            } else if (value == healthRange) {
                                if (absorption > 0) {
                                    hearts.add(HeartIcon.heart(true, true, healthColors[0], absorptionColors[0]));
                                } else hearts.add(HeartIcon.heart(value != maxHealth, false, healthColors[0]));
                            } else if (value < offsets[2]) {
                                hearts.add(HeartIcon.heart(true, true, absorptionColors[0]));
                            } else if (value == offsets[2]) {
                                hearts.add(HeartIcon.heart(false, false, absorptionColors[0]));
                            }
                        }
                        break;
                    case AFTER_HEALTH_ADVANCED:
                        if (health < 20 && value < healthRange) {
                            hearts.add(HeartIcon.heart(true, true, healthColors[0]));
                        } else if (health < 20 && value == healthRange) {
                            if (topAbsorptionRange > 0) {
                                hearts.add(HeartIcon.heart(true, true, healthColors[0], absorptionColors[1]));
                            } else if (absorptionRange > 0) {
                                hearts.add(HeartIcon.heart(true, true, healthColors[0], absorptionColors[0]));
                            } else {
                                hearts.add(HeartIcon.heart(false, false, healthColors[0]));
                            }
                        } else if (value < topHealthRange) {
                            hearts.add(HeartIcon.heart(true, true, healthColors[1]));
                        } else if (value == topHealthRange) {
                            ColoredHeart secondHalf;
                            if (topAbsorptionRange > 0) {
                                secondHalf = absorptionColors[1];
                            } else if (absorptionRange > 0) {
                                secondHalf = absorptionColors[0];
                            } else secondHalf = healthColors[0];
                            hearts.add(HeartIcon.heart(true, true, healthColors[1], secondHalf));
                        } else if (value < offsets[0]) {
                            hearts.add(HeartIcon.heart(true, true, absorptionColors[1]));
                        } else if (value == offsets[0]) {
                            hearts.add(HeartIcon.heart(true, true, absorptionColors[1], absorptionColors[0]));
                        } else if (value < offsets[1]) {
                            hearts.add(HeartIcon.heart(true, true, absorptionColors[0]));
                        } else if (value == offsets[1]) {
                            if (health < 20) {
                                hearts.add(HeartIcon.heart(false, false, absorptionColors[0]));
                            } else hearts.add(HeartIcon.heart(true, true, absorptionColors[0], healthColors[0]));
                        } else if (health > 20) {
                            if (value < healthRange) {
                                hearts.add(HeartIcon.heart(true, true, healthColors[0]));
                            } else if (value == healthRange) {
                                hearts.add(HeartIcon.heart(false, false, healthColors[0]));
                            }
                        }
                        break;
                    case AS_HEALTH:
                        if (value < topHealthRange) {
                            hearts.add(HeartIcon.heart(true, true, healthColors[1]));
                        } else if (value == topHealthRange) {
                            hearts.add(HeartIcon.heart(true, true, healthColors[1], healthColors[0]));
                        } else if (value < healthRange) {
                            hearts.add(HeartIcon.heart(true, true, healthColors[0]));
                        } else if (value == healthRange) {
                            hearts.add(HeartIcon.heart(value != maxHealth && value < absorptionRange, false, healthColors[0]));
                        }
                        break;
                }
            }
        } else {
            for (int i = 0; i < MathHelper.ceil(Math.min(maxHealth, 20) / 2.0F); i++) {
                int value = i * 2 + 1;

                if (value < topHealthRange) {
                    hearts.add(HeartIcon.heart(true, true, healthColors[1]));
                } else if (value == topHealthRange) {
                    hearts.add(HeartIcon.heart(true, true, healthColors[1], healthColors[0]));
                } else if (value < healthRange) {
                    hearts.add(HeartIcon.heart(true, true, healthColors[0]));
                } else if (value == healthRange) {
                    if (maxHealth > value) {
                        hearts.add(HeartIcon.heart(true, false, healthColors[0]));
                    } else {
                        hearts.add(HeartIcon.heart(false, false, healthColors[0]));
                    }
                } else if (value < maxHealth) {
                    hearts.add(HeartIcon.background(true));
                } else if (value == maxHealth) {
                    hearts.add(HeartIcon.background(false));
                }
            }

            if (absorption > 0) {
                for (int i = 0; i < MathHelper.ceil(absorptionRange / 2.0F); i++) {
                    int value = i * 2 + 1;

                    if (absorptionColors == null) {
                        if (value < topAbsorptionRange || value < absorptionRange) {
                            hearts.add(HeartIcon.background(true));
                        } else if (value == topAbsorptionRange || value == absorptionRange) {
                            hearts.add(HeartIcon.background(false));
                        }
                    } else if (value < topAbsorptionRange) {
                        hearts.add(HeartIcon.heart(true, true, absorptionColors[1]));
                    } else if (value == topAbsorptionRange) {
                        hearts.add(HeartIcon.heart(true, true, absorptionColors[1], absorptionColors[0]));
                    } else if (value < absorptionRange) {
                        hearts.add(HeartIcon.heart(true, true, absorptionColors[0]));
                    } else if (value == absorptionRange) {
                        hearts.add(HeartIcon.heart(false, false, absorptionColors[0]));
                    }
                }
            }
        }
        return hearts;
    }
}
