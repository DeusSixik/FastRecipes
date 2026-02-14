package dev.sixik.fastrecipes.mixin;


import com.mojang.authlib.GameProfile;
import dev.sixik.fastrecipes.FastRecipeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.network.Connection;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Shadow
    @Final
    @Mutable
    private RecipeManager recipeManager;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(Minecraft minecraft, Screen screen, Connection connection, ServerData serverData, GameProfile gameProfile, WorldSessionTelemetryManager worldSessionTelemetryManager, CallbackInfo ci) {
        this.recipeManager = new FastRecipeManager();
    }
}
