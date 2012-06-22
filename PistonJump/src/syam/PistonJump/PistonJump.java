package syam.PistonJump;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
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

	// Instance
	private static PistonJump instance;


	/**
	 * プラグイン起動処理
	 */
	public void onEnable(){
		instance = this;
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
	 * インスタンスを返す
	 * @return プラグインインスタンス
	 */
	public static PistonJump getInstance() {
    	return instance;
    }
}
