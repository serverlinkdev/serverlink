package org.uigl.ut2004.serverlink.database;

import org.apache.commons.codec.digest.DigestUtils;
import org.uigl.ut2004.serverlink.domain.*;
import org.uigl.ut2004.serverlink.exception.AuthenticationException;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

public class SQLiteDatabaseRepositoryImpl implements DatabaseRepository {

    private Connection mConnection;
    private Statement mStatement;
    private HashMap<UUID, HashMap<String, PlayerAggregate>> mServerToPlayerAggregate = new HashMap<UUID, HashMap<String, PlayerAggregate>>();

    private SecureRandom mSecureRandom = new SecureRandom();

    private HashMap<String, ServerAccount> mServers = new HashMap<String, ServerAccount>();
    private HashMap<UUID, ServerAccount> mServersIdMapping = new HashMap<UUID, ServerAccount>();

    @Override
    public void open(String databaseOptions) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        File databaseFile = new File(databaseOptions);
        if (!databaseFile.exists()) {
            throw new RuntimeException("Database not found " + databaseFile.getAbsolutePath());
        }

        try {
            mConnection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath().replace('\\', '/'));
            mStatement = mConnection.createStatement();
            mStatement.setQueryTimeout(30);

            //Load memory stores for fast lookups.

            ResultSet rs = mStatement.executeQuery("SELECT accountId, playerHash, COUNT(rowid) AS games, SUM(rounds) AS rounds, SUM(score) AS score, SUM(kills) AS kills, SUM(deaths) AS deaths, SUM(thaws) AS thaws, SUM(git) AS git FROM PlayerStat GROUP BY accountId, playerHash");
            while (rs.next()) {
                PlayerAggregate playerAggregate = new PlayerAggregate(
                        rs.getInt("games"),
                        rs.getInt("rounds"),
                        rs.getFloat("score"),
                        rs.getInt("kills"),
                        rs.getInt("deaths"),
                        rs.getInt("thaws"),
                        rs.getInt("git")
                );
                UUID accountId = UUID.fromString(rs.getString("accountId"));
                if (mServerToPlayerAggregate.get(accountId) == null) {
                    mServerToPlayerAggregate.put(accountId, new HashMap<String, PlayerAggregate>());
                }
                mServerToPlayerAggregate.get(accountId).put(rs.getString("playerHash"), playerAggregate);
            }
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            if (mConnection != null) {
                mConnection.close();
            }
        } catch (SQLException e) {
            // connection close failed.
            System.err.println(e.getMessage());
        }

        for (HashMap<String, PlayerAggregate> playerList : mServerToPlayerAggregate.values()) {
            playerList.clear();
        }
        mServerToPlayerAggregate.clear();
    }

    @Override
    public void putMapStat(ServerAccount serverAccount, MapStat mapStat) {
        try {
            mStatement.executeUpdate("INSERT INTO MapStat(mapId, accountId, mapName, timestamp, team1Score, team2Score) VALUES ('" + mapStat.getId() + "','" + serverAccount.getAccountId().toString() + "','" + mapStat.getName() + "'," + mapStat.getTimestamp() + "," + mapStat.getTeam1Score() + "," + mapStat.getTeam2Score() + ");");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void putPlayerStat(ServerAccount serverAccount, PlayerStat playerStat) {
        PlayerAggregate playerAggregate = getOrCreatePlayerAggregate(serverAccount, playerStat.getPlayerHash());

        playerAggregate.setGames(playerAggregate.getGames() + 1);
        playerAggregate.setRounds(playerAggregate.getRounds() + playerStat.getRounds());
        playerAggregate.setScore(playerAggregate.getScore() + playerStat.getScore());
        playerAggregate.setKills(playerAggregate.getKills() + playerStat.getKills());
        playerAggregate.setDeaths(playerAggregate.getDeaths() + playerStat.getDeaths());
        playerAggregate.setThaws(playerAggregate.getThaws() + playerStat.getThaws());
        playerAggregate.setGit(playerAggregate.getGit() + playerStat.getGit());

        try {
            mStatement.executeUpdate("INSERT INTO PlayerStat(accountId, mapId, timestamp, teamId, playerName, playerHash, rounds, score, kills, deaths, thaws, git) VALUES ('" + serverAccount.getAccountId().toString() + "','" + playerStat.getMapId() + "'," + playerStat.getTimestamp() + "," + playerStat.getTeamId() + ",'" + playerStat.getPlayerName() + "','" + playerStat.getPlayerHash() + "'," + playerStat.getRounds() + "," + playerStat.getScore() + "," + playerStat.getKills() + "," + playerStat.getDeaths() + "," + playerStat.getThaws() + "," + playerStat.getGit() + ");");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PlayerAggregate getPlayerAggregate(ServerAccount serverAccount, String playerHash) {
        return getOrCreatePlayerAggregate(serverAccount, playerHash);
    }

    private PlayerAggregate getOrCreatePlayerAggregate(ServerAccount serverAccount, String playerHash) {
        HashMap<String, PlayerAggregate> playerAggregateList = mServerToPlayerAggregate.get(serverAccount.getAccountId());
        if (playerAggregateList == null) {
            playerAggregateList = new HashMap<String, PlayerAggregate>();
            mServerToPlayerAggregate.put(serverAccount.getAccountId(), playerAggregateList);
        }

        PlayerAggregate playerAggregate = playerAggregateList.get(playerHash);
        if (playerAggregate == null) {
            playerAggregate = new PlayerAggregate(0, 0, 0f, 0, 0, 0, 0);
            playerAggregateList.put(playerHash, playerAggregate);
        }

        return playerAggregate;
    }

    @Override
    public ServerStatList getServerStatList(ServerAccount serverAccount) {
        ServerStatList serverStatList = new ServerStatList();

        long currentTimestamp = System.currentTimeMillis();
        long today = currentTimestamp - 86400000L;
        long month = currentTimestamp - 2592000000L;

        //All Time Score
        try {
            ServerStat scoreAll = new ServerStat("Top Scores");
            ResultSet rs = mStatement.executeQuery("SELECT playerName, SUM(score) AS stat FROM PlayerStat WHERE accountId='" + serverAccount.getAccountId().toString() + "' GROUP BY accountId, playerHash ORDER BY stat DESC LIMIT 10");
            while (rs.next()) {
                scoreAll.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat(rs.getString("playerName"), Integer.toString(rs.getInt("stat"))));
            }
            serverStatList.getServerStatList().add(scoreAll);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //All Time PPR
        try {
            ServerStat scoreAll = new ServerStat("Top PPR");
            ResultSet rs = mStatement.executeQuery("SELECT playerName, IFNULL(SUM(score) / SUM(rounds), 0) AS stat FROM PlayerStat WHERE accountId='" + serverAccount.getAccountId().toString() + "' GROUP BY accountId, playerHash ORDER BY stat DESC LIMIT 10");
            while (rs.next()) {
                scoreAll.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat(rs.getString("playerName"), String.format("%.1f", rs.getFloat("stat"))));
            }
            serverStatList.getServerStatList().add(scoreAll);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //All Time Kills
        try {
            ServerStat scoreAll = new ServerStat("Top Kills");
            ResultSet rs = mStatement.executeQuery("SELECT playerName, SUM(kills) AS stat FROM PlayerStat WHERE accountId='" + serverAccount.getAccountId().toString() + "' GROUP BY accountId, playerHash ORDER BY stat DESC LIMIT 10");
            while (rs.next()) {
                scoreAll.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat(rs.getString("playerName"), rs.getString("stat")));
            }
            serverStatList.getServerStatList().add(scoreAll);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Today Score
        try {
            ServerStat scoreAll = new ServerStat("Top Score (24hr)");
            ResultSet rs = mStatement.executeQuery("SELECT playerName, SUM(score) AS stat FROM PlayerStat WHERE accountId='" + serverAccount.getAccountId().toString() + "' AND timestamp>" + today + " GROUP BY accountId, playerHash ORDER BY stat DESC LIMIT 10");
            while (rs.next()) {
                scoreAll.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat(rs.getString("playerName"), Integer.toString(rs.getInt("stat"))));
            }
            serverStatList.getServerStatList().add(scoreAll);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Today PPR
        try {
            ServerStat scoreAll = new ServerStat("Top PPR (24hr)");
            ResultSet rs = mStatement.executeQuery("SELECT playerName, IFNULL(SUM(score) / SUM(rounds), 0) AS stat FROM PlayerStat WHERE accountId='" + serverAccount.getAccountId().toString() + "' AND timestamp>" + today + " GROUP BY accountId, playerHash ORDER BY stat DESC LIMIT 10");
            while (rs.next()) {
                scoreAll.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat(rs.getString("playerName"), String.format("%.1f", rs.getFloat("stat"))));
            }
            serverStatList.getServerStatList().add(scoreAll);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Today Kills
        try {
            ServerStat scoreAll = new ServerStat("Top Kills (24hr)");
            ResultSet rs = mStatement.executeQuery("SELECT playerName, SUM(kills) AS stat FROM PlayerStat WHERE accountId='" + serverAccount.getAccountId().toString() + "' AND timestamp>" + today + " GROUP BY accountId, playerHash ORDER BY stat DESC LIMIT 10");
            while (rs.next()) {
                scoreAll.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat(rs.getString("playerName"), rs.getString("stat")));
            }
            serverStatList.getServerStatList().add(scoreAll);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Month Score
        try {
            ServerStat scoreAll = new ServerStat("Top Score (30d)");
            ResultSet rs = mStatement.executeQuery("SELECT playerName, SUM(score) AS stat FROM PlayerStat WHERE accountId='" + serverAccount.getAccountId().toString() + "' AND timestamp>" + month + " GROUP BY accountId, playerHash ORDER BY stat DESC LIMIT 10");
            while (rs.next()) {
                scoreAll.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat(rs.getString("playerName"), Integer.toString(rs.getInt("stat"))));
            }
            serverStatList.getServerStatList().add(scoreAll);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Today PPR
        try {
            ServerStat scoreAll = new ServerStat("Top PPR (30d)");
            ResultSet rs = mStatement.executeQuery("SELECT playerName, IFNULL(SUM(score) / SUM(rounds), 0) AS stat FROM PlayerStat WHERE accountId='" + serverAccount.getAccountId().toString() + "' AND timestamp>" + month + " GROUP BY accountId, playerHash ORDER BY stat DESC LIMIT 10");
            while (rs.next()) {
                scoreAll.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat(rs.getString("playerName"), String.format("%.1f", rs.getFloat("stat"))));
            }
            serverStatList.getServerStatList().add(scoreAll);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Month Kills
        try {
            ServerStat scoreAll = new ServerStat("Top Kills (30d)");
            ResultSet rs = mStatement.executeQuery("SELECT playerName, SUM(kills) AS stat FROM PlayerStat WHERE accountId='" + serverAccount.getAccountId().toString() + "' AND timestamp>" + month + " GROUP BY accountId, playerHash ORDER BY stat DESC LIMIT 10");
            while (rs.next()) {
                scoreAll.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat(rs.getString("playerName"), rs.getString("stat")));
            }
            serverStatList.getServerStatList().add(scoreAll);
        } catch (SQLException e) {
            e.printStackTrace();
        }

//        ServerStat kuStat = new ServerStat("Best Clans");
//        kuStat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("[KU]_Clan", "1"));
//        serverStatList.getServerStatList().add(kuStat);
        return serverStatList;
    }

    @Override
    public ServerAccount getServerAccount(UUID accountId) {
        return mServersIdMapping.get(accountId);
    }

    @Override
    public UUID getServerAccountId(String username, String password, boolean allowCreate) throws AuthenticationException {
        username = username.replace('\'', '"');
        password = password.replace('\'', '"');


        ServerAccount account = mServers.get(username);
        boolean create = false;

        if (account == null) {
            try {
                ResultSet rs = mStatement.executeQuery("SELECT accountId, username, passwordHash, salt FROM ServerAccount WHERE username='" + username + "'");
                if (rs.next()) {
                    account = new ServerAccount(UUID.fromString(rs.getString("accountId")), rs.getString("username"), rs.getString("passwordHash"), rs.getString("salt"));
                } else {
                    account = new ServerAccount(UUID.randomUUID(), username, password, new BigInteger(130, mSecureRandom).toString(32));
                    create = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new AuthenticationException();
            }
        }

        String passwordHash = DigestUtils.sha1Hex(password + account.getSalt());

        if (!account.getPasswordHash().equals(passwordHash)) {
            throw new AuthenticationException();
        }

        if (create) {
            if (allowCreate) {
                try {
                    mStatement.executeUpdate("INSERT INTO ServerAccount(accountId, username , passwordHash, salt) VALUES ('" + account.getAccountId().toString() + "','" + account.getUsername() + "','" + account.getPasswordHash() + "','" + account.getSalt() + "')");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                throw new AuthenticationException();
            }
        }

        mServers.put(username, account);
        mServersIdMapping.put(account.getAccountId(), account);

        return account.getAccountId();
    }
}
