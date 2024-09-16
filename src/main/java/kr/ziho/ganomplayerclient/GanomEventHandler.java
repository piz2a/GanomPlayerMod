package kr.ziho.ganomplayerclient;

import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import java.util.Map;
import java.lang.reflect.Field;
import org.lwjgl.input.Keyboard;


public class GanomEventHandler {

    private static Map<String, KeyBinding> binds = null;

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {

    }

}