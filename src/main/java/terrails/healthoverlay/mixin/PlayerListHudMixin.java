package terrails.healthoverlay.mixin;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import terrails.healthoverlay.HeartRenderer;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    @Shadow private long showTime;

    /**
     * Need different methods for dev and prod due to a crash outside dev env
     * By LlamaLad7 on 'The Fabric Project' Discord server
     * https://discordapp.com/channels/507304429255393322/807617700734042122/851887787975245875
     */
    @Group(name = "healthRender")
    @Inject(method = "renderScoreboardObjective", cancellable = true, at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V", ordinal = 0, remap = false))
    private void renderScoreboardObjectiveDev(ScoreboardObjective scoreboardObjective, int y, String string, int x, int k, PlayerListEntry playerListEntry, MatrixStack matrixStack, CallbackInfo callbackInfo) {
        HeartRenderer.INSTANCE.renderPlayerListHud(scoreboardObjective, y, string, x, k, playerListEntry, matrixStack, showTime);
        callbackInfo.cancel();
    }

    @Group(name = "healthRender")
    @Inject(method = "renderScoreboardObjective", cancellable = true, at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/class_2960;)V", ordinal = 0, remap = false))
    private void renderScoreboardObjectiveProd(ScoreboardObjective scoreboardObjective, int y, String string, int x, int k, PlayerListEntry playerListEntry, MatrixStack matrixStack, CallbackInfo callbackInfo) {
        HeartRenderer.INSTANCE.renderPlayerListHud(scoreboardObjective, y, string, x, k, playerListEntry, matrixStack, showTime);
        callbackInfo.cancel();
    }
}
