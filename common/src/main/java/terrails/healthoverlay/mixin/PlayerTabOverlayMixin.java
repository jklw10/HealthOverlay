package terrails.healthoverlay.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import terrails.healthoverlay.render.TabHeartRenderer;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow private long visibilityId;

    /**
     * @author Terrails (Health Overlay)
     * @reason Replacement of the default tab heart renderer
     */
    @Overwrite
    private void renderTablistScore(Objective objective, int y, String string, int x, int x2, PlayerInfo playerInfo, PoseStack poseStack) {
        int score = objective.getScoreboard().getOrCreatePlayerScore(string, objective).getScore();
        if (objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
            TabHeartRenderer.INSTANCE.renderPlayerListHud(y, x, x2, playerInfo, poseStack, score, this.visibilityId);
        } else {
            String text = ChatFormatting.YELLOW.toString() + score;
            this.minecraft.font.drawShadow(poseStack, text, (float) (x2 - this.minecraft.font.width(text)), (float) y, 16777215);
        }
    }
}
