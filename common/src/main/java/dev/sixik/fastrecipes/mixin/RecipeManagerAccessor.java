package dev.sixik.fastrecipes.mixin;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {

    @Accessor
    Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> getRecipes();

    @Accessor
    Gson getGSON();

    @Accessor
    Logger getLOGGER();

    @Accessor
    void setHasErrors(boolean value);

    @Accessor
    void setByName(Map<ResourceLocation, Recipe<?>> byName);

    @Accessor
    void setRecipes(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes);
}
