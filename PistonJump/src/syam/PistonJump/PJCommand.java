package syam.PistonJump;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PJCommand implements CommandExecutor{
	public final static Logger log = PistonJump.log;
	public final static String logPrefix = PistonJump.logPrefix;
	public final static String msgPrefix = PistonJump.msgPrefix;

	private final PistonJump plugin;
	public PJCommand(final PistonJump plugin){
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		// 設定ファイル再読み込み
		if (args.length >= 1 && (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r"))){
			if (!sender.hasPermission("pistonjump.reload")){
				Actions.message(sender, null, "&cYou don't have permission to use this!");
				return true;
			}
			try{
				plugin.getConfigs().loadConfig(false);
			}catch(Exception ex){
				log.warning(logPrefix+ "an error occured while trying to load the config file.");
				ex.printStackTrace();
				return true;
			}
			Actions.message(sender, null, "&aConfiguration reloaded!");
			return true;
		}

		// コマンドヘルプを表示
		Actions.message(sender, null, "&c===================================");
		Actions.message(sender, null, "&bPistonJump Plugin version &3%version &bby syamn");
		Actions.message(sender, null, " &b<>&f = required, &b[]&f = optional");
		Actions.message(sender, null, " /pistonjump reload (/pj r)");
		Actions.message(sender, null, "   &7- Reload configs from config.yml");
		Actions.message(sender, null, "&c===================================");

		return true;
	}
}
