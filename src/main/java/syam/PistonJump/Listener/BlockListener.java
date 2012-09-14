package syam.PistonJump.Listener;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import syam.PistonJump.PistonJump;
import syam.PistonJump.Util.Actions;
import syam.PistonJump.Util.Util;

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

	public BlockListener(final PistonJump plugin){
		this.plugin = plugin;
	}

	/* 登録するイベントはここから下に */

	// 看板を設置した
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onSignChange(final SignChangeEvent event){
		Player player = event.getPlayer();
		Block block = event.getBlock();
		BlockState state = event.getBlock().getState();

		if (state instanceof Sign){
			Sign sign = (Sign)state;

			/* [PistonJump] 特殊看板 */

			if (event.getLine(0).toLowerCase().indexOf("[pistonjump]") != -1){
				// 権限チェック
				if (!player.hasPermission("pistonjump.placesign")){
					event.setLine(0, "§c[PistonJump]");
					event.setLine(1, "Perm Denied :(");
					Actions.message(null, player, "&cYou don't have permission to use this!");
					return;
				}
				// 権限あり 入力内容チェック
				else{
					boolean err = false; // エラーフラグ

					String line2s = event.getLine(1).trim();
					if (!Util.isDouble(line2s)){
						Actions.message(null, player, "&cThe 2nd line must be numeric (double)!"); err = true;
					}else{
						Double line2d = Double.parseDouble(line2s);
						if (line2d < 0.0D){
							Actions.message(null, player, "&cThe 2nd numeric cannot be negative! Changed to 0.0!");
							event.setLine(1, "0.0");
						}else if(line2d > 8.0D){
							Actions.message(null, player, "&cThe 2nd value is too big! Changed to max value!");
							event.setLine(1, "8.0");
						}
					}

					// 1行目の文字色
					if (err){
						event.setLine(0, "§c[PistonJump]");
					}else{
						event.setLine(0, "§a[PistonJump]");
					}
				}
			}
		}
	}
}
