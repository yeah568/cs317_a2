
import java.net.InetAddress;



// Lots of the action associated with handling a DNS query is processing 
// the response. Although not required you might find the following skeleton of
// a DNSreponse helpful. The class below has bunch of instance data that typically needs to be 
// parsed from the response. If you decide to use this class keep in mind that it is just a 
// suggestion and feel free to add or delete methods to better suit your implementation as 
// well as instance variables.



public class DNSResponse {
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


    private int answerCount = 0;            // number of answers
    private boolean decoded = false;        // Was this response successfully decoded
    private int nsCount = 0;                // number of nscount response records
    private int additionalCount = 0;        // number of additional (alternate) response records

    // Note you will almost certainly need some additional instance variables.

    // When in trace mode you probably want to dump out all the relevant information in a response

	void dumpResponse() {
		


	}

    // The constructor: you may want to add additional parameters, but the two shown are 
    // probably the minimum that you need.

	public DNSResponse (byte[] data, int len) {
	    int i = 0;


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

        int questions = (data[i++] << 8) | data[i++];

        // determine answer count

        this.answerCount = (data[i++] << 8) | data[i++];

        // determine NS Count

        this.nsCount = (data[i++] << 8) | data[i++];

	    // determine additional record count

        this.additionalCount = (data[i++] << 8) | data[i++];

	    // Extract list of answers, name server, and additional information response 
	    // records
	}


    // You will probably want a methods to extract a compressed FQDN, IP address
    // cname, authoritative DNS servers and other values like the query ID etc.


    // You will also want methods to extract the response records and record
    // the important values they are returning. Note that an IPV6 reponse record
    // is of type 28. It probably wouldn't hurt to have a response record class to hold
    // these records. 
}


