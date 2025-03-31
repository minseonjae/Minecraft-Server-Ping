package kr.codingtree.mcserverping;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Map;

@UtilityClass
public class MinecraftServerPing {

    public final byte PACKET_HANDSHAKE = 0x00,
            PACKET_STATUS = 0x00,
            PACKET_STATUS_SIZE = 0x01,
            PACKET_PING = 0x01,
            PACKET_PING_SIZE = 0x09;

    private final int STATUS_HANDSHAKE = 1;

    public MinecraftServerSimpleResponse getServerPing(String hostname, int port, int timeout, int protocolVersion, boolean simple) {
        long status_ping = -1;

        Socket socket = null;

        DataInputStream in = null;
        DataOutputStream out = null;
        ByteArrayOutputStream handshake_bytes = new ByteArrayOutputStream();
        DataOutputStream handshake = new DataOutputStream(handshake_bytes);

        Map.Entry<String, Integer> address = SRVResolver.lookupSRV(hostname, port);

        hostname = address.getKey();
        port = address.getValue();

        try {
            socket = new Socket();

            long start = System.currentTimeMillis();
            socket.connect(new InetSocketAddress(hostname, port), timeout);
            long connect_ping = System.currentTimeMillis() - start;

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // Handshake Start
            handshake_bytes = new ByteArrayOutputStream();
            handshake = new DataOutputStream(handshake_bytes);

            handshake.writeByte(PACKET_HANDSHAKE);
            writeVarInt(handshake, protocolVersion);
            writeVarInt(handshake, hostname.length());
            handshake.writeBytes(hostname);
            handshake.writeShort(port);
            writeVarInt(handshake, STATUS_HANDSHAKE);

            writeVarInt(out, handshake_bytes.size());
            out.write(handshake_bytes.toByteArray());
            // Handshake End

            String json = getStatus(out, in);

            status_ping = getPing(out, in);

            MinecraftServerSimpleResponse response = MinecraftServerResponse.fromJson(json, simple);

            response.setConnect_ping(connect_ping);
            response.setStatus_ping(status_ping);

            return response;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
            }
            try {
                in.close();
            } catch (Exception e) {
            }
            try {
                out.close();
            } catch (Exception e) {
            }
            try {
                handshake.close();
            } catch (Exception e) {
            }
            try {
                handshake_bytes.close();
            } catch (Exception e) {
            }
        }

        return null;
    }

    private String getStatus(DataOutputStream out, DataInputStream in) throws IOException {
        String result = null;
        out.writeByte(PACKET_STATUS_SIZE);
        out.writeByte(PACKET_STATUS);

        readVarInt(in);
        int id = readVarInt(in);

        if (id == -1) {
            System.out.println("Server prematurely ended stream. 0");
        } else if (id != PACKET_STATUS) {
            System.out.println("Server returned invalid packet. 1");
        } else {
            int length = readVarInt(in);

            if (length == -1) {
                System.out.println("Server prematurely ended stream. 2");
            } else if (length == 0) {
                System.out.println("Server returned unexpected value. 3");
            }

            byte[] data = new byte[length];
            in.readFully(data);
            result = new String(data, Charset.forName("UTF-8"));
        }
        return result;
    }

    private long getPing(DataOutputStream out, DataInputStream in) throws IOException {
        out.writeByte(PACKET_PING_SIZE);
        out.writeByte(PACKET_PING);
        out.writeLong(System.currentTimeMillis());

        //< Ping
        readVarInt(in); // Size
        int id = readVarInt(in);

        if (id == -1) {
            System.out.println("Server prematurely ended stream. 4");
        } else if (id != PACKET_PING) {
            System.out.println("Server returned invalid packet. 5");
        }

        return System.currentTimeMillis() - in.readLong();
    }

    private int readVarInt(DataInputStream in) throws IOException {
        int i = 0, j = 0, k = 0;

        do {
            k = (byte) in.read();
            int value = (k & 0b01111111);
            i |= (value << (7 * j));

            j++;

            if (j > 5) {
                throw new IOException("VarInt가 너무 큽니다");
            }

        } while ((k & 0x80) != 0);

        return i;
    }

    private void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        do {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }

            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        } while (paramInt != 0);
    }
}
