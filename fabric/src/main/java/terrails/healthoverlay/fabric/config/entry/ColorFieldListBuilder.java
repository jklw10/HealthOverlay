package terrails.healthoverlay.fabric.config.entry;

import me.shedaniel.clothconfig2.impl.builders.FieldBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ColorFieldListBuilder extends FieldBuilder<List<Integer>, ColorListEntry> {

    protected Function<String, Optional<Component>> cellErrorSupplier;
    private Consumer<List<String>> saveConsumer = null;
    private Function<List<String>, Optional<Component[]>> tooltipSupplier = (list) -> Optional.empty();

    private final List<String> value;
    private Supplier<List<String>> defaultValue;

    private boolean expanded = false;
    private Function<ColorListEntry, ColorListEntry.ColorListCell> createNewInstance;
    private Component addTooltip = new TranslatableComponent("text.cloth-config.list.add");
    private Component removeTooltip = new TranslatableComponent("text.cloth-config.list.remove");
    private boolean deleteButtonEnabled = true;
    private boolean insertInFront = true;

    private Integer min = null, max = null;

    private ColorFieldListBuilder(Component fieldNameKey, List<String> value) {
        super(new TranslatableComponent("text.cloth-config.reset_value"), fieldNameKey);
        this.value = value;
    }

    public static ColorFieldListBuilder create(Component fieldNameKey, List<String> value) {
        return new ColorFieldListBuilder(fieldNameKey, value);
    }

    public Function<String, Optional<Component>> getCellErrorSupplier() {
        return cellErrorSupplier;
    }

    public ColorFieldListBuilder setCellErrorSupplier(Function<String, Optional<Component>> cellErrorSupplier) {
        this.cellErrorSupplier = cellErrorSupplier;
        return this;
    }

    public ColorFieldListBuilder setDeleteButtonEnabled(boolean deleteButtonEnabled) {
        this.deleteButtonEnabled = deleteButtonEnabled;
        return this;
    }

    public ColorFieldListBuilder insertInBack() {
        this.insertInFront = false;
        return this;
    }

    public ColorFieldListBuilder setAddButtonTooltip(Component addTooltip) {
        this.addTooltip = addTooltip;
        return this;
    }

    public ColorFieldListBuilder setRemoveButtonTooltip(Component removeTooltip) {
        this.removeTooltip = removeTooltip;
        return this;
    }

    public ColorFieldListBuilder requireRestart() {
        super.requireRestart(true);
        return this;
    }

    public ColorFieldListBuilder setCreateNewInstance(Function<ColorListEntry, ColorListEntry.ColorListCell> createNewInstance) {
        this.createNewInstance = createNewInstance;
        return this;
    }

    public ColorFieldListBuilder setExpanded(boolean expanded) {
        this.expanded = expanded;
        return this;
    }

    public ColorFieldListBuilder setSaveConsumer(Consumer<List<String>> saveConsumer) {
        this.saveConsumer = saveConsumer;
        return this;
    }

    public ColorFieldListBuilder setDefaultValue(Supplier<List<String>> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public ColorFieldListBuilder setDefaultValue(List<String> defaultValue) {
        this.defaultValue = () -> defaultValue;
        return this;
    }

    public ColorFieldListBuilder minSize(Integer min) {
        this.min = min;
        return this;
    }

    public ColorFieldListBuilder maxSize(Integer max) {
        this.max = max;
        return this;
    }

    public ColorFieldListBuilder setTooltipSupplier(Function<List<String>, Optional<Component[]>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }

    public ColorFieldListBuilder setTooltip(Supplier<Optional<Component[]>> tooltip) {
        this.tooltipSupplier = (list) -> tooltip.get();
        return this;
    }

    public ColorFieldListBuilder setTooltip(Component... tooltip) {
        this.tooltipSupplier = (list) -> Optional.ofNullable(tooltip);
        return this;
    }

    @Override
    public @NotNull ColorListEntry build() {
        ColorListEntry entry = new ColorListEntry(this.getFieldNameKey(), this.value, this.expanded, null, this.saveConsumer, this.defaultValue, this.getResetButtonKey(), this.isRequireRestart(), this.deleteButtonEnabled, this.insertInFront);

        if (this.createNewInstance != null) {
            entry.setCreateNewInstance(this.createNewInstance);
        }

        entry.setCellErrorSupplier(this.cellErrorSupplier);
        entry.setTooltipSupplier(() -> this.tooltipSupplier.apply(entry.getValue()));
        entry.setAddTooltip(this.addTooltip);
        entry.setRemoveTooltip(this.removeTooltip);
        entry.minSize(this.min);
        entry.maxSize(this.max);

        return entry;
    }
}
