package kr.ziho.ganomplayerclient;
import java.util.Arrays;
import java.util.List;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;

public class ClientCommand implements ICommand {

	GanomPlayerClient mod;
	String name = "gc";
	String usage = EnumChatFormatting.RED + "Usage: /" + name;
	
	public ClientCommand(GanomPlayerClient mod) {
		this.mod = mod;
	}

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public String getCommandName() {
		return name;
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return usage;
	}

	@Override
	public List<String> getCommandAliases() {
		return Arrays.asList();
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args[0].equals("connect") || args[0].equals("c")) {
			mod.connect(Integer.parseInt(args[1]));
		} else if (args[0].equals("disconnect") || args[0].equals("dc")) {
			mod.disconnect();
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

}
