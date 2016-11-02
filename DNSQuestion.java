public class DNSQuestion {
    private byte[] qname;
    private int qtype = 0; // two bytes
    private int qclass = 0;

    public DNSQuestion(String qname) {
        // default to type 1, class 1
        // A records, Internet class
        new DNSQuestion(qname, 1, 1);
    }

    public DNSQuestion(String qname, int qtype, int qclass) {
        this.qname = new byte[qname.length()+1];

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
}

