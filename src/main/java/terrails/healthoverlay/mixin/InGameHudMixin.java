package terrails.healthoverlay.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import terrails.healthoverlay.HeartRenderer;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow private int ticks;

    @Inject(method = "renderHealthBar", at = @At(value = "HEAD", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=health"), cancellable = true)
    private void render(MatrixStack matrices, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int renderHealth, int absorption, boolean blinking, CallbackInfo callbackInfo) {
        // lines is useless
        // regeneratingHeartIndex causes issues due to extra lines in vanilla
        // lastHealth is the actual health, health parameter has been renamed to renderHealth as it behaves differently
        regeneratingHeartIndex = -1;
        if (player.hasStatusEffect(StatusEffects.REGENERATION)) {
            regeneratingHeartIndex = ticks % MathHelper.ceil(Math.min(player.getMaxHealth(), 20) + 5.0F);
        }

        HeartRenderer.INSTANCE.renderHeartBar(matrices, player, x, y, regeneratingHeartIndex, maxHealth, lastHealth, absorption, blinking);
        callbackInfo.cancel();
    }
}
