package gg.moonflower.pollen.api.registry.client.forge;

import gg.moonflower.pollen.api.registry.client.ModelRegistry;
import gg.moonflower.pollen.core.Pollen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Internal
@Mod.EventBusSubscriber(modid = Pollen.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModelRegistryImpl {

    private static final Set<ResourceLocation> SPECIAL_MODELS = ConcurrentHashMap.newKeySet();
    private static final Set<ModelRegistry.ModelFactory> FACTORIES = ConcurrentHashMap.newKeySet();

    @SubscribeEvent
    public static void onEvent(ModelRegistryEvent event) {
        SPECIAL_MODELS.forEach(ForgeModelBakery::addSpecialModel);
        FACTORIES.forEach(factory -> factory.registerModels(Minecraft.getInstance().getResourceManager(), ForgeModelBakery::addSpecialModel));
    }

    public static void registerSpecial(ResourceLocation location) {
        SPECIAL_MODELS.add(location);
    }

    public static void registerFactory(ModelRegistry.ModelFactory factory) {
        FACTORIES.add(factory);
    }

    public static BakedModel getModel(ResourceLocation location) {
        return Minecraft.getInstance().getModelManager().getModel(location);
    }
}
