package it.kytech.bowwarfare.hooks;

import it.kytech.bowwarfare.util.EconomyManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

/**
 * something wrote by pogo.
 */
public class EconHook extends HookBase {

	public static final String REMOVE = "remove";
	public static final String ADD = "add";
	public static final String SET = "set";
	
    public EconHook() {
		super("net.milkbowl.vault.economy.Economy");
	}
    
	@Override
    protected boolean execute(String... args) {
		// args: <add/set/remove>
        if (EconomyManager.getInstance().econPresent()) {
            Economy econ = EconomyManager.getInstance().getEcon();
            EconomyResponse response = null;
            if (args.length > 2) {
            	String player = args[1];
            	double amount = -1;
            	try {
            		amount = Double.parseDouble(args[2]);
            	} catch (NumberFormatException ex) {}
            	switch (args[0]) {
            	case REMOVE:
            		response = econ.withdrawPlayer(player, amount);
            		break;
            	case ADD:
            		response = econ.depositPlayer(player, amount);
            		break;
            	case SET:
            		double bal = econ.getBalance(player);
            		econ.withdrawPlayer(player, bal);
            		response = econ.depositPlayer(player, amount);
            		break;
            	}
            }
            if (response == null || response.type != EconomyResponse.ResponseType.SUCCESS || !response.transactionSuccess()) {
            	return false;
            }
            return true;
        }
        return false;
    }

	@Override
	protected boolean ready() {
		return EconomyManager.getInstance().getEcon() != null;
	}

	@Override
	public String getShortName() {
		return "economy";
	}

	@Override
	public Class<?>[] getParameters() {
		return new Class<?>[]{ String.class, String.class, Double.class };
	}

}
