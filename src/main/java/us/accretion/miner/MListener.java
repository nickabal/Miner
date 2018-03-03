package us.accretion.miner;


import java.math.BigDecimal;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;

public class MListener {
	private Miner plugin;

	public MListener(Miner plugin) {
		this.plugin = plugin;
	}
	
	@Listener
    public void PlayerLogin(ClientConnectionEvent.Login e){    	
    	User p = e.getTargetUser();
    	
    	MLogger.debug("Player Join Event!");
    	if (plugin.cfgs.getPlayerKey(p) == null){
    		plugin.cfgs.AddPlayer(p);
    	}
	}
	
	@Listener
	public void PlayerJoin(ClientConnectionEvent.Join e){
		Player player = e.getTargetEntity();
    	Integer unpaidBalance = plugin.cfgs.getUnpaidBalance(player);
    	if (unpaidBalance > 0){

    	  player.sendMessage(Text.of("While you were away you earned a mining profit of $"+unpaidBalance+""));
    	  //Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "pay "+player.getName()+" "+unpaidBalance);
    	  UniqueAccount acc = plugin.econ.getOrCreateAccount(player.getUniqueId()).get();
    	  MLogger.debug(acc.getBalance(plugin.econ.getDefaultCurrency()).toString());
			  TransactionResult result = acc.deposit(plugin.econ.getDefaultCurrency(), BigDecimal.valueOf(unpaidBalance), Cause.of(EventContext.builder().build(), unpaidBalance));
		    if (result.getResult() == ResultType.SUCCESS) {
		    	  MLogger.info("Paying "+player.getName()+" $"+unpaidBalance);
		    	  plugin.cfgs.clearUnpaidBalance(player);
		    	  plugin.cfgs.increasePaidBalance(player, unpaidBalance);
		    } else if (result.getResult() == ResultType.FAILED || result.getResult() == ResultType.ACCOUNT_NO_FUNDS) {
		      	  MLogger.warning("Miner deposit transaction failed.");
		    } else {
		      	  MLogger.warning("Miner deposit transaction failed...");
		    }
    	  }

    }
}	
