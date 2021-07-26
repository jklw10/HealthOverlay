package terrails.healthoverlay;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.common.collect.Lists;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import terrails.healthoverlay.heart.ColoredHeart;

import java.nio.file.Path;
import java.util.List;

@Mod(HealthOverlay.MOD_ID)
@EventBusSubscriber(bus = Bus.MOD)
public class HealthOverlay {

    public static final Logger LOGGER = LogManager.getLogger("HealthOverlay");
    public static final String MOD_ID = "healthoverlay";

    private static final ForgeConfigSpec CONFIG_SPEC;

    public static final ResourceLocation HEALTH_ICONS_LOCATION = new ResourceLocation(MOD_ID + ":textures/health.png");
    public static final ResourceLocation ABSORPTION_ICONS_LOCATION = new ResourceLocation(MOD_ID + ":textures/absorption.png");
    public static final ResourceLocation HALF_HEART_ICONS_LOCATION = new ResourceLocation(MOD_ID + ":textures/half_heart.png");

    public HealthOverlay() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG_SPEC, "healthoverlay.toml");
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        loadConfig(FMLPaths.CONFIGDIR.get().resolve("healthoverlay.toml"));
    }

    private static void loadConfig(Path path) {
        HealthOverlay.LOGGER.debug("Loading config file {}", path);

        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        HealthOverlay.LOGGER.debug("Built TOML config for {}", path.toString());
        configData.load();
        HealthOverlay.LOGGER.debug("Loaded TOML config file {}", path.toString());
        CONFIG_SPEC.setConfig(configData);
    }

    private static final Runnable run;

    public static boolean absorptionOverHealth;
    public static AbsorptionMode absorptionOverHealthMode;

    public static boolean healthVanilla;
    public static ColoredHeart[] healthColors;
    public static ColoredHeart[] healthPoisonColors;
    public static ColoredHeart[] healthWitherColors;

    public static boolean absorptionVanilla;
    public static ColoredHeart[] absorptionColors;
    public static ColoredHeart[] absorptionPoisonColors;
    public static ColoredHeart[] absorptionWitherColors;


    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("health").comment("All the values are written as a Hexadecimal number in the '#RRGGBB' format");

        ForgeConfigSpec.BooleanValue healthVanilla = builder
                .comment(" Show vanilla hearts")
                .define("healthVanilla", true);

        ForgeConfigSpec.ConfigValue<List<? extends String>> healthColors = builder
                .comment(" Colors for every 10 hearts (not counting the default red)\n" +
                        " All values are written as hexadecimal RGB color in '#RRGGBB' format")
                .defineList("healthColors", Lists.newArrayList(
                        "#F06E14", "#F5DC23", "#2DB928", "#1EAFBE", "#7346E1",
                        "#FA7DEB", "#EB375A", "#FF8278", "#AAFFFA", "#EBEBFF"),
                        o -> o != null && String.class.isAssignableFrom(o.getClass()));

        ForgeConfigSpec.ConfigValue<List<? extends String>> healthPoisonColors = builder
                .comment(" Two alternating colors when poisoned")
                .defineList("healthPoisonColors", Lists.newArrayList(
                        "#739B00", "#96CD00"
                ), o -> o != null && String.class.isAssignableFrom(o.getClass()));

        ForgeConfigSpec.ConfigValue<List<? extends String>> healthWitherColors = builder
                .comment(" Two alternating colors when withered")
                .defineList("healthWitherColors", Lists.newArrayList(
                        "#0F0F0F", "#2D2D2D"
                ), o -> o != null && String.class.isAssignableFrom(o.getClass()));

        builder.pop();

        builder.push("absorption");

        ForgeConfigSpec.BooleanValue absorptionVanilla = builder
                .comment(" Show vanilla hearts")
                .define("absorptionVanilla", true);

        ForgeConfigSpec.ConfigValue<List<? extends String>> absorptionColors = builder
                .comment(" Colors for every 10 hearts (not counting the default yellow)\n" +
                        " All values are written as hexadecimal RGB color in '#RRGGBB' format")
                .defineList("absorptionColors", Lists.newArrayList(
                        "#E1FA9B", "#A0FFAF",
                        "#AAFFFA", "#AACDFF",
                        "#D7B4FF", "#FAA5FF",
                        "#FFB4B4", "#FFAA7D",
                        "#D7F0FF", "#EBFFFA"
                ), o -> o != null && String.class.isAssignableFrom(o.getClass()));

        ForgeConfigSpec.ConfigValue<List<? extends String>> poisonAbsorptionColors = builder
                .comment(" Two alternating colors when poisoned\n" +
                        " Can be empty in case of vanilla behaviour where heart background is rendered without hearts")
                .defineList("absorptionPoisonColors", Lists.newArrayList(), o -> o != null && String.class.isAssignableFrom(o.getClass()));

        ForgeConfigSpec.ConfigValue<List<? extends String>> witherAbsorptionColors = builder
                .comment(" Two alternating colors when withered\n" +
                        " Can be empty in case of vanilla behaviour where heart background is rendered without hearts")
                .defineList("absorptionWitherColors", Lists.newArrayList(), o -> o != null && String.class.isAssignableFrom(o.getClass()));


        builder.push("advanced");

        ForgeConfigSpec.BooleanValue absorptionOverHealth = builder
                .comment(" Display absorption in the same row as health\n" +
                        " Absorption is rendered after and over health depending on the mode")
                .define("absorptionOverHealth", false);

        ForgeConfigSpec.ConfigValue<AbsorptionMode> absorptionOverHealthMode = builder
                .comment(" Display mode for absorption\n" +
                        " absorption.advanced.absorptionOverHealth must to be true\n" +
                        " Modes: \n" +
                        "   \"BEGINNING\":\n" +
                        "     Absorption always starts at first heart.\n" +
                        "   \"AFTER_HEALTH\":\n" +
                        "     Absorption starts after the last highest health heart and loops back to first health heart if overflowing.\n" +
                        "     This means that health hearts will be hidden when absorption has 10 or more hearts.\n" +
                        "       Example 1: If a player has 10 health (5 hearts), absorption will render itself in the last\n" +
                        "                    five hearts and in case it is higher it will loop back over first five health hearts.\n" +
                        "       Example 2: If a player has more than 20 absorption, second color is shown the same way as in \"BEGINNING\".\n" +
                        "       Example 3: If player health is divisible by 20, absorption is shown the same way as in \"BEGINNING\".\n" +
                        "   \"AFTER_HEALTH_ADVANCED\":\n" +
                        "     Absorption starts after the last highest health heart and loops back to first absorption heart if overflowing.\n" +
                        "     This means that no matter how much absorption there is, health hearts will almost always be visible.\n" +
                        "       Example 1: If a player has 18 health (9 hearts), absorption will render itself in the last\n" +
                        "                  empty heart and color itself accordingly, e.g. absorption 0 has 2 hearts and\n" +
                        "                  will render using the second color as the first color is used for the first heart.\n" +
                        "       Example 2: If a player has 30 health (15 hearts), absorption will render itself in the last\n" +
                        "                  five hearts and color itself accordingly, e.g. absorption 2 has 6 hearts and\n" +
                        "                  will render first heart using second color and rest using first color.\n" +
                        "       Example 3: If player health is divisible by 20, absorption is shown the same way as in \"BEGINNING\".\n" +
                        "   \"AS_HEALTH\":\n" +
                        "     Absorption is rendered as health, making all colors and values same as health.")
                .defineEnum("absorptionOverHealthMode", AbsorptionMode.AFTER_HEALTH_ADVANCED);

        builder.pop(2);

        run = (() -> {
            HealthOverlay.absorptionOverHealth = absorptionOverHealth.get();
            HealthOverlay.absorptionOverHealthMode = absorptionOverHealthMode.get();

            HealthOverlay.healthVanilla = healthVanilla.get();
            HealthOverlay.healthColors = getColors(healthColors.get(), false, false);
            HealthOverlay.healthPoisonColors = getColors(healthPoisonColors.get(), false, true);
            HealthOverlay.healthWitherColors = getColors(healthWitherColors.get(), false, true);

            HealthOverlay.absorptionVanilla = absorptionVanilla.get();
            HealthOverlay.absorptionColors = getColors(absorptionColors.get(), true, false);
            HealthOverlay.absorptionPoisonColors = getColors(poisonAbsorptionColors.get(), true, true);
            HealthOverlay.absorptionWitherColors = getColors(witherAbsorptionColors.get(), true, true);
        });

        CONFIG_SPEC = builder.build();
    }

    private static ColoredHeart[] getColors(List<? extends String> stringValues, boolean absorption, boolean effect) {
        ColoredHeart[] colors;
        int offset;
        if (absorption && effect && (stringValues.size() == 1 || stringValues.size() > 2)) {
            HealthOverlay.LOGGER.error("Absorption heart effect colors must be either empty or have 2 values.");
            throw new IllegalArgumentException(stringValues.toString());
        } else if (!absorption && effect && stringValues.size() != 2) {
            HealthOverlay.LOGGER.error("Health heart effect colors must have 2 values.");
            throw new IllegalArgumentException(stringValues.toString());
        } else if (absorption && !effect && HealthOverlay.absorptionVanilla) {
            colors = new ColoredHeart[stringValues.size() + 1];
            colors[0] = ColoredHeart.absorption();
            offset = 1;
        } else if (!absorption && !effect && HealthOverlay.healthVanilla) {
            colors = new ColoredHeart[stringValues.size() + 1];
            colors[0] = ColoredHeart.health();
            offset = 1;
        } else if (effect && absorption && stringValues.isEmpty()) {
            return new ColoredHeart[]{ColoredHeart.absorption(), ColoredHeart.absorption()};
        } else if (stringValues.isEmpty()) {
            return null;
        } else {
            colors = new ColoredHeart[stringValues.size()];
            offset = 0;
        }

        for (int i = 0; i < stringValues.size(); i++) {
            colors[i + offset] = ColoredHeart.parseColor(stringValues.get(i), absorption);
        }
        return colors;
    }

    @SubscribeEvent
    public static void configLoading(final ModConfig.ModConfigEvent event) {
        if (!event.getConfig().getModId().equals(HealthOverlay.MOD_ID))
            return;

        run.run();
        HealthOverlay.LOGGER.debug("Loaded {} config file {}", HealthOverlay.MOD_ID, event.getConfig().getFileName());
    }

    public enum AbsorptionMode {
        BEGINNING, AFTER_HEALTH, AFTER_HEALTH_ADVANCED, AS_HEALTH
    }
}
