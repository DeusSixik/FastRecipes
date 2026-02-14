package dev.sixik.fastrecipes.mixin;

import dev.sixik.fastrecipes.FastRecipes;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    private static boolean loaded = false;

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;tickServer(Ljava/util/function/BooleanSupplier;)V", shift = At.Shift.BEFORE))
    public void onStart(CallbackInfo ci) {
        if(!loaded) {
            if(FastRecipes.BaseConfig.getCurrentConfig().BenchmarkMode)
                FastRecipes.test((MinecraftServer)(Object) this);
            loaded = true;
        }
    }
}
