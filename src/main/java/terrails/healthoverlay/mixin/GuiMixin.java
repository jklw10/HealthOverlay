package terrails.healthoverlay.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import terrails.healthoverlay.HealthOverlay;
import terrails.healthoverlay.HeartRenderer;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow protected abstract Player getCameraPlayer();

    @Redirect(method = "renderPlayerHealth",
            slice = @Slice(from = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=health")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;ceil(F)I", ordinal = 0))
    private int runDefaultRenderer(float defaultValue) {
        return -1;
    }

    @Redirect(method = "renderPlayerHealth",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/world/entity/ai/attributes/Attribute;)D", ordinal = 0))
    private double limitMaxHealth(Player player, Attribute attribute) {
        return Math.min(player.getAttributeValue(attribute), 20);
    }

    @Redirect(method = "renderPlayerHealth",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAbsorptionAmount()F", ordinal = 0))
    private float limitAbsorption(Player player) {
        return HealthOverlay.absorptionOverHealth ? 0 : Math.min(player.getAbsorptionAmount(), 20);
    }

    @Inject(method = "renderPlayerHealth",
            at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=health"))
    private void render(PoseStack poseStack, CallbackInfo info) {
        HeartRenderer.INSTANCE.renderHeartBar(poseStack, this.getCameraPlayer());
    }
}
