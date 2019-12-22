package org.uigl.ut2004.serverlink.domain;

import java.util.UUID;

public class ServerAccount {

    public ServerAccount(UUID accountId, String username, String passwordHash, String salt) {
        mAccountId = accountId;
        mUsername = username;
        mPasswordHash = passwordHash;
        mSalt = salt;
    }

    private UUID mAccountId;
    private String mUsername;
    private String mPasswordHash;
    private String mSalt;

    public UUID getAccountId() {
        return mAccountId;
    }

    public void setAccountId(UUID accountId) {
        mAccountId = accountId;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getPasswordHash() {
        return mPasswordHash;
    }

    public void setPasswordHash(String password) {
        mPasswordHash = password;
    }

    public String getSalt() {
        return mSalt;
    }

    public void setSalt(String salt) {
        mSalt = salt;
    }
}
