
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/**
 * @author Donald Acton
 * This example is adapted from Kurose & Ross
 *
 */
public class DNSlookup {

	static final int MIN_PERMITTED_ARGUMENT_COUNT = 2;
	static boolean tracingOn = false;
	static InetAddress rootNameServer;

	static final int MAX_QUERIES = 30;
	static final int UDP_TIMEOUT = 5 * 1000; // milliseconds

    static DatagramSocket socket;


	static DNSMessage firstQuery;
	static int queryCount = 0;


	/**
     * @param args
	 */
	public static void main(String[] args) throws Exception {
		String fqdn;
		DNSMessage response; // Just to force compilation
		int argCount = args.length;

		if (argCount < 2 || argCount > 3) {
			usage();
			return;
		}

		int minTTL = Integer.MAX_VALUE;

		rootNameServer = InetAddress.getByName(args[0]);
		fqdn = args[1];

		if (argCount == 3 && args[2].equals("-t"))
				tracingOn = true;

		// Start adding code here to initiate the lookup

        // initialize socket
        socket = new DatagramSocket();
        socket.setSoTimeout(UDP_TIMEOUT);



        DNSMessage msg = new DNSMessage(fqdn);
		firstQuery = msg;
		sendMessage(msg, rootNameServer);
		receiveMessage();
	}

	private static void sendMessage(DNSMessage message, InetAddress ns) throws IOException {
		if (query)

		if (tracingOn) {
			System.out.println(String.format(
					"\n\nQuery ID     %s %s --> %s",
					Integer.toUnsignedString(message.getHeader().getQueryID()),
					message.getQuestions().get(0).getName(),
					ns.getHostAddress()
			));
		}

		byte[] buf = message.getBuffer();

		DatagramPacket packet = new DatagramPacket(buf, buf.length, ns, 53);
		socket.send(packet);
	}

	private static void receiveMessage() throws IOException {
		byte[] recvBuf = new byte[512];
		DatagramPacket recv = new DatagramPacket(recvBuf, recvBuf.length);

		socket.receive(recv);

		DNSMessage recvMsg = new DNSMessage(recv.getData());

		if (tracingOn) {
			recvMsg.dumpResponse();
		}

		if (recvMsg.getHeader().getReplyCode() == 3) {
			handleError(-1, firstQuery);
		} else if (recvMsg.getHeader().getReplyCode() != 0) {
			handleError(-4, firstQuery);
		}

		if (recvMsg.getAnswers().size() > 0) {
			// one or more answers
			RR firstAnswer = recvMsg.getAnswers().get(0);
			switch (firstAnswer.type) {
				case A:
					break;
				case AAAA:
					break;
				case CN:
					// CNAME, need to resolve
					DNSMessage msg = new DNSMessage(firstAnswer.data.toString());
					sendMessage(msg, rootNameServer);
					receiveMessage();
					break;
			}
		} else {
			// no answers, need to go deeper
			if (recvMsg.getAuthorities().size() > 0) {
				// get next nameserver to check
				String ns = recvMsg.getAuthorities().get(0).data.toString();

				// check if nameserver IP is given in additional info
				if (recvMsg.getAdditional().size() > 0) {
					for (RR additional : recvMsg.getAdditional()) {
						if (additional.name.equalsIgnoreCase(ns) && additional.type == RRType.A) {
							String nextNS = additional.data.toString();

							DNSMessage msg = new DNSMessage(firstQuery.getQuestions().get(0).getName());
							// NOTE: this does not do DNS resolution. If it's given an IP address string, it only
							// converts it to an InetAddress.
							sendMessage(msg, InetAddress.getByName(nextNS));
							receiveMessage();
							break;
						}
					}
				}

				// IP address not given, resolve name server IP.


			}
		}
	}


	private DNSMessage resolveName(String host, InetAddress ns) {
		DNSMessage msg = new DNSMessage(host);
		try {
			sendMessage(msg, ns);
		} catch (IOException e) {
			handleError(-4, msg);
		}


	}

	private static void handleError(int code, DNSMessage message) {
		System.out.println(String.format("%s %d 0.0.0.0", message.getQuestions().get(0).getName(), code));
		System.exit(1);
	}

	private static void usage() {
		System.out.println("Usage: java -jar DNSlookup.jar rootDNS name [-t]");
		System.out.println("   where");
		System.out.println("       rootDNS - the IP address (in dotted form) of the root");
		System.out.println("                 DNS server you are to start your search at");
		System.out.println("       name    - fully qualified domain name to lookup");
		System.out.println("       -t      -trace the queries made and responses received");
	}
}


