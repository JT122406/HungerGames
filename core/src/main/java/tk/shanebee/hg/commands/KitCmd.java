package tk.shanebee.hg.commands;

import tk.shanebee.hg.Status;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.util.Util;

import java.util.Objects;

public class KitCmd extends BaseCmd {

	public KitCmd() {
		forcePlayer = true;
		cmdName = "kit";
		forceInGame = true;
		argLength = 2;
		usage = "<kit>";
	}

	@Override
	public boolean run() {
		Game game = Objects.requireNonNull(playerManager.getPlayerData(player)).getGame();
		Status st = game.getGameArenaData().getStatus();
		if (!game.getKitManager().hasKits()) {
		    Util.scm(player, lang.kit_disabled);
		    return false;
        }
		if (st == Status.WAITING || st == Status.COUNTDOWN) {
			game.getKitManager().setKit(player, args[1]);
			//Bukkit.getScheduler().runTaskLater(plugin, () -> game.getKitManager().setKit(player, args[1]), 10L);
		} else {
			Util.scm(player, lang.cmd_kit_no_change);
		}
		return true;
	}

}
