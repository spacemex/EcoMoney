package com.github.spacemex.ecomoney;

import com.github.spacemex.ecomoney.providers.economy.EconomyProvider;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.logging.Logger;

public class Commands {
    private final Logger logger;
    private final EconomyProvider economyProvider;

    public Commands(Ecomoney plugin) {
        this.logger = plugin.getLogger();
        this.economyProvider = plugin.getEconomy();
        Objects.requireNonNull(plugin.getCommand("balance")).setExecutor(new BalanceCommand());
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        if (sender instanceof Server) return true;
        if (sender instanceof CommandBlock) return false;
        if (sender instanceof Player) return sender.hasPermission(permission) || sender.isOp()
                || sender.hasPermission("ecomoney.*") || sender.hasPermission("*");
        return false;
    }

    // Simplified permission check methods

    private boolean hasPermissions(CommandSender sender, String[] permissions) {
        if (sender instanceof Server) return true;
        if (sender instanceof CommandBlock) return false;
        if (sender instanceof Player) {
            for (String perm : permissions) {
                if (sender.hasPermission(perm)) {
                    return true;
                }
            }
        }
        return false;
    }

    public class BalanceCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            logger.info("BalanceCommand executed by " + sender.getName() + " with args " + String.join(" ", args));

            if (args.length == 0) {
                // Handling case with no arguments
                if (sender instanceof Player player) {
                    if (hasPermission(sender, "ecomoney.balance")) {
                        if (!economyProvider.hasAccount(player)) {
                            sender.sendMessage(ChatColor.RED + "You do not have an account. Please contact an administrator.");
                            return true;
                        }
                        double balance = economyProvider.getBalance(player);
                        String formattedBalance = economyProvider.format(balance);
                        sender.sendMessage(ChatColor.GREEN + "Balance: " + ChatColor.GOLD + formattedBalance);
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "Invalid permissions.");
                        return true;
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command without arguments.");
                    return true;
                }
            } else if (args.length == 1) {
                // Handling case with one argument
                if (!(sender instanceof Player) || hasPermission(sender, "ecomoney.balance.other")) {
                    String targetName = args[0];
                    OfflinePlayer targetPlayer = sender.getServer().getOfflinePlayer(targetName);

                    if (!economyProvider.hasAccount(targetPlayer)) {
                        sender.sendMessage(ChatColor.RED + "Player does not have an account.");
                        return true;
                    }

                    double balance = economyProvider.getBalance(targetPlayer);
                    String formattedBalance = economyProvider.format(balance);
                    sender.sendMessage(ChatColor.GREEN + targetName + "'s Balance: " + ChatColor.GOLD + formattedBalance);
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid permissions.");
                    return true;
                }
            }
            return false;
        }
    }
}