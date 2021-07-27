package terrails.healthoverlay.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import terrails.healthoverlay.HeartRenderer;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Redirect(method = "renderStatusBars", slice = @Slice(from = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=health")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;ceil(F)I", ordinal = 0))
    private int runDefaultRenderer(float def) {
        return -1;
    }

    @ModifyVariable(method = "renderStatusBars", at = @At(value = "STORE", ordinal = 0))
    private float modifyMaxHealth(float maxHealth) {
        return Math.min(20.0F, maxHealth);
    }

    @ModifyVariable(method = "renderStatusBars", at = @At(value = "STORE"), ordinal = 6)
    private int modifyAbsorption(int absorption) {
        return Math.min(20, absorption);
    }

    @Inject(method = "renderStatusBars", locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=health"))
    private void render(MatrixStack matrixStack, CallbackInfo info, PlayerEntity player) {
        HeartRenderer.INSTANCE.renderHeartBar(matrixStack, player);
    }
}
