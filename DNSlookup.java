
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Random;

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
		sendMessage(msg, rootNameServer);

		// TODO: check  length
		byte[] recvBuf = new byte[512];
		DatagramPacket recv = new DatagramPacket(recvBuf, recvBuf.length);
		try {
			socket.receive(recv);
		} catch (SocketTimeoutException e) {
			handleError(-2, msg);
		}

		DNSMessage recvMsg = new DNSMessage(recv.getData());

		recvMsg.dumpResponse();
	}

	private static void sendMessage(DNSMessage message, InetAddress ns) {
		System.out.println("\n\n");
		System.out.println(String.format(
				"Query ID     %s %s --> %s",
				Integer.toUnsignedString(message.getHeader().getQueryID()),
				message.getQuestions().get(0).getName(),
				ns.getHostAddress()
		));
		byte[] buf = message.getBuffer();

		DatagramPacket packet = new DatagramPacket(buf, buf.length, ns, 53);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
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


