package org.uigl.ut2004.serverlink;

import org.uigl.ut2004.serverlink.database.DatabaseRepositoryProvider;
import org.uigl.ut2004.serverlink.domain.*;
import org.uigl.ut2004.serverlink.exception.AuthenticationException;
import org.uigl.ut2004.serverlink.exception.ServerProtocolException;
import org.uigl.ut2004.serverlink.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ProtocolException;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;

class ServerLinkHandler extends Thread {

    private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

    private Socket mSocket;
    private long mClientId;

    private ServerAccount mServerAccount;
    private MapStat mCurrentMapStat;
    private boolean mAllowAnonymous = false;

    private ServerStatList mCurrentServerStatList = null;
    private int mServerStatRotation = 0;

    private boolean mRunning = true;

    ServerLinkHandler(Socket socket, long clientId, boolean allowAnonymous) {
        mSocket = socket;
        mClientId = clientId;
        mAllowAnonymous = allowAnonymous;
        log("InitConnection");
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            PrintWriter out = new PrintWriter(mSocket.getOutputStream(), true);

            while (mRunning) {
                String input = in.readLine();
                if (input == null) {
                    mRunning = false;
                    continue;
                }

                if (input.trim().isEmpty()) {
                    continue;
                }

                String[] params = input.split(" ");
                if (params.length == 0) {
                    continue;
                }

                String command = params[0];

                if (command.equals("LOGIN") && params.length == 3) {
                    statLogin(params);
                    continue;
                }

                if (mCurrentServerStatList == null) {
                    throw new ServerProtocolException();
                }

                if (command.equals("REGISTER_GAME") && params.length == 4) {
                    statRegisterGame(params);
                } else if (command.equals("REGISTER_STATS") && params.length == 11) {
                    statRegisterPlayerStat(params);
                } else if (command.equals("GET_STATS") && params.length == 3) {
                    statGetPlayerStat(out, params);
                } else if (command.equals("GET_STATS_LIST") && params.length == 1) {
                    statGetServerStat(out);
                } else if (command.equals("LOGOUT") && params.length == 1) {
                    statLogout();
                } else {
                    loge("NOOP:" + "\"" + input + "\"");
                    throw new ProtocolException();
                }

            }
        } catch (ParseException e) {
            loge("Invalid Date \"" + e.getMessage() + "\"");
        } catch (AuthenticationException e) {
            loge("Invalid Credentials");
        } catch (ServerProtocolException e) {
            loge("Invalid Protocol");
        } catch (IOException e) {
            loge(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                mSocket.close();
            } catch (IOException e) {
                loge("Couldn't close a socket.");
            }
            log("Connection Closed");
        }
    }

    private void statLogout() {
        log("Logout");
        mRunning = false;
    }

    private void statLogin(String[] params) throws AuthenticationException {
        String username = params[1];
        String password = params[2];

        if (password == null || password.length() == 0) {
            throw new AuthenticationException();
        }

        log("Login:" + username + ":" + password + " from " + mSocket.getInetAddress());

        UUID accountId = DatabaseRepositoryProvider.getDatabaseInstance().getServerAccountId(username, password, mAllowAnonymous);
        mServerAccount = DatabaseRepositoryProvider.getDatabaseInstance().getServerAccount(accountId);

        mCurrentServerStatList = DatabaseRepositoryProvider.getDatabaseInstance().getServerStatList(mServerAccount);
    }

    private void statGetServerStat(PrintWriter out) {
        if (mCurrentServerStatList.getServerStatList().size() == 0) {
            return;
        }

        //Lock to one stat
        for (int sendCount = 0; sendCount < 1; sendCount++) {
            ServerStat stat = mCurrentServerStatList.getServerStatList().get(mServerStatRotation++ % mCurrentServerStatList.getServerStatList().size());
            send(out, "SL_NAME", stat.getName().replace(" ", "_"));
            int index = 0;
            for (ServerStat.ServerPlayerStat playerStat : stat.getServerPlayerStatList()) {
                send(out, "SL_IDX", "" + index++, playerStat.getPlayer(), playerStat.getStat());
            }
        }
    }

    private void statGetPlayerStat(PrintWriter out, String[] params) throws ProtocolException {
        String playerIndex = params[1];
        String playerHash = params[2];

        PlayerAggregate playerAggregate = DatabaseRepositoryProvider.getDatabaseInstance().getPlayerAggregate(mServerAccount, playerHash);
        float playerPPR = playerAggregate.getRounds() == 0
                ? 0.0f
                : playerAggregate.getScore() / (float) playerAggregate.getRounds();

        float playerRank = (Math.max(0.0f, ((float) (playerAggregate.getKills() - playerAggregate.getDeaths()) / (float) (playerAggregate.getKills() + playerAggregate.getDeaths())) * 0.25f))
                + (Math.max(0.0f, Math.min(1.0f, (playerPPR / 4f) - 1f) * 0.5f))
                + ((playerAggregate.getGames() / 200) * 0.25f);

        log("Player Stats: Rank:" + Float.toString(playerRank) + " PPR:" + Float.toString(playerPPR));

        send(out,
                "STATS_UPDATE",
                playerIndex,
                Float.toString(playerRank),
                Float.toString(playerPPR)
        );
    }

    private void statRegisterPlayerStat(String[] params) {
        PlayerStat playerStat = new PlayerStat(
                mCurrentMapStat.getId(),
                mCurrentMapStat.getTimestamp(),
                Integer.parseInt(params[4]),
                params[2],
                params[3],
                Integer.parseInt(params[5]),
                Float.parseFloat(params[6]),
                Integer.parseInt(params[7]),
                Integer.parseInt(params[8]),
                Integer.parseInt(params[9]),
                Integer.parseInt(params[10])
        );
        DatabaseRepositoryProvider.getDatabaseInstance().putPlayerStat(mServerAccount, playerStat);
    }

    private void statRegisterGame(String[] params) throws ParseException {
        String[] teamScores = params[3].split(",");
        mCurrentMapStat = new MapStat(
                params[2],
                DEFAULT_DATE_FORMAT.parse(params[1]).getTime(),
                Integer.parseInt(teamScores[0]),
                Integer.parseInt(teamScores[1]));

        DatabaseRepositoryProvider.getDatabaseInstance().putMapStat(mServerAccount, mCurrentMapStat);
    }

    private void send(PrintWriter out, String command) {
        out.println(command);
        out.flush();
    }

    private void send(PrintWriter out, String command, String... params) {
        out.println(command + ' ' + StringUtil.join(" ", (CharSequence[]) params));
        out.flush();
    }

    private void log(String message) {
        System.out.println("Info:" + mClientId + ":" + message);
    }

    private void loge(String message) {
        System.out.println("Error:" + mClientId + ":" + message);
    }

}