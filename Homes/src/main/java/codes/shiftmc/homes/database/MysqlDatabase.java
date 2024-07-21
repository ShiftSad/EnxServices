package codes.shiftmc.homes.database;

import codes.shiftmc.homes.config.DatabaseConfig;
import codes.shiftmc.homes.model.Home;
import codes.shiftmc.homes.model.Position;
import codes.shiftmc.homes.model.UserData;
import codes.shiftmc.homes.model.User;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MysqlDatabase extends Database {

    private final HikariDataSource ds;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Adjust the thread pool size as needed

    public MysqlDatabase(DatabaseConfig config) {
        // Connect to the database
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setUsername(config.username());
        hikariConfig.setPassword(config.password());
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.host() + ":" + config.port() + "/" + config.database());
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setConnectionTimeout(10000);
        hikariConfig.setLeakDetectionThreshold(10000);
        hikariConfig.setPoolName("HomesPool");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(hikariConfig);

        // Create tables if they don't exist
        try (var con = ds.getConnection()) {
            // Names need to be 16 + 1 cause of Bedrock
            try (var pst = con.prepareStatement("CREATE TABLE IF NOT EXISTS enx_users (uuid VARCHAR(36) PRIMARY KEY, username VARCHAR(17) NOT NULL)")) {
                pst.executeUpdate();
            }
            try (var pst = con.prepareStatement("CREATE TABLE IF NOT EXISTS enx_homes (name VARCHAR(17) NOT NULL, owner VARCHAR(36) NOT NULL, position VARCHAR(64) NOT NULL, PRIMARY KEY (name, owner))")) {
                pst.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create tables", e);
        }
    }

    @Override
    public CompletableFuture<Optional<UserData>> getUser(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (var con = ds.getConnection()) {
                User user = null;
                // Fetch user details
                try (var pst = con.prepareStatement("SELECT 1 FROM enx_users WHERE uuid = ?")) {
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
                try (var pst = con.prepareStatement("SELECT 1 FROM enx_homes WHERE owner = ?")) {
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
    public CompletableFuture<Void> updateUser(@NotNull UserData userData) {
        return CompletableFuture.runAsync(() -> {
            Connection con = null;
            try {
                con = ds.getConnection();
                con.setAutoCommit(false); // Start transaction

                // Prepare SQL statement for updating/inserting homes
                String sql = "INSERT INTO enx_homes (name, owner, position) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE position = VALUES(position)";
                try (var pst = con.prepareStatement(sql)) {
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
                        // Handle or log error
                    }
                }
            }
        }, executorService);
    }
    }
