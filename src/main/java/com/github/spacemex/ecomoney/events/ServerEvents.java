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
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ServerEvents implements Listener {
    private final SQLManager sqlManager;

    public ServerEvents(Ecomoney plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        sqlManager = plugin.getSqlManager();
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        OfflinePlayer player = event.getPlayer();
        if (!sqlManager.hasAccount(player.getUniqueId())) {
            sqlManager.createPlayerAccount(player);
        }
    }
}
