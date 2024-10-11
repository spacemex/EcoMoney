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

import com.github.spacemex.ecomoney.events.ServerEvents;
import com.github.spacemex.ecomoney.providers.economy.EconomyProvider;
import com.github.spacemex.ecomoney.sql.SQLManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class Ecomoney extends JavaPlugin {

    private SQLManager sqlManager;
    private EconomyProvider economy;

    @Override
    public void onEnable() {
        getLogger().info("Plugin Enabling");

        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getServer().getPluginManager().disablePlugin(this);
            getLogger().severe("Vault dependency not found. Disabling plugin.");
            return;
        }

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        try {
            sqlManager = new SQLManager(this); // initialize SQLManager
            sqlManager.connect(); // ensure connection is established
        } catch (SQLException e) {
            getServer().getPluginManager().disablePlugin(this);
            getLogger().severe("Failed to establish database connection: " + e.getMessage());
            return;
        }

        try {
            economy = new EconomyProvider(this);
        } catch (Exception e) {
            getServer().getPluginManager().disablePlugin(this);
            getLogger().severe("Failed to set up EconomyProvider: " + e.getMessage());
            return;
        }

        getServer().getPluginManager().registerEvents(new ServerEvents(this), this);
        new Commands(this);

        getLogger().info("Plugin Enabled");
    }

    @Override
    public void onDisable() {
        getLogger().severe("Plugin Disabled");
    }

    public SQLManager getSqlManager() {
        return sqlManager;
    }

    public EconomyProvider getEconomy() {
        return economy;
    }
}