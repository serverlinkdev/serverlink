package org.uigl.ut2004.serverlink.repository;

import org.uigl.ut2004.serverlink.domain.ServerAccount;
import org.uigl.ut2004.serverlink.exception.AuthenticationException;

import java.util.UUID;

public interface ServerAccountRepository {

    ServerAccount getServerAccount(UUID accountId);

    UUID getServerAccountId(String username, String password, boolean allowCreate) throws AuthenticationException;

}
