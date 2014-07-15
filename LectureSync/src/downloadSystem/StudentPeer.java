package downloadSystem;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

import messages.message;
import net.jxta.document.AdvertisementFactory;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.logging.Logging;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaSocket;

public class StudentPeer extends Thread {
	private transient NetworkManager manager = null;
	private transient PeerGroup netPeerGroup = null;
	private transient PipeAdvertisement pipeAdv;
	private transient boolean waitForRendezvous = false;
	public final static String SOCKETIDSTR = "urn:jxta:uuid-59616261646162614E5047205032503393B5C2F6CA7A41FBB0F890173088E79404";
	private Integer key = 0;
	private String message_in = "";
	private int pageEvent = 0;
	private boolean isRunning = true;
	private String filename = null, serviceName = null;
	private boolean downloaded = false;

	// Constructor
	public StudentPeer(String serviceName, boolean waitForRendezvous) {
		this.serviceName = serviceName;
		System.setProperty(Logging.JXTA_LOGGING_PROPERTY, Level.OFF.toString());
		try {
			manager = new NetworkManager(NetworkManager.ConfigMode.EDGE,
					"SocketStudent", new File(new File(".cache"),
							"SocketStudent").toURI());
			manager.startNetwork();
		}
		catch (Exception e) {
			System.err.println(e);
		}
		netPeerGroup = manager.getNetPeerGroup();
		if (waitForRendezvous) {
			manager.waitForRendezvousConnection(0);
		}
	}

	/**
	 * Creates a pipe advertisement
	 * 
	 * @return The PiperAdvertisement
	 */
	public PipeAdvertisement createSocketAdvertisement() {
		PipeID socketID = null;

		try {
			socketID = (PipeID) IDFactory.fromURI(new URI(SOCKETIDSTR));
		}
		catch (URISyntaxException use) {
			use.printStackTrace();
		}
		PipeAdvertisement advertisement = (PipeAdvertisement) AdvertisementFactory
				.newAdvertisement(PipeAdvertisement.getAdvertisementType());
		advertisement.setPipeID(socketID);
		advertisement.setType(PipeService.UnicastType);
		advertisement.setName(serviceName);

		return advertisement;
	}

	/**
	 * Receives a message_in from the DataInput of a Jxta Socket
	 * 
	 * @param JxtaSocket
	 * @return The message_in
	 * @throws IOException
	 */
	public String receivemessage_in(JxtaSocket socket) throws IOException {
		InputStream in = socket.getInputStream();
		DataInputStream din = new DataInputStream(in);
		return din.readUTF();
	}

	/**
	 * Receives an integer from the DataInput of a Jxta Socket
	 * 
	 * @param socket
	 * @return The integer
	 * @throws IOException
	 */
	public int receiveInteger(JxtaSocket socket) throws IOException {
		InputStream in = socket.getInputStream();
		DataInputStream din = new DataInputStream(in);
		return din.readInt();
	}

	synchronized public int getPageEvent() {
		return pageEvent;
	}

	public void run() {
		pipeAdv = createSocketAdvertisement();
		JxtaSocket socket = null;
		int content_length = 0;

		if (waitForRendezvous) {
			manager.waitForRendezvousConnection(0);
		}

		try {
			System.err.println("Connecting to the Lecturer Server");
			socket = new JxtaSocket(netPeerGroup, null, pipeAdv, 5000, false);
			while (isRunning) {
				try {
					// Example of receiving the message_in and the key using
					key = receiveInteger(socket);
					switch (key) {
					case message.SERVICE_NAME: {
						serviceName = receivemessage_in(socket);
						System.err.println(message_in);
						break;
					}
					case message.SERVER_INFO: {
						message_in = receivemessage_in(socket);
						System.err.println(message_in);
						break;
					}
					case message.DOWNLOAD_FILE: {
						filename = receivemessage_in(socket);
						System.err.println("Downloading:" + filename);
						content_length = receiveInteger(socket);
						System.err.println("Packets:" + content_length);
						InputStream in = null;
						FileOutputStream fout = null;
						
						File file = new File(filename);
						if (!file.exists()) {
							System.err.println("Creating File locally");
							try{
							in = socket.getInputStream();
							fout = new FileOutputStream(file);
							byte data[] = new byte[1024];
							int count = 0,packets=0;
							//System.err.println("Packets to get:"+ content_length);
							while ((count = in.read(data, 0, 1024)) != -1 && packets <= content_length-1) {
								//System.err.println(packets+" of "+content_length+":"+(float)packets/content_length*100);
								packets++;
								fout.write(data, 0, count);
								//fout.flush();
							}
							System.err.println(count);
							System.err.println("After Download");

							in.close();
							fout.close();
							isRunning = false;
							System.err.println("File:" + filename
									+ " Downloaded");
							}catch(Exception e)
							{
								//System.err.println("Error download: "+e);
								if(in!=null){in.close();}
								if(fout!=null){fout.close();}
								isRunning = false;
								System.err.println("File:" + filename
										+ " Downloaded");
							}
						}
						else {
							System.err.println("File exists");
						}
						file = null;
						downloaded = true;
						break;
					}
					case message.PAGE_EVENT: {
						pageEvent = receiveInteger(socket);
						break;
					}
					default: {
						message_in = "";
						pageEvent = 0;
						break;
					}
					}
				}
				catch (Exception e) {
					//System.err.println("Caught something: " + e);
				}
			}

		}
		catch (IOException io) {
			System.err.println("IO Error in opening socket to master server :"
					+ io);
		}
		finally {
			if (socket != null) {
				try {
					socket.close();
				}
				catch (IOException e) {
					System.err
							.println("Failed to close the Peer socket properly: "
									+ e);
				}
			}
		}
	}

	synchronized public boolean fileToRead() {
		if (filename != null) {
			return true;
		}
		return false;
	}

	synchronized public String getFile() {
		return filename;
	}

	synchronized public boolean fileDownloaded() {
		return downloaded;
	}

	public void end() throws PeerGroupException, IOException {
		manager.stopNetwork();
	}

	public static void main(String[] args) {
		System.setProperty(Logging.JXTA_LOGGING_PROPERTY, Level.OFF.toString());

		try {
			String value = System.getProperty("RDVWait", "false");
			boolean waitForRendezvous = Boolean.valueOf(value);
			StudentPeer studPeer = new StudentPeer("SocketServer",
					waitForRendezvous);
			studPeer.setName(StudentPeer.class.getName() + ".main()");
			studPeer.run();
			studPeer.end();
		}
		catch (Throwable e) {
			System.err.println("Failed to start the Peer Server: " + e);
		}
	}

}