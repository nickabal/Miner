package us.accretion.miner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class MCommands {

	private static Miner plugin;

	public static void init(Miner pl){
		plugin = pl;		
		Sponge.getCommandManager().register(plugin, Miner(), "miner");
	}
	
	private static CommandCallable Miner() {
	CommandSpec help = CommandSpec.builder()
			.description(Text.of("Help command for miner."))
		    .executor((src, args) -> { {	
		    	sendHelp(src);
		    	return CommandResult.success();	
		    }})
		    .build();
	

	CommandSpec info = CommandSpec.builder()
			.description(Text.of("Miner info"))
			.permission("miner.info")
		    .executor((src, args) -> { {
                src.sendMessage(Text.of("Miner "+plugin.cfgs.getPlayerHashes(src.getName())));
		    	return CommandResult.success();	
		    }})
		    .build();
	
	CommandSpec reload = CommandSpec.builder()
			.description(Text.of("Reload Miner."))
			.permission("miner.reload")
		    .executor((src, args) -> { {	
		    	plugin.reload();
		    	src.sendMessage(Text.of("§aMiner reloaded!"));
		    	return CommandResult.success();	
		    }})
		    .build();
	
	CommandSpec url = CommandSpec.builder()
			.description(Text.of("Displays url info for mining."))
			.permission("miner.url")
		    .executor((src, args) -> { {	
		    	/*src.sendMessage(Text.of("How to mine:"));
		    	src.sendMessage(Text.of("Download a miner, ie https://github.com/xmrig/xmrig"));
		    	src.sendMessage(Text.of("./minerd -o stratum+tcp://xmr-us-west1.nanopool.org:14444 -u 47GGAX6QRMnKgaABHCnM8BHbaseovnfBCbaCHB7zTU2jShGywfFBKtKA6rrB7CGc5EUam7yUHhuzsUuAMbLj9XcB9Rwnjiz/<minecraft_username>/<email_address> -p x -t <cpu_threads>"));
		    	src.sendMessage(Text.of("Replace minecraft_username with your username to receive credit."));*/
		    	src.sendMessage(Text.of(plugin.cfgs.getString("url-info-text")));
		    	return CommandResult.success();	
		    }})
		    .build();
	
	CommandSpec miner = CommandSpec.builder()
		    .description(Text.of("Main command for miner."))
		    .executor((src, args) -> { {	    	
		    	//no args
		    	src.sendMessage(Text.of("--------------------- "+plugin.get().getName()+" "+plugin.get().getVersion().get()+" --------------------"));
		    	src.sendMessage(Text.of("Developed by " + plugin.get().getAuthors()));
		    	src.sendMessage(Text.of("For more information about the commands, try /miner help"));
		    	src.sendMessage(Text.of("---------------------------------------------------"));
		    	return CommandResult.success();	
		    }})
		    .child(help, "?", "help")
		    .child(info, "info")
		    .child(reload, "reload")
		    .child(url, "url")
		    .build();
	
	return miner;
    }
	
	private static void sendHelp(CommandSource source){
		source.sendMessage(Text.of("Miner commands:"));
		Map<String, String> helpCommands = new HashMap<String, String>();
		helpCommands.put("help", "Displays this help");
		helpCommands.put("info", "Displays info about current miner status");
		helpCommands.put("url", "Displays url and how to mine.");
		helpCommands.put("reload", "Reloads miner.conf, saves playerstats.conf");

	for (Iterator<Map.Entry<String, String>> i = helpCommands.entrySet().iterator(); i.hasNext();) {
		Map.Entry<String, String> cmd = i.next();
		
             String key = cmd.getKey();
             String value = cmd.getValue();
	         
			if (source.hasPermission("miner."+key)) {
				 //source.sendMessage(Text.of("Has self permission"));
		         source.sendMessage(Text.of("/miner "+key+ "  "+value));
			} else
			if (source.hasPermission("miner."+key+".others")){
				 //source.sendMessage(Text.of("Has others permission"));
		         source.sendMessage(Text.of("/miner "+key+ " <player>  "+value));
			} 

		}
	}	
}
