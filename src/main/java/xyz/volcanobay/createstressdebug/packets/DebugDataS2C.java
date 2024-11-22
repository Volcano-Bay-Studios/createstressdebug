package xyz.volcanobay.createstressdebug.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.volcanobay.createstressdebug.Createstressdebug;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DebugDataS2C {
    List<BlockPos> posList = new ArrayList<>();

    public DebugDataS2C(List<BlockPos> posList) {
        this.posList = posList;
    }

    public DebugDataS2C(FriendlyByteBuf buf) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            posList.add(buf.readBlockPos());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(posList.size());
        for (BlockPos pos : posList) {
            buf.writeBlockPos(pos);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Createstressdebug.posList = posList;
        });
        return true;
    }
}
