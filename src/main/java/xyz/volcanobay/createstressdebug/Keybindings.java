package xyz.volcanobay.createstressdebug;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;

public class Keybindings {

    public static final String CATEGORY = "key.categories."+ Createstressdebug.MODID;
    public static final KeyMapping DEBUG = new KeyMapping(
            "key." + Createstressdebug.MODID + ".debug",
            KeyConflictContext.IN_GAME,
            InputConstants.getKey(InputConstants.KEY_B, -1),
            CATEGORY
    );
}
