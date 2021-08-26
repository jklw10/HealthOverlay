package terrails.healthoverlay;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import terrails.healthoverlay.heart.ColoredHeart;
import terrails.healthoverlay.heart.HeartIcon;

import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class HeartRenderer {

    public static HeartRenderer INSTANCE = new HeartRenderer();

    private static final Random random = new Random();
    private static final Minecraft minecraft = Minecraft.getInstance();

    private static long prevSystemTime, nextHealthTicks;
    private static int previousHealth, previousMaxHealth, previousAbsorption, previousEffect;
    private static List<HeartIcon> hearts = Lists.newArrayList();

    private static int previousHealthHud;
    private static List<HeartIcon> tabHearts = Lists.newArrayList();

    public void renderPlayerListHud(Objective objective, int yPos, String string, int xPos, int xOffset, PlayerInfo playerEntry, PoseStack poseStack, long visibilityId) {
        int scoreValue = objective.getScoreboard().getOrCreatePlayerScore(string, objective).getScore();

        minecraft.getTextureManager().bind(GuiComponent.GUI_ICONS_LOCATION);
        long m = Util.getMillis();
        if (visibilityId == playerEntry.getRenderVisibilityId()) {
            if (scoreValue < playerEntry.getLastHealth()) {
                playerEntry.setLastHealthTime(m);
                playerEntry.setHealthBlinkTime(minecraft.gui.getGuiTicks() + 20);
            } else if (scoreValue > playerEntry.getLastHealth()) {
                playerEntry.setLastHealthTime(m);
                playerEntry.setHealthBlinkTime(minecraft.gui.getGuiTicks() + 10);
            }
        }

        if (m - playerEntry.getLastHealthTime() > 1000L || visibilityId != playerEntry.getRenderVisibilityId()) {
            playerEntry.setLastHealth(scoreValue);
            playerEntry.setDisplayHealth(scoreValue);
            playerEntry.setLastHealthTime(m);
        }

        playerEntry.setRenderVisibilityId(visibilityId);
        playerEntry.setLastHealth(scoreValue);

        int displayHealth = playerEntry.getDisplayHealth();

        if (Mth.ceil((float) Math.max(scoreValue, displayHealth) / 2.0F) > 0) {

            if (scoreValue != previousHealthHud) {
                tabHearts = calculateHearts(0, scoreValue, 20, 0);
                // Fixed maxHealth value as its not possible to attain it via the leaderboard,
                // might be possible by requesting PlayerEntity via the UUID,
                // but its probably a bad idea to get player instances
                previousHealthHud = scoreValue;
            }

            // Limit to 20 health
            displayHealth = Math.min(displayHealth, 20);
            scoreValue = Math.min(scoreValue, 20);

            int heartCount = Math.max(Mth.ceil((float) (scoreValue / 2)), Math.max(Mth.ceil((float) (displayHealth / 2)), 10));
            int spacingMultiplier = Mth.floor(Math.min((float) (xOffset - xPos - 4) / (float) heartCount, 9.0F));

            int ticks = Minecraft.getInstance().gui.getGuiTicks();
            boolean highlight = playerEntry.getHealthBlinkTime() > (long) ticks && (playerEntry.getHealthBlinkTime() - (long) ticks) / 3L % 2L == 1L;

            for (int i = 0; i < tabHearts.size(); i++) {
                HeartIcon heart = tabHearts.get(i);

                int xPosition = xPos + i % 10 * spacingMultiplier;

                heart.render(poseStack, xPosition, yPos, highlight, 0);
            }
        }
    }

    public void renderHeartBar(PoseStack poseStack, Player player) {
        if (HealthOverlay.healthColors.length == 0 || HealthOverlay.absorptionColors.length == 0) {
            return;
        }
        int ticks = Minecraft.getInstance().gui.getGuiTicks();

        int currentHealth = Mth.ceil(player.getHealth());
        int maxHealth = Mth.ceil(player.getMaxHealth());
        int absorption = Mth.ceil(player.getAbsorptionAmount());
        int currentEffect = player.hasEffect(MobEffects.POISON) ? (player.hasEffect(MobEffects.WITHER) ? 1 : 2) : (player.hasEffect(MobEffects.WITHER) ? 1 : 0);

        boolean highlight = nextHealthTicks > (long) ticks && (nextHealthTicks - (long) ticks) / 3L % 2L == 1L;
        long systemTime = Util.getMillis();
        if (currentHealth < previousHealth && player.invulnerableTime > 0) {
            prevSystemTime = systemTime;
            nextHealthTicks = (ticks + 20);
        } else if (currentHealth > previousHealth && player.invulnerableTime > 0) {
            prevSystemTime = systemTime;
            nextHealthTicks = (ticks + 10);
        }

        if (systemTime - prevSystemTime > 1000L) {
            prevSystemTime = systemTime;
        }

        random.setSeed(ticks * 312871L);

        int xPos = minecraft.getWindow().getGuiScaledWidth() / 2 - 91;
        int yPos = minecraft.getWindow().getGuiScaledHeight() - 39;

        //final int rowHeight = 10;
        // Armor gets rendered in the same row as health if this isn't set
        //ForgeIngameGui.left_height += rowHeight + (Math.min(MathHelper.ceil(player.getAbsorptionAmount()), 20) > 0 && !HealthOverlay.absorptionOverHealth ? rowHeight : 0);

        int regenHealth = -1;
        if (player.hasEffect(MobEffects.REGENERATION)) {
            regenHealth = ticks % Mth.ceil(Math.min(player.getMaxHealth(), 20) + 5.0F);
        }

        if (previousHealth != currentHealth || previousMaxHealth != maxHealth || previousAbsorption != absorption || previousEffect != currentEffect) {
            hearts = calculateHearts(absorption, currentHealth, maxHealth, currentEffect);
            previousHealth = currentHealth;
            previousMaxHealth = maxHealth;
            previousAbsorption = absorption;
            previousEffect = currentEffect;
        }

        for (int i = 0; i < hearts.size(); i++) {
            HeartIcon heart = hearts.get(i);

            int regenOffset = i < 10 && i == regenHealth ? -2 : 0;
            int absorptionOffset = i > 9 ? -10 : 0;

            int yPosition = yPos + regenOffset + absorptionOffset;
            int xPosition = xPos + i % 10 * 8;

            if (HealthOverlay.absorptionOverHealth || i < 10) {

                if (currentHealth <= 4 && (HealthOverlay.absorptionOverHealthMode != HealthOverlay.AbsorptionMode.AS_HEALTH || currentHealth + absorption <= 4)) {
                    yPosition += random.nextInt(2);
                }

                if (i == regenHealth) {
                    yPosition -= 2;
                }
            }

            heart.render(poseStack, xPosition, yPosition, highlight, currentEffect);
        }

    }

    private static List<HeartIcon> calculateHearts(int absorption, int health, int maxHealth, int effects) {
        List<HeartIcon> hearts = Lists.newArrayList();
        ColoredHeart[] healthColors, absorptionColors;

        if (HealthOverlay.absorptionOverHealth && HealthOverlay.absorptionOverHealthMode == HealthOverlay.AbsorptionMode.AS_HEALTH) {
            health += absorption;
        }

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
            } else {
                healthColors = null;
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
            } else {
                absorptionColors = null;
            }

        } else if (effects == 1) {
            healthColors = HealthOverlay.healthWitherColors;
            absorptionColors = HealthOverlay.absorptionWitherColors;
            assert absorptionColors != null;
        } else {
            healthColors = HealthOverlay.healthPoisonColors;
            absorptionColors = HealthOverlay.absorptionPoisonColors;
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

            for (int i = 0; i < Mth.ceil(Math.min(maxHealth + absorption, 20) / 2.0F); i++) {
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
            for (int i = 0; i < Mth.ceil(Math.min(maxHealth, 20) / 2.0F); i++) {
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
                for (int i = 0; i < Mth.ceil(absorptionRange / 2.0F); i++) {
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
