package terrails.healthoverlay;

import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Constants {

    public static final String MOD_ID = "healthoverlay";
    public static final Logger LOGGER = LogManager.getLogger("Health Overlay");

    public static final ResourceLocation HEALTH_ICONS_LOCATION = new ResourceLocation(MOD_ID, "textures/health.png");
    public static final ResourceLocation ABSORPTION_ICONS_LOCATION = new ResourceLocation(MOD_ID, "textures/absorption.png");
    public static final ResourceLocation HALF_HEART_ICONS_LOCATION = new ResourceLocation(MOD_ID, "textures/half_heart.png");
}
