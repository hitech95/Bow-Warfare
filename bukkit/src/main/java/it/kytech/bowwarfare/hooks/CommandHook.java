package it.kytech.bowwarfare.hooks;

import java.util.UUID;
import org.bukkit.Bukkit;

public class CommandHook extends HookBase {

    public CommandHook() {
        super(null);
    }

    @Override
    public boolean execute(String... args) {
        String player = args.length > 0 ? args[0] : "";
        if (player.equalsIgnoreCase("console")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), args[1]);
        } else if (!player.isEmpty()) {
            Bukkit.getPlayer(UUID.fromString(player)).chat("/" + args[1]);
        } else {
            return false;
        }
        return true;
    }

    @Override
    protected boolean ready() {
        return true;
    }

    @Override
    public String getShortName() {
        return "command";
    }

    @Override
    public Class<?>[] getParameters() {
        return new Class<?>[]{String.class};
    }

}
