package terrails.healthoverlay.forge;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkConstants;
import terrails.healthoverlay.Constants;
import terrails.healthoverlay.ModConfiguration;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

@Mod(Constants.MOD_ID)
@Mod.EventBusSubscriber(bus = Bus.MOD)
public class HealthOverlay {

    private static final ForgeConfigSpec CONFIG_SPEC;
    private static final String CONFIG_FILE = Constants.MOD_ID + ".toml";

    public HealthOverlay() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (remote, server) -> true));
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CONFIG_SPEC, CONFIG_FILE);
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        setupConfig(FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE));
    }

    private static void setupConfig(final Path path) {
        Constants.LOGGER.debug("Loading config file {}", path);

        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .autoreload()
                .writingMode(WritingMode.REPLACE)
                .build();

        Constants.LOGGER.debug("Built TOML config for {}", path.toString());
        configData.load();
        Constants.LOGGER.debug("Loaded TOML config file {}", path.toString());
        CONFIG_SPEC.setConfig(configData);
    }

    private static final List<Runnable> CONFIG_APPLY_LIST = Lists.newArrayList();

    @SubscribeEvent
    public static void configEvent(ModConfigEvent event) {
        if (!event.getConfig().getModId().equals(Constants.MOD_ID)) {
            return;
        }

        CONFIG_APPLY_LIST.forEach(Runnable::run);
        Constants.LOGGER.debug("Loaded {} config file {}", Constants.MOD_ID, event.getConfig().getFileName());
    }

    private static <T> void configValue(ForgeConfigSpec.ConfigValue<T> configValue, Consumer<ForgeConfigSpec.ConfigValue<T>> consumer) {
        CONFIG_APPLY_LIST.add(() -> consumer.accept(configValue));
    }

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("health");

        ForgeConfigSpec.BooleanValue healthVanilla = builder
                .comment("Show vanilla hearts")
                .define("healthVanilla", true);
        configValue(healthVanilla, (value) -> ModConfiguration.healthVanilla = value.get());

        ForgeConfigSpec.ConfigValue<List<? extends String>> healthColors = builder
                .comment("Colors for every 10 hearts (not counting the default red)\nAll values are written as hexadecimal RGB color in '#RRGGBB' format")
                .defineList("healthColors", Lists.newArrayList("#F06E14", "#F5DC23", "#2DB928", "#1EAFBE", "#7346E1", "#FA7DEB", "#EB375A", "#FF8278", "#AAFFFA", "#EBEBFF"), o -> o != null && String.class.isAssignableFrom(o.getClass()));
        configValue(healthColors, (value) -> ModConfiguration.healthColors = ModConfiguration.getColors(value.get(), false, false));

        ForgeConfigSpec.ConfigValue<List<? extends String>> healthPoisonColors = builder
                .comment("Two alternating colors when poisoned\nThere can be one color in case vanilla poisoned heart is wanted")
                .defineList("healthPoisonColors", Lists.newArrayList("#739B00"), o -> o != null && String.class.isAssignableFrom(o.getClass()));
        configValue(healthPoisonColors, (value) -> ModConfiguration.healthPoisonColors = ModConfiguration.getColors(value.get(), false, true));

        ForgeConfigSpec.ConfigValue<List<? extends String>> healthWitherColors = builder
                .comment("Two alternating colors when withered\nThere can be one color in case vanilla withered heart is wanted")
                .defineList("healthWitherColors", Lists.newArrayList("#0F0F0F"), o -> o != null && String.class.isAssignableFrom(o.getClass()));
        configValue(healthWitherColors, (value) -> ModConfiguration.healthWitherColors = ModConfiguration.getColors(value.get(), false, true));

        ForgeConfigSpec.ConfigValue<List<? extends String>> healthFrozenColors = builder
                .comment("Two alternating colors when frozen\nThere can be one color in case vanilla frozen heart is wanted")
                .defineList("healthFrozenColors", Lists.newArrayList("#3E70E6"), o -> o != null && String.class.isAssignableFrom(o.getClass()));
        configValue(healthFrozenColors, (value) -> ModConfiguration.healthFrozenColors = ModConfiguration.getColors(value.get(), false, true));

        builder.pop();

        builder.push("absorption");

        ForgeConfigSpec.BooleanValue absorptionVanilla = builder
                .comment("Show vanilla hearts")
                .define("absorptionVanilla", true);
        configValue(absorptionVanilla, (value) -> ModConfiguration.absorptionVanilla = value.get());

        ForgeConfigSpec.ConfigValue<List<? extends String>> absorptionColors = builder
                .comment("Colors for every 10 hearts (not counting the default yellow)\nAll values are written as hexadecimal RGB color in '#RRGGBB' format")
                .defineList("absorptionColors", Lists.newArrayList(
                                "#E1FA9B", "#A0FFAF", "#AAFFFA", "#AACDFF", "#D7B4FF",
                                "#FAA5FF", "#FFB4B4", "#FFAA7D", "#D7F0FF", "#EBFFFA"),
                        o -> o != null && String.class.isAssignableFrom(o.getClass()));
        configValue(absorptionColors, (value) -> ModConfiguration.absorptionColors = ModConfiguration.getColors(value.get(), true, false));

        ForgeConfigSpec.ConfigValue<List<? extends String>> absorptionPoisonColors = builder
                .comment("Two alternating colors when poisoned")
                .defineList("absorptionPoisonColors", Lists.newArrayList("#BFF230", "#7AA15A"), o -> o != null && String.class.isAssignableFrom(o.getClass()));
        configValue(absorptionPoisonColors, (value) -> ModConfiguration.absorptionPoisonColors = ModConfiguration.getColors(value.get(), true, true));

        ForgeConfigSpec.ConfigValue<List<? extends String>> absorptionWitherColors = builder
                .comment("Two alternating colors when withered")
                .defineList("absorptionWitherColors", Lists.newArrayList("#787061", "#73625C"), o -> o != null && String.class.isAssignableFrom(o.getClass()));
        configValue(absorptionWitherColors, (value) -> ModConfiguration.absorptionWitherColors = ModConfiguration.getColors(value.get(), true, true));

        ForgeConfigSpec.ConfigValue<List<? extends String>> absorptionFrozenColors = builder
                .comment("Two alternating colors when freezing")
                .defineList("absorptionFrozenColors", Lists.newArrayList("#90D136", "#36D183"), o -> o != null && String.class.isAssignableFrom(o.getClass()));
        configValue(absorptionFrozenColors, (value) -> ModConfiguration.absorptionFrozenColors = ModConfiguration.getColors(value.get(), true, true));

        builder.push("advanced");

        ForgeConfigSpec.BooleanValue absorptionOverHealth = builder
                .comment("Display absorption in the same row as health\nAbsorption is render after and over health depending on the mode")
                .define("absorptionOverHealth", false);
        configValue(absorptionOverHealth, (value) -> ModConfiguration.absorptionOverHealth = value.get());

        ForgeConfigSpec.ConfigValue<ModConfiguration.AbsorptionMode> absorptionOverHealthMode = builder
                .comment("""
                         Display mode for absorption
                         absorption.advanced.absorptionOverHealth must to be true
                         Modes:
                           "BEGINNING":
                             Absorption always starts at first heart.
                           "AFTER_HEALTH":
                             Absorption starts after the last highest health heart and loops back to first health heart if overflowing.
                             This means that health hearts will be hidden when absorption has 10 or more hearts.
                               Example 1: If a player has 10 health (5 hearts), absorption will render itself in the last
                                            five hearts and in case it is higher it will loop back over first five health hearts.
                               Example 2: If a player has more than 20 absorption, second color is shown the same way as in "BEGINNING".
                               Example 3: If player health is divisible by 20, absorption is shown the same way as in "BEGINNING".""")
                .defineEnum("absorptionOverHealthMode", ModConfiguration.AbsorptionMode.AFTER_HEALTH);
        configValue(absorptionOverHealthMode, (value) -> ModConfiguration.absorptionOverHealthMode = value.get());

        builder.pop(2);
        CONFIG_SPEC = builder.build();
    }
}
