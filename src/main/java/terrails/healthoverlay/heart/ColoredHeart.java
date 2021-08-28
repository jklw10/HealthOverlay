package terrails.healthoverlay.heart;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import org.jetbrains.annotations.Nullable;
import terrails.healthoverlay.HealthOverlay;
import terrails.healthoverlay.RenderUtils;

import java.util.Objects;

public class ColoredHeart {

    private static ColoredHeart VANILLA_HEALTH;
    private static ColoredHeart VANILLA_ABSORPTION;

    private int color;
    private final boolean vanilla;
    private final boolean absorption;

    protected ColoredHeart(boolean vanilla, boolean absorption) {
        this.vanilla = vanilla;
        this.absorption = absorption;
    }

    protected ColoredHeart(int color, boolean absorption) {
        this.color = color;
        this.vanilla = false;
        this.absorption = absorption;
    }

    public static ColoredHeart health() {
        if (VANILLA_HEALTH == null) {
            VANILLA_HEALTH = new ColoredHeart(true, false);
        }
        return VANILLA_HEALTH;
    }

    public static ColoredHeart absorption() {
        if (VANILLA_ABSORPTION == null) {
            VANILLA_ABSORPTION = new ColoredHeart(true, true);
        }
        return VANILLA_ABSORPTION;
    }

    public static ColoredHeart create(int color, boolean absorption) {
        return new ColoredHeart(color, absorption);
    }

    public static ColoredHeart parseColor(String hexString, boolean absorption) {
        int color = Integer.parseInt(hexString.substring(1), 16);
        return ColoredHeart.create(color, absorption);
    }

    public int getColor() {
        return color;
    }

    public boolean isVanilla() {
        return vanilla;
    }

    public boolean isAbsorption() {
        return absorption;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            ColoredHeart heart = (ColoredHeart) obj;
            return this.color == heart.color && this.absorption == heart.absorption;
        } else return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.color);
    }

    public void render(PoseStack poseStack, int xPosition, int yPosition, boolean blinking, int currentEffect) {
        this.render(poseStack, xPosition, yPosition, blinking, currentEffect, null);
    }

    public void render(PoseStack poseStack, int xPosition, int yPosition, boolean blinking, int currentEffect, boolean firstHalf) {
        this.render(poseStack, xPosition, yPosition, blinking, currentEffect, Boolean.valueOf(firstHalf));
    }

    private void render(PoseStack poseStack, int xPosition, int yPosition, boolean blinking, int currentEffect, @Nullable Boolean firstHalf) {
        assert Minecraft.getInstance().level != null;

        xPosition += (firstHalf != null ? (firstHalf ? 0 : 5) : 0);
        int xTex = (firstHalf != null ? (firstHalf ? 9 : 5) : 0);
        int yTex;

        int xOffset = (firstHalf != null ? (firstHalf ? 9 : 5) : 9);

        if (isVanilla()) {

            xTex += 52 + 36 * currentEffect;
            if (this.absorption) {
                xTex = 160 + (firstHalf != null ? (firstHalf ? 9 : 5) : 0);
            } else if (currentEffect == 3) {
                xTex += 18;
            } else if (blinking) {
                xTex += 18;
            }

            yTex = Minecraft.getInstance().level.getLevelData().isHardcore() ? 45 : 0;

            // Draw heart
            RenderUtils.drawTexture(poseStack, xPosition, xPosition + xOffset, yPosition, yPosition + 9, xTex, xTex + xOffset, yTex, yTex + 9);
        } else {
            RenderSystem.setShaderTexture(0, this.absorption ? HealthOverlay.ABSORPTION_ICONS_LOCATION : HealthOverlay.HEALTH_ICONS_LOCATION);

            xTex += 18 * currentEffect;
            yTex = Minecraft.getInstance().level.getLevelData().isHardcore() ? 36 : 0;//

            // Render heart
            RenderUtils.drawTexture(poseStack, xPosition, xPosition + xOffset, yPosition, yPosition + 9, xTex, xTex + xOffset, yTex, yTex + 9, this.getColor(), 255);

            // Add shading / withered overlay
            RenderUtils.drawTexture(poseStack, xPosition, xPosition + xOffset, yPosition, yPosition + 9, xTex, xTex + xOffset, yTex + 18, yTex + 18 + 9, currentEffect == 2 ? 216 : 56);

            // Add hardcore overlay
            if (Minecraft.getInstance().level.getLevelData().isHardcore()) {
                RenderUtils.drawTexture(poseStack, xPosition, xPosition + xOffset, yPosition, yPosition + 9, xTex, xTex + xOffset, yTex + 9, yTex + 9 + 9, this.absorption ? 88 : 178);
            } else { // Add white dot
                RenderUtils.drawTexture(poseStack, xPosition, xPosition + xOffset, yPosition, yPosition + 9, xTex, xTex + xOffset, yTex + 9, yTex + 9 + 9, 216);
            }

            if (blinking) {
                int alpha = 127;
                if (currentEffect == 2) {
                    alpha = 56;
                }
                RenderUtils.drawTexture(poseStack, xPosition, xPosition + xOffset, yPosition, yPosition + 9, xTex, xTex + xOffset, yTex, yTex + 9, alpha);
            }

            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        }
    }
}
