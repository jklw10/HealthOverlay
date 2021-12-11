package terrails.healthoverlay.heart;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import terrails.healthoverlay.Constants;
import terrails.healthoverlay.render.RenderUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class Heart {

    /** Set with all created {@link Heart} objects
     * in order to avoid creating multiple objects with same values.
     */
    private static final Set<Heart> HEART_CACHE;

    /** Two predefined background/container hearts that should always be present and easier to access. */
    public static final Heart CONTAINER_FULL, CONTAINER_HALF;

    /** Heart background/container that is always present, either full or halved. */
    private final boolean isBackgroundFull;

    /** First half of a heart. This value may never be null outside the two predefined instances.
     * @see #CONTAINER_FULL
     * @see #CONTAINER_HALF
     */
    private final HeartPiece firstHalf;
    /** Second half of a heart. May be null, equal or different from firstHalf. */
    private final HeartPiece secondHalf;

    private Heart(boolean isBackgroundFull, HeartPiece firstHalf, HeartPiece secondHalf) {
        this.isBackgroundFull = isBackgroundFull;
        this.firstHalf = firstHalf;
        this.secondHalf = secondHalf;
    }

    static {
        HEART_CACHE = Sets.newHashSet();
        CONTAINER_FULL = new Heart(true, null, null);
        CONTAINER_HALF = new Heart(false, null, null);
    }

    public static Heart full(HeartPiece firstHalf, HeartPiece secondHalf) {
        if (firstHalf == null || secondHalf == null) {
            Constants.LOGGER.error("Something went very wrong with heart creation. HeartPiece cannot be null, returning heart container to prevent crashes...");
            return CONTAINER_FULL;
        }

        Optional<Heart> optional = HEART_CACHE.stream()
                .filter(heart -> heart.isBackgroundFull && Objects.equals(heart.firstHalf, firstHalf) && Objects.equals(heart.secondHalf, secondHalf))
                .findAny();

        return optional.orElseGet(() -> {
            Heart heart = new Heart(true, firstHalf, secondHalf);
            HEART_CACHE.add(heart);
            return heart;
        });
    }

    public static Heart full(HeartPiece heartPiece) {
        if (heartPiece == null) {
            Constants.LOGGER.error("Something went very wrong with heart creation. HeartPiece cannot be null, returning heart container to prevent crashes...");
            return CONTAINER_FULL;
        }

        Optional<Heart> optional = HEART_CACHE.stream()
                .filter(heart -> heart.isBackgroundFull && Objects.equals(heart.firstHalf, heartPiece) && Objects.equals(heart.secondHalf, heartPiece))
                .findAny();

        return optional.orElseGet(() -> {
            Heart heart = new Heart(true, heartPiece, heartPiece);
            HEART_CACHE.add(heart);
            return heart;
        });
    }

    public static Heart half(HeartPiece heartPiece, boolean isBackgroundFull) {
        if (heartPiece == null) {
            Constants.LOGGER.error("Something went very wrong with heart creation. HeartPiece cannot be null, returning heart container to prevent crashes...");
            return (isBackgroundFull ? CONTAINER_FULL : CONTAINER_HALF);
        }

        Optional<Heart> optional = HEART_CACHE.stream()
                .filter(heart -> heart.isBackgroundFull == isBackgroundFull && Objects.equals(heart.firstHalf, heartPiece) && heart.secondHalf == null)
                .findAny();

        return optional.orElseGet(() -> {
            Heart heart = new Heart(isBackgroundFull, heartPiece, null);
            HEART_CACHE.add(heart);
            return heart;
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            Heart heart = (Heart) obj;
            return this.isBackgroundFull == heart.isBackgroundFull && Objects.equals(this.firstHalf, heart.firstHalf) && Objects.equals(this.secondHalf, heart.secondHalf);
        } else return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.isBackgroundFull, this.firstHalf, this.secondHalf);
    }

    public void draw(PoseStack poseStack, int xPos, int yPos, boolean blinking, HeartType type) {
        assert Minecraft.getInstance().level != null;
        boolean hardcore = Minecraft.getInstance().level.getLevelData().isHardcore();

        // Draw background/container
        if (this.isBackgroundFull) {
            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
            RenderUtils.drawTexture(poseStack, xPos, yPos, 16 + (blinking ? 9 : 0), hardcore ? 45 : 0);
        } else {
            RenderSystem.setShaderTexture(0, Constants.HALF_HEART_ICONS_LOCATION);
            RenderUtils.drawTexture(poseStack, xPos, yPos, 0, (blinking ? 9 : 0));
            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        }

        // First half is usually not null, but this check is to avoid crashes while using predefined CONTAINER instances
        if (this.firstHalf != null) {

            if (this.secondHalf == null) {
                // If second half is null draw only the first half
                this.firstHalf.draw(poseStack, xPos, yPos, blinking, hardcore, type, true);
            } else if (Objects.equals(this.firstHalf, this.secondHalf)) {
                // If halves are equal draw one whole heart
                this.firstHalf.draw(poseStack, xPos, yPos, blinking, hardcore, type);
            } else {
                // If halves are not equal render the first and second half separately
                this.firstHalf.draw(poseStack, xPos, yPos, blinking, hardcore, type, true);
                this.secondHalf.draw(poseStack, xPos, yPos, blinking, hardcore, type, false);
            }
        }
    }
}
