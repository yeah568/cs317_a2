import java.net.InetAddress;

public class RR {
    String name;
    RRType type;
    int rclass;
    int ttl;
    int rdlength;
    RRData data;

    public RR(byte[] data, DNSMessage message) {
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


}

class RRData {
    public RRData() {

    }
}

class A_RRData extends RRData {
    InetAddress address;
    public A_RRData(byte[] data, DNSMessage message) {
        super();

    }
}

class AAAA_RRData extends RRData {
    public AAAA_RRData(byte[] data, DNSMessage message) {
        super();
    }
}

class CNAME_RRData extends RRData {
    public CNAME_RRData(byte[] data, DNSMessage message) {
        super();
    }
}

class NS_RRData extends RRData {
    public NS_RRData(byte[] data, DNSMessage message) {
        super();
    }
}