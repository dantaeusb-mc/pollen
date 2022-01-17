package gg.moonflower.pollen.core.mixin.client;

import gg.moonflower.pollen.core.extensions.CompiledChunkExtension;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.Set;

@Mixin(ChunkRenderDispatcher.CompiledChunk.class)
public class CompiledChunkMixin implements CompiledChunkExtension {

    @Unique
    private final Set<BlockPos> renderableBlocks = new HashSet<>();
    @Unique
    private final Set<BlockPos> tickingBlocks = new HashSet<>();

    @Override
    public Set<BlockPos> pollen_getBlockRenderers() {
        return this.renderableBlocks;
    }

    @Override
    public Set<BlockPos> pollen_getTickingBlockRenderers() {
        return this.tickingBlocks;
    }
}
