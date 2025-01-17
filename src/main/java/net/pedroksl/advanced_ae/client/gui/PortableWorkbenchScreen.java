package net.pedroksl.advanced_ae.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.pedroksl.advanced_ae.gui.PortableWorkbenchMenu;

import appeng.api.config.ActionItems;
import appeng.api.config.CopyMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.Icon;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;

public class PortableWorkbenchScreen extends UpgradeableScreen<PortableWorkbenchMenu> {

    private final ToggleButton copyMode;

    private final SettingToggleButton<FuzzyMode> fuzzyMode;

    public PortableWorkbenchScreen(
            PortableWorkbenchMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.fuzzyMode = addToLeftToolbar(
                new SettingToggleButton<>(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL, this::toggleFuzzyMode));
        this.addToLeftToolbar(new ActionButton(ActionItems.COG, act -> menu.partition()));
        this.addToLeftToolbar(new ActionButton(ActionItems.CLOSE, act -> menu.clear()));
        this.copyMode = this.addToLeftToolbar(new ToggleButton(
                Icon.COPY_MODE_ON,
                Icon.COPY_MODE_OFF,
                GuiText.CopyMode.text(),
                GuiText.CopyModeDesc.text(),
                act -> menu.nextWorkBenchCopyMode()));
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.copyMode.setState(this.menu.getCopyMode() == CopyMode.CLEAR_ON_REMOVE);

        boolean hasFuzzy = menu.getCachedUpgrades().isInstalled(AEItems.FUZZY_CARD);
        this.fuzzyMode.set(menu.getFuzzyMode());
        this.fuzzyMode.setVisibility(hasFuzzy);
    }

    private void toggleFuzzyMode(SettingToggleButton<FuzzyMode> button, boolean backwards) {
        var fz = button.getNextValue(backwards);
        menu.setCellFuzzyMode(fz);
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        var cell = getMenu().getWorkbenchItem();
        if (cell.isEmpty()) {
            return super.getTooltipFromContainerItem(stack);
        }

        // Don't show the tooltip for the cell itself
        if (cell == stack) {
            return super.getTooltipFromContainerItem(stack);
        }

        AEKey what;
        var genericStack = GenericStack.unwrapItemStack(stack);
        if (genericStack != null) {
            what = genericStack.what();
        } else {
            what = AEItemKey.of(stack);
        }

        if (what == null) {
            return super.getTooltipFromContainerItem(stack);
        }

        // Is it allowed in any slot?
        var configInventory = getMenu().getHost().getCell().getConfigInventory(cell);

        // Check if the type is supported at all
        if (!configInventory.isSupportedType(what.getType())) {
            var lines = new ArrayList<>(super.getTooltipFromContainerItem(stack));
            lines.add(GuiText.IncompatibleWithCell.text().withStyle(ChatFormatting.RED));
            return lines;
        }

        var filter = configInventory.getFilter();
        if (filter != null) {
            boolean anySlotMatches = false;
            for (int i = 0; i < configInventory.size(); i++) {
                if (configInventory.isAllowedIn(i, what)) {
                    anySlotMatches = true;
                    break;
                }
            }

            if (!anySlotMatches) {
                var lines = new ArrayList<>(super.getTooltipFromContainerItem(stack));
                lines.add(GuiText.IncompatibleWithCell.text().withStyle(ChatFormatting.RED));
                return lines;
            }
        }

        return super.getTooltipFromContainerItem(stack);
    }
}
