package xyz.yooniks.spigotguard.sql;

import xyz.yooniks.spigotguard.helper.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import xyz.yooniks.spigotguard.user.*;
import xyz.yooniks.spigotguard.config.*;
import java.util.*;
import xyz.yooniks.spigotguard.event.*;
import java.io.*;
import java.util.concurrent.*;
import java.sql.*;

public class SqlDatabase
{
    private final ExecutorService executor;
    private Connection connection;
    private boolean initialized;
    private boolean connecting;
    
    private void lambda$saveUser$2(final User user) {
        final String value = String.valueOf(new StringBuilder().append("SELECT `uuid` FROM `spigotguard_users` where `uuid` = '").append(user.getUuid().toString()).append("' LIMIT 1;"));
        try {
            final Statement statement = this.connection.createStatement();
            try {
                final ResultSet executeQuery = statement.executeQuery(value);
                try {
                    if (!executeQuery.next()) {
                        statement.executeUpdate(String.valueOf(new StringBuilder().append("INSERT INTO `spigotguard_users` (`uuid`, `name`, `ip`, `exploits`, `lastJoin`) VALUES ('").append(user.getUuid().toString()).append("','").append(user.getName()).append("','").append(user.getIp()).append("','").append(SerializerHelper.toString(user.getAttempts())).append("','").append(user.getLastJoin()).append("');")));
                    }
                    else {
                        statement.executeUpdate(String.valueOf(new StringBuilder().append("UPDATE `spigotguard_users` SET `name` = '").append(user.getName()).append("', `lastJoin` = '").append(user.getLastJoin()).append("', `exploits` = '").append(SerializerHelper.toString(user.getAttempts())).append("' where `uuid` = '").append(user.getUuid().toString()).append("';")));
                    }
                    if (executeQuery != null) {
                        executeQuery.close();
                    }
                }
                catch (Throwable t) {
                    if (executeQuery != null) {
                        try {
                            executeQuery.close();
                        }
                        catch (Throwable t2) {
                            t.addSuppressed(t2);
                        }
                    }
                    throw t;
                }
                if (statement != null) {
                    statement.close();
                }
            }
            catch (Throwable t3) {
                if (statement != null) {
                    try {
                        statement.close();
                    }
                    catch (Throwable t4) {
                        t3.addSuppressed(t4);
                    }
                }
                throw t3;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void createTable() throws SQLException {
        final PreparedStatement prepareStatement = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS `spigotguard_users` (`uuid` CHAR(36) PRIMARY KEY NOT NULL,`name` VARCHAR(16) NOT NULL,`ip` VARCHAR(16) NOT NULL,`exploits` text,`lastJoin` BIGINT NOT NULL);");
        try {
            prepareStatement.executeUpdate();
            if (prepareStatement != null) {
                prepareStatement.close();
            }
        }
        catch (Throwable t) {
            if (prepareStatement != null) {
                try {
                    prepareStatement.close();
                }
                catch (Throwable t2) {
                    t.addSuppressed(t2);
                }
            }
            throw t;
        }
        SpigotGuardLogger.log(Level.INFO, "[Database] Created sql database table!", new Object[0]);
    }
    
    private boolean isInvalidName(final String s) {
        return s.contains("'") || s.contains("\"");
    }
    
    private void lambda$removeUser$1(final String s) {
        try {
            final PreparedStatement prepareStatement = this.connection.prepareStatement(s);
            try {
                prepareStatement.execute();
                if (prepareStatement != null) {
                    prepareStatement.close();
                }
            }
            catch (Throwable t) {
                if (prepareStatement != null) {
                    try {
                        prepareStatement.close();
                    }
                    catch (Throwable t2) {
                        t.addSuppressed(t2);
                    }
                }
                throw t;
            }
        }
        catch (SQLException ex) {}
    }
    
    private void loadUsers(final UserManager userManager) throws IOException, SQLException, ClassNotFoundException {
        final PreparedStatement prepareStatement = this.connection.prepareStatement("SELECT * FROM `spigotguard_users`;");
        try {
            final ResultSet executeQuery = prepareStatement.executeQuery();
            try {
                while (executeQuery.next()) {
                    final String string = executeQuery.getString("uuid");
                    final long long1 = executeQuery.getLong("lastJoin");
                    final String string2 = executeQuery.getString("name");
                    if (System.currentTimeMillis() - long1 > 86400000L * Settings.IMP.SQL.PURGE_TIME) {
                        this.removeUser(String.valueOf(new StringBuilder().append("DELETE FROM `spigotguard_users` WHERE `uuid`='").append(string).append("'")));
                        if (!Settings.IMP.SQL.PURGE_CONSOLE_INFO) {
                            continue;
                        }
                        SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("[Database] Removing user ").append(string2).append(", he has not joined server for ").append(Settings.IMP.SQL.PURGE_TIME).append(" days, so he is wasting memory only!")), new Object[0]);
                    }
                    else {
                        final List<?> fromString = SerializerHelper.fromString(executeQuery.getString("exploits"));
                        final User user = new User(string2, executeQuery.getString("ip"), UUID.fromString(string));
                        user.addAttempts((List<ExploitDetails>)fromString);
                        user.setLastJoin(long1);
                        userManager.addUser(user);
                    }
                }
                SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("[Database] Loaded ").append(userManager.getUsers().size()).append(" users!")), new Object[0]);
                if (executeQuery != null) {
                    executeQuery.close();
                }
            }
            catch (Throwable t) {
                if (executeQuery != null) {
                    try {
                        executeQuery.close();
                    }
                    catch (Throwable t2) {
                        t.addSuppressed(t2);
                    }
                }
                throw t;
            }
            if (prepareStatement != null) {
                prepareStatement.close();
            }
        }
        catch (Throwable t3) {
            if (prepareStatement != null) {
                try {
                    prepareStatement.close();
                }
                catch (Throwable t4) {
                    t3.addSuppressed(t4);
                }
            }
            throw t3;
        }
    }
    
    private void removeUser(final String s) {
        if (this.connection != null) {
            this.executor.execute(this::lambda$removeUser$1);
        }
    }
    
    public SqlDatabase() {
        this.executor = Executors.newCachedThreadPool();
        this.connecting = false;
        this.initialized = false;
    }
    
    private void connectToDatabase(final String s, final String s2, final String s3) throws SQLException {
        this.connection = DriverManager.getConnection(s, s2, s3);
        SpigotGuardLogger.log(Level.INFO, "[Database] Connected to sqlite database!", new Object[0]);
    }
    
    public void setupConnect(final UserManager userManager) {
        if (this.initialized) {
            throw new UnsupportedOperationException("SqlDatabase has been already initialized");
        }
        this.initialized = true;
        this.executor.submit(this::lambda$setupConnect$0);
    }
    
    public void close() {
        this.executor.shutdownNow();
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        }
        catch (SQLException ex) {}
        this.connection = null;
        SpigotGuardLogger.log(Level.INFO, "[Database] Closed sql connection!", new Object[0]);
    }
    
    private void lambda$setupConnect$0(final UserManager userManager) {
        try {
            this.connecting = true;
            if (this.executor.isShutdown()) {
                this.initialized = true;
                return;
            }
            if (this.connection != null && this.connection.isValid(3)) {
                this.initialized = true;
                return;
            }
            if (Integer.valueOf(73844866).equals(Settings.IMP.SQL.STORAGE_TYPE.toUpperCase().hashCode())) {
                final Settings.SQL sql = Settings.IMP.SQL;
                this.connectToDatabase(String.format("JDBC:mysql://%s:%s/%s?useSSL=false&useUnicode=true&characterEncoding=utf-8&autoReconnect=true", sql.HOSTNAME, sql.PORT, sql.DATABASE), sql.USER, sql.PASSWORD);
            }
            else {
                Class.forName("org.sqlite.JDBC");
                this.connectToDatabase("JDBC:sqlite:plugins/SpigotGuard/database.db", null, null);
            }
            this.createTable();
            if (Settings.IMP.SQL.ENABLED) {
                this.loadUsers(userManager);
            }
            this.connecting = false;
            this.initialized = true;
        }
        catch (SQLException ex2) {}
        catch (ClassNotFoundException ex3) {}
        catch (IOException ex) {
            ex.printStackTrace();
            this.connection = null;
            this.connecting = false;
            this.initialized = true;
        }
        finally {
            this.connecting = false;
            this.initialized = true;
        }
    }
    
    public void saveUser(final User user) {
        if (this.connecting || this.isInvalidName(user.getName())) {
            return;
        }
        if (this.connection != null) {
            this.executor.execute(this::lambda$saveUser$2);
        }
    }
}
