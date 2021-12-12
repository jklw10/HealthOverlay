package terrails.healthoverlay.fabric;

import com.google.common.collect.Lists;
import io.github.fablabsmc.fablabs.api.fiber.v1.FiberId;
import io.github.fablabsmc.fablabs.api.fiber.v1.builder.ConfigTreeBuilder;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.ValueDeserializationException;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.StringSerializableType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.EnumConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ListConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigAttribute;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.PropertyMirror;
/*import me.shedaniel.fiber2cloth.api.ClothAttributes;*/
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import terrails.healthoverlay.Constants;
import terrails.healthoverlay.ModConfiguration;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class HealthOverlay implements ClientModInitializer {

    public static final JanksonValueSerializer JANKSON_SERIALIZER = new JanksonValueSerializer(false);
    public static final ConfigBranch CONFIG_NODE;
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(Constants.MOD_ID + ".json5");

    public static final FiberId LIST_MIN_SIZE = new FiberId(Constants.MOD_ID, "list_min_size");
    public static final FiberId LIST_MAX_SIZE = new FiberId(Constants.MOD_ID, "list_max_size");

    @Override
    public void onInitializeClient() {
        setupConfig(true);
    }

    public static void setupConfig(boolean deserialize) {
        while (true) {
            try {
                if (deserialize) {
                    if (Files.exists(CONFIG_PATH)) {
                        try {
                            FiberSerialization.deserialize(CONFIG_NODE, Files.newInputStream(CONFIG_PATH), JANKSON_SERIALIZER);
                        } catch (ValueDeserializationException e) {
                            Constants.LOGGER.error("Found a syntax error in '{}'.", CONFIG_PATH.toString());
                            e.printStackTrace();

                            String deformedFile = (Constants.MOD_ID + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss")) + ".json5");
                            Files.move(CONFIG_PATH, FabricLoader.getInstance().getConfigDir().resolve(deformedFile));
                            Constants.LOGGER.info("Deformed config file renamed to '{}'.", deformedFile);
                            continue;
                        }
                    } else {
                        Constants.LOGGER.info("Creating config file at '{}'.", CONFIG_PATH.toString());
                    }
                }

                FiberSerialization.serialize(CONFIG_NODE, Files.newOutputStream(CONFIG_PATH), JANKSON_SERIALIZER);
                Constants.LOGGER.info("Successfully loaded '{}'.", CONFIG_PATH.toString());
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        CONFIG_APPLY_LIST.forEach(Runnable::run);
    }

    private static final List<Runnable> CONFIG_APPLY_LIST = Lists.newArrayList();

    private static <T> void configValue(PropertyMirror<T> configValue, Consumer<PropertyMirror<T>> consumer) {
        CONFIG_APPLY_LIST.add(() -> consumer.accept(configValue));
    }

    static {
        ListConfigType<List<String>, String> COLOR = ConfigTypes.makeList(ConfigTypes.STRING.withType(new StringSerializableType(7, 7, Pattern.compile("^#[0-9a-fA-F]{6}+$"))));
        EnumConfigType<ModConfiguration.AbsorptionMode> ABSORPTION_MODE = ConfigTypes.makeEnum(ModConfiguration.AbsorptionMode.class);

        ConfigTreeBuilder tree = ConfigTree.builder();

        /*ConfigAttribute<String> colorAttribute = ClothAttributes.colorPicker(false);*/
        ConfigAttribute<BigDecimal> listMinSizeAttribute = ConfigAttribute.create(LIST_MIN_SIZE, ConfigTypes.INTEGER, 1);
        ConfigAttribute<BigDecimal> listMaxSizeAttribute = ConfigAttribute.create(LIST_MAX_SIZE, ConfigTypes.INTEGER, 2);

        ConfigTreeBuilder healthCategory = tree.fork("health");

        PropertyMirror<Boolean> healthVanilla = PropertyMirror.create(ConfigTypes.BOOLEAN);
        healthCategory.beginValue("health_vanilla", ConfigTypes.BOOLEAN, true)
                .withComment("Show vanilla hearts").finishValue(healthVanilla::mirror);
        configValue(healthVanilla, (value) -> ModConfiguration.healthVanilla = value.getValue());

        PropertyMirror<List<String>> healthColors = PropertyMirror.create(COLOR);
        healthCategory.beginValue("health_colors", COLOR.withMinSize(1),
                        Lists.newArrayList("#F06E14", "#F5DC23", "#2DB928", "#1EAFBE", "#7346E1", "#FA7DEB", "#EB375A", "#FF8278", "#AAFFFA", "#EBEBFF"))
                .withComment("Colors for every 10 hearts (not counting the default red)\nAll values are written as hexadecimal RGB color in '#RRGGBB' format")
                /*.withAttribute(colorAttribute)*/.withAttribute(listMinSizeAttribute).finishValue(healthColors::mirror);
        configValue(healthColors, (value) -> ModConfiguration.healthColors = ModConfiguration.getColors(value.getValue(), false, false));

        PropertyMirror<List<String>> healthPoisonColors = PropertyMirror.create(COLOR);
        healthCategory.beginValue("health_poison_colors", COLOR.withMinSize(1).withMaxSize(2), Lists.newArrayList("#739B00", "#96CD00"))
                .withComment("Two alternating colors when poisoned\nThere can be one color in case vanilla poisoned heart is wanted")
                /*.withAttribute(colorAttribute)*/.withAttribute(listMinSizeAttribute).withAttribute(listMaxSizeAttribute).finishValue(healthPoisonColors::mirror);
        configValue(healthPoisonColors, (value) -> ModConfiguration.healthPoisonColors = ModConfiguration.getColors(value.getValue(), false, true));

        PropertyMirror<List<String>> healthWitherColors = PropertyMirror.create(COLOR);
        healthCategory.beginValue("health_wither_colors", COLOR.withMinSize(1).withMaxSize(2), Lists.newArrayList("#0F0F0F", "#2D2D2D"))
                .withComment("Two alternating colors when withered\nThere can be one color in case vanilla withered heart is wanted")
                /*.withAttribute(colorAttribute)*/.withAttribute(listMinSizeAttribute).withAttribute(listMaxSizeAttribute).finishValue(healthWitherColors::mirror);
        configValue(healthWitherColors, (value) -> ModConfiguration.healthWitherColors = ModConfiguration.getColors(value.getValue(), false, true));

        PropertyMirror<List<String>> healthFrozenColors = PropertyMirror.create(COLOR);
        healthCategory.beginValue("health_frozen_colors", COLOR.withMinSize(1).withMaxSize(2), Lists.newArrayList("#3E70E6", "#873EE6"))
                .withComment("Two alternating colors when freezing\nThere can be one color in case vanilla frozen heart is wanted")
                /*.withAttribute(colorAttribute)*/.withAttribute(listMinSizeAttribute).withAttribute(listMaxSizeAttribute).finishValue(healthFrozenColors::mirror);
        configValue(healthFrozenColors, (value) -> ModConfiguration.healthFrozenColors = ModConfiguration.getColors(value.getValue(), false, true));

        healthCategory.finishBranch();


        ConfigTreeBuilder absorptionCategory = tree.fork("absorption");

        PropertyMirror<Boolean> absorptionVanilla = PropertyMirror.create(ConfigTypes.BOOLEAN);
        absorptionCategory.beginValue("absorption_vanilla", ConfigTypes.BOOLEAN, true)
                .withComment("Show vanilla hearts").finishValue(absorptionVanilla::mirror);
        configValue(absorptionVanilla, (value) -> ModConfiguration.absorptionVanilla = value.getValue());

        PropertyMirror<List<String>> absorptionColors = PropertyMirror.create(COLOR);
        absorptionCategory.beginValue("absorption_colors", COLOR.withMinSize(1),
                        Lists.newArrayList("#E1FA9B", "#A0FFAF", "#AAFFFA", "#AACDFF", "#D7B4FF", "#FAA5FF", "#FFB4B4", "#FFAA7D", "#D7F0FF", "#EBFFFA"))
                .withComment("Colors for every 10 hearts (not counting the default yellow)\nAll values are written as hexadecimal RGB color in '#RRGGBB' format")
                /*.withAttribute(colorAttribute)*/.withAttribute(listMinSizeAttribute).finishValue(absorptionColors::mirror);
        configValue(absorptionColors, (value) -> ModConfiguration.absorptionColors = ModConfiguration.getColors(value.getValue(), true, false));

        listMinSizeAttribute = ConfigAttribute.create(LIST_MIN_SIZE, ConfigTypes.INTEGER, 2);

        PropertyMirror<List<String>> absorptionPoisonColors = PropertyMirror.create(COLOR);
        absorptionCategory.beginValue("absorption_poison_colors", COLOR.withMinSize(2).withMaxSize(2), Lists.newArrayList("#BFF230", "#7AA15A"))
                .withComment("Two alternating colors when poisoned")
                /*.withAttribute(colorAttribute)*/.withAttribute(listMinSizeAttribute).withAttribute(listMaxSizeAttribute).finishValue(absorptionPoisonColors::mirror);
        configValue(absorptionPoisonColors, (value) -> ModConfiguration.absorptionPoisonColors = ModConfiguration.getColors(value.getValue(), true, true));

        PropertyMirror<List<String>> absorptionWitherColors = PropertyMirror.create(COLOR);
        absorptionCategory.beginValue("absorption_wither_colors", COLOR.withMinSize(2).withMaxSize(2), Lists.newArrayList("#787061", "#73625C"))
                .withComment("Two alternating colors when withered")
                /*.withAttribute(colorAttribute)*/.withAttribute(listMinSizeAttribute).withAttribute(listMaxSizeAttribute).finishValue(absorptionWitherColors::mirror);
        configValue(absorptionWitherColors, (value) -> ModConfiguration.absorptionWitherColors = ModConfiguration.getColors(value.getValue(), true, true));

        PropertyMirror<List<String>> absorptionFrozenColors = PropertyMirror.create(COLOR);
        absorptionCategory.beginValue("absorption_frozen_colors", COLOR.withMinSize(2).withMaxSize(2), Lists.newArrayList("#90D136", "#36D183"))
                .withComment("Two alternating colors when freezing")
                /*.withAttribute(colorAttribute)*/.withAttribute(listMinSizeAttribute).withAttribute(listMaxSizeAttribute).finishValue(absorptionFrozenColors::mirror);
        configValue(absorptionFrozenColors, (value) -> ModConfiguration.absorptionFrozenColors = ModConfiguration.getColors(value.getValue(), true, true));

        ConfigTreeBuilder absorptionSubCategory = absorptionCategory.fork("advanced");

        PropertyMirror<Boolean> absorptionOverHealth = PropertyMirror.create(ConfigTypes.BOOLEAN);
        absorptionSubCategory.beginValue("absorption_over_health", ConfigTypes.BOOLEAN, false)
                .withComment("Display absorption in the same row as health\nAbsorption is rendered after and over health depending on the mode").finishValue(absorptionOverHealth::mirror);
        configValue(absorptionOverHealth, (value) -> ModConfiguration.absorptionOverHealth = value.getValue());

        PropertyMirror<ModConfiguration.AbsorptionMode> absorptionOverHealthMode = PropertyMirror.create(ABSORPTION_MODE);
        absorptionSubCategory.beginValue("absorption_over_health_mode", ABSORPTION_MODE, ModConfiguration.AbsorptionMode.AFTER_HEALTH)
                .withComment("""
                        Display mode for absorption
                        absorption.advanced.absorption_over_health must be true
                        Modes:
                          "BEGINNING":
                            Absorption always starts at first heart.
                          "AFTER_HEALTH":
                            Absorption starts after the last highest health heart and loops back to first health heart if overflowing.
                            This means that health hearts will be hidden when absorption has 10 or more hearts.
                              Example 1: If a player has 10 health (5 hearts), absorption will render itself in the last
                                           five hearts and in case it is higher it will loop back over first five health hearts.
                              Example 2: If a player has more than 20 absorption, second color is shown the same way as in "BEGINNING".
                              Example 3: If player health is divisible by 20, absorption is shown the same way as in "BEGINNING".""").finishValue(absorptionOverHealthMode::mirror);
        configValue(absorptionOverHealthMode, (value) -> ModConfiguration.absorptionOverHealthMode = value.getValue());

        absorptionSubCategory.finishBranch();
        absorptionCategory.finishBranch();

        CONFIG_NODE = tree.build();
    }
}