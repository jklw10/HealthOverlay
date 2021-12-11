package terrails.healthoverlay;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class HOExpectPlatform {

    @ExpectPlatform
    public static int modifyStatusBarYPos(int absorption) {
        throw new AssertionError();
    }
}
