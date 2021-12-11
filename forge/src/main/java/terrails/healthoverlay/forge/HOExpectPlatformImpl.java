package terrails.healthoverlay.forge;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.gui.ForgeIngameGui;
import terrails.healthoverlay.ModConfiguration;

public class HOExpectPlatformImpl {

    public static int modifyStatusBarYPos(int absorption) {
        int offset = 10 + (absorption > 0 && !ModConfiguration.absorptionOverHealth ? 10 : 0);
        ((ForgeIngameGui) Minecraft.getInstance().gui).left_height += offset;
        return offset;
    }
}
