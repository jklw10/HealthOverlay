package terrails.healthoverlay.mixin;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import terrails.healthoverlay.HeartRenderer;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin extends DrawableHelper {

    @Shadow private long showTime;

    @Inject(method = "renderScoreboardObjective", cancellable = true, at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V", shift = At.Shift.BEFORE))
    private void renderScoreboardObjective(ScoreboardObjective scoreboardObjective, int y, String string, int x, int k, PlayerListEntry playerListEntry, MatrixStack matrixStack, CallbackInfo callbackInfo) {
        HeartRenderer.INSTANCE.renderPlayerListHud(scoreboardObjective, y, string, x, k, playerListEntry, matrixStack, showTime);
        callbackInfo.cancel();
    }
}
