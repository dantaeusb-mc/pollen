package gg.moonflower.pollen.core.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import gg.moonflower.pollen.api.registry.client.ItemRendererRegistry;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Shadow
    @Final
    private ItemModelShaper itemModelShaper;

    @Unique
    private Item capturedItem;

    @Unique
    private Item capturedHandItem;

    @Unique
    private boolean useSprite;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", ordinal = 0, shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void capture(ItemStack itemStack, ItemTransforms.TransformType transformType, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BakedModel bakedModel, CallbackInfo ci, boolean itemForm) {
        this.capturedItem = itemStack.getItem();
        this.useSprite = itemForm;
    }

    @ModifyVariable(method = "render", index = 8, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", ordinal = 0, shift = At.Shift.BEFORE), argsOnly = true)
    public BakedModel render(BakedModel original) {
        if (this.useSprite && ItemRendererRegistry.getHandModel(this.capturedItem) != null)
            return this.itemModelShaper.getItemModel(this.capturedItem);
        return original;
    }

    @Inject(method = "getModel", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void capture(ItemStack itemStack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<BakedModel> cir) {
        this.capturedHandItem = itemStack.getItem();
    }

    @ModifyVariable(method = "getModel", index = 4, at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/renderer/ItemModelShaper;getItemModel(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/resources/model/BakedModel;", shift = At.Shift.AFTER))
    public BakedModel getModel(BakedModel original) {
        ModelResourceLocation modelLocation = ItemRendererRegistry.getHandModel(this.capturedHandItem);
        if (modelLocation != null)
            return this.itemModelShaper.getModelManager().getModel(modelLocation);
        return original;
    }
}
