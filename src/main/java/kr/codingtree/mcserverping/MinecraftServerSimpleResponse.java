package kr.codingtree.mcserverping;

import com.google.gson.JsonElement;
import lombok.Data;

@Data
public class MinecraftServerSimpleResponse {

    private String protocolName;
    private int protocol;
    private int maxPlayers = 0, onlinePlayers = 0;
    private long status_ping = -1, connect_ping = -1;
    private JsonElement motd;

}
