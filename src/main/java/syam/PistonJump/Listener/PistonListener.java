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

		// 押した先が空気の場合
		if (headBlock.getType() == Material.AIR){
			double flyVector = plugin.getConfigs().playerDefaultPower;

			// 横向きのピストンでの動作が無効に設定されかつ、上向きでないピストンは何もしない
			if (direction != BlockFace.UP && !plugin.getConfigs().playerEnableSidewaysPiston){
				return;
			}

			// 設定が有効ならば真下の看板をチェックする
			if (plugin.getConfigs().playerCheckSign && Actions.checkUnderSign(block) >= 0.0D){
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
			Vector vect = Actions.getEjectionVector(direction, flyVector);

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
				if (!plugin.getConfigs().playerIgnorePermission && !player.hasPermission("pistonjump.jump")){
					continue;
				}

				player.teleport(playerLoc.add(0, 0.5, 0), TeleportCause.PLUGIN);
				player.setVelocity(vect); // 飛ばす
			}
		}

		// 押した先がブロックの場合
		else{
			// Check Block.Enable config
			if (!plugin.getConfigs().blockEnable){
				return;
			}

			double flyVector = plugin.getConfigs().blockDefaultPower;

			// 横向きのピストンでの動作が無効に設定されかつ、上向きでないピストンは何もしない
			if (direction != BlockFace.UP && !plugin.getConfigs().blockEnableSidewaysPiston){
				return;
			}

			// 設定が有効ならば真下の看板をチェックする
			if (plugin.getConfigs().blockCheckSign && Actions.checkUnderSign(block) >= 0.0D){
				flyVector = Actions.checkUnderSign(block);
			}

			// 飛ばす強さチェック
			if (flyVector <= 0.0D){
				return;
			}
			else if (flyVector > 8.0D){
				flyVector = 8.0D;
			}

			// ベクトル設定
			Vector vect = Actions.getEjectionVector(direction, flyVector);

			// スポーンさせる座標
			Location loc = headBlock.getLocation().clone().add(0.5D, 1.5D, 0.5D);

			// エンティティ化
			//FallingSand fSand = headBlock.getWorld().spawn(headBlock.getLocation().clone().add(0.5D, 0.5D, 0.5D), FallingSand.class);
			FallingBlock fBlock = headBlock.getWorld().spawnFallingBlock(loc, headBlock.getType(), (byte) 0);
			fBlock.setVelocity(vect);

			// 飛ばしたブロックを消す →不具合で正常にクライアントがアップデートされない
			// Bukkitに報告済み Issue:BUKKIT-2514 :: https://bukkit.atlassian.net/browse/BUKKIT-2514
			headBlock.setTypeIdAndData(Material.AIR.getId(), (byte)0, true);
		}
	}
}
