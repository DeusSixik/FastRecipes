package dev.sixik.fastrecipes.mixin;

import dev.sixik.fastrecipes.FastRecipeManager;
import dev.sixik.fastrecipes.FastRecipes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow
    public abstract RecipeManager getRecipeManager();

    private static boolean loaded = false;

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;tickServer(Ljava/util/function/BooleanSupplier;)V"))
    public void onStart(CallbackInfo ci) {
        if(!loaded) {
            if(FastRecipes.getConfig().getCurrentConfig().BenchmarkMode)
                FastRecipes.test((MinecraftServer)(Object) this);
            loaded = true;
        }
    }
}
