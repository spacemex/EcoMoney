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