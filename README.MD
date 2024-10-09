# Ecomoney

Ecomoney is a lightweight and simple economy plugin for Minecraft servers running Spigot or Paper Spigot (version 1.21+). It supports both MySQL and SQLite databases and can either register as its own economy provider or allow another plugin to be the provider. This plugin is designed to be efficient and straightforward, making it ideal for servers that need a robust yet uncomplicated economy system.

## Features

- Supports MySQL and SQLite databases.
- Integrates with Vault for economy-related functions.
- Provides a set of simple and intuitive commands for managing the economy.
- Can act as its own economy provider or defer to another plugin.
- Lightweight and easy-to-use.
- More coming soon!

## Commands

- **/pay `<player>` `<amount>`**: Pay another player.
- **/deposit `<amount>`**: Deposit money into your account.
- **/withdraw `<amount>`**: Withdraw money from your account as a physical item.
- **/give `<player>` `<amount>`**: Give money to a player.
- **/take `<player>` `<amount>`**: Take money from a player.
- **/reset `<player>`**: Reset a player's balance.
- **/set `<player>` `<amount>`**: Set a player's balance.
- **/delete `<player>`**: Delete a player's account.

## Permissions

- **ecomoney.pay**: Allows the player to use the `/pay` command.
- **ecomoney.deposit**: Allows the player to use the `/deposit` command.
- **ecomoney.withdraw**: Allows the player to use the `/withdraw` command.
- **ecomoney.give**: Allows the player to use the `/give` command.
- **ecomoney.take**: Allows the player to use the `/take` command.
- **ecomoney.reset**: Allows the player to use the `/reset` command.
- **ecomoney.set**: Allows the player to use the `/set` command.
- **ecomoney.delete**: Allows the player to use the `/delete` command.

## Installation

1. Download the latest version of the plugin jar file from the (coming soon! [Spigot]()).
2. Place the jar file in your server's `plugins` folder.
3. Restart your server to generate the configuration files.

## Configuration

Once the plugin is loaded, you can find the configuration file in the `plugins/Ecomoney/config.yml`. Edit this file to configure the database connection (MySQL or SQLite) and any other settings.

### Example `config.yml`

```yaml

setup-provider: true
starting-balance: 500.0


data:
  storage-file: sqlite
mysql:
  host: "localhost"
  port: 3306
  database: "economy"
  user: "admin"
  password: 1234
sqlite:
  path: plugins/ecomoney/econ/Economy.db
```

## Dependencies

- **Vault**: Required for economy functions. The plugin depends on Vault to interface with the economy system.

## License

This project is licensed under the GNU General Public License v3.0. See [LICENSE.md](./LICENSE.md) for more details.

---

### Contributing

We welcome contributions! Please see our [CONTRIBUTING.md](./CONTRIBUTING.md) file for more details on how to get started.

### Support

If you encounter any issues or have questions, feel free to create an issue in the [GitHub repository](https://github.com/spacemex/EcoMoney/issues).

---

Thank you for using Ecomoney! We hope it helps enhance your server's economy system.