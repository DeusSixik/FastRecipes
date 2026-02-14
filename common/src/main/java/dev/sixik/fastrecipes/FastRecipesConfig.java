package dev.sixik.fastrecipes;

import net.shadowking21.shadowconfig.annotation.ConfigComment;

public class FastRecipesConfig {

    @ConfigComment("Allows recipes to be loaded in parallel, independently of each other. If you're experiencing recipe loading issues during your build, disable this.\n")
    public boolean UseAsyncRecipesLoader = true;

    @ConfigComment("Launches the Speed Benchmark when the server starts, namely at the moment of the first tick")
    public boolean BenchmarkMode = false;

    public FastRecipesConfig() {}
}
