package terrails.healthoverlay.heart;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import terrails.healthoverlay.Constants;
import terrails.healthoverlay.render.RenderUtils;

import java.util.Objects;

public class HeartPiece {

    /** Two predefined vanilla heart parts. */
    public static final HeartPiece VANILLA_HEALTH, VANILLA_ABSORPTION;

    /** Hexadecimal color in Integer form.
     * If null we can take it that we're a vanilla heart and render accordingly.
     * @see #VANILLA_HEALTH
     * @see #VANILLA_ABSORPTION
     */
    private final Integer color;

    private final boolean absorption;

    protected HeartPiece(int color, boolean absorption) {
        this.absorption = absorption;
        this.color = color;
    }

    private HeartPiece(boolean absorption) {
        this.absorption = absorption;
        this.color = null;
    }

    static {
        VANILLA_HEALTH = new HeartPiece(false);
        VANILLA_ABSORPTION = new HeartPiece(true);
    }

    public static HeartPiece custom(int color, boolean absorption) {
        return new HeartPiece(color, absorption);
    }

    public static HeartPiece custom(String hexColor, boolean absorption) {
        return custom(Integer.parseInt(hexColor.substring(1), 16), absorption);
    }

    public Integer getColor() {
        return this.color;
    }

    public boolean isAbsorption() {
        return absorption;
    }

    public boolean isVanilla() {
        return this.color == null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            HeartPiece part = (HeartPiece) obj;
            return Objects.equals(this.color, part.color) && this.absorption == part.absorption;
        } else return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.absorption, this.color);
    }

    public void draw(PoseStack poseStack, int xPos, int yPos, boolean blinking, boolean hardcore, HeartType type) {
        this.draw(poseStack, xPos, yPos, blinking, hardcore, type, Part.FULL);
    }

    public void draw(PoseStack poseStack, int xPos, int yPos, boolean blinking, boolean hardcore, HeartType type, boolean firstHalf) {
        this.draw(poseStack, xPos, yPos, blinking, hardcore, type, firstHalf ? Part.FIRST_HALF : Part.SECOND_HALF);
    }

    private void draw(PoseStack poseStack, int xPos, int yPos, boolean blinking, boolean hardcore, HeartType type, Part part) {
        xPos += part == Part.SECOND_HALF ? 5 : 0;

        int xTex = type.getX(part.getXOffset(), this.isVanilla(), this.isAbsorption(), blinking);

        assert Minecraft.getInstance().level != null;
        int yTex = hardcore ? (this.isVanilla() ? 45 : 36) : 0;

        int x2 = (part == Part.FULL || part == Part.FIRST_HALF) ? 9 : 5;
        int y2 = 9;

        if (this.isVanilla()) {
            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
            // Draw vanilla heart
            RenderUtils.drawTexture(poseStack, xPos, xPos + x2, yPos, yPos + y2, xTex, xTex + x2, yTex, yTex + y2);
        } else {
            RenderSystem.setShaderTexture(0, this.isAbsorption() ? Constants.ABSORPTION_ICONS_LOCATION : Constants.HEALTH_ICONS_LOCATION);

            // Draw colored heart
            RenderUtils.drawTexture(poseStack, xPos, xPos + x2, yPos, yPos + y2, xTex, xTex + x2, yTex, yTex + y2, this.getColor(), 255);

            // Add hardcore overlay / white dot
            yTex += y2;
            if (hardcore) {
                RenderUtils.drawTexture(poseStack, xPos, xPos + x2, yPos, yPos + y2, xTex, xTex + x2, yTex, yTex + y2, this.isAbsorption() ? 88 : 178);
            } else {
                RenderUtils.drawTexture(poseStack, xPos, xPos + x2, yPos, yPos + y2, xTex, xTex + x2, yTex, yTex + y2, 216);
            }

            // Add shading / withered overlay
            yTex += y2;
            RenderUtils.drawTexture(poseStack, xPos, xPos + x2, yPos, yPos + y2, xTex, xTex + x2, yTex, yTex + y2, type == HeartType.WITHERED ? 216 : 56);

            // Add blinking
            if (blinking && !this.isAbsorption()) {
                yTex -= 2 * y2;
                RenderUtils.drawTexture(poseStack, xPos, xPos + x2, yPos, yPos + y2, xTex, xTex + x2, yTex, yTex + y2, type == HeartType.WITHERED ? 56 : 127);
            }

            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        }
    }

    private enum Part {
        FULL(0),
        FIRST_HALF(9),
        SECOND_HALF(5);

        private final int x;

        Part(int x) {
            this.x = x;
        }

        public int getXOffset() {
            return x;
        }
    }
}
