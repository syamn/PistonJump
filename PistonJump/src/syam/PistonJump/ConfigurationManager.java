package syam.PistonJump;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;

/*     Copyright (C) 2012  syamn <admin@sakura-server.net>
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class ConfigurationManager {
	public final static Logger log = PistonJump.log;
	public final static String logPrefix = PistonJump.logPrefix;
	public final static String msgPrefix = PistonJump.msgPrefix;

	private PistonJump plugin;
	//private FileConfiguration conf;

	private static File pluginDir = new File("plugins", "PistonJump");

	// 設定項目
	/* Basic Configs */
	public boolean ignorePermission = new Boolean(false);
	public boolean enableSidewaysPiston = new Boolean(true);

	// 設定ここまで

	/**
	 * コンストラクタ
	 * @param plugin PistonJump instance
	 */
	public ConfigurationManager(final PistonJump plugin){
		this.plugin = plugin;
		pluginDir = this.plugin.getDataFolder();
	}

	/**
	 * 必要なディレクトリ群を作成する
	 */
	private void createDirs(){
		createDir(plugin.getDataFolder());
	}

	/**
	 * 設定ファイルに設定を書き込む
	 * @throws Exception
	 */
	public void save() throws Exception{
		plugin.saveConfig();
	}

	/**
	 * 設定ファイルを読み込む
	 * @param initialLoad 初回ロード時かどうか
	 */
	public void loadConfig(boolean initialLoad){
		// ディレクトリ作成
		createDirs();

		// 設定ファイルパス取得
		File file = new File(pluginDir, "config.yml");
		String filepath = file.getPath();
		// 設定ファイルが見つからなければデフォルトのファイルをコピー
		if (!file.exists()){
			extractResource("/config.yml", pluginDir, false, true);
			log.info(logPrefix+ "config.yml is not found! Created default config.yml!");
		}

		plugin.reloadConfig();

		// 先にバージョンチェック
		double version = plugin.getConfig().getDouble("version", 0.1D);
		checkver(version);

		// 項目取得
		/* Basic Config */
		ignorePermission = plugin.getConfig().getBoolean("IgnorePermission", false);
		enableSidewaysPiston = plugin.getConfig().getBoolean("EnableSidewaysPiston", true);
	}

	/**
	 * 設定ファイルのバージョンをチェックする
	 * @param ver
	 */
	private void checkver(final double ver){
		double configVersion = ver;
		double nowVersion = 0.1D;
		try{
			nowVersion = Double.parseDouble(plugin.getDescription().getVersion());
		}catch (NumberFormatException ex){
			log.warning(logPrefix+ "Cannot parse version string!");
		}

		// 比較 設定ファイルのバージョンが古ければ config.yml を上書きする
		if (configVersion < nowVersion){
			// 先に古い設定ファイルをリネームする
			String destName = "oldconfig-v"+configVersion+".yml";
			String srcPath = new File(pluginDir, "config.yml").getPath();
			String destPath = new File(pluginDir, destName).getPath();
			try{
				copyTransfer(srcPath, destPath);
				log.info(logPrefix+ "Copied old config.yml to "+destName+"!");
			}catch(Exception ex){
				log.warning(logPrefix+ "Cannot copy old config.yml!");
			}

			// config.ymlを強制コピー
			extractResource("/config.yml", pluginDir, true, false);

			log.info(logPrefix+ "Deleted existing configuration file and generate a new one!");
		}

	}

	/**
	 * 存在しないディレクトリを作成する
	 * @param dir File 作成するディレクトリ
	 */
	private static void createDir(File dir){
		// 既に存在すれば作らない
		if (dir.isDirectory()){
			return;
		}
		if (!dir.mkdir()){
			log.warning(logPrefix+ "Can't create directory: " + dir.getName());
		}
	}

	/**
	 * リソースファイルをファイルに出力する
	 * @param from 出力元のファイルパス
	 * @param to 出力先のファイルパス
	 * @param force jarファイルの更新日時より新しいファイルが既にあっても強制的に上書きするか
	 * @param checkenc 出力元のファイルを環境によって適したエンコードにするかどうか
	 * @author syam
	 */
	static void extractResource(String from, File to, boolean force, boolean checkenc){
		File of = to;

		// ファイル展開先がディレクトリならファイルに変換、ファイルでなければ返す
		if (to.isDirectory()){
			String filename = new File(from).getName();
			of = new File(to, filename);
		}else if(!of.isFile()){
			log.warning(logPrefix+ "not a file:" + of);
			return;
		}

		// ファイルが既に存在して、そのファイルの最終変更日時がJarファイルより後ろなら、forceフラグが真の場合を除いて返す
		if (of.exists() && of.lastModified() > getJarFile().lastModified() && !force){
			return;
		}

		OutputStream out = null;
		InputStream in = null;
		InputStreamReader reader = null;
		OutputStreamWriter writer =null;
		DataInputStream dis = null;
		try{
			// jar内部のリソースファイルを取得
			URL res = PistonJump.class.getResource(from);
			if (res == null){
				log.warning(logPrefix+ "Can't find "+ from +" in plugin Jar file");
				return;
			}
			URLConnection resConn = res.openConnection();
			resConn.setUseCaches(false);
			in = resConn.getInputStream();

			if (in == null){
				log.warning(logPrefix+ "Can't get input stream from " + res);
			}else{
				// 出力処理 ファイルによって出力方法を変える
				if (checkenc){
					// 環境依存文字を含むファイルはこちら環境

					reader = new InputStreamReader(in, "UTF-8");
					writer = new OutputStreamWriter(new FileOutputStream(of)); // 出力ファイルのエンコードは未指定 = 自動で変わるようにする

					int text;
					while ((text = reader.read()) != -1){
						writer.write(text);
					}
				}else{
					// そのほか

					out = new FileOutputStream(of);
					byte[] buf = new byte[1024]; // バッファサイズ
					int len = 0;
					while((len = in.read(buf)) >= 0){
						out.write(buf, 0, len);
					}
				}
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}finally{
			// 後処理
			try{
				if (out != null)
					out.close();
				if (in != null)
					in.close();
				if (reader != null)
					reader.close();
				if (writer != null)
					writer.close();
			}catch (Exception ex){}
		}
	}

	/**
	 * コピー元のパス[srcPath]から、コピー先のパス[destPath]へファイルのコピーを行います。
	 * コピー処理にはFileChannel#transferToメソッドを利用します。
	 * コピー処理終了後、入力・出力のチャネルをクローズします。
	 * @param srcPath コピー元のパス
	 * @param destPath  コピー先のパス
	 * @throws IOException 何らかの入出力処理例外が発生した場合
	 */
	public static void copyTransfer(String srcPath, String destPath) throws IOException {
		FileChannel srcChannel = new FileInputStream(srcPath).getChannel();
		FileChannel destChannel = new FileOutputStream(destPath).getChannel();
		try {
		    srcChannel.transferTo(0, srcChannel.size(), destChannel);
		} finally {
		    srcChannel.close();
		    destChannel.close();
		}
	}

	public static File getJarFile(){
		return new File("plugins", "PistonJump.jar");
	}
}
