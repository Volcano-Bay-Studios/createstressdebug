package xyz.volcanobay.createstressdebug.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.volcanobay.createstressdebug.Createstressdebug;

import java.util.function.Supplier;

public class StartDebuggingC2S {
    BlockPos pos;

    public StartDebuggingC2S(BlockPos pos) {
        this.pos = pos;
    }

    public StartDebuggingC2S(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Createstressdebug.playerDebugging.put(context.getSender(),pos);
        });
        return true;
    }
}
