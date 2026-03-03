package com.saltclient.module.impl.movement;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.KeySetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public final class ElytraSwapModule extends Module {
    private final KeySetting bindKey;

    private static final KeyBinding SWAP_KEY = new KeyBinding(
        "key.elytraswap",
        GLFW.GLFW_KEY_GRAVE_ACCENT,
        "category.saltclient.movement"
    );

    public ElytraSwapModule() {
        super("elytraswap", "ElytraSwap", "Quick swap between elytra and chestplate.", ModuleCategory.MOVEMENT, true);
        this.bindKey = addSetting(new KeySetting("key", "Keybind", "Key to swap elytra/chestplate.", GLFW.GLFW_KEY_GRAVE_ACCENT));
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.currentScreen != null) return;
        
        if (SWAP_KEY.isPressed()) {
            swapChestplate(mc);
        }
    }

    private void swapChestplate(MinecraftClient client) {
        if (client.player == null || !(client.player instanceof ClientPlayerEntity) || client.player.isDead()) {
            return;
        }

        int elytraSlot = -1;
        int chestplateSlot = -1;

        int HOTBAR_SIZE = PlayerInventory.getHotbarSize();
        int MAIN_SIZE = PlayerInventory.MAIN_SIZE;
        int TOTAL_SIZE = MAIN_SIZE + 1;

        int[] range = new int[TOTAL_SIZE];

        for (int i = 0; i < MAIN_SIZE - HOTBAR_SIZE; i++) {
            range[i] = i + HOTBAR_SIZE;
        }

        range[MAIN_SIZE - HOTBAR_SIZE] = PlayerInventory.OFF_HAND_SLOT;

        for (int i = 0; i < HOTBAR_SIZE; i++) {
            range[i + MAIN_SIZE - HOTBAR_SIZE + 1] = i;
        }

        for (int slot : range) {
            ItemStack stack = client.player.getInventory().getStack(slot);
            if (!stack.isEmpty()) {
                if (isElytra(stack) && elytraSlot < 0) {
                    elytraSlot = slot;
                } else if (isChestplate(stack) && chestplateSlot < 0) {
                    chestplateSlot = slot;
                }
            }
        }

        ItemStack wornItemStack = client.player.getInventory().getStack(38);
        
        if (wornItemStack.isEmpty() && elytraSlot >= 0) {
            sendSwapPackets(elytraSlot, client);
        }
        else if (isElytra(wornItemStack) && chestplateSlot >= 0) {
            sendSwapPackets(chestplateSlot, client);
        }
        else if (isChestplate(wornItemStack) && elytraSlot >= 0) {
            sendSwapPackets(elytraSlot, client);
        }
    }

    private boolean isElytra(ItemStack stack) {
        return stack.isOf(Items.ELYTRA);
    }

    private boolean isChestplate(ItemStack stack) {
        return stack.isOf(Items.LEATHER_CHESTPLATE) ||
               stack.isOf(Items.IRON_CHESTPLATE) ||
               stack.isOf(Items.GOLDEN_CHESTPLATE) ||
               stack.isOf(Items.DIAMOND_CHESTPLATE) ||
               stack.isOf(Items.NETHERITE_CHESTPLATE) ||
               stack.isOf(Items.CHAINMAIL_CHESTPLATE);
    }

    private void sendSwapPackets(int slot, MinecraftClient client) {
        int sentSlot = slot;
        
        if (sentSlot == PlayerInventory.OFF_HAND_SLOT) {
            sentSlot += 5;
        }
        if (sentSlot < PlayerInventory.getHotbarSize()) {
            sentSlot += PlayerInventory.MAIN_SIZE;
        }

        client.interactionManager.clickSlot(0, sentSlot, 0, SlotActionType.PICKUP, client.player);
        client.interactionManager.clickSlot(0, 6, 0, SlotActionType.PICKUP, client.player);
        client.interactionManager.clickSlot(0, sentSlot, 0, SlotActionType.PICKUP, client.player);
    }
}
