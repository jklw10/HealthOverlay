package terrails.healthoverlay;

import com.google.common.collect.Lists;
import io.github.fablabsmc.fablabs.api.fiber.v1.builder.ConfigTreeBuilder;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.ValueDeserializationException;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.StringSerializableType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.EnumConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ListConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import terrails.healthoverlay.heart.ColoredHeart;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

public class HealthOverlay implements ClientModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("HealthOverlay");

    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "healthoverlay.json5");
    private static final JanksonValueSerializer CONFIG_SERIALIZER = new JanksonValueSerializer(false);
    private static final ConfigBranch CONFIG_NODE;

    public static final Identifier HEALTH_ICONS_LOCATION = new Identifier("healthoverlay:textures/health.png");
    public static final Identifier ABSORPTION_ICONS_LOCATION = new Identifier("healthoverlay:textures/absorption.png");
    public static final Identifier HALF_HEART_ICONS_LOCATION = new Identifier("healthoverlay:textures/half_heart.png");

    private static final Runnable run;

    public static boolean absorptionOverHealth;
    public static AbsorptionMode absorptionOverHealthMode;

    public static boolean healthVanilla;
    public static ColoredHeart[] healthColors;
    public static ColoredHeart[] healthPoisonColors;
    public static ColoredHeart[] healthWitherColors;
    public static ColoredHeart[] healthFrozenColors;

    public static boolean absorptionVanilla;
    public static ColoredHeart[] absorptionColors;
    public static ColoredHeart[] absorptionPoisonColors;
    public static ColoredHeart[] absorptionWitherColors;
    public static ColoredHeart[] absorptionFrozenColors;

    static {
        ListConfigType<List<String>, String> COLOR = ConfigTypes.makeList(ConfigTypes.STRING.withType(new StringSerializableType(7, 7, Pattern.compile("^#[0-9a-fA-F]{6}+$"))));

        ConfigTreeBuilder tree = ConfigTree.builder();

        ConfigTreeBuilder healthCategory = tree.fork("health");

        PropertyMirror<Boolean> healthVanilla = PropertyMirror.create(ConfigTypes.BOOLEAN);
        healthCategory.beginValue("health_vanilla", ConfigTypes.BOOLEAN, true)
                .withComment("Show vanilla hearts").finishValue(healthVanilla::mirror);

        PropertyMirror<List<String>> healthColors = PropertyMirror.create(COLOR);
        healthCategory.beginValue("health_colors", COLOR.withMinSize(1),
                Lists.newArrayList("#F06E14", "#F5DC23", "#2DB928", "#1EAFBE", "#7346E1", "#FA7DEB", "#EB375A", "#FF8278", "#AAFFFA", "#EBEBFF"))
                .withComment("Colors for every 10 hearts (not counting the default red)\nAll values are written as hexadecimal RGB color in '#RRGGBB' format").finishValue(healthColors::mirror);

        PropertyMirror<List<String>> healthPoisonColors = PropertyMirror.create(COLOR);
        healthCategory.beginValue("health_poison_colors", COLOR.withMinSize(1).withMaxSize(2), Lists.newArrayList("#739B00", "#96CD00"))
                .withComment("Two alternating colors when poisoned\nThere can be one color in case vanilla poisoned heart is wanted").finishValue(healthPoisonColors::mirror);

        PropertyMirror<List<String>> healthWitherColors = PropertyMirror.create(COLOR);
        healthCategory.beginValue("health_wither_colors", COLOR.withMinSize(1).withMaxSize(2), Lists.newArrayList("#0F0F0F", "#2D2D2D"))
                .withComment("Two alternating colors when withered\nThere can be one color in case vanilla withered heart is wanted").finishValue(healthWitherColors::mirror);

        PropertyMirror<List<String>> healthFrozenColors = PropertyMirror.create(COLOR);
        healthCategory.beginValue("health_frozen_colors", COLOR.withMinSize(1).withMaxSize(2), Lists.newArrayList("#3E70E6", "#873EE6"))
                .withComment("Two alternating colors when freezing\nThere can be one color in case vanilla frozen heart is wanted").finishValue(healthFrozenColors::mirror);

        healthCategory.finishBranch();


        ConfigTreeBuilder absorptionCategory = tree.fork("absorption");

        PropertyMirror<Boolean> absorptionVanilla = PropertyMirror.create(ConfigTypes.BOOLEAN);
        absorptionCategory.beginValue("absorption_vanilla", ConfigTypes.BOOLEAN, true)
                .withComment("Show vanilla hearts").finishValue(absorptionVanilla::mirror);

        PropertyMirror<List<String>> absorptionColors = PropertyMirror.create(COLOR);
        absorptionCategory.beginValue("absorption_colors", COLOR.withMinSize(1),
                Lists.newArrayList("#E1FA9B", "#A0FFAF", "#AAFFFA", "#AACDFF", "#D7B4FF", "#FAA5FF", "#FFB4B4", "#FFAA7D", "#D7F0FF", "#EBFFFA"))
                .withComment("Colors for every 10 hearts (not counting the default yellow)\nAll values are written as hexadecimal RGB color in '#RRGGBB' format").finishValue(absorptionColors::mirror);

        PropertyMirror<List<String>> absorptionPoisonColors = PropertyMirror.create(COLOR);
        absorptionCategory.beginValue("absorption_poison_colors", COLOR.withMinSize(2).withMaxSize(2), Lists.newArrayList("#BFF230", "#7AA15A"))
                .withComment("Two alternating colors when poisoned").finishValue(absorptionPoisonColors::mirror);

        PropertyMirror<List<String>> absorptionWitherColors = PropertyMirror.create(COLOR);
        absorptionCategory.beginValue("absorption_wither_colors", COLOR.withMinSize(2).withMaxSize(2), Lists.newArrayList("#787061", "#73625C"))
                .withComment("Two alternating colors when withered").finishValue(absorptionWitherColors::mirror);

        PropertyMirror<List<String>> absorptionFrozenColors = PropertyMirror.create(COLOR);
        absorptionCategory.beginValue("absorption_frozen_colors", COLOR.withMinSize(2).withMaxSize(2), Lists.newArrayList("#90D136", "#36D183"))
                .withComment("Two alternating colors when freezing").finishValue(absorptionFrozenColors::mirror);

        ConfigTreeBuilder absorptionSubCategory = absorptionCategory.fork("advanced");

        PropertyMirror<Boolean> absorptionOverHealth = PropertyMirror.create(ConfigTypes.BOOLEAN);
        absorptionSubCategory.beginValue("absorption_over_health", ConfigTypes.BOOLEAN, false)
                .withComment("Display absorption in the same row as health\nAbsorption is rendered after and over health depending on the mode").finishValue(absorptionOverHealth::mirror);

        EnumConfigType<AbsorptionMode> ABSORPTION_MODE = ConfigTypes.makeEnum(AbsorptionMode.class);

        PropertyMirror<AbsorptionMode> absorptionOverHealthMode = PropertyMirror.create(ABSORPTION_MODE);
        absorptionSubCategory.beginValue("absorption_over_health_mode", ABSORPTION_MODE, AbsorptionMode.AFTER_HEALTH_ADVANCED)
                .withComment("""
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
                                   Example 3: If player health is divisible by 20, absorption is shown the same way as in "BEGINNING".
                               "AFTER_HEALTH_ADVANCED":
                                 Absorption starts after the last highest health heart and loops back to first absorption heart if overflowing.
                                 This means that no matter how much absorption there is, health hearts will almost always be visible.
                                   Example 1: If a player has 18 health (9 hearts), absorption will render itself in the last
                                              empty heart and color itself accordingly, e.g. absorption 0 has 2 hearts and
                                              will render using the second color as the first color is used for the first heart.
                                   Example 2: If a player has 30 health (15 hearts), absorption will render itself in the last
                                              five hearts and color itself accordingly, e.g. absorption 2 has 6 hearts and
                                              will render first heart using second color and rest using first color.
                                   Example 3: If player health is divisible by 20, absorption is shown the same way as in "BEGINNING".
                               "AS_HEALTH":
                                 Absorption is rendered as health, making all colors and values same as health.""").finishValue(absorptionOverHealthMode::mirror);

        absorptionSubCategory.finishBranch();
        absorptionCategory.finishBranch();

        run = (() -> {
            HealthOverlay.absorptionOverHealth = absorptionOverHealth.getValue();
            HealthOverlay.absorptionOverHealthMode = absorptionOverHealthMode.getValue();

            HealthOverlay.healthVanilla = healthVanilla.getValue();
            HealthOverlay.healthColors = getColors(healthColors.getValue(), false, false);
            HealthOverlay.healthPoisonColors = getColors(healthPoisonColors.getValue(), false, true);
            HealthOverlay.healthWitherColors = getColors(healthWitherColors.getValue(), false, true);
            HealthOverlay.healthFrozenColors = getColors(healthFrozenColors.getValue(), false, true);

            HealthOverlay.absorptionVanilla = absorptionVanilla.getValue();
            HealthOverlay.absorptionColors = getColors(absorptionColors.getValue(), true, false);
            HealthOverlay.absorptionPoisonColors = getColors(absorptionPoisonColors.getValue(), true, true);
            HealthOverlay.absorptionWitherColors = getColors(absorptionWitherColors.getValue(), true, true);
            HealthOverlay.absorptionFrozenColors = getColors(absorptionFrozenColors.getValue(), true, true);
        });

        CONFIG_NODE = tree.build();
    }

    private static ColoredHeart[] getColors(List<? extends String> stringValues, boolean absorption, boolean effect) {
        ColoredHeart[] colors;
        int offset;
        if (absorption && effect && (stringValues.size() == 1 || stringValues.size() > 2)) {
            HealthOverlay.LOGGER.error("Absorption effect colors must either be empty or have 2 values.");
            throw new IllegalArgumentException(stringValues.toString());
        } else if (absorption && !effect && HealthOverlay.absorptionVanilla) {
            colors = new ColoredHeart[stringValues.size() + 1];
            colors[0] = ColoredHeart.absorption();
            offset = 1;
        } else if (!absorption && effect && stringValues.size() == 1) {
            colors = new ColoredHeart[2];
            colors[0] = ColoredHeart.health();
            offset = 1;
        } else if (!absorption && !effect && HealthOverlay.healthVanilla) {
            colors = new ColoredHeart[stringValues.size() + 1];
            colors[0] = ColoredHeart.health();
            offset = 1;
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

    public enum AbsorptionMode {
        BEGINNING, AFTER_HEALTH, AFTER_HEALTH_ADVANCED, AS_HEALTH
    }

    @Override
    public void onInitializeClient() {
        boolean recreate = false;
        while (true) {
            try {
                if (!CONFIG_FILE.exists() || recreate) {
                    FiberSerialization.serialize(CONFIG_NODE, Files.newOutputStream(CONFIG_FILE.toPath()), CONFIG_SERIALIZER);
                    LOGGER.info("Successfully created the config file in '{}'", CONFIG_FILE.toString());
                    break;
                } else {
                    try {
                        FiberSerialization.deserialize(CONFIG_NODE, Files.newInputStream(CONFIG_FILE.toPath()), CONFIG_SERIALIZER);

                        // Checks values and makes a copy of the config file before fixing the errors via the next method call
                        //          ....

                        // Load current values and write to the file again in case a new value was added
                        // TODO: Add some kind of error checking to the values in the file and rename the file before loading the corrected values from the branch
                        FiberSerialization.serialize(CONFIG_NODE, Files.newOutputStream(CONFIG_FILE.toPath()), CONFIG_SERIALIZER);
                        break;
                    } catch (ValueDeserializationException e) {
                        String fileName = ("healthoverlay-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss")) + ".json5");
                        LOGGER.error("Found a syntax error in the config.");
                        if (CONFIG_FILE.renameTo(new File(CONFIG_FILE.getParent(), fileName))) { LOGGER.info("Config file successfully renamed to '{}'.", fileName); }
                        recreate = true;
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        run.run();
    }
}
