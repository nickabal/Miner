package us.accretion.miner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class MConfig{
	
	//getters	
	private CommentedConfigurationNode config;
	public CommentedConfigurationNode configs(){
		return config;
	}
	
	//getters	
	private ConfigurationLoader<CommentedConfigurationNode> statsManager;
	private CommentedConfigurationNode stats;
	public CommentedConfigurationNode stats(){
		return stats;
	}
	
	Miner plugin;
	private Path defDir;
	
	public MConfig(Miner plugin, Path configDir, File defConfig) {
		this.defDir = configDir;
		this.plugin = plugin;
		try {
			Files.createDirectories(configDir);
			if (!defConfig.exists()){
				plugin.getLogger().info("Creating config file...");
				defConfig.createNewFile();
			}
			config = plugin.getCfManager().load();
			
			config.getNode("pool-address").setValue(config.getNode("pool-address").getString("https://api.nanopool.org/v1/xmr/user/47GGAX6QRMnKgaABHCnM8BHbaseovnfBCbaCHB7zTU2jShGywfFBKtKA6rrB7CGc5EUam7yUHhuzsUuAMbLj9XcB9Rwnjiz"))
			.setComment("Server Monero Pool Endpoint Address:");
			
			config.getNode("url-info-text").setValue(config.getNode("url-info-text").getString("https://accretion.network/miner"))
			.setComment("Text of /miner url");
			
			config.getNode("debug-messages").setValue(config.getNode("debug-messages").getBoolean(false))
			.setComment("Enable debug messages?");
			
			config.getNode("use-uuids-instead-names").setValue(config.getNode("use-uuids-instead-names").getBoolean(true))
			.setComment("Use uuids to store players stats on playerstats.conf?");
			
			config.getNode("flat-file-save-interval").setValue(config.getNode("flat-file-save-interval").getInt(15))
			.setComment("Save to file every X minutes.");
			
			config.getNode("check-pool-interval").setValue(config.getNode("check-pool-interval").getInt(15))
			.setComment("Update pool stats and disburse funds every X minutes.");
			
			config.getNode("net-retry-count").setValue(config.getNode("net-retry-count").getInt(2))
			.setComment("How many times to try again if updating pool net request fails.");
			
	        loadPlayerStats();
	        /*---------------*/
	                
			save();        			
			MLogger.info("All configurations loaded!");
				
        } catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public void loadPlayerStats(){
		File pStats = new File(defDir+File.separator+"playerstats.conf");
		try {
			if (!pStats.exists()) {
		 		pStats.createNewFile();			 	
		    }
			
	    	statsManager = HoconConfigurationLoader.builder().setFile(pStats).build();
	    	stats = statsManager.load();
		} catch (IOException e1) {			
			MLogger.severe("The default configuration could not be loaded or created!");
			e1.printStackTrace();
		}		
	}

    public Boolean getBool(Object... key){		
		return config.getNode(key).getBoolean();
	}
    public String getString(Object... key){		
		return config.getNode(key).getString();
	}
    public Integer getInt(Object... key){		
		return config.getNode(key).getInt();
	}
        
    public void save(){
    	try {
			plugin.getCfManager().save(config);
		} catch (IOException e) {
			MLogger.severe("Problems during save file:");
			e.printStackTrace();
		}
		savePlayersStats(); 
    }
    
    public void savePlayersStats(){
    	try {
			statsManager.save(stats);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public boolean useUUIDs() {
    	return getBool("use-uuids-instead-names");
    }
    
	public void AddPlayer(Player p) {
		String PlayerString = p.getUniqueId().toString();
		if (!useUUIDs()){
			PlayerString = p.getName();
		}
		stats.getNode(PlayerString,"PlayerName").setValue(p.getName());
		stats.getNode(PlayerString,"UnpaidBalance").setValue(0);		
		stats.getNode(PlayerString,"PaidBalance").setValue(0);
		stats.getNode(PlayerString,"hashrate").setValue(0);
		stats.getNode(PlayerString,"h1").setValue(0);
		stats.getNode(PlayerString,"h3").setValue(0);
		stats.getNode(PlayerString,"h6").setValue(0);
		stats.getNode(PlayerString,"h12").setValue(0);
		stats.getNode(PlayerString,"h24").setValue(0);
	}
	
	public void AddPlayer(User p) {
		String PlayerString = p.getUniqueId().toString();
		if (!useUUIDs()){
			PlayerString = p.getName();
		}
		stats.getNode(PlayerString,"PlayerName").setValue(p.getName());
		stats.getNode(PlayerString,"UnpaidBalance").setValue(0);		
		stats.getNode(PlayerString,"PaidBalance").setValue(0);
		stats.getNode(PlayerString,"hashrate").setValue(0);
		stats.getNode(PlayerString,"h1").setValue(0);
		stats.getNode(PlayerString,"h3").setValue(0);
		stats.getNode(PlayerString,"h6").setValue(0);
		stats.getNode(PlayerString,"h12").setValue(0);
		stats.getNode(PlayerString,"h24").setValue(0);
	}
	
	public String getPlayerKey(User user){
		if (useUUIDs() && stats.getNode(user.getUniqueId().toString(),"PlayerName").getString() != null){
			return user.getUniqueId().toString();
		} else if (!useUUIDs() && stats.getNode(user.getName(),"PlayerName").getString() != null){
			return user.getName();
		}
		return null;
	}
	public Integer getUnpaidBalance(User p){
		String PlayerString = p.getUniqueId().toString();
		if (!useUUIDs()){
			PlayerString = p.getName();
		}
        return stats.getNode(PlayerString,"UnpaidBalance").getInt();
	}
	public Integer getPaidBalance(User p){
		String PlayerString = p.getUniqueId().toString();
		if (!useUUIDs()){
			PlayerString = p.getName();
		}
        return stats.getNode(PlayerString,"PaidBalance").getInt();
	}
	public void clearUnpaidBalance(User p){
		String PlayerString = p.getUniqueId().toString();
		if (!useUUIDs()){
			PlayerString = p.getName();
		}
	    stats.getNode(PlayerString,"UnpaidBalance").setValue(0);
	}
	public void increaseUnpaidBalance(User p, Integer amount){
		String PlayerString = p.getUniqueId().toString();
		if (!useUUIDs()){
			PlayerString = p.getName();
		}
		Integer currentBalance = stats.getNode(PlayerString,"UnpaidBalance").getInt();
	    stats.getNode(PlayerString,"UnpaidBalance").setValue(currentBalance + amount);
	}
	public void increasePaidBalance(User p, Integer amount){
		String PlayerString = p.getUniqueId().toString();
		if (!useUUIDs()){
			PlayerString = p.getName();
		}
		Integer currentBalance = stats.getNode(PlayerString,"PaidBalance").getInt();
	    stats.getNode(PlayerString,"PaidBalance").setValue(currentBalance + amount);
	}
	public void updatePlayerHashes(User p, Float hashrate, Float h1, Float h3, Float h6, Float h12, Float h24) {
		String PlayerString = p.getUniqueId().toString();
		if (!useUUIDs()){
			PlayerString = p.getName();
		}
		stats.getNode(PlayerString,"hashrate").setValue(Math.round(hashrate));
		stats.getNode(PlayerString,"h1").setValue(Math.round(h1));
		stats.getNode(PlayerString,"h3").setValue(Math.round(h3));
		stats.getNode(PlayerString,"h6").setValue(Math.round(h6));
		stats.getNode(PlayerString,"h12").setValue(Math.round(h12));
		stats.getNode(PlayerString,"h24").setValue(Math.round(h24));
	}
	public String getPlayerHashes(User p) {
		String PlayerString = p.getUniqueId().toString();
		if (!useUUIDs()){
			PlayerString = p.getName();
		}
	    return "hashrate: "+ 
		stats.getNode(PlayerString,"hashrate").getValue().toString()+" H/s"+
		" Averages: "+
		stats.getNode(PlayerString,"h1").getValue().toString()+"/"+
		stats.getNode(PlayerString,"h3").getValue().toString()+"/"+
		stats.getNode(PlayerString,"h6").getValue().toString()+"/"+
		stats.getNode(PlayerString,"h12").getValue().toString()+"/"+
		stats.getNode(PlayerString,"h24").getValue().toString()+ 
		" (1h/3h/6h/12h/24h)";	
	}

	public String getPlayerHashes(String PlayerName) {
		
   	    UserStorageService userStorage = Sponge.getServiceManager().provide(UserStorageService.class).get();
	    String PlayerString = userStorage.get(PlayerName).get().getUniqueId().toString();
	    
	    return "hashrate: "+ 
		stats.getNode(PlayerString,"hashrate").getValue().toString()+" H/s"+
		" Averages: "+
		stats.getNode(PlayerString,"h1").getValue().toString()+"/"+
		stats.getNode(PlayerString,"h3").getValue().toString()+"/"+
		stats.getNode(PlayerString,"h6").getValue().toString()+"/"+
		stats.getNode(PlayerString,"h12").getValue().toString()+"/"+
		stats.getNode(PlayerString,"h24").getValue().toString()+ 
		" (1h/3h/6h/12h/24h)";	
				
	}
}
		
		