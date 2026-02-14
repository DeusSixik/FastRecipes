package dev.sixik.fastrecipes;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import dev.sixik.fastrecipes.mixin.RecipeManagerAccessor;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Unique;

import java.io.Reader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class FastRecipeManager extends RecipeManager {

    private final Map<RecipeType<?>, Map<Item, List<Recipe<?>>>> fastRecipeIndex = new IdentityHashMap<>();

    private final Map<RecipeType<?>, List<Recipe<?>>> fallbackRecipes = new IdentityHashMap<>();

    @Override
    public <C extends Container, T extends Recipe<C>> List<T> getRecipesFor(RecipeType<T> recipeType, C container, Level level) {
        final List<T> outList = new ArrayList<>();

        for (T recipe : this.byType(recipeType).values()) {
            if(recipe.matches(container, level)) {
                outList.add(recipe);
            }
        }

        outList.sort(Comparator.comparing((recipe) -> recipe.getResultItem(level.registryAccess()).getDescriptionId()));
        return outList;
    }

    @Override
    public <C extends Container, T extends Recipe<C>> Optional<Pair<ResourceLocation, T>> getRecipeFor(RecipeType<T> recipeType, C container, Level level, @Nullable ResourceLocation resourceLocation) {
        final Map<ResourceLocation, T> map = this.byType(recipeType);
        if (resourceLocation != null) {
            T recipe = map.get(resourceLocation);
            if (recipe != null && recipe.matches(container, level)) {
                return Optional.of(Pair.of(resourceLocation, recipe));
            }
        }

        for (Map.Entry<ResourceLocation, T> entry : map.entrySet()) {
            final T recipe = entry.getValue();

            if(recipe.matches(container, level)) {
                return Optional.of(Pair.of(entry.getKey(), recipe));
            }
        }

        return Optional.empty();
    }

    public <C extends Container, T extends Recipe<C>> @NotNull Optional<T> super_getRecipeFor(RecipeType<T> recipeType, C container, Level level)
    {
        return super.getRecipeFor(recipeType, container, level);
    }

    @Override
    public <C extends Container, T extends Recipe<C>> @NotNull Optional<T> getRecipeFor(RecipeType<T> recipeType, C container, Level level) {
        if (container.isEmpty()) return Optional.empty();

        final Map<Item, List<Recipe<?>>> index = fastRecipeIndex.get(recipeType);

        if (index != null && !container.isEmpty()) {
            Item keyItem = Items.AIR;
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (!stack.isEmpty()) {
                    keyItem = stack.getItem();
                    break;
                }
            }

            if (keyItem != Items.AIR) {
                final List<Recipe<?>> candidates = index.get(keyItem);

                if (candidates != null) {
                    for (int i = 0; i < candidates.size(); i++) {
                        final Recipe<?> recipe = candidates.get(i);
                        if (((Recipe<C>) recipe).matches(container, level)) {
                            return Optional.of((T) recipe);
                        }
                    }
                }
            }
        }

        /*
            Если не нашли в индексе или контейнер пуст, проверяем Fallback (рецепты без предметов)
         */
        final List<Recipe<?>> fallbacks = fallbackRecipes.get(recipeType);
        if (fallbacks != null) {
            for (int i = 0; i < fallbacks.size(); i++) {
                final Recipe<?> recipe = fallbacks.get(i);
                if (((Recipe<C>) recipe).matches(container, level)) {
                    return Optional.of((T) recipe);
                }
            }
        }

        return Optional.empty();
    }

    @Unique
    private void rebuildIndex() {
        fastRecipeIndex.clear();
        fallbackRecipes.clear();

        for (Recipe<?> recipe : getRecipes()) {
            indexRecipe(recipe);
        }

        /*
             Для оптимизации памяти обрезаем списки до реальных размеров
         */
        fastRecipeIndex.values().forEach(map ->
                map.values().forEach(list -> ((ArrayList<?>)list).trimToSize())
        );
    }

    private void indexRecipe(Recipe<?> recipe) {
        final NonNullList<Ingredient> ingredients = recipe.getIngredients();

        if (ingredients.isEmpty()) {
            addToFallback(recipe);
            return;
        }

        Ingredient keyIngredient = null;
        for (int i = 0; i < ingredients.size(); i++) {
            final Ingredient ing = ingredients.get(i);
            if (!ing.isEmpty() && ing.getItems().length > 0) {
                keyIngredient = ing;
                break;
            }
        }

        if (keyIngredient == null || keyIngredient.isEmpty()) {
            addToFallback(recipe);
            return;
        }

        final ItemStack[] matchingStacks = keyIngredient.getItems();

        if (matchingStacks.length == 0) {
            addToFallback(recipe);
            return;
        }

        final Map<Item, List<Recipe<?>>> typeIndex = fastRecipeIndex
                .computeIfAbsent(recipe.getType(), k -> new IdentityHashMap<>());

        for (int i = 0; i < matchingStacks.length; i++) {
            typeIndex.computeIfAbsent(matchingStacks[i].getItem(), k -> new ArrayList<>()).add(recipe);
        }
    }

    private void addToFallback(Recipe<?> recipe) {
        fallbackRecipes.computeIfAbsent(recipe.getType(), k -> new ArrayList<>()).add(recipe);
    }

    private <C extends Container, T extends Recipe<C>> Map<ResourceLocation, T> byType(RecipeType<T> recipeType) {
        return (Map)((RecipeManagerAccessor)this).getRecipes().getOrDefault(recipeType, Collections.emptyMap());
    }

//    private <CONTAINER extends Container> boolean isCachedRecipe(RecipeType<?> recipeType, CONTAINER container, Level level) {
//        return lastRecipe != null && lastRecipe.getType() == recipeType && ((Recipe<CONTAINER>)lastRecipe).matches(container, level);
//    }


    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        if(!FastRecipes.getConfig().getCurrentConfig().UseAsyncRecipesLoader)
            return super.prepare(resourceManager, profiler);

        final Map<ResourceLocation, JsonElement> map = new ConcurrentHashMap<>();

        FileToIdConverter converter = FileToIdConverter.json("recipes");
        final Map<ResourceLocation, Resource> resources = converter.listMatchingResources(resourceManager);

        final List<CompletableFuture<Void>> futures = new ArrayList<>(resources.size());

        final Gson gson = ((RecipeManagerAccessor)this).getGSON();
        final Logger logger = ((RecipeManagerAccessor)this).getLOGGER();

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            final ResourceLocation fullLocation = entry.getKey();
            final Resource resource = entry.getValue();

            futures.add(CompletableFuture.runAsync(() -> {
                final ResourceLocation id = converter.fileToId(fullLocation);
                try (Reader reader = resource.openAsReader()) {
                    final JsonElement json = GsonHelper.fromJson(gson, reader, JsonElement.class);
                    final JsonElement existing = map.put(id, json);
                    if (existing != null) {
                        throw new IllegalStateException("Duplicate data file ignored with ID " + id);
                    }
                } catch (Exception e) {
                    logger.error("Couldn't parse data file {} from {}", id, fullLocation, e);
                }
            }));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return map;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        super.apply(map, resourceManager, profiler);
        rebuildIndex();
    }
}
