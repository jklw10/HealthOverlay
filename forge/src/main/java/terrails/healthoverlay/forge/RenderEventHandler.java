package terrails.healthoverlay.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import terrails.healthoverlay.Constants;
import terrails.healthoverlay.render.HeartRenderer;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Constants.MOD_ID)
public class RenderEventHandler {

    private static final Minecraft client = Minecraft.getInstance();

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void renderHearts(RenderGameOverlayEvent.PreLayer event) {
        if (event.isCanceled()
                || client.options.hideGui
                || event.getOverlay() != ForgeIngameGui.PLAYER_HEALTH_ELEMENT
                || !((ForgeIngameGui) client.gui).shouldDrawSurvivalElements()
                || !(client.getCameraEntity() instanceof Player player)) {
            return;
        }

        HeartRenderer.INSTANCE.renderPlayerHearts(event.getMatrixStack(), player);
        event.setCanceled(true);
    }
}
