package kr.codingtree.mcserverping;

import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

@UtilityClass
public class MCServerPing {

    private final byte PACKET_HANDSHAKE = 0x00,
                        PACKET_STATUS = 0x00,
                        PACKET_PING = 0x01;

    private final int STATUS_HANDSHAKE = 1;

    private final String SRV_PREFIX = "_minecraft._tcp.%s";

    private final char COLOR_CHAR = '\u00A7';
    private final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + COLOR_CHAR + "([0-9A-FK-ORX]|#[0-9A-Fa-f]{3,6})");
    
    public String getServerPing(String hostname, int port, int timeout, int protocolVersion) {
        String json = null;
        long ping = -1;

        try {

           Record[] records = new Lookup(String.format(SRV_PREFIX, hostname), Type.SRV).run();

            if (records != null) {

                for (Record record : records) {
                    SRVRecord srv = (SRVRecord) record;

                    hostname = srv.getTarget().toString().replaceFirst("\\.$", "");
                    port = srv.getPort();

                    System.out.println("hostname : " + hostname + ", port : " + port);
                }

            }
        } catch (TextParseException e) {
            e.printStackTrace();
        }

        Socket socket = null;

        DataInputStream in = null;
        DataOutputStream out = null;
        ByteArrayOutputStream handshake_bytes = new ByteArrayOutputStream();
        DataOutputStream handshake = new DataOutputStream(handshake_bytes);

        try {
            socket = new Socket();

            long start = System.currentTimeMillis();
            socket.connect(new InetSocketAddress(hostname, port), timeout);
            ping = System.currentTimeMillis() - start;

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            //> Handshake
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

            //> Status request
            out.writeByte(0x01); // Size of packet
            out.writeByte(PACKET_STATUS);

            //< Status response
            readVarInt(in); // Size
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
                String result = new String(data, Charset.forName("UTF-8"));

                //> Ping
                out.writeByte(0x09);
                out.writeByte(PACKET_PING);
                out.writeLong(System.currentTimeMillis());

                //< Ping
                readVarInt(in); // Size
                id = readVarInt(in);

                if (id == -1) {
                    System.out.println("Server prematurely ended stream. 4");
                } else if (id != PACKET_STATUS) {
                    System.out.println("Server returned invalid packet. 5");
                }

                System.out.println("Ping : " + ping);
                json = result;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (Exception e) {}
            try { in.close(); } catch (Exception e) {}
            try { out.close(); } catch (Exception e) {}
            try { handshake.close(); } catch (Exception e) {}
            try { handshake_bytes.close(); } catch (Exception e) {}
        }

        return json;
    }

    private int readVarInt(DataInputStream in) throws IOException {
        int i = 0, j = 0;

        while (true) {
            int k = in.readByte();

            i |= (k & 0x7F) << j++ * 7;

            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }

            if ((k & 0x80) != 128) {
                break;
            }
        }

        return i;
    }

    private void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }

            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }

    private String stripColors(final String input) {
        return input == null ? null : STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }
}