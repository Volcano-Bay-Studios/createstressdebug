package xyz.volcanobay.createstressdebug.mixin;

import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.TorquePropagator;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(TorquePropagator.class)
public interface TorquePropagatorMixin {
    @Accessor
    Map<LevelAccessor, Map<Long, KineticNetwork>> getNetworks();
}
