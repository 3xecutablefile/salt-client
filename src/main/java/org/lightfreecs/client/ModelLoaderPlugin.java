/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.util.Identifier;

@Environment(value = EnvType.CLIENT)
public class ModelLoaderPlugin implements ModelLoadingPlugin {
    public void initialize(ModelLoadingPlugin.Context context) {
        for (String cosmetic : new String[]{"gold", "iron", "wood", "netherite"}) {
            context.addModels(Identifier.of("mcpvp", "item/" + cosmetic + "_sword_cosmetic"));
        }
    }
}
