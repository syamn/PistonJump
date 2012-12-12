package syam.PistonJump.Util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import syam.PistonJump.PistonJump;

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

/**
 * ユーティリティクラス
 * @author syam
 */
public class Actions {
	public final static Logger log = PistonJump.log;
	private static final String logPrefix = PistonJump.logPrefix;
	private static final String msgPrefix = PistonJump.msgPrefix;

	public static PistonJump plugin;
	public Actions(PistonJump instance){
		plugin = instance;
	}

	/****************************************/
	// メッセージ送信系関数
	/****************************************/
	/**
	 * メッセージをユニキャスト
	 * @param sender Sender (null可)
	 * @param player Player (null可)l
	 * @param message メッセージ
	 */
	public static void message(CommandSender sender, Player player, String message){
		if (message != null){
			message = message
					.replaceAll("&([0-9a-fk-or])", "\u00A7$1")
					.replaceAll("%version", PistonJump.getInstance().getDescription().getVersion());
			if (player != null){
				player.sendMessage(message);
			}
			else if (sender != null){
				sender.sendMessage(message);
			}
		}
	}
	/**
	 * メッセージをブロードキャスト
	 * @param message メッセージ
	 */
	public static void broadcastMessage(String message){
		if (message != null){
			message = message
					.replaceAll("&([0-9a-fk-or])", "\u00A7$1")
					.replaceAll("%version", PistonJump.getInstance().getDescription().getVersion());
			Bukkit.broadcastMessage(message);
		}
	}
	/**
	 * メッセージをワールドキャスト
	 * @param world
	 * @param message
	 */
	public static void worldcastMessage(World world, String message){
		if (world != null && message != null){
			message = message
					.replaceAll("&([0-9a-fk-or])", "\u00A7$1")
					.replaceAll("%version", PistonJump.getInstance().getDescription().getVersion());
			for(Player player: world.getPlayers()){
				log.info("[Worldcast]["+world.getName()+"]: " + message);
				player.sendMessage(message);
			}
		}
	}
	/**
	 * メッセージをパーミッションキャスト(指定した権限ユーザにのみ送信)
	 * @param permission 受信するための権限ノード
	 * @param message メッセージ
	 */
	public static void permcastMessage(String permission, String message){
		// 動かなかった どうして？
		//int i = Bukkit.getServer().broadcast(message, permission);

		// OK
		int i = 0;
		for (Player player : Bukkit.getServer().getOnlinePlayers()){
			if (player.hasPermission(permission)){
				Actions.message(null, player, message);
				i++;
			}
		}

		log.info("Received "+i+"players: "+message);
	}

	/****************************************/
	// ユーティリティ
	/****************************************/
	/**
	 * 文字配列をまとめる
	 * @param s つなげるString配列
	 * @param glue 区切り文字 通常は半角スペース
	 * @return
	 */
	public static String combine(String[] s, String glue)
    {
      int k = s.length;
      if (k == 0){ return null; }
      StringBuilder out = new StringBuilder();
      out.append(s[0]);
      for (int x = 1; x < k; x++){
        out.append(glue).append(s[x]);
      }
      return out.toString();
    }
	/**
	 * コマンドをコンソールから実行する
	 * @param command
	 */
	public static void executeCommandOnConsole(String command){
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
	}
	/**
	 * 文字列の中に全角文字が含まれているか判定
	 * @param s 判定する文字列
	 * @return 1文字でも全角文字が含まれていればtrue 含まれていなければfalse
	 * @throws UnsupportedEncodingException
	 */
	public static boolean containsZen(String s)
			throws UnsupportedEncodingException {
		for (int i = 0; i < s.length(); i++) {
			String s1 = s.substring(i, i + 1);
			if (URLEncoder.encode(s1,"MS932").length() >= 4) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 現在の日時を yyyy-MM-dd HH:mm:ss 形式の文字列で返す
	 * @return
	 */
	public static String getDatetime(){

		Date date = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(date);
	}
	/**
	 * 座標データを ワールド名:x, y, z の形式の文字列にして返す
	 * @param loc
	 * @return
	 */
	public static String getLocationString(Location loc){
		return loc.getWorld().getName()+":"+loc.getX()+","+loc.getY()+","+loc.getZ();
	}
	public static String getBlockLocationString(Location loc){
		return loc.getWorld().getName()+":"+loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ();
	}
	/**
	 * デバッグ用 syamnがオンラインならメッセージを送る
	 * @param msg
	 */
	public static void debug(String msg){
		OfflinePlayer syamn = Bukkit.getServer().getOfflinePlayer("syamn");
		if (syamn.isOnline()){
			Actions.message(null, (Player) syamn, msg);
		}
	}

	/****************************************/
	// PistonJump
	/****************************************/

	// 真下の看板をチェックして強さを決定する
	public static double checkUnderSign(Block pistonBlock){
		// 真下ブロックをチェック
		BlockState checkBlock = pistonBlock.getRelative(BlockFace.DOWN, 1).getState();
		if (checkBlock instanceof Sign){
			Sign sign = (Sign)checkBlock;
			// 1行目が &a[PistonJump] の看板
			if (sign.getLine(0).equalsIgnoreCase("§a[PistonJump]")){
				// 2行目がdoubleにパースできなければ -1.0を返す
				String line2 = sign.getLine(1).trim();
				if (!Util.isDouble(line2)){
					return -1.0D;
				}else{
					double d = Double.parseDouble(line2);
					// 入力値の許容チェック
					if (d < 0.0D){ // 負数は0に設定して飛ばさない
						d = 0.0D;
					}
					else if (d > 8.0D){ // 8.0以上は8.0に戻す
						d = 8.0D;
					}

					// チェックおわり
					return d;
				}
			}
		}

		// エラーは-1.0を返す
		return -1.0D;
	}

	/**
	 * ピストンの方向と強さを指定して適切なベクトルを返す
	 * @param direction ピストンの方向
	 * @param power 強さ
	 * @return ベクトル
	 */
	public static Vector getEjectionVector(BlockFace direction, double power){
		switch (direction){
			case UP: // 上向き
				return new Vector(0, power, 0);
			case EAST: // 東向き →実際には北向き？ Z軸を負に
				return new Vector(power, 0, 0);
			case WEST: // 西向き→実際には南 Z軸を正に
				return new Vector(-power, 0, 0);
			case SOUTH: // 南向き→東 X軸を正に
				return new Vector(0, 0, power);
			case NORTH: // 北向き→西 X軸を負に
				return new Vector(0, 0, -power);
			case DOWN: // 下向き
				return new Vector(0, -power, 0);
			default:
				return new Vector(0, 0, 0);
		}
	}

}
