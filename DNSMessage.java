
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


// Lots of the action associated with handling a DNS query is processing
// the response. Although not required you might find the following skeleton of
// a DNSreponse helpful. The class below has bunch of instance data that typically needs to be 
// parsed from the response. If you decide to use this class keep in mind that it is just a 
// suggestion and feel free to add or delete methods to better suit your implementation as 
// well as instance variables.



public class DNSMessage {
	private int bufIndex = 0;

    private DNSHeader header;
	private List<DNSQuestion> questions;
	private List<RR> answers;
    private List<RR> authorities;
    private List<RR> additional;


    private boolean decoded = false;        // Was this response successfully decoded

    // Note you will almost certainly need some additional instance variables.

    // When in trace mode you probably want to dump out all the relevant information in a response

	void dumpResponse() {
        System.out.println(String.format("Response ID: %d Authoritative %s",
                                            this.header.getQueryID(),
                                            this.header.isAuthoritative() ? "true" : "false"));
        System.out.println(String.format("  Answers (%d)", this.answers.size()));
        for (RR r : this.answers) {
            System.out.format("       %-30s %-10d %-4s %s\n", r.name, r.ttl, r.type.toString(), r.data.toString());
        }

        System.out.println(String.format("  Nameservers (%d)", this.authorities.size()));
        for (RR r : this.authorities) {
            System.out.format("       %-30s %-10d %-4s %s\n", r.name, r.ttl, r.type.toString(), r.data.toString());
        }

        System.out.println(String.format("  Additional Information (%d)", this.additional.size()));
        for (RR r : this.additional) {
            System.out.format("       %-30s %-10d %-4s %s\n", r.name, r.ttl, r.type.toString(), r.data.toString());
        }
	}

    // The constructor: you may want to add additional parameters, but the two shown are 
    // probably the minimum that you need.

	public DNSMessage (byte[] data) {

        // parse header
        this.header = new DNSHeader(data, this);

	    // Extract list of answers, name server, and additional information response 
	    // records

        this.questions = new ArrayList<>();
        this.answers = new ArrayList<>();
        this.authorities = new ArrayList<>();
        this.additional = new ArrayList<>();

        for (int i = 0; i < this.header.getQuestionCount(); i++) {
            this.questions.add(new DNSQuestion(data, this));
        }

        for (int i = 0; i < this.header.getAnswerCount(); i++) {
            this.answers.add(new RR(data, this));
        }

        for (int i = 0; i < this.header.getNsCount(); i++) {
            this.authorities.add(new RR(data, this));
        }

        for (int i = 0; i < this.header.getAdditionalCount(); i++) {
            this.additional.add(new RR(data, this));
        }
	}

	public DNSMessage(String fqdn) {
        this.header = new DNSHeader();
        this.questions = new ArrayList<>();
        this.questions.add(new DNSQuestion(fqdn));
    }

    public byte[] getBuffer() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write(this.header.getBuffer(), 0, this.header.getBuffer().length);

        for (DNSQuestion q : this.questions) {
            out.write(q.getBuffer(), 0, q.getBuffer().length);
        }
/*
        for (RR r : this.answers) {
            out.write(r.getBuffer(), 0, r.getBuffer().length);
        }*/

        return out.toByteArray();

    }


    // You will probably want a methods to extract a compressed FQDN, IP address
    // cname, authoritative DNS servers and other values like the query ID etc.


    // You will also want methods to extract the response records and record
    // the important values they are returning. Note that an IPV6 reponse record
    // is of type 28. It probably wouldn't hurt to have a response record class to hold
    // these records.

	public int getBufIndex() {
		return bufIndex;
	}

	public void setBufIndex(int bufIndex) {
		this.bufIndex = bufIndex;
	}

    public DNSHeader getHeader() {
        return header;
    }

    public void setHeader(DNSHeader header) {
        this.header = header;
    }

    public List<DNSQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<DNSQuestion> questions) {
        this.questions = questions;
    }

    public List<RR> getAnswers() {
        return answers;
    }

    public void setAnswers(List<RR> answers) {
        this.answers = answers;
    }

    public List<RR> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<RR> authorities) {
        this.authorities = authorities;
    }

    public List<RR> getAdditional() {
        return additional;
    }

    public void setAdditional(List<RR> additional) {
        this.additional = additional;
    }
}


