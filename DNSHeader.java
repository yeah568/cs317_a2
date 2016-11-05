import java.util.Random;

public class DNSHeader {
    private int queryID;                    // this is for the response it must match the one in the request

    // flags
    private boolean response = false;       // is this a response message
    private int opcode = 0;                 // opcode
    private boolean authoritative = false;  // Is this an authoritative record
    private boolean truncated = false;      // is this message truncated
    private boolean recursion = false;      // is recursion desired
    private boolean recursionAvailable = false; // is recursion availabe on server
    // Z: reserved
    private boolean authenticated = false;  // is answer/authority authenticated by server
    private boolean nonAuthDataAcceptable = false; // is non-authenticated data acceptable
    private int replyCode = 0;              // reply code

    private int questionCount = 0;          // number of questions
    private int answerCount = 0;            // number of answers
    private int nsCount = 0;                // number of nscount response records
    private int additionalCount = 0;        // number of additional (alternate) response records


    public DNSHeader() {
        this.queryID = new Random().nextInt(65535);

        this.questionCount = 1; // only dealing with one query

    }

    public DNSHeader (byte[] data, DNSMessage message) {
        int i = message.getBufIndex();

        // The following are probably some of the things
        // you will need to do.
        // Extract the query ID

        this.queryID = (data[i++] << 8) | data[i++];

        // Make sure the message is a query response and determine
        // if it is an authoritative response or note

        int flags = (data[i++] << 8) | data[i++];

        this.response = ((flags >> 15) & 0x1) == 0x1;
        this.opcode = (flags >> 11) & 0b1111;
        this.authoritative = ((flags >> 10) & 0x1) == 0x1;
        this.truncated = ((flags >> 9) & 0x1) == 0x1;
        this.recursion = ((flags >> 8) & 0x1) == 0x1;
        this.recursionAvailable = ((flags >> 7) & 0x1) == 0x1;
        this.authenticated = ((flags >> 5) & 0x1) == 0x1;
        this.nonAuthDataAcceptable = ((flags >> 4) & 0x1) == 0x1;
        this.replyCode = flags & 0b1111;

        this.questionCount = (data[i++] << 8) | data[i++];

        // determine answer count

        this.answerCount = (data[i++] << 8) | data[i++];

        // determine NS Count

        this.nsCount = (data[i++] << 8) | data[i++];

        // determine additional record count

        this.additionalCount = (data[i++] << 8) | data[i++];

        message.setBufIndex(i);
    }

    public byte[] getBuffer() {
        byte[] buf = new byte[12];

        int i = 0;
        buf[i++] = (byte) ((queryID >> 8) & 0xff);
        buf[i++] = (byte) (queryID & 0xff);

        byte flag1 = 0;
        if (response) {
            flag1 |= (1 << 7);
        }
        flag1 |= (opcode & 0xf) << 3;
        if (authoritative) {
            flag1 |= (1 << 2);
        }
        if (truncated) {
            flag1 |= (1 << 1);
        }
        if (recursion) {
            flag1 |= 1;
        }
        buf[i++] = flag1;

        byte flag2 = 0;
        if (recursionAvailable) {
            flag2 |= (1 << 7);
        }
        if (authenticated) {
            flag2 |= (1 << 5);
        }
        if (nonAuthDataAcceptable) {
            flag2 |= (1 << 4);
        }
        flag2 |= replyCode & 0xf;
        buf[i++] = flag2;

        buf[i++] = (byte) ((questionCount >> 8) & 0xff);
        buf[i++] = (byte) (questionCount & 0xff);

        buf[i++] = (byte) ((answerCount >> 8) & 0xff);
        buf[i++] = (byte) (answerCount & 0xff);

        buf[i++] = (byte) ((nsCount >> 8) & 0xff);
        buf[i++] = (byte) (nsCount & 0xff);

        buf[i++] = (byte) ((additionalCount >> 8) & 0xff);
        buf[i++] = (byte) (additionalCount & 0xff);

        return buf;
    }


    public int getQueryID() {
        return queryID;
    }

    public boolean isResponse() {
        return response;
    }

    public int getOpcode() {
        return opcode;
    }

    public boolean isAuthoritative() {
        return authoritative;
    }

    public boolean isTruncated() {
        return truncated;
    }

    public boolean isRecursion() {
        return recursion;
    }

    public boolean isRecursionAvailable() {
        return recursionAvailable;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public boolean isNonAuthDataAcceptable() {
        return nonAuthDataAcceptable;
    }

    public int getReplyCode() {
        return replyCode;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public int getAnswerCount() {
        return answerCount;
    }

    public int getNsCount() {
        return nsCount;
    }

    public int getAdditionalCount() {
        return additionalCount;
    }
}
