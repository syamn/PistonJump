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
					vect = new Vector(0.0D * sideMultiply, 0, -2.0D * sideMultiply);
				}else if(direction == BlockFace.WEST){
					// 西向き→実際には南 Z軸を正に
					vect = new Vector(0.0D * sideMultiply, 0, 2.0D * sideMultiply);
				}else if(direction == BlockFace.SOUTH){
					// 南向き→東 X軸を正に
					vect = new Vector(2.0D * sideMultiply, 0, 0.0D * sideMultiply);
				}else if(direction == BlockFace.NORTH){
					// 北向き→西 X軸を負に
					vect = new Vector(-2.0D * sideMultiply, 0, 0.0D * sideMultiply);
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
	}
}
