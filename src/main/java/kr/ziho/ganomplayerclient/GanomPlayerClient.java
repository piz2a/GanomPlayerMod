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

import org.lwjgl.input.Keyboard;

@Mod(modid = GanomPlayerClient.MODID, version = GanomPlayerClient.VERSION)
public class GanomPlayerClient {
	
    public static final String MODID = "ganomplayerclient";
    public static final String VERSION = "1.0";
    
    public static final String HOST = "127.0.0.1";
    
    public static final int framesInTimeline = 10;
    public static final int frameInterval = 100;  // microseconds

	public static final int inventorySlot = 0;

    private ServerSocket serverSocket = null;
    private boolean running = false;
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	ClientCommandHandler.instance.registerCommand(new ClientCommand(this));
        // MinecraftForge.EVENT_BUS.register(new GanomEventHandler());
    }
    
    public void connect(int port) {
        Thread socketThread = new Thread(new SocketThread(port));
        socketThread.start();
        String successMessage = "\u00A7a[GanomPlayerClient] Socket thread is now running";
    	Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(successMessage));
    }
    
    public void disconnect() {
        String successMessage = "\u00A7a[GanomPlayerClient] Socket thread stopped";
        Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(successMessage));
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    	running = false;
    }
    
    public void behave(EntityPlayerSP player, JsonObject jsonObject) {
        // WASD key press
        float strafe = jsonObject.get("ADmove").getAsFloat();
        float forward = jsonObject.get("WSmove").getAsFloat();
        float dyaw = jsonObject.get("DelYaw").getAsFloat();
        float dpitch = jsonObject.get("DelPitch").getAsFloat();
        System.out.println("strafe " + strafe + " forward " + forward);
        System.out.println("dyaw " + dyaw + " dpitch " + dpitch);
        // player.setRotation(player.rotationYaw + dyaw, player.rotationPitch + dpitch);
        player.rotationYaw += dyaw;
        player.rotationPitch += dpitch;
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

    public String keylog() {
        String result = "";

        result += Keyboard.isKeyDown(Keyboard.KEY_SPACE) ? "1" : "0";
        result += Keyboard.isKeyDown(Keyboard.KEY_W) ? "1" : "0";
        result += Keyboard.isKeyDown(Keyboard.KEY_A) ? "1" : "0";
        result += Keyboard.isKeyDown(Keyboard.KEY_S) ? "1" : "0";
        result += Keyboard.isKeyDown(Keyboard.KEY_D) ? "1" : "0";

        return result;
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
                if (serverSocket != null) serverSocket.close();
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
                        if (line == null) continue;
                        if (line.equals("keylog")) {  // informs what key the player is pressing
                            String log = keylog();
                            System.out.println(log);
                            writer.println(log);
                        } else {
                            JsonObject receivedJson = (JsonObject) new JsonParser().parse(line);
                            behave(Minecraft.getMinecraft().thePlayer, receivedJson);
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
	        	String errorMessage = "\u00A7c[GanomPlayerClient] Socket connection failure";
	        	Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(errorMessage));
	        	return;
	        }
		}
	}
    
}
