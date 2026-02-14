package dev.sixik.fastrecipes;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.shadowking21.shadowconfig.config.BaseShadowConfig;
import net.shadowking21.shadowconfig.config.ConfigSide;
import net.shadowking21.shadowconfig.config.exstensions.toml.SCTomlConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class FastRecipes {

    public static final String MOD_ID = "fastrecipes";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static BaseShadowConfig<FastRecipesConfig> BaseConfig;

    public static void init() {

    }

    public static BaseShadowConfig<FastRecipesConfig> getConfig() {
        if(BaseConfig == null) {
            BaseConfig = SCTomlConfig.Builder.builder(FastRecipesConfig.class)
                    .defaults(new FastRecipesConfig())
                    .modId(FastRecipes.MOD_ID)
                    .side(ConfigSide.COMMON)
                    .build();
        }
        return BaseConfig;
    }


    private static class TestMenu extends AbstractContainerMenu {

        protected TestMenu() {
            super(null, -1);
        }

        @Override
        public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player pPlayer) {
            return true;
        }

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void test(MinecraftServer server) {
        LOGGER.info("Initiating FastSuite Tests...");
        FastRecipeManager mgr = (FastRecipeManager) server.getRecipeManager();
        CraftingContainer inv = new TransientCraftingContainer(new TestMenu(), 2, 2);
        Level world = server.overworld();
        inv.setItem(0, new ItemStack(Items.ACACIA_LOG));

        CraftingContainer inv2 = new TransientCraftingContainer(new TestMenu(), 2, 2);
        inv2.setItem(0, new ItemStack(Items.BIRCH_PLANKS));
        inv2.setItem(2, new ItemStack(Items.BIRCH_PLANKS));

        CraftingContainer inv3 = new TransientCraftingContainer(new TestMenu(), 2, 2);
        for (int i = 0; i < 4; i++)
            inv3.setItem(i, new ItemStack(Items.OAK_PLANKS));

        CraftingContainer inv4 = new TransientCraftingContainer(new TestMenu(), 2, 2);
        inv4.setItem(0, new ItemStack(Items.SHULKER_BOX));
        inv4.setItem(3, new ItemStack(Items.BLACK_DYE));

        CraftingContainer inv5 = new TransientCraftingContainer(new TestMenu(), 3, 3);
        inv5.setItem(0, new ItemStack(Items.STICK));
        inv5.setItem(1, new ItemStack(Items.STICKY_PISTON));
        inv5.setItem(2, new ItemStack(Items.ACACIA_FENCE));
        inv5.setItem(3, new ItemStack(Items.ACACIA_LEAVES));
        inv5.setItem(4, new ItemStack(Items.APPLE));
        inv5.setItem(5, new ItemStack(Items.BEEHIVE));
        inv5.setItem(6, new ItemStack(Items.BEE_NEST));
        inv5.setItem(7, new ItemStack(Items.BLACK_DYE));
        inv5.setItem(8, new ItemStack(Items.SHULKER_BOX));

        CraftingContainer[] arr = { inv, inv2, inv3, inv4, inv5 };
        String[] names = { "acacia planks", "sticks", "crafting table", "black shulker box", "failed match" };

        for (int testCase = 0; testCase < names.length; testCase++) {
            testMulti(mgr, world, arr[testCase], names[testCase]);
            testSingle(mgr, world, arr[testCase], names[testCase]);
        }
    }

    private static void testMulti(FastRecipeManager mgr, Level level, CraftingContainer input, String recipeName) {
        long time, time2;
        long deltaSum = 0;
        int iterations = 10000;
        for (int i = 0; i < iterations; i++) {
            time = System.nanoTime();
            mgr.getRecipeFor(RecipeType.CRAFTING, input, level);
            time2 = System.nanoTime();
            deltaSum += time2 - time;
        }
        LOGGER.info("[Custom Test] - Took an average of {} ns to find the recipe for {}", deltaSum / (float) iterations, recipeName);
    }

    private static void testSingle(FastRecipeManager mgr, Level level, CraftingContainer input, String recipeName) {
        long time, time2;
        long deltaSum = 0;
        int iterations = 10000;
        for (int i = 0; i < iterations; i++) {
            time = System.nanoTime();
            mgr.super_getRecipeFor(RecipeType.CRAFTING, input, level);
            time2 = System.nanoTime();
            deltaSum += time2 - time;
        }
        LOGGER.info("[Vanilla Test] - Took an average of {} ns to find the recipe for {}", deltaSum / (float) iterations, recipeName);
    }
}
