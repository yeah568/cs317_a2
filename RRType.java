public enum RRType {
    A      (1),  // a host address
    NS     (2),  // an authoritative name server
    MD     (3),  // a mail destination (Obsolete - use MX)
    MF     (4),  // a mail forwarder (Obsolete - use MX)
    CN     (5),  // the canonical name for an alias
    SOA    (6),  // marks the start of a zone of authority
    MB     (7),  // a mailbox domain name (EXPERIMENTAL)
    MG     (8),  // a mail group member (EXPERIMENTAL)
    MR     (9),  // a mail rename domain name (EXPERIMENTAL)
    NULL   (10), // a null RRData (EXPERIMENTAL)
    WKS    (11), // a well known service description
    PTR    (12), // a domain name pointer
    HINFO  (13), // host information
    MINFO  (14), // mailbox or mail list information
    MX     (15), // mail exchange
    TXT    (16), // text strings
    AAAA   (28); // IPV6 host address


    private final int RRTypeCode;
    RRType(int RRTypeCode) {
        this.RRTypeCode = RRTypeCode;
    }

    public static RRType fromInt(int i) {
        for (RRType t : RRType.values()) {
            if (t.RRTypeCode == i) { return t; }
        }
        return null;
    }

    @Override
    public String toString() {
        switch (this) {
            case A:
            case AAAA:
            case CN:
            case NS:
                return this.name();
            default:
                return String.format("%d", this.RRTypeCode);
        }
    }
}
