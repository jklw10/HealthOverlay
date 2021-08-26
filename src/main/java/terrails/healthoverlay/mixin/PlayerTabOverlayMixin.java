package terrails.healthoverlay.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import terrails.healthoverlay.HeartRenderer;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin extends GuiComponent {

    @Shadow private long visibilityId;

    @Inject(method = "renderTablistScore",
            cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;bind(Lnet/minecraft/resources/ResourceLocation;)V"))
    private void renderTablistScore(Objective objective, int y, String string, int x, int k, PlayerInfo playerInfo, PoseStack poseStack, CallbackInfo callbackInfo) {
        HeartRenderer.INSTANCE.renderPlayerListHud(objective, y, string, x, k, playerInfo, poseStack, visibilityId);
        callbackInfo.cancel();
    }
}
