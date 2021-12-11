package terrails.healthoverlay.fabric.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import me.shedaniel.fiber2cloth.api.ClothAttributes;
import me.shedaniel.fiber2cloth.api.Fiber2Cloth;
import me.shedaniel.fiber2cloth.impl.ColorPickerFormat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import terrails.healthoverlay.Constants;
import terrails.healthoverlay.fabric.HealthOverlay;
import terrails.healthoverlay.fabric.config.entry.ColorFieldListBuilder;

import java.util.Collections;

public class ConfigGuiCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> Fiber2Cloth.create(screen, Constants.MOD_ID, HealthOverlay.CONFIG_NODE, HealthOverlay.CONFIG_PATH.getFileName().toString())
                .setSaveRunnable(() -> HealthOverlay.setupConfig(false))
                .registerLeafEntryFunction(ConfigTypes.makeList(ConfigTypes.STRING), (leaf, type, mirror, defaultValue, errorSupplier) -> {
                    Component component = new TranslatableComponent("config." + Constants.MOD_ID + "." + leaf.getName());
                    if (leaf.getAttributeValue(ClothAttributes.COLOR_PICKER, ColorPickerFormat.TYPE).isPresent()) {
                        Integer minSize = leaf.getAttributeValue(HealthOverlay.LIST_MIN_SIZE, ConfigTypes.INTEGER).orElse(null);
                        Integer maxSize = leaf.getAttributeValue(HealthOverlay.LIST_MAX_SIZE, ConfigTypes.INTEGER).orElse(null);
                        return Collections.singletonList(ColorFieldListBuilder.create(component, mirror.getValue())
                                .setDefaultValue(defaultValue)
                                .setExpanded(true)
                                .minSize(minSize)
                                .maxSize(maxSize)
                                .setSaveConsumer(mirror::setValue)
                                .build());
                    } else return null;
                }).build().getScreen();
    }
}
