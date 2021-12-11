package terrails.healthoverlay.fabric;

import terrails.healthoverlay.ModConfiguration;

public class HOExpectPlatformImpl {

    public static int modifyStatusBarYPos(int absorption) {
        return (!ModConfiguration.absorptionOverHealth && absorption > 0) ? 2 : 1;
    }
}
