package org.uigl.ut2004.serverlink.domain;

public class PlayerAggregate {

    public PlayerAggregate(int games, int rounds, float score, int kills, int deaths, int thaws, int git) {
        this.mGames = games;
        this.mRounds = rounds;
        this.mScore = score;
        this.mKills = kills;
        this.mDeaths = deaths;
        this.mThaws = thaws;
        this.mGit = git;
    }

    private int mGames;
    private int mRounds;
    private float mScore;
    private int mKills;
    private int mDeaths;
    private int mThaws;
    private int mGit;

    public int getGames() {
        return mGames;
    }

    public void setGames(int games) {
        mGames = games;
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
