package us.accretion.miner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Optional;

import javax.net.ssl.HttpsURLConnection;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

//import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MPool {
	private static Miner plugin;

	public static void init(Miner pl){
		plugin = pl;
	}
	
	public static void checkPool() {
		
	  Integer retryCount = 0;
	  Integer maxRetries = plugin.cfgs.getInt("net-retry-count"); 
	  
	  while (retryCount<maxRetries) {
		
	  try {	  
		    //String httpsURL = "https://api.nanopool.org/v1/xmr/user/47GGAX6QRMnKgaABHCnM8BHbaseovnfBCbaCHB7zTU2jShGywfFBKtKA6rrB7CGc5EUam7yUHhuzsUuAMbLj9XcB9Rwnjiz";
		    String httpsURL = plugin.cfgs.getString("pool-address");

	        URL myUrl = new URL(httpsURL);
	        HttpsURLConnection conn = (HttpsURLConnection)myUrl.openConnection();
	        conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
	        conn.setConnectTimeout(3000);
	        conn.setReadTimeout(3000);
	        InputStream is = conn.getInputStream();
	        InputStreamReader isr = new InputStreamReader(is);
	        BufferedReader br = new BufferedReader(isr);
	        String inputLine;
            String jsonresp = ""; 
	        while ((inputLine = br.readLine()) != null) {
	        	jsonresp += inputLine;
	        }
	        br.close();
	        conn.disconnect();

	        JsonParser parser = new JsonParser();
	        JsonObject rootObj = parser.parse(jsonresp).getAsJsonObject();
	        String  status = rootObj.get("status").getAsString();
	        if (status == "true") { 
	        	MLogger.debug("Got response: "+status);
	        
	        	JsonObject dataObj = rootObj.get("data").getAsJsonObject();
	        	JsonArray workersArray = dataObj.getAsJsonArray("workers");
	        
	        	for (JsonElement wa : workersArray) {
	        		JsonObject workerObj = wa.getAsJsonObject();
	        		String  id = workerObj.get("id").getAsString();
	        		Float h1 = workerObj.get("h1").getAsFloat();
	        		Float h3 = workerObj.get("h3").getAsFloat();
	        		Float h6 = workerObj.get("h6").getAsFloat();
	        		Float h12 = workerObj.get("h12").getAsFloat();
	        		Float h24 = workerObj.get("h24").getAsFloat();
	        		Float hashrate = workerObj.get("h1").getAsFloat();

	        		Integer tc_h1 = Math.round( h1 / ( 60 / plugin.cfgs.getInt("check-pool-interval") ) ); //Take the 1 hour average and divide by frequency of pool-check payouts 
	        		MLogger.debug("Retrieved: id:"+id+" h1:"+h1+" hashrate:"+hashrate);
	        		
	        		Optional<Player> player = Sponge.getServer().getPlayer(id);	    
   		    	    Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        		    User user = userStorage.get().get(id).get();
        			plugin.cfgs.updatePlayerHashes(user, hashrate, h1, h3, h6, h12, h24);

        		      if(player.isPresent()) {
        		    	  Player onlinePlayer = player.get();
	        	     	  onlinePlayer.sendMessage(Text.of("Your mining has returned a profit of $"+tc_h1));
	        	    	  UniqueAccount acc = plugin.econ.getOrCreateAccount(onlinePlayer.getUniqueId()).get();
	        				  TransactionResult result = acc.deposit(plugin.econ.getDefaultCurrency(), BigDecimal.valueOf(tc_h1), Cause.of(EventContext.builder().build(), tc_h1));
	        			    if (result.getResult() == ResultType.SUCCESS) {
	        			    	  MLogger.info("Paying "+onlinePlayer.getName()+" $"+tc_h1);
	        			    } else if (result.getResult() == ResultType.FAILED || result.getResult() == ResultType.ACCOUNT_NO_FUNDS) {
	        			      	  MLogger.warning("Miner deposit transaction failed.");
	        			    } else {
	        			      	  MLogger.warning("Miner deposit transaction failed...");
	        			    }
		  
	        		  } else {
	        			  plugin.cfgs.increaseUnpaidBalance(user, tc_h1);
	        		  }
	        	}
	        	MLogger.info("Updated pool stats!"); 
	        	break;
	        } else { 
	  		    retryCount++;
	        	MLogger.severe("Failed to update pool stats!"); 
	        }

	  } catch(IOException e){
		  retryCount++;
	      MLogger.warning("Failed to update from pool: "+e);
	  }
	  
	  }
	}
}
