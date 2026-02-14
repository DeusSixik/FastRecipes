# FastRecipes

**FastRecipes** is a performance mod that drastically optimizes Minecraft's recipe system by replacing efficient lookups with intelligent caching and indexing.

You may have heard of [FastSuite](https://www.curseforge.com/minecraft/mc-mods/fastsuite), which optimizes recipe lookups using multithreading. While FastSuite tries to brute-force the problem by using more CPU cores, **FastRecipes** solves the root cause by fixing the algorithm itself.

### The Difference: FastSuite vs FastRecipes

| Feature       | FastSuite                               | FastRecipes                  |
|:--------------|:----------------------------------------|:-----------------------------|
| **Approach**  | Multithreading (Parallel Search)        | **Smart Indexing & Caching** |
| **Algorithm** | Linear Search $O(N)$ / Threads          | **Constant Lookup $O(1)$**   |
| **Overhead**  | High (Thread synchronization & locking) | **Zero** (Instant lookup)    |
| **RAM Usage** | Low                                     | Low to Moderate (+10-20 MB)  |

> [!IMPORTANT]
> While FastSuite uses multithreading to mitigate the slow linear search, FastRecipes eliminates the search overhead entirely through smart indexing.

---

## Performance Benchmarks

The tests were conducted on a high-end machine (Threadripper 1950x) using the **All The Mods 9** modpack, which contains over 100,000 recipes.

> [!NOTE]
> Time is shown in **milliseconds (ms)**. Lower is better.
> One game tick is 50ms. If a lookup takes >50ms, the server lags.

### ATM 9 Test (Heavy Load)
This is where FastRecipes shines. In the "Failed Match" scenario (common during gameplay), FastRecipes is **~300x faster** than Vanilla and **~22x faster** than FastSuite.

| Test Case         | FastSuite | FastRecipes   | Vanilla    | Improvement (vs Vanilla) |
|:------------------|:----------|:--------------|:-----------|:-------------------------|
| **Simple Item**   | 0.6550 ms | **0.1358 ms** | 15.9896 ms | **~117x**                |
| **Double Item**   | 0.9431 ms | **0.0811 ms** | 22.4544 ms | **~276x**                |
| **Full Grid**     | 0.5537 ms | **0.0944 ms** | 15.6336 ms | **~165x**                |
| **Complex NBT**   | 1.0606 ms | **0.0296 ms** | 22.9309 ms | **~774x**                |
| **Failed Match*** | 2.9304 ms | **0.1294 ms** | 40.5612 ms | **~313x**                |

*Failed Match: Searching for a recipe that doesn't exist. This is usually the most expensive operation in Minecraft.*

### Vanilla Test (Light Load)
Even with few recipes, the overhead of FastSuite's multithreading makes it slower than FastRecipes' instant lookup.

| Test Case        | FastSuite | FastRecipes   | Vanilla   |
|:-----------------|:----------|:--------------|:----------|
| **Simple Item**  | 0.1027 ms | **0.0045 ms** | 0.0810 ms |
| **Double Item**  | 0.1033 ms | **0.0038 ms** | 0.1566 ms |
| **Full Grid**    | 0.0821 ms | **0.0008 ms** | 0.0697 ms |
| **Complex NBT**  | 0.1036 ms | **0.0008 ms** | 0.1449 ms |
| **Failed Match** | 0.0952 ms | **0.0011 ms** | 0.1482 ms |

---

## Verify Results on Your Hardware
Performance results can vary depending on your CPU architecture and modpack. We encourage you to run your own benchmarks!

### How to run the benchmark:
1. Open the config file: `config/fastrecipes-common.toml`.
2. Set `BenchmarkMode` to `true`.
3. Start your server (or single-player world).

### What the test does:
The benchmark executes automatically during the **first server tick**. It simulates 5 distinct crafting scenarios 10,000 times each to ensure statistical accuracy. The results will be printed directly to your server console/logs.

> [!TIP]
> Don't forget to set `BenchmarkMode` back to `false` after testing to avoid unnecessary processing during startup.

---

## Compatibility & Configuration

We prioritize stability. Since we don't use forced synchronization or complex thread locks during gameplay, **FastRecipes is compatible with virtually every mod.**

### Troubleshooting
If you encounter a crash during the "Loading Recipes" stage (usually caused by other mods having non-thread-safe recipe serializers), you can disable the parallel loader:

1. Open `fastrecipes-common.toml`.
2. Set `UseAsyncRecipesLoader` to `false`.

> [!NOTE]
> This only affects the startup speed the in-game recipe search will remain ultra-fast.

---

### Credits & Attribution
The benchmark methodology and test scenarios used in these comparisons were adapted from the [FastSuite](https://github.com/Shadows-of-Fire/FastSuite) project to ensure a fair and direct comparison.