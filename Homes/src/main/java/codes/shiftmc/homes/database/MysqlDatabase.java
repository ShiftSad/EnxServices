package codes.shiftmc.homes.database;

import codes.shiftmc.homes.config.DatabaseConfig;
import codes.shiftmc.homes.model.Home;
import codes.shiftmc.homes.model.Position;
import codes.shiftmc.homes.model.User;
import codes.shiftmc.homes.model.UserData;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MysqlDatabase extends Database {

    private final Logger logger = LoggerFactory.getLogger(MysqlDatabase.class);

    private final HikariDataSource ds;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Adjust the thread pool size as needed

    public MysqlDatabase(DatabaseConfig config) {
        // Connect to the database
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setUsername(config.username());
        hikariConfig.setPassword(config.password());
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.host() + ":" + config.port() + "/" + config.database());
        hikariConfig.setMaximumPoolSize(config.maximumPoolSize());
        hikariConfig.setMaxLifetime(config.maxLifeTime());
        hikariConfig.setKeepaliveTime(config.keepaliveTime());
        hikariConfig.setConnectionTimeout(config.connectionTimeout());
        hikariConfig.setLeakDetectionThreshold(config.leakDetectionThreshold());
        hikariConfig.setPoolName("HomesPool");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(hikariConfig);

        // Create tables if they don't exist
        try (var con = ds.getConnection()) {
            logger.debug("Creating tables if they don't exist");
            // Names need to be 16 + 1 cause of Bedrock
            try (var pst = con.prepareStatement("CREATE TABLE IF NOT EXISTS enx_users (uuid VARCHAR(36) PRIMARY KEY, username VARCHAR(17) NOT NULL)")) {
                pst.executeUpdate();
            }
            try (var pst = con.prepareStatement("CREATE TABLE IF NOT EXISTS enx_homes (name VARCHAR(17) NOT NULL, owner VARCHAR(36) NOT NULL, position VARCHAR(255) NOT NULL, PRIMARY KEY (name, owner))")) {
                pst.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create tables", e);
        }
    }

    @Override
    public CompletableFuture<Optional<UserData>> getUser(@NotNull UUID uuid) {
        logger.debug("Fetching user data for {}", uuid);
        return CompletableFuture.supplyAsync(() -> {
            try (var con = ds.getConnection()) {
                User user = null;
                // Fetch user details
                try (var pst = con.prepareStatement("SELECT * FROM enx_users WHERE uuid = ?")) {
                    pst.setString(1, uuid.toString());
                    try (var rs = pst.executeQuery()) {
                        if (rs.next()) {
                            user = new User(UUID.fromString(rs.getString("uuid")), rs.getString("username"));
                        }
                    }
                }
                if (user == null) return Optional.empty();

                // Fetch homes
                List<Home> homes = new ArrayList<>();
                try (var pst = con.prepareStatement("SELECT * FROM enx_homes WHERE owner = ?")) {
                    pst.setString(1, uuid.toString());
                    try (var rs = pst.executeQuery()) {
                        while (rs.next()) {
                            Position position = Position.fromString(rs.getString("position"));
                            homes.add(new Home(rs.getString("name"), UUID.fromString(rs.getString("owner")), position));
                        }
                    }
                }
                return Optional.of(new UserData(user, homes));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<Void> createUser(@NotNull User user) {
        logger.debug("Creating user: {}", user.username());
        return CompletableFuture.runAsync(() -> {
            try (var con = ds.getConnection()) {
                try (var pst = con.prepareStatement("INSERT INTO enx_users (uuid, username) VALUES (?, ?)")) {
                    pst.setString(1, user.uuid().toString());
                    pst.setString(2, user.username());
                    pst.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<Void> createIfNotExists(@NotNull User user) {
        logger.debug("Creating user if not exists: {}", user.username());
        return CompletableFuture.supplyAsync(() -> {
            try (var con = ds.getConnection()) {
                // Check if user exists
                try (var pst = con.prepareStatement("SELECT * FROM enx_users WHERE uuid = ?")) {
                    pst.setString(1, user.uuid().toString());
                    try (var rs = pst.executeQuery()) {
                        if (!rs.next()) { // It doesn't
                            createUser(user).join();
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null; // Return null to match the CompletableFuture<Void> signature
        }, executorService);
    }

    @Override
    public CompletableFuture<Void> updateUser(@NotNull UserData userData) {
        logger.debug("Updating user: {}", userData.user().username());
        return CompletableFuture.runAsync(() -> {
            Connection con = null;
            try {
                con = ds.getConnection();
                con.setAutoCommit(false); // Start transaction

                // Fetch current homes from the database
                List<Home> currentHomes = new ArrayList<>();
                try (var pst = con.prepareStatement("SELECT * FROM enx_homes WHERE owner = ?")) {
                    pst.setString(1, userData.user().uuid().toString());
                    try (var rs = pst.executeQuery()) {
                        while (rs.next()) {
                            Position position = Position.fromString(rs.getString("position"));
                            currentHomes.add(new Home(rs.getString("name"), UUID.fromString(rs.getString("owner")), position));
                        }
                    }
                }

                // Identify homes to be removed
                List<Home> homesToRemove = new ArrayList<>(currentHomes);
                homesToRemove.removeAll(userData.homes());

                // Remove homes that are no longer present in userData
                String deleteSql = "DELETE FROM enx_homes WHERE name = ? AND owner = ?";
                try (var pst = con.prepareStatement(deleteSql)) {
                    for (Home home : homesToRemove) {
                        pst.setString(1, home.name());
                        pst.setString(2, home.owner().toString());
                        pst.executeUpdate();
                    }
                }

                // Prepare SQL statement for updating/inserting homes
                String insertSql = "INSERT INTO enx_homes (name, owner, position) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE position = VALUES(position)";
                try (var pst = con.prepareStatement(insertSql)) {
                    for (Home home : userData.homes()) {
                        pst.setString(1, home.name());
                        pst.setString(2, home.owner().toString());
                        pst.setString(3, home.position().toString());
                        pst.executeUpdate();
                    }
                }

                con.commit(); // Commit transaction
            } catch (SQLException e) {
                if (con != null) {
                    try {
                        logger.error("Failed to update user homes for user: {}", userData.user().username(), e);
                        con.rollback(); // Rollback transaction in case of error
                    } catch (SQLException ex) {
                        throw new RuntimeException("Failed to rollback transaction", ex);
                    }
                }
                throw new RuntimeException("Failed to update user homes", e);
            } finally {
                if (con != null) {
                    try {
                        con.setAutoCommit(true); // Reset auto-commit
                        con.close();
                    } catch (SQLException e) {
                        logger.error("Failed to reset auto-commit or close connection for user: {}", userData.user().username(), e);
                    }
                }
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<Void> bulkDelete(UUID... uuids) {
        logger.debug("Deleting users: {}", Arrays.stream(uuids).map(UUID::toString).toArray());
        return CompletableFuture.runAsync(() -> {
            try (var con = ds.getConnection()) {
                con.setAutoCommit(false); // Start transaction

                // Delete homes
                try (var pst = con.prepareStatement("DELETE FROM enx_homes WHERE owner = ?")) {
                    for (UUID uuid : uuids) {
                        pst.setString(1, uuid.toString());
                        pst.executeUpdate();
                    }
                }

                // Delete users
                try (var pst = con.prepareStatement("DELETE FROM enx_users WHERE uuid = ?")) {
                    for (UUID uuid : uuids) {
                        pst.setString(1, uuid.toString());
                        pst.executeUpdate();
                    }
                }

                con.commit(); // Commit transaction
            } catch (SQLException e) {
                throw new RuntimeException("Failed to bulk delete users", e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<Void> bulkCreate(ArrayList<UserData> users) {
        logger.debug("Bulk creating users and their homes");
        return CompletableFuture.runAsync(() -> {
            Connection con = null;
            try {
                con = ds.getConnection();
                con.setAutoCommit(false); // Start transaction

                // Prepare SQL statements
                String userSql = "INSERT INTO enx_users (uuid, username) VALUES (?, ?) ON DUPLICATE KEY UPDATE username = VALUES(username)";
                String homeSql = "INSERT INTO enx_homes (name, owner, position) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE position = VALUES(position)";

                try (var userPst = con.prepareStatement(userSql);
                     var homePst = con.prepareStatement(homeSql)) {

                    for (UserData userData : users) {
                        // Insert user
                        userPst.setString(1, userData.user().uuid().toString());
                        userPst.setString(2, userData.user().username());
                        userPst.addBatch();

                        // Insert homes
                        for (Home home : userData.homes()) {
                            homePst.setString(1, home.name());
                            homePst.setString(2, home.owner().toString());
                            homePst.setString(3, home.position().toString());
                            homePst.addBatch();
                        }
                    }

                    // Execute batch inserts
                    userPst.executeBatch();
                    homePst.executeBatch();
                }

                con.commit(); // Commit transaction
            } catch (SQLException e) {
                if (con != null) {
                    try {
                        logger.error("Failed to bulk create users and homes", e);
                        con.rollback(); // Rollback transaction in case of error
                    } catch (SQLException ex) {
                        throw new RuntimeException("Failed to rollback transaction", ex);
                    }
                }
                throw new RuntimeException("Failed to bulk create users and homes", e);
            } finally {
                if (con != null) {
                    try {
                        con.setAutoCommit(true); // Reset auto-commit
                        con.close();
                    } catch (SQLException e) {
                        logger.error("Failed to reset auto-commit or close connection", e);
                    }
                }
            }
        }, executorService);
    }
}