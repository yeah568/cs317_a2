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

        this.type = RRType.fromInt(((data[i++] << 8) & 0xff00) | (data[i++] & 0xff));
        this.rclass = ((data[i++] << 8) & 0xff00) | (data[i++] & 0xff);
        this.ttl = ((data[i++] << 24) & 0xff000000) | ((data[i++] << 16) & 0xff0000) | ((data[i++] << 8) & 0xff00) | (data[i++] & 0xff);
        this.rdlength = ((data[i++] << 8) & 0xff00) | (data[i++] & 0xff);

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
            case CN:
                this.data = new CNAME_RRData(data, message);
                break;
            default:
                this.data = new RRData();
                message.setBufIndex(i+rdlength);
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
                int offset = ((len << 8) & 0x3f00) | (data[i++] & 0xff);
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

    public String toString() {
        return "----";
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

    @Override
    public String toString() {
        return address.getHostAddress();
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

    @Override
    public String toString() {
        return address.getHostAddress();
    }
}

class CNAME_RRData extends RRData {
    String domain;
    public CNAME_RRData(byte[] data, DNSMessage message) {
        super();
        this.domain = RR.getName(data, message);
    }

    @Override
    public String toString() {
        return domain;
    }
}

class NS_RRData extends RRData {
    String ns;
    public NS_RRData(byte[] data, DNSMessage message) {
        super();
        this.ns = RR.getName(data, message);
    }

    @Override
    public String toString() {
        return ns;
    }
}