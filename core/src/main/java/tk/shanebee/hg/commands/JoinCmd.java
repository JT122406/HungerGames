package tk.shanebee.hg.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.util.Util;

import java.util.List;

public class JoinCmd extends BaseCmd {

	public JoinCmd() {
		forcePlayer = true;
		cmdName = "join";
		forceInGame = false;
		argLength = 2;
		usage = "<arena-name>";
	}

	@Override
	public boolean run() {
		if (playerManager.hasPlayerData(player) || playerManager.hasSpectatorData(player)) {
			Util.scm(player, HG.getPlugin().getLang().cmd_join_in_game);
		} else {
			Game g = gameManager.getGame(args[1]);
			if (g != null && !g.getGamePlayerData().getPlayers().contains(player.getUniqueId())) {
				if (!HG.getParty().hasParty(player)) {
					//no party
					g.getGamePlayerData().join(player, true);
					return true;
				}
				else if ((HG.getParty().isOwner(player)) &&  (g.getGamePlayerData().getPlayers().size() + HG.getParty().partySize(player)) <= g.getGameArenaData().getMaxPlayers())
				{
					List<Player> party = HG.getParty().getMembers(player);
					for (int i = 0; i < party.size(); i++) {
						Player p = party.get(i);
						if (i != 0){
							Bukkit.getScheduler().runTaskLater(HG.getPlugin(), () -> g.getGamePlayerData().join(p, true), 20);
							continue;
						}
						g.getGamePlayerData().join(p, true);
					}

				} else if (!HG.getParty().isOwner(player)) {
					player.sendMessage("You are in a party but not the leader, unable to join game");
				} else {
					player.sendMessage("Party is too Large to join this arena");
				}
			} else {
				Util.scm(player, lang.cmd_delete_noexist);
			}
		}
		return true;
	}

}
