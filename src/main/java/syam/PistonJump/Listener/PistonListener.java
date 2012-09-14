/**
 * PistonJump - Package: syam.PistonJump.Listener
 * Created: 2012/09/15 0:16:18
 */
package syam.PistonJump.Listener;

import java.util.logging.Logger;

import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet53BlockChange;
import net.minecraft.server.World;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import syam.PistonJump.PistonJump;
import syam.PistonJump.Util.Actions;

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
 * PistonListener (PistonListener.java)
 * @author syam(syamn)
 */
public class PistonListener implements Listener {
	public final static Logger log = PistonJump.log;
	private static final String logPrefix = PistonJump.logPrefix;
	private static final String msgPrefix = PistonJump.msgPrefix;

	private final PistonJump plugin;

	public PistonListener(final PistonJump plugin){
		this.plugin = plugin;
	}

	/* 登録するイベントはここから下に */

	// ピストンが展開した
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPistonExtend(final BlockPistonExtendEvent event){
		Block block = event.getBlock();
		BlockFace direction = event.getDirection();
		Block headBlock = block.getRelative(direction, 1); // ピストンが押された位置にあるブロック

		// 押した先のブロックが空気(0:AIR)の場合
		if (headBlock.getType() == Material.AIR){
			double flyVector = plugin.getConfigs().defaultPower;

			// 横向きのピストンでの動作が無効に設定されかつ、上向きでないピストンは何もしない
			if (direction != BlockFace.UP && !plugin.getConfigs().enableSidewaysPiston){
				return;
			}

			// 設定が有効ならば真下の看板をチェックする
			if (plugin.getConfigs().checkSign && Actions.checkUnderSign(block) >= 0.0D){
				flyVector = Actions.checkUnderSign(block);
			}

			// 飛ばす強さチェック
			if (flyVector <= 0.0D){
				return;
			}
			else if (flyVector > 8.0D){
				flyVector = 8.0D;
			}

			Location headBlockLoc = headBlock.getLocation().add(0.5, 0.0, 0.5);


			// ベクトル設定
			//Vector dir = player.getVelocity();
			Vector vect = null;
			if (direction == BlockFace.UP) // 上方向
				vect = new Vector(0, flyVector, 0);
			//	vect = new Vector(dir.getX() * 3.0D, flyVector, dir.getZ() * 3.0D);
			else if (direction == BlockFace.EAST) // 東向き→実際には北向き？ Z軸を負に
				vect = new Vector(0, 0, -flyVector);
			else if(direction == BlockFace.WEST) // 西向き→実際には南 Z軸を正に
				vect = new Vector(0, 0, flyVector);
			else if(direction == BlockFace.SOUTH) // 南向き→東 X軸を正に
				vect = new Vector(flyVector, 0, 0);
			else if(direction == BlockFace.NORTH) // 北向き→西 X軸を負に
				vect = new Vector(-flyVector, 0, 0);

			// オンラインプレイヤーを走査
			for (Player player : Bukkit.getServer().getOnlinePlayers()){
				// 別ワールドを除外
				Location playerLoc = player.getLocation();
				if (playerLoc.getWorld() != headBlockLoc.getWorld()){
					continue;
				}

				// ピストンの上に居ないプレイヤーを除外
				double distance = playerLoc.distance(headBlockLoc);
				if (distance >= 1.0){
					continue;
				}

				// 権限チェック
				if (!plugin.getConfigs().ignorePermission && !player.hasPermission("pistonjump.jump")){
					continue;
				}

				player.teleport(playerLoc.add(0, 0.5, 0), TeleportCause.PLUGIN);
				player.setVelocity(vect); // 飛ばす
			}
		}

		// 押した先のブロックが砂(12:SAND)の場合
		else if (headBlock.getType() == Material.SAND || headBlock.getType() == Material.GRAVEL){
			// 上向きのピストンのみ対応
			if (direction == BlockFace.UP){
				double flyVector = plugin.getConfigs().defaultPower;
				if (plugin.getConfigs().checkSign && Actions.checkUnderSign(block) >= 0.0D){
					flyVector = Actions.checkUnderSign(block);
				}

				// スポーンさせる座標
				Location loc = headBlock.getLocation().clone().add(0.5D, 1.5D, 0.5D); // Y=1.5だと丁度着地済みになるためエンティティ化しない

				// エンティティ化
				//FallingSand fSand = headBlock.getWorld().spawn(headBlock.getLocation().clone().add(0.5D, 0.5D, 0.5D), FallingSand.class);
				FallingBlock fBlock = headBlock.getWorld().spawnFallingBlock(loc, headBlock.getType(), (byte) 0);
				Vector vect = new Vector(0.0D, flyVector, 0.0D);
				fBlock.setVelocity(vect);

				headBlock.setTypeIdAndData(Material.AIR.getId(), (byte)0, true);
				//headBlock.getWorld().refreshChunk(headBlock.getX(), headBlock.getZ());
			}
		}
	}
}
