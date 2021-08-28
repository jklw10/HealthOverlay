package terrails.healthoverlay.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import terrails.healthoverlay.HealthOverlay;
import terrails.healthoverlay.HeartRenderer;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow protected abstract Player getCameraPlayer();

    @Redirect(method = "renderPlayerHealth",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 0))
    private float limitMaxHealth(float one, float two) {
        return Math.min(this.getCameraPlayer().getMaxHealth(), 20.0F);
    }

    @Redirect(method = "renderPlayerHealth",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAbsorptionAmount()F", ordinal = 0))
    private float limitAbsorption(Player player) {
        return HealthOverlay.absorptionOverHealth ? 0 : Math.min(player.getAbsorptionAmount(), 20);
    }

    @Redirect(method = "renderPlayerHealth",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHearts(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V"))
    private void renderHearts(Gui gui, PoseStack poseStack, Player player, int xPos, int yPos, int k, int regeneratingHeartIndex, float f, int m, int n, int o, boolean blinking) {
        HeartRenderer.INSTANCE.renderHeartBar(poseStack, player, xPos, yPos, regeneratingHeartIndex, blinking);
    }
}
