/*
 * Copyright (c) 2024. Space_Mex
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.spacemex.ecomoney;

import com.github.spacemex.ecomoney.providers.economy.EconomyProvider;
import org.bukkit.*;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class Commands {
    private final Logger logger;
    private final EconomyProvider economyProvider;
    private final boolean useVerboseLogging;
    private final FileConfiguration config;
    private final Ecomoney plugin;

    public Commands(Ecomoney plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.economyProvider = plugin.getEconomy();
        this.useVerboseLogging = plugin.getConfig().getBoolean("verbose-logging");
        this.config = plugin.getConfig();

        Objects.requireNonNull(plugin.getCommand("balance")).setExecutor(new BalanceCommand());
        Objects.requireNonNull(plugin.getCommand("ecogive")).setExecutor(new GiveCommand());
        Objects.requireNonNull(plugin.getCommand("ecoreset")).setExecutor(new ResetCommand());
        Objects.requireNonNull(plugin.getCommand("ecotake")).setExecutor(new TakeCommand());
        Objects.requireNonNull(plugin.getCommand("pay")).setExecutor(new PayCommand());
        Objects.requireNonNull(plugin.getCommand("ecodelete")).setExecutor(new DeleteCommand());
        Objects.requireNonNull(plugin.getCommand("withdraw")).setExecutor(new WithdrawCommand());
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        if (sender instanceof Server){
            if (useVerboseLogging){
                sender.sendMessage("Sender is Server bypassing permission check.");
            }
            return true;
        }
        if (sender instanceof ConsoleCommandSender){
            if (useVerboseLogging){
                sender.sendMessage("Sender is Console bypassing permission check.");
            }
            return true;
        }
        if (sender instanceof CommandBlock){
            if (useVerboseLogging){
                sender.sendMessage("Sender is CommandBlock blocking command usage");
            }
            return false;
        }
        if (sender instanceof Player){
            if (useVerboseLogging){
                if (sender.hasPermission(permission)){
                    logger.info("sender has permission");
                }
                if (sender.isOp()){
                    logger.info("sender is op");
                }
                if (sender.hasPermission("ecomoney.*")){
                    logger.info("sender has ecomoney.* bypassing check");
                }
                if (sender.hasPermission("*")){
                    logger.info("sender has * bypassing check");
                }
            }
            return sender.hasPermission(permission) || sender.isOp()
                    || sender.hasPermission("ecomoney.*") || sender.hasPermission("*");
        }
        if (useVerboseLogging){
            logger.info("sender is not a player or server, Bailing out of permission check.");
        }
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
            if (useVerboseLogging){
                logger.info("BalanceCommand executed by " + sender.getName() + " with args " + String.join(" ", args));
            }
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

    public class GiveCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (useVerboseLogging){
                logger.info("GiveCommand executed by " + sender.getName() + " with args " + String.join(" ", args));
            }
            if (!hasPermission(sender, "ecomoney.give")) {
                sender.sendMessage(ChatColor.RED + "Invalid permissions.");
                return true;  // Don't return false here, as it will show usage message.
            }

            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Invalid arguments. Usage: /give <player> <amount>");
                return true;  // Shows the usage message from plugin.yml
            }

            String targetName = args[0];
            String amountStr = args[1];
            double amount;

            // Parse the amount safely
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount. Please enter a numeric value.");
                return true;
            }

            OfflinePlayer targetPlayer = sender.getServer().getOfflinePlayer(targetName);

            if (!economyProvider.hasAccount(targetPlayer)) {
                sender.sendMessage(ChatColor.RED + "Player does not have an account.");
                return true;
            }

            // Perform the deposit
            economyProvider.depositPlayer(targetPlayer.getName(), amount);
            sender.sendMessage(ChatColor.GREEN + "Successfully gave " + ChatColor.GOLD + amount + ChatColor.GREEN + " to " + ChatColor.GOLD + targetName);
            return true;
        }
    }

    public class ResetCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (useVerboseLogging){
                logger.info("ResetCommand executed by " + sender.getName() + " with args " + String.join(" ", args));
            }
            if (!hasPermission(sender, "ecomoney.reset")) {
                sender.sendMessage(ChatColor.RED + "Invalid permissions.");
                return true;
            }
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Invalid arguments. Usage: /ecogive <player> <amount>");
                return true;
            }

            String targetName = args[0];
            OfflinePlayer player = sender.getServer().getOfflinePlayer(targetName);
            if (!economyProvider.hasAccount(player)) {
                sender.sendMessage(ChatColor.RED + "Player does not have an account.");
                return true;
            }

            if (economyProvider.resetPlayerAccount(player)){
                sender.sendMessage(ChatColor.GREEN + "Successfully Reset: " + ChatColor.GOLD + targetName + "'s Account");
                return true;
            }else {
                sender.sendMessage(ChatColor.RED + "Unknown Error. Please contact an administrator.");
                return true;
            }
        }
    }

    public class TakeCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (useVerboseLogging){
                logger.info("TakeCommand executed by " + sender.getName() + " with args " + String.join(" ", args));
            }
            if (!hasPermission(sender, "ecomoney.take")) {
                sender.sendMessage(ChatColor.RED + "Invalid permissions.");
                return true;
            }
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Invalid arguments. Usage: /ecotake <player> <amount>");
                return true;
            }
            String targetName = args[0];
            String amountStr = args[1];
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount. Please enter a numeric value.");
                return true;
            }
            OfflinePlayer targetPlayer = sender.getServer().getOfflinePlayer(targetName);
            if (!economyProvider.hasAccount(targetPlayer)) {
                sender.sendMessage(ChatColor.RED + "Player does not have an account.");
                return true;
            }
            if (economyProvider.getBalance(targetPlayer) < amount) {
                sender.sendMessage(ChatColor.GOLD + "Player does not have enough money: " + economyProvider.format(economyProvider.getBalance(targetPlayer)));
                return true;
            }
            economyProvider.withdrawPlayer(targetPlayer.getName(), amount);
            sender.sendMessage(ChatColor.GREEN + "Successfully Took " + ChatColor.GOLD + amount + ChatColor.GREEN + " from " + ChatColor.GOLD + targetName);
            return true;
        }
    }

    public class PayCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (useVerboseLogging){
                logger.info("PayCommand executed by " + sender.getName() + " with args " + String.join(" ", args));
            }
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                return true;
            }
            if (!hasPermission(sender, "ecomoney.pay")) {
                sender.sendMessage(ChatColor.RED + "Invalid permissions.");
                return true;
            }
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Invalid arguments. Usage: /pay <player> <amount>");
                return true;
            }
            String targetName = args[0];
            String amountStr = args[1];
            double amount;

            try{
                amount = Double.parseDouble(amountStr);
            }catch (NumberFormatException e){
                sender.sendMessage(ChatColor.RED + "Invalid amount. Please enter a numeric value.");
                return true;
            }
            OfflinePlayer targetPlayer = sender.getServer().getOfflinePlayer(targetName);
            if (!economyProvider.hasAccount(targetPlayer)) {
                sender.sendMessage(ChatColor.RED + "Player does not have an account.");
                return true;
            }
            if(!economyProvider.hasAccount(sender.getName())){
                sender.sendMessage(ChatColor.RED + "You do not have an account. Please contact an administrator.");
                return true;
            }
            if (!economyProvider.has(sender.getName(), amount)) {
                sender.sendMessage(ChatColor.RED + "You do not have enough money: " + economyProvider.format(economyProvider.getBalance(sender.getName())));
                return true;
            }

            economyProvider.withdrawPlayer(sender.getName(), amount);
            economyProvider.depositPlayer(targetPlayer.getName(), amount);
            sender.sendMessage(ChatColor.GREEN + "Successfully Paid " + ChatColor.GOLD + amount + ChatColor.GREEN + " to " + ChatColor.GOLD + targetName);
            return true;
        }
    }

    public class DeleteCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (useVerboseLogging){
                logger.info("DeleteCommand executed by " + sender.getName() + " with args " + String.join(" ", args));
            }
            if (!hasPermission(sender, "ecomoney.delete")) {
                sender.sendMessage(ChatColor.RED + "Invalid permissions.");
                return true;
            }
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Invalid arguments. Usage: /ecodelete <player>");
                return true;
            }
            String targetName = args[0];
            OfflinePlayer targetPlayer = sender.getServer().getOfflinePlayer(targetName);
            if (!economyProvider.hasAccount(targetPlayer)) {
                sender.sendMessage(ChatColor.RED + "Player does not have an account.");
                return true;
            }
            if (economyProvider.deletePlayerAccount(targetPlayer)) {
                sender.sendMessage(ChatColor.GREEN + "Successfully Deleted: " + ChatColor.GOLD + targetName + "'s Account");
            }
            else {
                sender.sendMessage(ChatColor.RED + "Unknown Error. Please contact an administrator.");
            }
            return true;
        }
    }

    public class WithdrawCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!plugin.getConfig().getBoolean("physical-currency.enabled")) return true;

            if (useVerboseLogging) {
                logger.info("WithdrawCommand executed by " + sender.getName() + " with args " + String.join(" ", args));
            }

            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("ecomoney.withdraw")) {
                player.sendMessage(ChatColor.RED + "Invalid permissions.");
                return true;
            }

            if (args.length != 2) {
                player.sendMessage(ChatColor.RED + "Invalid arguments. Usage: /withdraw <type> <amount>");
                return true;
            }

            String type = args[0];
            String amountStr = args[1];
            double amount;

            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid amount. Please enter a numeric value.");
                return true;
            }

            if (!economyProvider.hasAccount(player)) {
                player.sendMessage(ChatColor.RED + "You do not have an account. Please contact an administrator.");
                return true;
            }

            if (type.equalsIgnoreCase("exp")) {
                player.sendMessage(ChatColor.RED + "EXP withdrawal not implemented.");
                return true;
            }

            if (type.equalsIgnoreCase("money")) {
                Material material = Material.matchMaterial(plugin.getConfig().getString("physical-currency.money.item", "PAPER"));
                if (material == null) {
                    material = Material.PAPER; // Fallback to PAPER if the item is not found
                }
                String displayName = formatConfig(plugin.getConfig().getString("physical-currency.money.display-name", "&eBank Note"),
                        player, economyProvider.format(amount));
                boolean glowing = plugin.getConfig().getBoolean("physical-currency.money.glowing", false);
                List<String> lore = formatConfig(plugin.getConfig().getStringList("physical-currency.money.lore"),
                        player, economyProvider.format(amount));

                ItemStack itemStack = new ItemStack(material, 1);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(displayName);
                    meta.setLore(lore);
                    if (glowing) {
                        meta.addEnchant(Enchantment.LURE, 1, true);
                        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                    }
                    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "balance"), PersistentDataType.DOUBLE, amount);
                    itemStack.setItemMeta(meta);
                } else {
                    player.sendMessage(ChatColor.RED + "Error creating item metadata.");
                    return true;
                }

                if (economyProvider.getBalance(player) < amount) {
                    player.sendMessage(ChatColor.RED + "You do not have enough money: " + economyProvider.format(economyProvider.getBalance(player)));
                    return true;
                }

                economyProvider.withdrawPlayer(player, amount);
                player.getInventory().addItem(itemStack);
                player.saveData();
                player.sendMessage(ChatColor.GREEN + "Successfully withdrew a bank note worth " + economyProvider.format(amount) + "!");
                return true;
            }

            return false;
        }

        private String formatConfig(String config, Player player, String amount) {
            if (config == null) {
                return "";
            }
            String result = ChatColor.translateAlternateColorCodes('&', config);
            return result.replace("%amount%", amount).replace("%signer%", player.getName());
        }

        private List<String> formatConfig(List<String> config, Player player, String amount) {
            List<String> formattedList = new ArrayList<>();
            if (config != null) {
                for (String line : config) {
                    String formattedLine = ChatColor.translateAlternateColorCodes('&', line);
                    formattedList.add(formattedLine.replace("%amount%", amount).replace("%signer%", player.getName()));
                }
            }
            return formattedList;
        }
    }
}