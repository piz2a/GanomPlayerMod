package kr.ziho.ganomplayerclient;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;

@Mod(modid = GanomPlayerClient.MODID, version = GanomPlayerClient.VERSION)
public class GanomPlayerClient {
	
    public static final String MODID = "ganomplayerclient";
    public static final String VERSION = "1.0";
    
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 25567;
    
    public static final int framesInTimeline = 10;
    public static final int frameInterval = 100;  // microseconds

	public static final int inventorySlot = 0;
	
    private Socket socket;
    private SocketAddress address;
    private boolean running = false;
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	ClientCommandHandler.instance.registerCommand(new ClientCommand(this));
    }
    
    public void connect() {
        Thread socketThread = new Thread(new SocketThread());
        socketThread.start();
        String successMessage = "\u00A7a[GanomPlayerClient] Socket thread is now running";
    	Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(successMessage));
    }
    
    public void disconnect() {
    	running = false;
    }
    
    public void behave(EntityPlayerSP player, JsonObject jsonObject) {
        // Sneaking & Sprinting
    	player.setSneaking(jsonObject.get("sneaking").getAsBoolean());
    	player.setSprinting(jsonObject.get("sprinting").getAsBoolean());
    	
    	// Item in hand
        player.replaceItemInInventory(inventorySlot, ItemLimited.from(jsonObject.get("itemInHand").getAsInt()).toItemStack());
        
        // Jump
        if (jsonObject.get("jump").getAsBoolean()) {
        	player.jump();
        }
        
        // WASD
        GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
        int forward = gameSettings.keyBindForward.getKeyCode();
    	int left = gameSettings.keyBindLeft.getKeyCode();
    	int back = gameSettings.keyBindBack.getKeyCode();
    	int right = gameSettings.keyBindRight.getKeyCode();
    	int[] keyCodes = {forward, left, back, right};
    	for (int i = 0; i < 4; i++) {
    		KeyBinding.setKeyBindState(
    			keyCodes[i],
    			jsonObject.get("key").getAsJsonArray().get(i).getAsBoolean()
    		);
    	}

        // Rotation + Head Rotation
        JsonArray rotationArray = jsonObject.get("rotation").getAsJsonArray();
        float yaw = rotationArray.get(3).getAsFloat();
        float pitch = rotationArray.get(4).getAsFloat();
        BlockPos pos = player.getPosition();
        player.setLocationAndAngles(pos.getX(), pos.getY(), pos.getZ(), yaw, pitch);
    }
	
	private class SocketThread implements Runnable {
		@Override
		public void run() {
			Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("thread"));
			socket = new Socket();
	        address = new InetSocketAddress(GanomPlayerClient.HOST, GanomPlayerClient.PORT);
	        try {
	        	socket.connect(address);
	        } catch (IOException e) {
	        	String errorMessage = "\u00A7c[GanomPlayerClient] Socket connection failure";
	        	Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(errorMessage));
	        	return;
	        }
			
			running = true;
			try {
				InputStream in = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in), 4096);
                double startTime = System.currentTimeMillis();
                
                while (running) {
    				EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
    				if (player == null) {
    					running = false;
    					continue;
    				}
    				// Reconnect if connection was lost
                    if (!socket.isConnected())
                        socket.connect(address);

                    // Receive
                    System.out.println("receiving...");
                    String line = reader.readLine();
                    System.out.println("readLine: " + line);
                    
                    boolean doBehave = true;
                    JsonArray frames = new JsonArray();
                    JsonObject receivedJson = (JsonObject) new JsonParser().parse(line);
                    frames = (JsonArray) receivedJson.get("frames");
                    if (frames == null) doBehave = false;
                    
                    int count = 1;

                    while (count <= framesInTimeline) {
                        if (System.currentTimeMillis() >= startTime + frameInterval * count) {
                            // Make AI behave
                            if (doBehave) behave(player, (JsonObject) frames.get(count - 1));
                            count++;
                        }
                    }
    			}
                socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    
}
