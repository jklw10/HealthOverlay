package terrails.healthoverlay.fabric.config.entry;

/*
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.gui.entries.AbstractTextFieldListListEntry;
import me.shedaniel.clothconfig2.gui.widget.ColorDisplayWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import terrails.healthoverlay.Constants;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
*/

public class ColorListEntry {} /*extends AbstractTextFieldListListEntry<String, ColorListEntry.ColorListCell, ColorListEntry> {

    private Integer min = null, max = null;

    public ColorListEntry(Component fieldName, List<String> value, boolean defaultExpanded,
                          Supplier<Optional<Component[]>> tooltipSupplier, Consumer<List<String>> saveConsumer, Supplier<List<String>> defaultValue, Component resetButtonKey) {
        this(fieldName, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, false);
    }

    public ColorListEntry(Component fieldName, List<String> value, boolean defaultExpanded,
                          Supplier<Optional<Component[]>> tooltipSupplier, Consumer<List<String>> saveConsumer, Supplier<List<String>> defaultValue, Component resetButtonKey, boolean requiresRestart) {
        this(fieldName, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, requiresRestart, true, true);
    }

    public ColorListEntry(Component fieldName, List<String> value, boolean defaultExpanded,
                          Supplier<Optional<Component[]>> tooltipSupplier, Consumer<List<String>> saveConsumer, Supplier<List<String>> defaultValue, Component resetButtonKey, boolean requiresRestart, boolean deleteButtonEnabled, boolean insertInFront) {
        super(fieldName, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, requiresRestart, deleteButtonEnabled, insertInFront, ColorListCell::new);
    }

    public void minSize(Integer min) {
        this.min = min;
    }

    public void maxSize(Integer max) {
        this.max = max;
    }

    @Override
    public Optional<Component> getError() {
        int size = this.getValue().size();
        if (this.min != null && size < this.min) {
            return Optional.of(new TranslatableComponent("config." + Constants.MOD_ID + ".list_too_small"));
        } else if (this.max != null && size > this.max) {
            return Optional.of(new TranslatableComponent("config." + Constants.MOD_ID + ".list_too_big"));
        }
        return super.getError();
    }

    @Override
    public ColorListEntry self() {
        return this;
    }

    public static class ColorListCell extends AbstractTextFieldListCell<String, ColorListEntry.ColorListCell, ColorListEntry> {

        private final ColorDisplayWidget colorDisplayWidget;

        public ColorListCell(@Nullable String value, ColorListEntry listListEntry) {
            super(value == null || value.isEmpty() ? "#" : value, listListEntry);
            this.colorDisplayWidget = new ColorDisplayWidget(this.widget, 0, 0, 20, this.getColor());
        }

        @Override
        protected @Nullable String substituteDefault(@Nullable String value) {
            return value == null ? "" : value;
        }

        @Override
        protected boolean isValidText(@NotNull String text) {
            return text.startsWith("#") && (text.length() < 2 || text.substring(1).matches("^[0-9a-fA-F]{1,6}+$"));
        }

        @Override
        public String getValue() {
            try {
                return this.widget.getValue();
            } catch (NumberFormatException var2) {
                return "";
            }
        }

        public int getColor() {
            String value = this.getValue();
            if (value != null && value.matches("^#[0-9a-fA-F]{6}+$")) {
                return Integer.parseInt(value.substring(1), 16);
            } else return  0;
        }

        @Override
        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            super.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
            this.colorDisplayWidget.y = y - this.widget.getHeight() / 4;

            this.colorDisplayWidget.setColor(-16777216 | this.getColor());

            if (Minecraft.getInstance().font.isBidirectional()) {
                this.colorDisplayWidget.x = x + this.widget.getWidth();
            } else {
                this.colorDisplayWidget.x = this.widget.x - 23;
            }

            this.colorDisplayWidget.render(matrices, mouseX, mouseY, delta);
        }

        @Override
        public Optional<Component> getError() {
            String value = this.getValue();
            if (!value.matches("^#[0-9a-fA-F]{6}+$")) {
                return Optional.of(new TranslatableComponent("config." + Constants.MOD_ID + ".invalid_hex_color"));
            } else return Optional.empty();
        }
    }
}
*/