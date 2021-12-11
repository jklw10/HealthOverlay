package terrails.healthoverlay.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.util.Mth;
import terrails.healthoverlay.heart.Heart;
import terrails.healthoverlay.heart.HeartType;

import java.util.List;

public class TabHeartRenderer {

    public static TabHeartRenderer INSTANCE = new TabHeartRenderer();

    private final Minecraft client = Minecraft.getInstance();

    private List<Heart> hearts = Lists.newArrayList();
    private int previousHealth;

    public void renderPlayerListHud(int yPos, int xPos, int xPos2, PlayerInfo playerInfo, PoseStack poseStack, int scoreHealth, long visibilityId) {
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        long currentTime = Util.getMillis();

        if (visibilityId == playerInfo.getRenderVisibilityId()) {
            if (scoreHealth < playerInfo.getLastHealth()) {
                playerInfo.setLastHealthTime(currentTime);
                playerInfo.setHealthBlinkTime(this.client.gui.getGuiTicks() + 20);
            } else if (scoreHealth > playerInfo.getLastHealth()) {
                playerInfo.setLastHealthTime(currentTime);
                playerInfo.setHealthBlinkTime(this.client.gui.getGuiTicks() + 10);
            }
        }

        playerInfo.setRenderVisibilityId(visibilityId);
        playerInfo.setLastHealth(scoreHealth);
        if (currentTime - playerInfo.getLastHealthTime() > 1000L || visibilityId != playerInfo.getRenderVisibilityId()) {
            playerInfo.setDisplayHealth(scoreHealth);
            playerInfo.setLastHealthTime(currentTime);
        }

        int displayHealth = playerInfo.getDisplayHealth();

        if (Mth.ceil((float) Math.max(scoreHealth, displayHealth) / 2.0F) > 0) {

            int healthAmount = Mth.ceil(Math.max(scoreHealth, displayHealth));
            if (healthAmount != this.previousHealth) {
                // Fixed maxHealth value as it is not possible to attain it via the leaderboard.
                // There might be a way by requesting PlayerEntity via the UUID, but that's most likely a bad idea
                this.hearts = RenderUtils.calculateHearts(0, healthAmount, healthAmount, HeartType.NORMAL);
                this.previousHealth = healthAmount;
            }

            long ticks = this.client.gui.getGuiTicks();
            boolean blinking = (playerInfo.getHealthBlinkTime() > ticks) && ((((playerInfo.getHealthBlinkTime() - ticks) / 3L) % 2L) == 1L);

            // Limit below calculated spacing to 20 health,
            // e.g. add spacing when health is lower than 20 and don't stack hearts on top of each when over 20.
            scoreHealth = Math.min(scoreHealth, 20);
            displayHealth = Math.min(displayHealth, 20);

            int spacingDivisor = Math.max(Mth.ceil((float) (scoreHealth / 2)), Math.max(Mth.ceil((float) (displayHealth / 2)), 10));
            // Adds space between hearts when there's less than 10 hearts, otherwise its the standard 9 pixels
            int spacingMultiplier = Mth.floor(Math.min((float) (xPos2 - xPos - 4) / (float) spacingDivisor, 9.0F));

            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
            RenderSystem.enableBlend();
            for (int i = 0; i < this.hearts.size(); i++) {
                Heart heart = this.hearts.get(i);

                int xPosition = xPos + i % 10 * spacingMultiplier;

                heart.draw(poseStack, xPosition, yPos, blinking, HeartType.NORMAL);
            }
            RenderSystem.disableBlend();
        }
    }
}