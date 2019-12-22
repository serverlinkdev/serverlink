package org.uigl.ut2004.serverlink.domain;

import java.util.UUID;

public class MapStat {

    public MapStat(String name, long timestamp, int team1Score, int team2Score) {
        this(UUID.randomUUID(), name, timestamp, team1Score, team2Score);
    }

    public MapStat(UUID id, String name, long timestamp, int team1Score, int team2Score) {
        mId = id;
        mName = name;
        mTimestamp = timestamp;
        mTeam1Score = team1Score;
        mTeam2Score = team2Score;
    }

    private UUID mId;
    private String mName;
    private long mTimestamp;
    private int mTeam1Score;
    private int mTeam2Score;

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    public int getTeam1Score() {
        return mTeam1Score;
    }

    public void setTeam1Score(int team1Score) {
        mTeam1Score = team1Score;
    }

    public int getTeam2Score() {
        return mTeam2Score;
    }

    public void setTeam2Score(int team2Score) {
        mTeam2Score = team2Score;
    }
}
