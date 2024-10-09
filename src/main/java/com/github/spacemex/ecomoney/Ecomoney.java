package com.github.spacemex.ecomoney;

import com.github.spacemex.ecomoney.sql.SQLManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Ecomoney extends JavaPlugin {

    private SQLManager sqlManager;

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public SQLManager getSqlManager() {
        if (sqlManager == null) {
            return null;
        }
        return sqlManager;
    }

}
