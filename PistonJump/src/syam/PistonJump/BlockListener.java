package syam.PistonJump;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.util.Vector;

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

public class BlockListener implements Listener {
	public final static Logger log = PistonJump.log;
	private static final String logPrefix = PistonJump.logPrefix;
	private static final String msgPrefix = PistonJump.msgPrefix;

	private final PistonJump plugin;

	public BlockListener(PistonJump plugin){
		this.plugin = plugin;
	}

	/* 登録するイベントはここから下に */

	// ピストンが展開した
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPistonExtend(BlockPistonExtendEvent event){
		Block block = event.getBlock();
		BlockFace direction = event.getDirection();
		Block headBlock = block.getRelative(direction, 1);

		// 押した先のブロックが空気(0:AIR)の場合
		if (headBlock.getType() == Material.AIR){
			// 飛ばす強さ(Y軸方向のベクトル) 真下の看板によって変更されない場合はこの値
			double flyVector = 3.0D;
			// 横方向へ飛ばす強さ(XZ軸方向のベクトルへ掛ける) 変更されない場合は1.0倍
			double sideMultiply = 1.0D;

			// 落下死対策のジャンプポーション効果時間(sec)
			//int potionDurationInSec = 6;

			/* 横向きのピストンでの動作が無効に設定されかつ、上向きでないピストンは何もしない */
			if (direction != BlockFace.UP && !plugin.getConfigs().enableSidewaysPiston){
				return;
			}


			// add(0.5, 0.0, 0.5) は上向きの場合？
			Location headBlockLoc = headBlock.getLocation().add(0.5, 0.0, 0.5);

			// オンラインプレイヤーを走査
			for (Player player : Bukkit.getServer().getOnlinePlayers()){
				Location playerLoc = player.getLocation();

				if (playerLoc.getWorld() != headBlockLoc.getWorld()){
					continue;
				}
				// ピストンに押されたブロックの座標とプレイヤーの座標を計算
				double distance = playerLoc.distance(headBlockLoc);

				// ピストンの上に居ることの
				if (distance >= 1.0){
					continue;
				}

				// 権限チェック
				if (!plugin.getConfigs().ignorePermission && !player.hasPermission("pistonjump.jump")){
					continue;
				}

				// プレイヤーのベクトルを飛ばすためのベクトル初期値に
				Vector dir = player.getVelocity();
				Vector vect = null;

				// ピストンの方向によってベクトルを分ける
				if (direction == BlockFace.UP){
					// 上方向
					vect = new Vector(dir.getX() * 3.0D, flyVector, dir.getZ() * 3.0D);
				}else if (direction == BlockFace.EAST){
					// 東向き→実際には北向き？ Z軸を負に
					vect = new Vector(0.0D * sideMultiply, 0, -3.0D * sideMultiply);
				}else if(direction == BlockFace.WEST){
					// 西向き→実際には南 Z軸を正に
					vect = new Vector(0.0D * sideMultiply, 0, 3.0D * sideMultiply);
				}else if(direction == BlockFace.SOUTH){
					// 南向き→東 X軸を正に
					vect = new Vector(3.0D * sideMultiply, 0, 0.0D * sideMultiply);
				}else if(direction == BlockFace.NORTH){
					// 北向き→西 X軸を負に
					vect = new Vector(-3.0D * sideMultiply, 0, 0.0D * sideMultiply);
				}

				// 上手く飛ぶようにプレイヤーを浮かす
				player.teleport(playerLoc.add(0, 0.5, 0));
				player.setVelocity(vect); // 飛ばす

				// 落下死対策
				/*
				if (player.hasPotionEffect(PotionEffectType.JUMP))
					player.removePotionEffect(PotionEffectType.JUMP);
				player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, potionDurationInSec * 20, 0));

				Actions.message(null, player, "Fly!");*/
			}
		}
		/*
		// 押した先のブロックが砂(12:SAND)の場合
		//else if (headBlock.getType() == Material.SAND){
			// 上向きのピストンのみ対応
			if (direction == BlockFace.UP){
				// スポーンさせる座標
				Location loc = headBlock.getLocation().clone().add(0.5D, 30.0D, 0.5D); // Y=1.5だと丁度着地済みになるためエンティティ化しない

				// エンティティ化
				/*
				 * MEMO: 12/06/27現在、Bukkitのバグで通常通り砂、砂岩のエンティティをスポーンさせることができない → CraftBukkitを使う
				 *
				 * FallingSand fSand = headBlock.getWorld().spawn(headBlock.getLocation().clone().add(0.5D, 0.5D, 0.5D), FallingSand.class);
				 * Exception: Caused by: java.lang.IllegalArgumentException: Don't know how to add class net.minecraft.server.EntityFallingBlock!
				 *//*

				Entity fSand = null;

				// CraftBukkit Start
				net.minecraft.server.World cWorld = ((CraftWorld)headBlock.getWorld()).getHandle();
				EntityFallingBlock cSand = new EntityFallingBlock(cWorld, loc.getX(), loc.getY(), loc.getZ(), 12, cWorld.getData(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
				headBlock.setType(Material.AIR); // Non-CraftBukkit
				cWorld.addEntity(cSand);
				cSand.getBukkitEntity().setVelocity(cSand.getBukkitEntity().getVelocity().setY(3.0D)); // 飛ばす
				// CraftBukkit End

				//event.getBlocks().get(0).setType(Material.AIR);

				// ベクトル設定
				//Vector vect = new Vector(0.0D, 3.0D, 0.0D);

				//fSand.setVelocity(vect);

				// チャンク情報を再送信してみる？
//				Block upBlock = headBlock.getRelative(BlockFace.UP, 1);
//				for (Player p : headBlock.getWorld().getPlayers()){
//					if(p.getLocation().distance(headBlock.getLocation()) < 50){
//						((CraftPlayer)p).getHandle().netServerHandler.sendPacket(
//								new Packet51MapChunk()
//							)
//					}
//				}
			}
		}// 砂利(13:GRAVEL)の場合
		else if (headBlock.getType() == Material.GRAVEL){

		}
		*/
	}
}
