package org.uigl.ut2004.serverlink.database;

import org.uigl.ut2004.serverlink.domain.*;
import org.uigl.ut2004.serverlink.exception.AuthenticationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MemoryDatabaseRepositoryImpl implements DatabaseRepository {

    private HashMap<UUID, List<MapStat>> mServerToMapList = new HashMap<UUID, List<MapStat>>();
    private HashMap<UUID, HashMap<String, PlayerAggregate>> mServerToPlayerAggregate = new HashMap<UUID, HashMap<String, PlayerAggregate>>();

    private ServerStatList mFakeServerStatList = new ServerStatList();

    public MemoryDatabaseRepositoryImpl() {
        ServerStat stat = new ServerStat("Top Fake Stats");
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test1", "20"));
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test2", "19"));
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test3", "18"));
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test4", "17"));
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test5", "16"));
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test6", "15"));
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test7", "14"));
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test8", "13"));
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test9", "12"));
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test10", "11"));
        mFakeServerStatList.getServerStatList().add(stat);


        stat = new ServerStat("Top Fake Names");
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test11", "30"));
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test12", "29"));
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test13", "28"));
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test14", "27"));
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test15", "26"));
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test16", "25"));
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test17", "24"));
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test18", "23"));
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test19", "22"));
        stat.getServerPlayerStatList().add(new ServerStat.ServerPlayerStat("Test20", "21"));
        mFakeServerStatList.getServerStatList().add(stat);
    }

    @Override
    public void putMapStat(ServerAccount serverAccount, MapStat mapStat) {
        List<MapStat> mapStatList = mServerToMapList.get(serverAccount.getAccountId());
        if (mapStatList == null) {
            mapStatList = new ArrayList<MapStat>();
            mServerToMapList.put(serverAccount.getAccountId(), mapStatList);
        }
        mapStatList.add(mapStat);
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
        return mFakeServerStatList;
    }

    private HashMap<String, ServerAccount> mServers = new HashMap<String, ServerAccount>();
    private HashMap<UUID, ServerAccount> mServersIdMapping = new HashMap<UUID, ServerAccount>();

    @Override
    public ServerAccount getServerAccount(UUID accountId) {
        return mServersIdMapping.get(accountId);
    }

    @Override
    public UUID getServerAccountId(String username, String password, boolean allowCreate) throws AuthenticationException {
        ServerAccount account = mServers.get(username);

        if (account == null) {
            if (allowCreate) {
                account = new ServerAccount(UUID.randomUUID(), username, password, "");
                mServers.put(username, account);
                mServersIdMapping.put(account.getAccountId(), account);
                return account.getAccountId();
            } else {
                throw new AuthenticationException();
            }
        }

        if (!account.getPasswordHash().equals(password)) {
            throw new AuthenticationException();
        }

        return account.getAccountId();
    }


    @Override
    public void open(String databaseOptions) {

    }

    @Override
    public void close() {

    }
}
