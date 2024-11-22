package xyz.volcanobay.createstressdebug;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class CSDRenderTypes extends RenderType {
    private static final RenderType SILLY_DEBUG_FILLED_BOX;

    static {
        SILLY_DEBUG_FILLED_BOX =
                create("silly_debug_filled_box",
                        DefaultVertexFormat.POSITION_COLOR,
                        VertexFormat.Mode.TRIANGLE_STRIP,
                        131072,
                        false,
                        true,
                        RenderType.CompositeState.builder()
                                .setShaderState(POSITION_COLOR_SHADER)
                                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                                .setDepthTestState(NO_DEPTH_TEST)
                                .createCompositeState(false));
    }

    public CSDRenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
        super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
    }

    public static RenderType sillyDebugFilledBox() {
        return SILLY_DEBUG_FILLED_BOX;
    }
}
