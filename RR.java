import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class RR {
    String name;
    RRType type;
    int rclass;
    int ttl;
    int rdlength;
    RRData data;

    RR(byte[] data, DNSMessage message) {

        this.name = getName(data, message);

        int i = message.getBufIndex();

        this.type = RRType.fromInt((data[i++] << 8) | data[i++]);
        this.rclass = (data[i++] << 8) | data[i++];
        this.ttl = (data[i++] << 24) | (data[i++] << 16) | (data[i++] << 8) | data[i++];
        this.rdlength = (data[i++] << 8) | data[i++];

        message.setBufIndex(i);


        switch (this.type) {
            case A:
                this.data = new A_RRData(data, message);
                break;
            case AAAA:
                this.data = new AAAA_RRData(data, message);
                break;
            case NS:
                this.data = new NS_RRData(data, message);
                break;
            case CNAME:
                this.data = new CNAME_RRData(data, message);
                break;
        }
    }

    public static String getName(byte[] data, DNSMessage message) {
        int i = message.getBufIndex();

        int len = data[i++];
        List<String> labels= new ArrayList<>();

        while (len != 0) {
            if (((len >> 6) & 0b11) == 0b11) {
                // pointer
                // next 14 bits are offset, aka new index
                int offset = ((len & 0b111111) << 8) | data[i++];
                labels.add(getLabel(data, offset));
                break;
            } else {
                byte[] chars = new byte[len];

                for (int j = 0; j < len; j++) {
                    chars[j] = data[i++];
                }
                labels.add(new String(chars));
                len = data[i++];
            }
        }

        message.setBufIndex(i);

        return String.join(".", labels);
    }

    private static String getLabel(byte[] data, int index) {
        int len = data[index++];
        List<String> labels= new ArrayList<>();

        while (len != 0) {
            if (((len >> 6) & 0b11) == 0b11) {
                // pointer
                // next 14 bits are offset, aka new index
                int offset = ((len & 0b111111) << 8) | data[index++];
                labels.add(getLabel(data, offset));
                break;
            } else {
                byte[] chars = new byte[len];

                for (int i = 0; i < len; i++) {
                    chars[i] = data[index++];
                }
                labels.add(new String(chars));
                len = data[index++];
            }
        }

        return String.join(".", labels);
    }

/*    public byte[] getBuffer() {

    }*/
}

class RRData {
    public RRData() {

    }
}

class A_RRData extends RRData {
    InetAddress address;
    public A_RRData(byte[] data, DNSMessage message) {
        super();
        int i = message.getBufIndex();

        byte[] addr = new byte[4];
        for (int j = 0; j < addr.length; j++) {
            addr[j] = data[i++];
        }

        try {
            address = InetAddress.getByAddress(addr);
        } catch (UnknownHostException e) {
            System.err.println("IP address parsing failed.");
            e.printStackTrace();
        }

        message.setBufIndex(i);
    }


}

class AAAA_RRData extends RRData {
    InetAddress address;
    public AAAA_RRData(byte[] data, DNSMessage message) {
        super();
        int i = message.getBufIndex();

        byte[] addr = new byte[16];
        for (int j = 0; j < addr.length; j++) {
            addr[j] = data[i++];
        }

        try {
            address = InetAddress.getByAddress(addr);
        } catch (UnknownHostException e) {
            System.err.println("IP address parsing failed.");
            e.printStackTrace();
        }

        message.setBufIndex(i);

    }
}

class CNAME_RRData extends RRData {
    public CNAME_RRData(byte[] data, DNSMessage message) {
        super();
    }
}

class NS_RRData extends RRData {
    String ns;
    public NS_RRData(byte[] data, DNSMessage message) {
        super();
        this.ns = RR.getName(data, message);
    }
}