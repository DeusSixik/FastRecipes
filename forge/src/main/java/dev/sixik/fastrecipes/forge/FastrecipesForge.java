package dev.sixik.fastrecipes.forge;

import dev.sixik.fastrecipes.FastRecipes;
import net.minecraftforge.fml.common.Mod;

@Mod(FastRecipes.MOD_ID)
public final class FastrecipesForge {
    public FastrecipesForge() {
        // Run our common setup.
        FastRecipes.init();
    }
}
