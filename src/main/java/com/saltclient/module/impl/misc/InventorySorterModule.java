package com.saltclient.module.impl.misc;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.KeySetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public final class InventorySorterModule extends Module {
    private final KeySetting sortKey;

    public InventorySorterModule() {
        super("inventorysorter", "InventorySorter", "Sort your inventory with a keybind.", ModuleCategory.MISC, true);
        this.sortKey = addSetting(new KeySetting("sortKey", "Sort Key", "Key to sort inventory.", GLFW.GLFW_KEY_R));
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.currentScreen != null) return;
        if (!mc.options.dropKey.isPressed()) return;

        sortInventory(mc);
    }

    private void sortInventory(MinecraftClient mc) {
        if (mc.player == null) return;
        if (mc.interactionManager == null) return;

        PlayerInventory inv = mc.player.getInventory();
        ItemStack[] mainInv = new ItemStack[PlayerInventory.MAIN_SIZE];

        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
            mainInv[i] = inv.getStack(i);
        }

        for (int i = 0; i < mainInv.length - 1; i++) {
            for (int j = i + 1; j < mainInv.length; j++) {
                if (mainInv[i].isEmpty() && !mainInv[j].isEmpty()) {
                    ItemStack temp = mainInv[i];
                    mainInv[i] = mainInv[j];
                    mainInv[j] = temp;
                } else if (!mainInv[i].isEmpty() && !mainInv[j].isEmpty()) {
                    if (compareItems(mainInv[j], mainInv[i]) < 0) {
                        ItemStack temp = mainInv[i];
                        mainInv[i] = mainInv[j];
                        mainInv[j] = temp;
                    }
                }
            }
        }

        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
            inv.setStack(i, mainInv[i]);
        }
    }

    private int compareItems(ItemStack a, ItemStack b) {
        Item itemA = a.getItem();
        Item itemB = b.getItem();
        int idA = Item.getRawId(itemA);
        int idB = Item.getRawId(itemB);
        
        if (idA != idB) {
            return Integer.compare(idA, idB);
        }
        
        return Integer.compare(a.getDamage(), b.getDamage());
    }
}
