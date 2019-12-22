package org.uigl.ut2004.serverlink.repository;

import org.uigl.ut2004.serverlink.domain.*;

public interface PlayerStatRepository {

    void putMapStat(ServerAccount serverAccount, MapStat mapStat);
    void putPlayerStat(ServerAccount serverAccount, PlayerStat playerStat);

    PlayerAggregate getPlayerAggregate(ServerAccount serverAccount, String playerHash);
    ServerStatList getServerStatList(ServerAccount serverAccount);
}
