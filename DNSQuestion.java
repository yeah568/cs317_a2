import java.util.ArrayList;
import java.util.List;

public class DNSQuestion {
    private byte[] qname;
    private int qtype = 0; // two bytes
    private int qclass = 0;

    public DNSQuestion(String qname) {
        // default to type 1, class 1
        // A records, Internet class
        this(qname, 1, 1);
    }

    public DNSQuestion(String qname, int qtype, int qclass) {
        this.qname = new byte[qname.length()+2];

        int i = 0;
        for (String label : qname.split("\\.")) {
            this.qname[i++] = (byte) label.length();
            byte[] labelBuf = label.getBytes();
            System.arraycopy(labelBuf, 0, this.qname, i, labelBuf.length);
            i += labelBuf.length;
        }
        this.qname[i] = (byte) 0; // null terminator

        this.qtype = qtype;
        this.qclass = qclass;
    }

    public DNSQuestion(byte[] data, DNSMessage message) {
        int i = message.getBufIndex();

        int totalLength = 0;

        int len = data[i];
        do {
            totalLength += 1 + len;
            len = data[i+totalLength];
        } while (len != 0);

        totalLength++;

        this.qname = new byte[totalLength];
        System.arraycopy(data, i, this.qname, 0, totalLength);

        i += totalLength;

        this.qtype = (data[i++] << 8) | data[i++];
        this.qclass  = (data[i++] << 8) | data[i++];

        message.setBufIndex(i);
    }

    public byte[] getBuffer() {
        byte[] buf = new byte[qname.length + 2 + 2];
        System.arraycopy(this.qname, 0, buf, 0, this.qname.length);

        int i = this.qname.length;

        buf[i++] = (byte) ((this.qtype >> 4) & 0xf);
        buf[i++] = (byte) (this.qtype & 0xf);

        buf[i++] = (byte) ((this.qclass >> 4) & 0xf);
        buf[i++] = (byte) (this.qclass & 0xf);

        return buf;
    }

    public String getName() {
        int i = 0;
        int len = this.qname[i++];
        List<String> labels= new ArrayList<>();

        while (len != 0) {
            byte[] chars = new byte[len];

            for (int j = 0; j < len; j++) {
                chars[j] = this.qname[i++];
            }
            labels.add(new String(chars));
            len = this.qname[i++];
        }

        return String.join(".", labels);
    }
}

