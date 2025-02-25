package gg.moonflower.pollen.core.forge;

import gg.moonflower.pollen.api.event.events.client.InputEvents;
import gg.moonflower.pollen.api.event.events.client.render.FogEvents;
import gg.moonflower.pollen.api.event.events.lifecycle.TickEvents;
import gg.moonflower.pollen.api.event.events.network.ClientNetworkEvents;
import gg.moonflower.pollen.core.Pollen;
import gg.moonflower.pollen.core.extensions.MouseHandlerExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Mod.EventBusSubscriber(modid = Pollen.MOD_ID, value = Dist.CLIENT)
public class PollenClientForgeEvents {

    @SubscribeEvent
    public static void onEvent(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        switch (event.phase) {
            case START:
                TickEvents.CLIENT_PRE.invoker().tick();
                break;
            case END:
                TickEvents.CLIENT_POST.invoker().tick();
                break;
        }
    }

    @SubscribeEvent
    public static void onEvent(ClientPlayerNetworkEvent.LoggedInEvent event) {
        ClientNetworkEvents.LOGIN.invoker().login(event.getMultiPlayerGameMode(), event.getPlayer(), event.getConnection());
    }

    @SubscribeEvent
    public static void onEvent(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        ClientNetworkEvents.LOGOUT.invoker().logout(event.getMultiPlayerGameMode(), event.getPlayer(), event.getConnection());
    }

    @SubscribeEvent
    public static void onEvent(ClientPlayerNetworkEvent.RespawnEvent event) {
        ClientNetworkEvents.RESPAWN.invoker().respawn(event.getMultiPlayerGameMode(), event.getOldPlayer(), event.getPlayer(), event.getConnection());
    }

    @SubscribeEvent
    public static void onEvent(ScreenEvent.MouseScrollEvent.Pre event) {
        MouseHandler mouseHandler = Minecraft.getInstance().mouseHandler;
        if (InputEvents.GUI_MOUSE_SCROLL_EVENT_PRE.invoker().mouseScrolled(mouseHandler, ((MouseHandlerExtension) mouseHandler).pollen_getXOffset(), event.getScrollDelta()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEvent(ScreenEvent.MouseScrollEvent.Post event) {
        MouseHandler mouseHandler = Minecraft.getInstance().mouseHandler;
        InputEvents.GUI_MOUSE_SCROLL_EVENT_POST.invoker().mouseScrolled(mouseHandler, ((MouseHandlerExtension) mouseHandler).pollen_getXOffset(), event.getScrollDelta());
    }

    @SubscribeEvent
    public static void onEvent(net.minecraftforge.client.event.InputEvent.MouseScrollEvent event) {
        MouseHandler mouseHandler = Minecraft.getInstance().mouseHandler;
        if (InputEvents.MOUSE_SCROLL_EVENT.invoker().mouseScrolled(mouseHandler, ((MouseHandlerExtension) mouseHandler).pollen_getXOffset(), event.getScrollDelta()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEvent(InputEvent.MouseInputEvent event) {
        InputEvents.MOUSE_INPUT_EVENT.invoker().mouseInput(Minecraft.getInstance().mouseHandler, event.getButton(), event.getAction(), event.getModifiers());
    }

    @SubscribeEvent
    public static void onEvent(InputEvent.KeyInputEvent event) {
        InputEvents.KEY_INPUT_EVENT.invoker().keyInput(event.getKey(), event.getScanCode(), event.getAction(), event.getModifiers());
    }

    @SubscribeEvent
    public static void onEvent(EntityViewRenderEvent.FogColors event) {
        FogEvents.FOG_COLOR.invoker().setupFogColors(event.getRenderer(), event.getCamera(), new FogColorContextImpl(event), (float) event.getPartialTicks());
    }

    @SubscribeEvent
    public static void onEvent(EntityViewRenderEvent.RenderFogEvent event) {
        FogEvents.FOG_DENSITY.invoker().setupFogDensity(event.getRenderer(), event.getCamera(), event.getFarPlaneDistance(), (float) event.getPartialTicks());
    }

    private static class FogColorContextImpl implements FogEvents.ColorContext {

        private final EntityViewRenderEvent.FogColors event;

        private FogColorContextImpl(EntityViewRenderEvent.FogColors event) {
            this.event = event;
        }

        @Override
        public float getRed() {
            return this.event.getRed();
        }

        @Override
        public float getGreen() {
            return this.event.getGreen();
        }

        @Override
        public float getBlue() {
            return this.event.getBlue();
        }

        @Override
        public void setRed(float red) {
            this.event.setRed(red);
        }

        @Override
        public void setGreen(float green) {
            this.event.setGreen(green);
        }

        @Override
        public void setBlue(float blue) {
            this.event.setBlue(blue);
        }
    }
}
