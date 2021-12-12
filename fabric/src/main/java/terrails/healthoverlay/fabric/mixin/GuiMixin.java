package terrails.healthoverlay.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import terrails.healthoverlay.HOExpectPlatform;
import terrails.healthoverlay.render.HeartRenderer;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow protected abstract Player getCameraPlayer();

    /**
     * @author Terrails (Health Overlay)
     * @reason Replacement of the default heart bar renderer
     */
    @Overwrite
    private void renderHearts(PoseStack poseStack, Player player, int x, int y, int yOffset, int regeneratingHeartIndex, float maxHealth, int currentHealth, int displayHealth, int absorption, boolean blinking) {
        HeartRenderer.INSTANCE.renderPlayerHearts(poseStack, player);
    }

    @ModifyVariable(method = "renderPlayerHealth", at = @At("STORE"), index = 15, ordinal = 7)
    private int modifyRows(int defaultValue) {
        return HOExpectPlatform.modifyStatusBarYPos(Mth.ceil(this.getCameraPlayer().getAbsorptionAmount()));
    }
}
