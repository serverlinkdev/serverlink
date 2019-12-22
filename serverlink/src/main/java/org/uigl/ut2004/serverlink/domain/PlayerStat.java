package org.uigl.ut2004.serverlink.domain;

import java.util.UUID;

public class PlayerStat {

    public PlayerStat(UUID mapId, long timestamp, int teamId, String playerName, String playerHash, int rounds, float score, int kills, int deaths, int thaws, int git) {
        mMapId = mapId;
        mTimestamp = timestamp;
        mTeamId = teamId;
        mPlayerName = playerName;
        mPlayerHash = playerHash;
        mRounds = rounds;
        mScore = score;
        mKills = kills;
        mDeaths = deaths;
        mThaws = thaws;
        mGit = git;
    }

    private UUID mMapId;
    private long mTimestamp;
    private int mTeamId;
    private String mPlayerName;
    private String mPlayerHash;
    private int mRounds;
    private float mScore;
    private int mKills;
    private int mDeaths;
    private int mThaws;
    private int mGit;

    public UUID getMapId() {
        return mMapId;
    }

    public void setMapId(UUID mapId) {
        mMapId = mapId;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    public int getTeamId() {
        return mTeamId;
    }

    public void setTeamId(int teamId) {
        mTeamId = teamId;
    }

    public String getPlayerName() {
        return mPlayerName;
    }

    public void setPlayerName(String playerName) {
        mPlayerName = playerName;
    }

    public String getPlayerHash() {
        return mPlayerHash;
    }

    public void setPlayerHash(String playerHash) {
        mPlayerHash = playerHash;
    }

    public int getRounds() {
        return mRounds;
    }

    public void setRounds(int rounds) {
        mRounds = rounds;
    }

    public float getScore() {
        return mScore;
    }

    public void setScore(float score) {
        mScore = score;
    }

    public int getKills() {
        return mKills;
    }

    public void setKills(int kills) {
        mKills = kills;
    }

    public int getDeaths() {
        return mDeaths;
    }

    public void setDeaths(int deaths) {
        mDeaths = deaths;
    }

    public int getThaws() {
        return mThaws;
    }

    public void setThaws(int thaws) {
        mThaws = thaws;
    }

    public int getGit() {
        return mGit;
    }

    public void setGit(int git) {
        mGit = git;
    }

}
