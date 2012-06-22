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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

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
			// 落下死対策のジャンプポーション効果時間(sec)
			//int potionDurationInSec = 6;

			// 上向きのピストンの場合
			if (direction == BlockFace.UP){
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
					if (distance >= 1.0){
						continue;
					}

					// 権限で弾く
					if (!player.hasPermission("pistonjump.jump")){
						continue;
					}

					// プレイヤーのベクトルを初期値に
					Vector dir = player.getVelocity();
					Vector vect = new Vector(dir.getX() * 3.0D, flyVector, dir.getZ() * 3.0D);

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
		}
	}
}
