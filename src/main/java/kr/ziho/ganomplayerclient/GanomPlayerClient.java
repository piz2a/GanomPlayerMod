package kr.ziho.ganomplayerclient;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

import java.io.*;
import java.net.*;
import java.util.*;

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
    
    public static final int framesInTimeline = 10;
    public static final int frameInterval = 100;  // microseconds

	public static final int inventorySlot = 0;

    private ServerSocket serverSocket;
    private boolean running = false;
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	ClientCommandHandler.instance.registerCommand(new ClientCommand(this));
        MinecraftForge.EVENT_BUS.register(new GanomEventHandler());
    }
    
    public void connect(int port) {
        Thread socketThread = new Thread(new SocketThread(port));
        socketThread.start();
        String successMessage = "\u00A7a[GanomPlayerClient] Socket thread is now running";
    	Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(successMessage));
    }
    
    public void disconnect() {
    	running = false;
    }
    
    public void behave(EntityPlayerSP player, JsonObject jsonObject) {
        /*
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
         */

        // WASD key press
        float strafe, forward;  // Initialization required
        strafe = jsonObject.get("strafe").getAsFloat();
        forward = jsonObject.get("forward").getAsFloat();
        player.moveEntityWithHeading(strafe, forward);

        /*
        // Rotation + Head Rotation (Debug required)
        JsonArray rotationArray = jsonObject.get("rotation").getAsJsonArray();
        float yaw = rotationArray.get(3).getAsFloat();
        float pitch = rotationArray.get(4).getAsFloat();
        BlockPos pos = player.getPosition();
        player.setLocationAndAngles(pos.getX(), pos.getY(), pos.getZ(), yaw, pitch);
        */
    }
	
	private class SocketThread implements Runnable {

        private int port;

        SocketThread(int port) {
            super();
            this.port = port;
        }

		@Override
		public void run() {
			Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("thread"));
			try {
                serverSocket = new ServerSocket(port);
                String listeningMessage = "\u00A7a[GanomPlayerClient] Socket Server is listening on port " + port;
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(listeningMessage));
                running = true;
                while (running) {
                    Socket socket = serverSocket.accept();
                    System.out.println("[ "+socket.getInetAddress()+" ] client connected");
                    OutputStream output = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);
                    InputStream input = socket.getInputStream();
                    Scanner reader = new Scanner(input);

                    while (running) {
                        String line = reader.hasNextLine() ? reader.nextLine() : null;
                        if (line.equals("keylog")) {  // informs what key the player is pressing
                            // writer.println(new Date().toString());
                        } else {
                            JsonObject receivedJson = (JsonObject) new JsonParser().parse(line);
                            behave(Minecraft.getMinecraft().thePlayer, receivedJson);
                        }
                    }
                }
            } catch (IOException e) {
	        	String errorMessage = "\u00A7c[GanomPlayerClient] Socket connection failure";
	        	Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(errorMessage));
	        	return;
	        }
		}
	}
    
}
