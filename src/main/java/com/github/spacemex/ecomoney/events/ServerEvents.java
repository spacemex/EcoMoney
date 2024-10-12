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

package com.github.spacemex.ecomoney.events;

import com.github.spacemex.ecomoney.Ecomoney;
import com.github.spacemex.ecomoney.sql.SQLManager;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ServerEvents implements Listener {
    private final SQLManager sqlManager;
    private final FileConfiguration config;
    private final Ecomoney plugin;
    private static final String KEY_BALANCE = "balance";

    public ServerEvents(Ecomoney plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        sqlManager = plugin.getSqlManager();
        this.config = plugin.getConfig();
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        OfflinePlayer player = event.getPlayer();
        if (!sqlManager.hasAccount(player.getUniqueId())) {
            sqlManager.createPlayerAccount(player);
        }
    }

    @EventHandler
    public void onPlayerInteractWithBankNote(PlayerInteractEvent event) {
        if (event.getItem() != null
                && event.getItem().getType() == Material.valueOf(plugin.getConfig().getString("item", "PAPER").toUpperCase())
                && event.getItem().getItemMeta() != null) {

            ItemMeta meta = event.getItem().getItemMeta();
            NamespacedKey balanceKey = new NamespacedKey(plugin, KEY_BALANCE);

            if (meta.getPersistentDataContainer().has(balanceKey, PersistentDataType.DOUBLE)) {
                double balance = meta.getPersistentDataContainer().get(balanceKey, PersistentDataType.DOUBLE);

                if (!plugin.getEconomy().hasAccount(event.getPlayer())) {
                    event.getPlayer().sendMessage(ChatColor.RED + "You do not have an account. Please contact an administrator.");
                    return;
                }

                plugin.getEconomy().depositPlayer(event.getPlayer(), balance);
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

                // Reduce the amount of items by 1 in the stack
                event.getItem().setAmount(event.getItem().getAmount() - 1);
                event.setCancelled(true);
                event.getPlayer().saveData();

                // Feedback message
                event.getPlayer().sendMessage(ChatColor.GREEN + "Deposited " + balance + " to your account!");
            }
        }
    }
}
