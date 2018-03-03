package us.accretion.miner;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;

import com.google.inject.Inject;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.text.Text;



@Plugin(id="miner", 
name="Miner", 
version="0.1.1",
authors="RioS2", 
description="Miner for Minecraft")
public class Miner {
	
	public Game game;
	public EconomyService econ;
	public MConfig cfgs;

	private PluginContainer instance;
	public PluginContainer get(){
		return this.instance;
	}
	
	@Inject private Logger logger;
	public Logger getLogger(){	
		return logger;
	}
	
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;

	@Inject
	@DefaultConfig(sharedRoot = false)
	private File defConfig;
	
	@Inject
	@DefaultConfig(sharedRoot = true)
	private ConfigurationLoader<CommentedConfigurationNode> configManager;	
	public ConfigurationLoader<CommentedConfigurationNode> getCfManager(){
		return configManager;
	}
	
	@Listener
	public void onServerStart(GameStartedServerEvent event) {
    	game = Sponge.getGame();
    	instance = Sponge.getPluginManager().getPlugin("miner").get();
    	
    	MLogger.init(this);
    	MLogger.info("Logger initialized...");
    	
    	MLogger.info("Init config module...");
    	configManager = HoconConfigurationLoader.builder().setFile(defConfig).build();	
        cfgs = new MConfig(this, configDir, defConfig);
        
        MLogger.info("Init commands module...");
        MCommands.init(this);
        
        game.getEventManager().registerListeners(this, new MListener(this));
        AutoSaveHandler();
        
        MPool.init(this);
        CheckPoolHandler();

        MLogger.success("Miner enabled.");

    }
	@Listener
	public void onStopServer(GameStoppingServerEvent event) {
        MLogger.info("Stopping Plugin...");
		cfgs.savePlayersStats();
        for (Task task:Sponge.getScheduler().getScheduledTasks(this)){
        	task.cancel();
        }
        MLogger.info("Saved config.");
        MLogger.severe("Miner disabled.");
    }
	
	public void reload(){
		for (Task task:Sponge.getScheduler().getScheduledTasks(this)){
			task.cancel();
		}
		cfgs.savePlayersStats();
		cfgs = new MConfig(this, configDir, defConfig);
		AutoSaveHandler();
        CheckPoolHandler();
	}
	
	@Listener
	public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
		if (event.getService().equals(EconomyService.class)) {
            econ = (EconomyService) event.getNewProviderRegistration().getProvider();
		}
	}
	
	private void AutoSaveHandler() {
		MLogger.info("Saving database every "+ cfgs.getInt("flat-file-save-interval") + " minute(s)!");  
		
		Sponge.getScheduler().createSyncExecutor(this).scheduleWithFixedDelay(new Runnable() {  
			public void run() {
				MLogger.debug("Saving Database File!");
				cfgs.savePlayersStats();	
				} 
			},cfgs.getInt("flat-file-save-interval"), cfgs.getInt("flat-file-save-interval"), TimeUnit.MINUTES);	
	}
	
	private void CheckPoolHandler() {
		MLogger.info("Checking pool every "+ cfgs.getInt("check-pool-interval") + " minute(s)!");  
		Sponge.getScheduler().createAsyncExecutor(this).scheduleWithFixedDelay(new Runnable() {
		//Sponge.getScheduler().createSyncExecutor(this).scheduleWithFixedDelay(new Runnable() {  
			public void run() {
				MLogger.debug("Updating mining pool stats");
				MPool.checkPool();	
				} 
			},cfgs.getInt("check-pool-interval"), cfgs.getInt("check-pool-interval"), TimeUnit.MINUTES);	
	}

}

class MLogger{	
	private static Miner plugin;

	public static void init(Miner pl){
		plugin = pl;
	}
	
	public static void success(String s) {
		Sponge.getServer().getConsole().sendMessage(Text.of("Miner: [§a§l"+s+"§r]"));
    }
	
    public static void info(String s) {
    	Sponge.getServer().getConsole().sendMessage(Text.of("Miner: ["+s+"]"));
    }
    
    public static void warning(String s) {
    	Sponge.getServer().getConsole().sendMessage(Text.of("Miner: [§6"+s+"§r]"));
    }
    
    public static void severe(String s) {
    	Sponge.getServer().getConsole().sendMessage(Text.of("Miner: [§c§l"+s+"§r]"));
    }
    
    public static void log(String s) {
    	Sponge.getServer().getConsole().sendMessage(Text.of("Miner: ["+s+"]"));
    }
    
    public static void debug(String s) {
        if (plugin.cfgs.getBool("debug-messages")) {
        	Sponge.getServer().getConsole().sendMessage(Text.of("Miner: [§b"+s+"§r]"));
        }  
    }
}



