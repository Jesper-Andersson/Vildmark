package com.github.Jesper_Andersson.nordic.client.gui;

import com.github.Jesper_Andersson.nordic.world.level.levelgen.biome.BiomeToggles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Saves and loads named biome on/off presets. Loading edits the toggle screen's pending set, which is only written to the
 * config on Done there. Biomes a preset doesn't mention, and preset ids that aren't known, are left alone.
 */
public class BiomePresetScreen extends Screen {
    private static final SystemToast.SystemToastId TOAST = new SystemToast.SystemToastId();
    private static final int LIST_TOP = 32;
    private static final int FOOTER_HEIGHT = 84;

    private final Screen parent;
    private final Set<String> disabled;
    private final Runnable onLoad;
    private PresetList list;
    private EditBox nameField;
    private Button saveButton;
    private Button loadButton;
    private Button deleteButton;
    private Button doneButton;
    private String name = "";
    @Nullable
    private String selected;

    /** onLoad runs after a preset changed disabled, before returning to parent. */
    public BiomePresetScreen(Screen parent, Set<String> disabled, Runnable onLoad) {
        super(Component.translatable("nordic.configuration.presets.title"));
        this.parent = parent;
        this.disabled = disabled;
        this.onLoad = onLoad;
    }

    @Override
    protected void init() {
        list = addRenderableWidget(new PresetList(minecraft));
        nameField = addRenderableWidget(new EditBox(font, 0, 0, 200, 20, Component.translatable("nordic.configuration.presets.name")));
        nameField.setMaxLength(64);
        nameField.setHint(Component.translatable("nordic.configuration.presets.name"));
        nameField.setValue(name);
        nameField.setResponder(value -> {
            name = value;
            updateButtons();
        });
        saveButton = addRenderableWidget(Button.builder(Component.translatable("nordic.configuration.presets.save"), button -> save()).width(100).build());
        loadButton = addRenderableWidget(Button.builder(Component.translatable("nordic.configuration.presets.load"), button -> load()).width(100).build());
        deleteButton = addRenderableWidget(Button.builder(Component.translatable("nordic.configuration.presets.delete"), button -> delete()).width(100).build());
        doneButton = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).width(200).build());
        refreshList();
        repositionElements();
    }

    @Override
    protected void repositionElements() {
        list.updateSizeAndPosition(width, height - FOOTER_HEIGHT - LIST_TOP, LIST_TOP);
        nameField.setPosition(width / 2 - 100, height - FOOTER_HEIGHT + 6);
        saveButton.setPosition(width / 2 - 155, height - 52);
        loadButton.setPosition(width / 2 - 50, height - 52);
        deleteButton.setPosition(width / 2 + 55, height - 52);
        doneButton.setPosition(width / 2 - 100, height - 28);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private void refreshList() {
        list.replaceEntries(BiomePresets.list().stream().map(PresetEntry::new).toList());
        if (selected != null) {
            list.children().stream().filter(entry -> entry.name.equals(selected)).findFirst()
                    .ifPresentOrElse(list::setSelected, () -> selected = null);
        }
        updateButtons();
    }

    private void updateButtons() {
        saveButton.active = BiomePresets.fileName(name) != null;
        loadButton.active = selected != null;
        deleteButton.active = selected != null;
    }

    private void save() {
        String file = BiomePresets.fileName(name);
        if (file == null) return;
        if (!BiomePresets.exists(file)) {
            write(file);
            return;
        }
        minecraft.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) write(file);
            minecraft.setScreen(this);
        }, title, Component.translatable("nordic.configuration.presets.overwrite.confirm", file)));
    }

    private void write(String file) {
        Map<ResourceLocation, Boolean> values = new HashMap<>();
        for (ResourceLocation id : BiomeToggles.knownBiomes()) values.put(id, !disabled.contains(id.toString()));
        BiomePresets.save(file, values);
        selected = file;
        refreshList();
    }

    private void load() {
        if (selected == null) return;
        Map<ResourceLocation, Boolean> values = BiomePresets.load(selected);
        Set<ResourceLocation> known = BiomeToggles.knownBiomes();
        int applied = 0;
        for (Map.Entry<ResourceLocation, Boolean> entry : values.entrySet()) {
            if (!known.contains(entry.getKey())) continue;
            if (entry.getValue()) disabled.remove(entry.getKey().toString());
            else disabled.add(entry.getKey().toString());
            applied++;
        }
        SystemToast.addOrUpdate(minecraft.getToasts(), TOAST,
                Component.translatable("nordic.configuration.presets.loaded", selected, applied, values.size()), null);
        onLoad.run();
        onClose();
    }

    private void delete() {
        String file = selected;
        if (file == null) return;
        minecraft.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                BiomePresets.delete(file);
                selected = null;
                refreshList();
            }
            minecraft.setScreen(this);
        }, title, Component.translatable("nordic.configuration.presets.delete.confirm", file)));
    }

    private final class PresetList extends ObjectSelectionList<PresetEntry> {
        PresetList(Minecraft minecraft) {
            super(minecraft, 0, 0, 0, 18);
        }

        @Override
        public void replaceEntries(Collection<PresetEntry> entries) {
            super.replaceEntries(entries);
        }

        @Override
        public void setSelected(@Nullable PresetEntry entry) {
            super.setSelected(entry);
            if (entry == null) return;
            selected = entry.name;
            name = entry.name;
            nameField.setValue(entry.name);
            updateButtons();
        }
    }

    private final class PresetEntry extends ObjectSelectionList.Entry<PresetEntry> {
        private final String name;

        PresetEntry(String name) {
            this.name = name;
        }

        @Override
        public Component getNarration() {
            return Component.literal(name);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            guiGraphics.drawCenteredString(font, name, left + width / 2, top + (height - font.lineHeight) / 2, 0xFFFFFF);
        }
    }
}
