
import java.io.IOException;
import java.net.*;

/**
 * @author Donald Acton
 * This example is adapted from Kurose & Ross
 *
 */
public class DNSlookup {

	static final int MIN_PERMITTED_ARGUMENT_COUNT = 2;
	static boolean tracingOn = false;

	static final int MAX_QUERIES = 30;
	static final int UDP_TIMEOUT = 5 * 1000; // milliseconds

    static DatagramSocket socket;


	static DNSMessage firstQuery;
	static int queryCount = 0;


    static boolean isSecondAttempt = false;
    static boolean continueQuerying = true;
    static boolean isNSResolution = false;

    static int minTTL = Integer.MAX_VALUE;


    static String fqdn;
    static InetAddress rootNameServer;

    static InetAddress currentNS;
    static String currentHost;


	/**
     * @param args
	 */
	public static void main(String[] args) throws Exception {
		int argCount = args.length;

		if (argCount < 2 || argCount > 3) {
			usage();
			return;
		}


		rootNameServer = InetAddress.getByName(args[0]);
		fqdn = args[1];

		if (argCount == 3 && args[2].equals("-t"))
				tracingOn = true;

		// Start adding code here to initiate the lookup

        // initialize socket
        socket = new DatagramSocket();
        socket.setSoTimeout(UDP_TIMEOUT);



        currentHost = fqdn;
        currentNS = rootNameServer;

        do {
            DNSMessage msg = new DNSMessage(currentHost);

            sendMessage(msg, currentNS);
            receiveMessage();
        } while (continueQuerying);
	}

	private static void sendMessage(DNSMessage message, InetAddress ns) {
		if (queryCount >= 30) {
            handleError(-3);
        }

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
        try {
            socket.send(packet);
        } catch (IOException e) {
            handleError(-4);
        }
    }

	private static void receiveMessage() {
		byte[] recvBuf = new byte[512];
		DatagramPacket recv = new DatagramPacket(recvBuf, recvBuf.length);


        try {
            socket.receive(recv);
        } catch (IOException e) {
            if (isSecondAttempt) {
                handleError(-2);
            }
            isSecondAttempt = true;
            continueQuerying = true;
            return;
        }
        isSecondAttempt = false;

        queryCount++;

        DNSMessage recvMsg = new DNSMessage(recv.getData());

		if (tracingOn) {
			recvMsg.dumpResponse();
		}

		if (recvMsg.getHeader().getReplyCode() == 3) {
			handleError(-1);
		} else if (recvMsg.getHeader().getReplyCode() != 0) {
			handleError(-4);
		}

		if (recvMsg.getAnswers().size() > 0) {
			// one or more answers
			RR firstAnswer = recvMsg.getAnswers().get(0);
			switch (firstAnswer.type) {
				case A:
				case AAAA:
				    if (isNSResolution) {
                        // have IP for next NS, resume
                        try {
                            currentNS = InetAddress.getByName(firstAnswer.data.toString());
                            isNSResolution = false;
                        } catch (UnknownHostException e) {
                            handleError(-4);
                        }
                        currentHost = fqdn;
                    } else {
                        // is final result
                        continueQuerying = false;
                        for (RR answer : recvMsg.getAnswers()) {
                            System.out.println(String.format("%s %d %s", fqdn, Math.min(minTTL, answer.ttl), answer.data.toString()));
                        }
                    }

					break;
				case CN:
					// CNAME, need to resolve
                    minTTL = Math.min(minTTL, firstAnswer.ttl);
					currentNS = rootNameServer;
                    currentHost = firstAnswer.data.toString();
                    return;
			}
		} else {
			// no answers, need to go deeper
			if (recvMsg.getAuthorities().size() > 0) {
                RR firstAuthority = recvMsg.getAuthorities().get(0);
                if (firstAuthority.type != RRType.NS) {
                    handleError(-4);
                }

                // get next nameserver to check
				String ns = firstAuthority.data.toString();

				// check if nameserver IP is given in additional info
				if (recvMsg.getAdditional().size() > 0) {
					for (RR additional : recvMsg.getAdditional()) {
						if (additional.name.equalsIgnoreCase(ns) && additional.type == RRType.A) {
							String nextNS = additional.data.toString();

							// NOTE: this does not do DNS resolution. If it's given an IP address string, it only
							// converts it to an InetAddress.
                            try {
                                currentNS =  InetAddress.getByName(nextNS);
                            } catch (UnknownHostException e) {
                                handleError(-4);
                            }
                            return;
						}
					}
				}

				// IP address not given, resolve name server IP.
                isNSResolution = true;
                currentNS = rootNameServer;
                currentHost = ns;
			}
		}
	}


/*	private DNSMessage resolveName(String host, InetAddress ns) {
		DNSMessage msg = new DNSMessage(host);
		try {
			sendMessage(msg, ns);
		} catch (IOException e) {
			handleError(-4);
		}


	}*/

	private static void handleError(int code) {
		System.out.println(String.format("%s %d 0.0.0.0", fqdn, code));
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


