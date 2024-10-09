package com.github.spacemex.ecomoney.sql;

import com.github.spacemex.ecomoney.Ecomoney;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.Warning;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class SQLManager {
    private Connection mysqlConnection;
    private Connection sqliteConnection;
    private final FileConfiguration config;
    private final Logger logger;
    private String storageType;

    public SQLManager(Ecomoney plugin) throws SQLException {
        this.logger = plugin.getLogger();
        this.config = plugin.getConfig();
    }

    public void connect() throws SQLException {
        this.storageType = config.getString("data.storage-file");

        if ("mysql".equalsIgnoreCase(storageType)) {
            String host = config.getString("mysql.host");
            String port = config.getString("mysql.port");
            String database = config.getString("mysql.database");
            String user = config.getString("mysql.user");
            String password = config.getString("mysql.password");
            connectToMySQL(host, Integer.parseInt(Objects.requireNonNull(port)), database, user, password);
        } else if ("sqlite".equalsIgnoreCase(storageType)) {
            String filePath = config.getString("sqlite.path");
            connectToSQLite(filePath);
        } else {
            logger.severe("Invalid storage file type: ->" + config.getString("data.storage-file"));
            return;
        }

        // Prime the table after connection is established
        primeTable();
    }

    /**
     * Establishes a MySQL connection.
     * @param host MySQL server host.
     * @param port MySQL server port.
     * @param database MySQL database name.
     * @param user MySQL username.
     * @param password MySQL password.
     */
    public void connectToMySQL(String host, int port, String database, String user, String password) throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        mysqlConnection = DriverManager.getConnection(url, user, password);
        logger.info("Connected to MySQL");
    }

    /**
     * Establishes a SQLite connection.
     * @param filePath The file path to the SQLite database file.
     */
    public void connectToSQLite(String filePath) throws SQLException {
        String url = "jdbc:sqlite:" + filePath;
        sqliteConnection = DriverManager.getConnection(url);
        logger.info("Connected to SQLite");
    }

    /**
     * Closes the MySQL connection.
     */
    public void closeMySQLConnection() throws SQLException {
        if (mysqlConnection != null && !mysqlConnection.isClosed()) {
            mysqlConnection.close();
            logger.info("MySQL connection closed");
        }
    }

    /**
     * Closes the SQLite connection.
     */
    public void closeSQLiteConnection() throws SQLException {
        if (sqliteConnection != null && !sqliteConnection.isClosed()) {
            sqliteConnection.close();
            logger.info("SQLite connection closed");
        }
    }

    /**
     * Primes the database table based on the active connection type.
     */
    private void primeTable() throws SQLException {
        String tableSQL = "CREATE TABLE IF NOT EXISTS economy(" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "name VARChar(255) NOT NULL," +
                "balance DOUBLE NOT NULL" +
                ");";

        if ("mysql".equalsIgnoreCase(storageType)) {
            try (Statement stmt = mysqlConnection.createStatement()) {
                stmt.execute(tableSQL);
                logger.info("MySQL table primed successfully.");
            }
        } else if ("sqlite".equalsIgnoreCase(storageType)) {
            try (Statement stmt = sqliteConnection.createStatement()) {
                stmt.execute(tableSQL);
                logger.info("SQLite table primed successfully.");
            }
        }
    }

    /**
     * Retrieves the active database connection based on the storage type.
     * @return the active Connection object.
     * @throws SQLException if no active connection is found.
     */
    private Connection getConnection() throws SQLException {
        if ("mysql".equalsIgnoreCase(storageType) && mysqlConnection != null) {
            return mysqlConnection;
        } else if ("sqlite".equalsIgnoreCase(storageType) && sqliteConnection != null) {
            return sqliteConnection;
        } else {
            throw new SQLException("No active connection found for the configured storage type.");
        }
    }

    /**
     * Checks if an account with the given name exists, case-insensitively.
     * @param name The name to check.
     * @return true if the account exists, false otherwise.
     */
    public boolean hasAccount(String name) {
        String query = "SELECT COUNT(*) FROM economy WHERE LOWER(name) = Lower(?)";

        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1,name);
            try (ResultSet resultSet = statement.executeQuery()){
                if (resultSet.next()){
                    return resultSet.getInt(1) > 0;
                }
            }
        }catch (SQLException e){
            logger.severe("Error Checking account existence: " + e.getMessage());
        }
        return false;
    }
    public boolean hasAccount(UUID uuid) {
        String query = "SELECT COUNT(*) FROM economy WHERE uuid = ?";

        try (PreparedStatement statement = getConnection().prepareStatement(query)){
            statement.setString(1,uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()){
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }catch (SQLException e){
            logger.severe("Error Checking account existence: " + e.getMessage());
        }
        return false;
    }


    public boolean hasBalance(String name,double amount) {
        String query = "SELECT balance FROM economy WHERE LOWER(name) = LOWER(?)";

        try(PreparedStatement statement = getConnection().prepareStatement(query)){
            statement.setString(1,name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble(1) >= amount;
                }
            }
        }catch (SQLException e){
            logger.severe("Error Checking account balance: " + e.getMessage());
        }
        return false;
    }

    public boolean hasBalance(UUID uuid,double amount) {
        String query = "SELECT balance FROM economy WHERE uuid = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)){
            statement.setString(1,uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()){
                if (resultSet.next()){
                    return resultSet.getDouble(1) >= amount;
                }
            }
        }catch (SQLException e){
            logger.severe("Error Checking account balance: " + e.getMessage());
        }
        return false;
    }

    public double getBalance(String name) {
        String query = "SELECT balance FROM economy WHERE LOWER(name) = LOWER(?)";

        try(PreparedStatement statement = getConnection().prepareStatement(query)){
            statement.setString(1,name);
            try (ResultSet resultSet = statement.executeQuery()){
                if (resultSet.next()){
                    return resultSet.getDouble(1);
                }
            }
        }catch (SQLException e){
            logger.severe("Error Getting account balance: " + e.getMessage());
        }
        return 0;
    }

    public double getBalance(UUID uuid) {
        String query = "SELECT balance FROM economy WHERE uuid = ?";

        try(PreparedStatement statement = getConnection().prepareStatement(query)){
            statement.setString(1,uuid.toString());
            try(ResultSet resultSet = statement.executeQuery()){
                if (resultSet.next()){
                    return resultSet.getDouble(1);
                }
            }
        }catch (SQLException e){
            logger.severe("Error Getting account balance: " + e.getMessage());
        }
        return 0;
    }

    public EconomyResponse withdrawFunds(String name, double amount) {
        // Update the balance of the specified account, ensuring case-insensitive name matching.
        String query = "UPDATE economy SET balance = balance - ? WHERE LOWER(name) = LOWER(?)";

        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            // Set the parameters for the update query.
            statement.setDouble(1, amount); // The amount to subtract.
            statement.setString(2, name);   // The name of the account.

            // Execute the update.
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                logger.severe("No account found with the name: " + name);
            }
            return new EconomyResponse(amount,getBalance(name), EconomyResponse.ResponseType.SUCCESS,null);
        } catch (SQLException e) {
            logger.severe("Error withdrawing funds: " + e.getMessage());
        }
        return new EconomyResponse(amount,0, EconomyResponse.ResponseType.FAILURE,null);
    }

    public EconomyResponse withdrawFunds(UUID uuid, double amount) {
        String query = "UPDATE economy SET balance = balance - ? WHERE uuid = ?";

        try(PreparedStatement statement = getConnection().prepareStatement(query)){
            statement.setDouble(1,amount);
            statement.setString(2,uuid.toString());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                logger.severe("No account found with the UUID: " + uuid);
            }
            return new EconomyResponse(amount,getBalance(uuid), EconomyResponse.ResponseType.SUCCESS,null);
        }catch (SQLException e){
            logger.severe("Error withdrawing funds: " + e.getMessage());
        }
        return new EconomyResponse(amount,0, EconomyResponse.ResponseType.FAILURE,null);
    }

    public EconomyResponse depositFunds(String name, double amount) {
        String query = "UPDATE economy SET balance = balance + ? WHERE LOWER(name) = LOWER(?)";

        try(PreparedStatement statement = getConnection().prepareStatement(query)){
            statement.setDouble(1,amount);
            statement.setString(2,name);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                logger.severe("No account found with the name: " + name);
            }
            return new EconomyResponse(amount,getBalance(name), EconomyResponse.ResponseType.SUCCESS,null);
        }catch (SQLException e){
            logger.severe("Error depositing funds: " + e.getMessage());
        }
        return new EconomyResponse(amount,0, EconomyResponse.ResponseType.FAILURE,null);
    }

    public EconomyResponse depositFunds(UUID uuid, double amount) {
        String query = "UPDATE economy SET balance = balance + ? WHERE uuid = ?";

        try(PreparedStatement statement = getConnection().prepareStatement(query)){
            statement.setDouble(1,amount);
            statement.setString(2,uuid.toString());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                logger.severe("No account found with the UUID: " + uuid);
            }
            return new EconomyResponse(amount,getBalance(uuid), EconomyResponse.ResponseType.SUCCESS,null);
        }catch (SQLException e){
            logger.severe("Error depositing funds: " + e.getMessage());
        }
        return new EconomyResponse(amount,0, EconomyResponse.ResponseType.FAILURE,null);
    }

    @Warning(value = true, reason = "Unchecked")
    public boolean createPlayerAccount(OfflinePlayer player) {
        double balance = config.getDouble("starting-balance");
        String query = "INSERT INTO economy (uuid, name, balance) VALUES (?, ?, ?)";
        try (PreparedStatement statement = getConnection().prepareStatement(query)){
            statement.setString(1,player.getUniqueId().toString());
            statement.setString(2,player.getName());
            statement.setDouble(3,balance);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                logger.severe("Failed to create player account: " + player.getName());
                return false;
            }
            return true;
        }catch (SQLException e){
            logger.severe("Error creating player account: " + e.getMessage());
        }
        return false;
    }
}