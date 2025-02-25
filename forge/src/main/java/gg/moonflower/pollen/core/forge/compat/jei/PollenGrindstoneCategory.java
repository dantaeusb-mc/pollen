package gg.moonflower.pollen.core.forge.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import gg.moonflower.pollen.api.crafting.grindstone.PollenGrindstoneRecipe;
import gg.moonflower.pollen.core.Pollen;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.Optional;

@ApiStatus.Internal
public class PollenGrindstoneCategory implements IRecipeCategory<PollenGrindstoneRecipe> {

    private static final String TOP_SLOT = "topSlot";
    private static final String BOTTOM_SLOT = "bottomSlot";

    private final IDrawable background;
    private final IDrawable icon;

    public PollenGrindstoneCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(new ResourceLocation("textures/gui/container/grindstone.png"), 30, 15, 116, 56).build();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(Blocks.GRINDSTONE));
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getUid() {
        return getRecipeType().getUid();
    }

    @SuppressWarnings("removal")
    @Override
    public Class<? extends PollenGrindstoneRecipe> getRecipeClass() {
        return getRecipeType().getRecipeClass();
    }

    @Override
    public RecipeType<PollenGrindstoneRecipe> getRecipeType() {
        return PollenJeiPlugin.GRINDSTONE_CATEGORY_ID;
    }

    @Override
    public Component getTitle() {
        return Blocks.GRINDSTONE.getName();
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PollenGrindstoneRecipe recipe, IFocusGroup focus) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        if (!ingredients.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 19, 4).addIngredients(ingredients.get(0)).setSlotName(TOP_SLOT);
            if (ingredients.size() > 1) {
                builder.addSlot(RecipeIngredientRole.INPUT, 19, 25).addIngredients(ingredients.get(1)).setSlotName(BOTTOM_SLOT);
            }
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 99, 19).addItemStack(recipe.getResultItem());
    }

    private static int getExperienceFromItem(ItemStack stack) {
        if (stack.isEmpty())
            return 0;

        int i = 0;
        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);

        for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer integer = entry.getValue();
            if (!enchantment.isCurse())
                i += enchantment.getMinCost(integer);
        }

        return i;
    }

    @Override
    public void draw(PollenGrindstoneRecipe recipe, IRecipeSlotsView view, PoseStack matrixStack, double mouseX, double mouseY) {
        int experience = recipe.getResultExperience();

        if (experience == -1) {
            Optional<ItemStack> topStack = view.findSlotByName(TOP_SLOT).flatMap(slot -> slot.getDisplayedIngredient(VanillaTypes.ITEM));
            Optional<ItemStack> bottomStack = view.findSlotByName(BOTTOM_SLOT).flatMap(slot -> slot.getDisplayedIngredient(VanillaTypes.ITEM));

            if (topStack.isEmpty() && bottomStack.isEmpty())
                return;

            experience = topStack.map(PollenGrindstoneCategory::getExperienceFromItem).orElse(0) + bottomStack.map(PollenGrindstoneCategory::getExperienceFromItem).orElse(0);
        }

        if (experience > 0) {
            TranslatableComponent experienceString = new TranslatableComponent("gui.jei.category." + Pollen.MOD_ID + ".grindstone.experience", (int) Math.ceil((double) experience / 2.0), experience);
            Font font = Minecraft.getInstance().font;
            font.draw(matrixStack, experienceString, background.getWidth() - font.width(experienceString), 0, 0xFF808080);
        }
    }

    @Override
    public boolean isHandled(PollenGrindstoneRecipe recipe) {
        return !recipe.isSpecial();
    }
}
