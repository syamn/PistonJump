package syam.PistonJump;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * メインクラス
 * @author syam
 */
public class PistonJump extends JavaPlugin{
	// Logger
	public final static Logger log = Logger.getLogger("Minecraft");
	public final static String logPrefix = "[PistonJump] ";
	public final static String msgPrefix = "&c[PistonJump] &f";

	// Listener
	private final BlockListener blockListener = new BlockListener(this);

	// Private classes
	private ConfigurationManager config;

	// Instance
	private static PistonJump instance;


	/**
	 * プラグイン起動処理
	 */
	public void onEnable(){
		instance = this;
		config = new ConfigurationManager(this);
		PluginManager pm = getServer().getPluginManager();

		// 設定読み込み
		try{
			config.loadConfig(true);
		}catch(Exception ex){
			log.warning(logPrefix+ "an error occured while trying to load the config file.");
			ex.printStackTrace();
		}

		// イベントを登録
		pm.registerEvents(blockListener, this);

		// メッセージ表示
		PluginDescriptionFile pdfFile=this.getDescription();
		log.info("["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" is enabled!");
	}

	/**
	 * プラグイン停止処理
	 */
	public void onDisable(){
		// メッセージ表示
		PluginDescriptionFile pdfFile=this.getDescription();
		log.info("["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" is disabled!");
	}

	/**
	 * 設定マネージャを返す
	 * @return ConfigurationManager
	 */
	public ConfigurationManager getConfigs(){
		return config;
	}

	/**
	 * インスタンスを返す
	 * @return プラグインインスタンス
	 */
	public static PistonJump getInstance() {
    	return instance;
    }
}
