package kr.ziho.ganomplayerclient;

import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import java.util.Map;
import java.lang.reflect.Field;

public class GanomEventHandler {

    private static Map<String, KeyBinding> binds = null;

    @SubscribeEvent (priority = EventPriority.LOWEST)  // END is not present
    public void onClientTick(TickEvent.ClientTickEvent event) throws Exception {
        if (binds == null) {
            Field campo = KeyBinding.class.getDeclaredField("KEYBIND_ARRAY");
            campo.setAccessible(true);
            binds = (Map<String, KeyBinding>) campo.get(null);
        }
        for (String bind : binds.keySet()) {
            if (binds.get(bind).isKeyDown()){
                String bindMessage = "\u00A76[GanomPlayerClient] " + bind + " - " + binds.get(bind).getKeyCode();
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(bindMessage));
                break;
            }
        }
    }

}