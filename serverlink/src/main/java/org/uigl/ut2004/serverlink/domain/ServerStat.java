package org.uigl.ut2004.serverlink.domain;

import java.util.ArrayList;
import java.util.List;

public class ServerStat {

    public static class ServerPlayerStat {

        public ServerPlayerStat(String player, String stat) {
            mPlayer = player;
            mStat = stat;
        }

        private String mPlayer;
        private String mStat;

        public String getPlayer() {
            return mPlayer;
        }

        public void setPlayer(String player) {
            mPlayer = player;
        }

        public String getStat() {
            return mStat;
        }

        public void setStat(String stat) {
            mStat = stat;
        }
    }

    public ServerStat() {
    }

    public ServerStat(String name) {
        mName = name;
    }

    private String mName;
    private List<ServerPlayerStat> mServerPlayerStatList = new ArrayList<ServerPlayerStat>();

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public List<ServerPlayerStat> getServerPlayerStatList() {
        return mServerPlayerStatList;
    }
}
