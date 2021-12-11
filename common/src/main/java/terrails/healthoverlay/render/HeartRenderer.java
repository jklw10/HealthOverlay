package terrails.healthoverlay.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import terrails.healthoverlay.HOExpectPlatform;
import terrails.healthoverlay.ModConfiguration;
import terrails.healthoverlay.heart.Heart;
import terrails.healthoverlay.heart.HeartType;

import java.util.List;
import java.util.Random;

public class HeartRenderer {

    public static HeartRenderer INSTANCE = new HeartRenderer();

    private final Minecraft client = Minecraft.getInstance();
    private final Random random = new Random();

    private long lastHealthTime, healthBlinkTime;
    private int displayHealth, lastHealth;

    private List<Heart> hearts = Lists.newArrayList();

    private int previousHealth, previousMaxHealth, previousAbsorption;
    private HeartType previousHeartType;

    public void renderPlayerHearts(PoseStack poseStack, Player player) {
        int currentHealth = Mth.ceil(player.getHealth());
        long tickCount = this.client.gui.getGuiTicks();

        boolean blinking = (this.healthBlinkTime > tickCount) && (((this.healthBlinkTime - tickCount) / 3L) % 2L == 1L);
        long currentTime = Util.getMillis();
        if (currentHealth < this.lastHealth && player.invulnerableTime > 0) {
            this.lastHealthTime = currentTime;
            this.healthBlinkTime = tickCount + 20;
        } else if (currentHealth > this.lastHealth && player.invulnerableTime > 0) {
            this.lastHealthTime = currentTime;
            this.healthBlinkTime = tickCount + 10;
        }

        this.lastHealth = currentHealth;
        if ((currentTime - this.lastHealthTime) > 1000L) {
            this.displayHealth = currentHealth;
            this.lastHealthTime = currentTime;
        }

        this.random.setSeed(tickCount * 312871);

        int xPos = (this.client.getWindow().getGuiScaledWidth()) / 2 - 91;
        int yPos = this.client.getWindow().getGuiScaledHeight() - 39;

        int maxHealth = Math.max((int) player.getMaxHealth(), Math.max(this.displayHealth, currentHealth));
        int absorption = Mth.ceil(player.getAbsorptionAmount());

        int regenerationIndex = -1;
        if (player.hasEffect(MobEffects.REGENERATION)) {
            regenerationIndex = ((int) tickCount) % Mth.ceil(maxHealth + 5.0F);
        }

        HeartType heartType = HeartType.forPlayer(player);

        if (this.previousHealth != currentHealth || this.previousMaxHealth != maxHealth || this.previousAbsorption != absorption || this.previousHeartType != heartType) {
            this.hearts = RenderUtils.calculateHearts(absorption, currentHealth, maxHealth, heartType);
            this.previousHealth = currentHealth;
            this.previousMaxHealth = maxHealth;
            this.previousAbsorption = absorption;
            this.previousHeartType = heartType;
        }

        /*
            Modify current highest row so that armor isn't rendered way higher when having high health values
            Forge has a working implementation, Fabric on the other hand requires a mixin (see terrails.healthoverlay.fabric.mixin.GuiMixin#modifyRows).
         */
        HOExpectPlatform.modifyStatusBarYPos(absorption);

        RenderSystem.enableBlend();
        for (int index = 0; index < this.hearts.size(); index++) {
            Heart heart = this.hearts.get(index);

            int regenOffset = index < 10 && index == regenerationIndex ? -2 : 0;
            int absorptionOffset = index > 9 ? -10 : 0;

            int yPosition = yPos + regenOffset + absorptionOffset;
            int xPosition = xPos + index % 10 * 8;

            if (ModConfiguration.absorptionOverHealth || index < 10) {

                if (currentHealth <= 4) {
                    yPosition += this.random.nextInt(2);
                }

                if (index == regenerationIndex) {
                    yPosition -= 2;
                }
            }

            heart.draw(poseStack, xPosition, yPosition, blinking, heartType);
        }
        RenderSystem.disableBlend();
    }
}