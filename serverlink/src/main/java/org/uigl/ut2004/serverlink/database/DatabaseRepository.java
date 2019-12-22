package org.uigl.ut2004.serverlink.database;

import org.uigl.ut2004.serverlink.repository.PlayerStatRepository;
import org.uigl.ut2004.serverlink.repository.ServerAccountRepository;

public interface DatabaseRepository extends PlayerStatRepository, ServerAccountRepository {

    void open(String databaseOptions);
    void close();

}
