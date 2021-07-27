package terrails.healthoverlay.heart;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import terrails.healthoverlay.HealthOverlay;
import terrails.healthoverlay.RenderUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class HeartIcon {

    private static HeartIcon EMPTY_FULL, EMPTY_HALF;

    private static final Set<HeartIcon> heartIconCache = Sets.newHashSet();

    /** Heart icon background
     * always present, either full or half */
    private final boolean isBackgroundFull;

    /** Colored heart
     *  null: No heart
     *  true: Full heart (requires this.isBackgroundFull == true)
     *  false: Half heart */
    private final Boolean isHeartFull;

    /**
     * Colors for the heart
     * .isColoredHeartFull must be Boolean.TRUE or Boolean.FALSE
     * length == 1: The heart is in one color, either full or halved
     * length == 2: The heart is definitely full (.isColoredHeartFull must be Boolean.TRUE)
     *              and each half is in its own color, hearts[0]: second half, hearts[1]: first half */
    private final ColoredHeart[] hearts;

    protected HeartIcon(boolean isBackgroundFull, boolean isHeartFull, @Nullable ColoredHeart color1, @Nullable ColoredHeart color2) {
        if (isHeartFull) {
            if (!isBackgroundFull) throw new IllegalArgumentException("Full hearts cannot have half backgrounds.");
        } else {
            if (color1 == null) throw new IllegalArgumentException("Half hearts must have one color.");
            else if (color2 != null) throw new IllegalArgumentException("Half hearts cannot have a second color.");
        }

        this.isBackgroundFull = isBackgroundFull;
        this.isHeartFull = isHeartFull;
        this.hearts = (color1 == null && color2 == null) ? null : (color2 == null ? new ColoredHeart[]{color1} : new ColoredHeart[]{color2, color1});
    }

    protected HeartIcon(boolean isBackgroundFull, @Nullable Boolean isHeartFull, @Nullable ColoredHeart color1, @Nullable ColoredHeart color2) {
        if (isHeartFull != null) {
            if (isHeartFull) {
                if (!isBackgroundFull) throw new IllegalArgumentException("Full hearts cannot have half backgrounds.");
            } else {
                if (color1 == null) throw new IllegalArgumentException("Half hearts must have one color.");
                else if (color2 != null) throw new IllegalArgumentException("Half hearts cannot have a second color.");
            }
        } else {
            if (color1 != null || color2 != null) throw new IllegalArgumentException("Background only hearts cannot have colors.");
        }

        this.isBackgroundFull = isBackgroundFull;
        this.isHeartFull = isHeartFull;
        this.hearts = (color1 == null && color2 == null) ? null : (color2 == null ? new ColoredHeart[]{color1} : new ColoredHeart[]{color2, color1});
    }

    public static HeartIcon background(boolean isFull) {
        if (isFull) {
            if (EMPTY_FULL == null) {
                EMPTY_FULL = new HeartIcon(true, null, null, null);
            }
            return EMPTY_FULL;
        } else if (EMPTY_HALF == null) {
            EMPTY_HALF = new HeartIcon(false, null, null, null);
        }
        return EMPTY_HALF;
    }

    public static HeartIcon heart(boolean isBackgroundFull, boolean isHeartFull, ColoredHeart color) {
        Optional<HeartIcon> optional = heartIconCache.stream()
                .filter(icon -> icon.isBackgroundFull == isBackgroundFull && icon.isHeartFull != null
                        && icon.isHeartFull == isHeartFull && icon.hearts != null
                        && icon.hearts.length == 1 && icon.hearts[0] != null && icon.hearts[0].equals(color)).findAny();

        return optional.orElseGet(() -> {
            HeartIcon icon = new HeartIcon(isBackgroundFull, isHeartFull, color, null);
            heartIconCache.add(icon);
            return icon;
        });
    }

    public static HeartIcon heart(boolean isBackgroundFull, boolean isHeartFull, ColoredHeart color1, ColoredHeart color2) {
        if (color1 == null && color2 == null) {
            return background(isBackgroundFull);
        } else if (color1 != null && color1.equals(color2)) {
            return heart(isBackgroundFull, isHeartFull, color1);
        }

        Optional<HeartIcon> optional = heartIconCache.stream()
                .filter(icon -> icon.isBackgroundFull == isBackgroundFull && icon.isHeartFull != null
                        && icon.isHeartFull == isHeartFull && icon.hearts != null
                        && icon.hearts.length == 2 && icon.hearts[0].equals(color2) && icon.hearts[1].equals(color1)).findAny();

        return optional.orElseGet(() -> {
            HeartIcon icon = new HeartIcon(isBackgroundFull, isHeartFull, color1, color2);
            heartIconCache.add(icon);
            return icon;
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            HeartIcon heart = (HeartIcon) obj;
            return this.isBackgroundFull == heart.isBackgroundFull && this.isHeartFull == heart.isHeartFull && Arrays.equals(this.hearts, heart.hearts);
        } else return false;
    }

    public void render(MatrixStack matrixStack, int xPosition, int yPosition, boolean blinking, int currentEffect) {
        // Background
        if (this.isBackgroundFull) {
            assert MinecraftClient.getInstance().world != null;
            RenderUtils.drawTexture(matrixStack, xPosition, yPosition, 16 + (blinking ? 9 : 0), MinecraftClient.getInstance().world.getLevelProperties().isHardcore() ? 45 : 0);
        } else {
            RenderSystem.setShaderTexture(0, HealthOverlay.HALF_HEART_ICONS_LOCATION);
            RenderUtils.drawTexture(matrixStack, xPosition, yPosition, 0, (blinking ? 1 : 0) * 9);
            RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
        }

        if (this.hearts != null) {
            for (int i = 0; i < this.hearts.length; i++) {
                ColoredHeart heart = this.hearts[i];

                if (this.isHeartFull != null) {
                    if (this.hearts.length == 1) {
                        if (this.isHeartFull) {
                            heart.render(matrixStack, xPosition, yPosition, blinking, currentEffect);
                        } else {
                            heart.render(matrixStack, xPosition, yPosition, blinking, currentEffect, true);
                        }
                    } else {
                        heart.render(matrixStack, xPosition, yPosition, blinking, currentEffect, i == 1);
                    }
                }
            }
        }
    }
}
