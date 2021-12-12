package terrails.healthoverlay.fabric.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ConfigGuiCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
/*        if (FabricLoader.getInstance().isModLoaded("cloth_config")) {
            return FiberClothCompat.getModConfigScreenFactory();
        } else */return screen -> null;
    }
}
