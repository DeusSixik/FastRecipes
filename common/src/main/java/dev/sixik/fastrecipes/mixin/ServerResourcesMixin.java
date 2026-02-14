package dev.sixik.fastrecipes.mixin;

import dev.sixik.fastrecipes.FastRecipeManager;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ReloadableServerResources.class, priority = 500)
public class ServerResourcesMixin {

    @Final
    @Shadow
    @Mutable
    private RecipeManager recipes;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(RegistryAccess.Frozen frozen, FeatureFlagSet featureFlagSet, Commands.CommandSelection commandSelection, int i, CallbackInfo ci) {
        this.recipes = new FastRecipeManager();
    }
}
