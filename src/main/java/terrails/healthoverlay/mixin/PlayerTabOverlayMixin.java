package terrails.healthoverlay.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import terrails.healthoverlay.HeartRenderer;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @Shadow private long visibilityId;

    /**
     * Need different methods for dev and prod due to a crash outside dev env
     * By LlamaLad7 on 'The Fabric Project' Discord server
     * https://discordapp.com/channels/507304429255393322/807617700734042122/851887787975245875
     */
    @Group(name = "healthRender")
    @Inject(method = "renderTablistScore",
            cancellable = true,
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V", ordinal = 0, remap = false))
    private void renderTablistScoreDev(Objective objective, int y, String string, int x, int k, PlayerInfo playerInfo, PoseStack poseStack, CallbackInfo callbackInfo) {
        HeartRenderer.INSTANCE.renderPlayerListHud(objective, y, string, x, k, playerInfo, poseStack, visibilityId);
        callbackInfo.cancel();
    }

    @Group(name = "healthRender")
    @Inject(method = "renderTablistScore",
            cancellable = true,
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/class_2960;)V", ordinal = 0, remap = false))
    private void renderTablistScoreProd(Objective objective, int y, String string, int x, int k, PlayerInfo playerInfo, PoseStack poseStack, CallbackInfo callbackInfo) {
        HeartRenderer.INSTANCE.renderPlayerListHud(objective, y, string, x, k, playerInfo, poseStack, visibilityId);
        callbackInfo.cancel();
    }
}
