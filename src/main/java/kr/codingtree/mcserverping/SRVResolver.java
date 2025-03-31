package kr.codingtree.mcserverping;

import lombok.experimental.UtilityClass;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.util.AbstractMap;
import java.util.Map;

@UtilityClass
public class SRVResolver {

    public static final String SRV_PREFIX = "_minecraft._tcp.";

    public Map.Entry<String, Integer> lookupSRV(String hostname, int port) {

        try {
            Record[] records = new Lookup(SRV_PREFIX + hostname, Type.SRV).run();

            if (records != null && records.length > 0) {

                for (Record record : records) {
                    SRVRecord srv = (SRVRecord) record;

                    hostname = srv.getTarget().toString().replaceFirst("\\.$", "");
                    port = srv.getPort();
                }

            }
        } catch (TextParseException e) {
            e.printStackTrace();
        }

        return new AbstractMap.SimpleEntry<>(hostname, port);
    }

}
