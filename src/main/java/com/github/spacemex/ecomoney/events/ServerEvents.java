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
