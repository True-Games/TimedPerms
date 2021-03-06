package timedpermissions.commands;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import timedpermissions.subscription.Storage;
import timedpermissions.subscription.Subscription;
import timedpermissions.subscription.SubscriptionMainGroup;

public class SubCommandSetMainGroup implements SubCommand {

	private final Storage storage;
	public SubCommandSetMainGroup(Storage storage) {
		this.storage = storage;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void handle(CommandSender sender, String[] args) {
		String username = args[0];
		OfflinePlayer player = Bukkit.getOfflinePlayer(username);
		if (!player.isOnline() && !player.hasPlayedBefore()) {
			sender.sendMessage(ChatColor.RED + MessageFormat.format("Player {0} never played on server before", username));
			return;
		}
		String groupName = args[1];
		String time = args[2];
		try {
			int days = Integer.parseInt(time);
			for (Subscription sub : storage.getSubscriptions()) {
				if (sub instanceof SubscriptionMainGroup) {
					if (sub.getPlayerUUID().equals(player.getUniqueId())) {
						if (((SubscriptionMainGroup) sub).getGroupName().equals(groupName)) {
							days += TimeUnit.MILLISECONDS.toDays(sub.getExpireTime() - System.currentTimeMillis());
						}
						storage.removeSubscription(sub);
						break;
					}
				}
			}
			Subscription sub = new SubscriptionMainGroup(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(days), player.getUniqueId(), groupName);
			String errMsg = sub.validate();
			if (errMsg != null) {
				sender.sendMessage(ChatColor.RED + MessageFormat.format("Error: {0}", errMsg));
			} else {
				storage.addSubscription(sub);
				sub.apply();
				sender.sendMessage(ChatColor.YELLOW + MessageFormat.format("Changed {0} main group to {1} for {2} days", username, groupName, days));
			}
		} catch (NumberFormatException e) {
			sender.sendMessage(ChatColor.RED + MessageFormat.format("{0} is not a valid number", time));
		}
	}

}
