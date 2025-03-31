package kr.codingtree.mcserverping;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;

@Data
public class MinecraftServerResponse extends MinecraftServerSimpleResponse {

    private String protocolName;
    private int protocol;
    private int maxPlayers = 0, onlinePlayers = 0;
    private long status_ping = -1, connect_ping = -1;
    private JsonElement motd;
    private String favicon = "";


    public static MinecraftServerSimpleResponse fromJson(String json, boolean simple) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        MinecraftServerSimpleResponse result = simple ? new MinecraftServerSimpleResponse() : new MinecraftServerResponse();

        JsonObject version = root.getAsJsonObject("version");
        result.setProtocolName(version.get("name").getAsString());
        result.setProtocol(version.get("protocol").getAsInt());

        JsonObject players = root.getAsJsonObject("players");
        result.setMaxPlayers(players.get("max").getAsInt());
        result.setOnlinePlayers(players.get("online").getAsInt());

        if (!simple && root.has("favicon")) {
            ((MinecraftServerResponse) result).setFavicon(root.get("favicon").getAsString());
        }

        JsonObject description = root.getAsJsonObject("description");

        if (description.has("text")) {
            result.setMotd(description.get("text"));
        }

        if (description.has("extra")) {
            result.setMotd(description.get("extra"));
        }

        return result;
    }
}
