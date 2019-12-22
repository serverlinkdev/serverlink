package org.uigl.ut2004.serverlink.domain;

import java.util.ArrayList;
import java.util.List;

public class ServerStatList {

    private List<ServerStat> mServerStatList = new ArrayList<ServerStat>();
    private int mRoundsPlayed = 0;
    private int mMapsPlayed = 0;
    private float mTeam0Score = 0f;
    private float mTeam1Score = 0f;

    public List<ServerStat> getServerStatList() {
        return mServerStatList;
    }

    public int getRoundsPlayed() {
        return mRoundsPlayed;
    }

    public int getMapsPlayed() {
        return mMapsPlayed;
    }

    public float getTeam0Score() {
        return mTeam0Score;
    }

    public float getTeam1Score() {
        return mTeam1Score;
    }

    public void setServerAggregate(int mapsPlayed, int roundsPlayed, float team0Score, float team1Score) {
        mMapsPlayed = mapsPlayed;
        mRoundsPlayed = roundsPlayed;
        mTeam0Score = team0Score;
        mTeam1Score = team1Score;
    }
}
