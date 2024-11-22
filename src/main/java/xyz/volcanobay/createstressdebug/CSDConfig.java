package xyz.volcanobay.createstressdebug;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class CSDConfig {
    public static void registerClientConfigs() {
        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
        registerClientConfig(CLIENT_BUILDER);
        //noinspection removal
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_BUILDER.build());
    }
    public static ForgeConfigSpec.IntValue COLOR_RED;
    public static ForgeConfigSpec.IntValue COLOR_BLUE;
    public static ForgeConfigSpec.IntValue COLOR_GREEN;
    public static ForgeConfigSpec.DoubleValue SELECTED_ALPHA;
    public static ForgeConfigSpec.DoubleValue HOVER_ALPHA;
    public static ForgeConfigSpec.BooleanValue OPTIMISE_VOXELS;

    public static void registerClientConfig(ForgeConfigSpec.Builder CLIENT_BUILDER) {
        CLIENT_BUILDER.comment("Color in RGB of the overlay");
        COLOR_RED = CLIENT_BUILDER.defineInRange("color_red",200,0,256);
        COLOR_BLUE = CLIENT_BUILDER.defineInRange("color_blue",100,0,256);
        COLOR_GREEN = CLIENT_BUILDER.defineInRange("color_green",100,0,256);
        CLIENT_BUILDER.comment("Alpha values for a network and when it is hovered.");
        SELECTED_ALPHA = CLIENT_BUILDER.defineInRange("selected_alpha",0.5,0,1);
        HOVER_ALPHA = CLIENT_BUILDER.defineInRange("hover_alpha",0.01,0,1);
        CLIENT_BUILDER.comment("Whether or not to optimise the voxels of some blocks like belts");
        OPTIMISE_VOXELS = CLIENT_BUILDER.define("optimise_voxel",true);
    }
}
