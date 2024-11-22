package xyz.volcanobay.createstressdebug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import xyz.volcanobay.createstressdebug.mixin.TorquePropagatorMixin;
import xyz.volcanobay.createstressdebug.packets.DebugDataS2C;
import xyz.volcanobay.createstressdebug.packets.StartDebuggingC2S;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mod(Createstressdebug.MODID)
public class Createstressdebug {
    public static HashMap<ServerPlayer, BlockPos> playerDebugging = new HashMap<>();
    public static List<BlockPos> posList = new ArrayList<>();
    private static long kineticId;
    // Define mod id in a common place for everything to reference
    public static final String MODID = "createstressdebug";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    private static float alpha = 0.5f;


    public Createstressdebug() {
        //noinspection removal
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        CSDConfig.registerClientConfigs();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(Createstressdebug::onServerTick);
    }


    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        event.enqueueWork(() -> {
            Messages.register();
        });
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
    }

    public static void onServerTick(TickEvent.LevelTickEvent event) {
        Level level = event.level;
        List<ServerPlayer> playersForRemoval = new ArrayList<>();
        if (level instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : playerDebugging.keySet()) {
                if (player.level() == level) {
                    BlockPos pos = playerDebugging.get(player);
                    BlockEntity be = serverLevel.getBlockEntity(pos);
                    if (be instanceof KineticBlockEntity kbe) {
                        KineticNetwork kineticNetwork1 = ((TorquePropagatorMixin) (Create.TORQUE_PROPAGATOR)).getNetworks().get(level).get(kbe.network);
                        if (kineticNetwork1 != null) {
                            List<BlockPos> posList = new ArrayList<>();
                            for (KineticBlockEntity kbe1 : kineticNetwork1.members.keySet()) {
                                posList.add(kbe1.getBlockPos());
                            }
                            for (KineticBlockEntity kbe1 : kineticNetwork1.sources.keySet()) {
                                posList.add(kbe1.getBlockPos());
                            }
                            Messages.sendToPlayer(new DebugDataS2C(posList), player);
                        }
                    }
                } else if (!player.isAddedToWorld())
                    playersForRemoval.add(player);
            }
        }
        for (ServerPlayer player : playersForRemoval)
            playerDebugging.remove(player);
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = Createstressdebug.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(Keybindings.DEBUG);
        }
    }

    @Mod.EventBusSubscriber(Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            Double alphaGoal = CSDConfig.SELECTED_ALPHA.get();
            if (event.phase == TickEvent.Phase.END) { // Only call code once as the tick event is called twice every tick
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    if (Keybindings.DEBUG.consumeClick()) {
                        Vec3 endPos = (player.getEyePosition().add(player.getEyePosition().add(player.getForward().subtract(player.getEyePosition())).scale(10)));
                        BlockHitResult hit = player.level().clip(new ClipContext(player.getEyePosition(), endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                        if (hit.getType() == HitResult.Type.BLOCK) {
                            BlockEntity be = player.level().getBlockEntity(hit.getBlockPos());
                            if (be instanceof KineticBlockEntity kineticBlockEntity) {
                                Messages.sendToServer(new StartDebuggingC2S(kineticBlockEntity.getBlockPos()));
                                if (kineticBlockEntity.network != null)
                                    kineticId = kineticBlockEntity.network;
                                LOGGER.info("Debuging Nework!");
                            }
                        } else {
                            LOGGER.info("Stopping Debug!");
                            kineticId = 0;
                        }
                        // Execute logic to perform on click here
                    }
                    if (kineticId != 0) {
                        Vec3 endPos = (player.getEyePosition().add(player.getEyePosition().add(player.getForward().subtract(player.getEyePosition())).scale(10)));
                        BlockHitResult hit = player.level().clip(new ClipContext(player.getEyePosition(), endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                        if (hit.getType() == HitResult.Type.BLOCK) {
                            BlockEntity be = player.level().getBlockEntity(hit.getBlockPos());
                            if (be instanceof KineticBlockEntity kineticBlockEntity) {
                                if (kineticBlockEntity.network != null && kineticBlockEntity.network == kineticId)
                                    alphaGoal = CSDConfig.HOVER_ALPHA.get();
                            }
                        }
                    } else {
                        alphaGoal = 0d;
                    }
                }
            }
            alpha = (float) (alpha + ((alphaGoal - alpha) / 1.5d));
        }
        public static boolean voxelOverride(BlockState state) {
            return state.is(AllBlocks.BELT.get());
        }

        @SubscribeEvent
        public static void renderLevelLastEvent(RenderLevelStageEvent event) {
            if (kineticId != 0) {
                if (!posList.isEmpty() && event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
                    Level level = Minecraft.getInstance().level;
                    PoseStack ps = event.getPoseStack();
                    ps.pushPose();
                    Vec3 translation = event.getCamera().getPosition();
                    ps.translate(-translation.x, -translation.y, -translation.z);
                    RenderSystem.disableDepthTest();
                    MultiBufferSource.BufferSource multiBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                    float width = 0.51f;
                    VertexConsumer buffer = multiBufferSource.getBuffer(CSDRenderTypes.sillyDebugFilledBox());
                    for (BlockPos blockPos : posList) {
                        Vec3 pos = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                        VoxelShape shape = voxelOverride(level.getBlockState(blockPos)) && CSDConfig.OPTIMISE_VOXELS.get() ? Shapes.block() : level.getBlockState(blockPos).getShape(level, blockPos);
                        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
                            LevelRenderer.addChainedFilledBoxVertices(ps, buffer, pos.x + x1, pos.y + y1, pos.z + z1, pos.x + x2, pos.y + y2, pos.z + z2, CSDConfig.COLOR_RED.get()/255f, CSDConfig.COLOR_BLUE.get()/255f, CSDConfig.COLOR_GREEN.get()/255f, alpha);
                        });
                    }
                    ps.popPose();
                    multiBufferSource.endBatch(CSDRenderTypes.sillyDebugFilledBox());
                    RenderSystem.enableDepthTest();
                }
            }
        }
    }
}
