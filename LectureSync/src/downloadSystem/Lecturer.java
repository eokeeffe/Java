package downloadSystem;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
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
import net.jxta.socket.JxtaServerSocket;

import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * Creates a JxtaSocket for the Lecturer to send data to the Student Peers
 */
@SuppressWarnings("deprecation")
public class Lecturer extends Thread {
	private transient PeerGroup netPeerGroup = null;
	public final static String SOCKETIDSTR = "urn:jxta:uuid-59616261646162614E5047205032503393B5C2F6CA7A41FBB0F890173088E79404";
	private boolean isRunning = true;
	private String serviceName, dir, filename;

	public Lecturer(String ServiceName) throws IOException, PeerGroupException {
		System.setProperty(Logging.JXTA_LOGGING_PROPERTY, Level.OFF.toString());

		serviceName = ServiceName;
		NetworkManager manager = new NetworkManager(
				NetworkManager.ConfigMode.EDGE, serviceName, new File(new File(
						".cache"), serviceName).toURI());
		manager.setConfigPersistent(true);
		manager.startNetwork();
		netPeerGroup = manager.getNetPeerGroup();
	}

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

	public void setFileDirectory(String dir) {
		this.dir = dir;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * Wait for connections
	 */
	@Override
	public void run() {
		System.err.println(serviceName);
		JxtaServerSocket serverSocket = null;
		PipeAdvertisement adv = createSocketAdvertisement();
		try {
			serverSocket = new JxtaServerSocket(netPeerGroup, adv, 20, 0);
		}
		catch (IOException e) {
			System.err.println("failed to send message: " + e);
		}

		Socket socket = null;
		while (isRunning) {
			try {
				// System.err.println("Waiting for connections");
				socket = serverSocket.accept();
				if (socket != null) {
					System.err.println("New socket connection accepted");
					Thread thread = new Thread(new ConnectionHandler(socket,
							dir, filename), "Connection Handler Thread");
					thread.start();
				}
			}
			catch (Exception e) {
				// System.err.println("Connection Fail: "+e);
			}
		}

		if (serverSocket != null) {
			try {
				serverSocket.close();
			}
			catch (IOException e) {
				System.err
						.println("Error closing lecturer server socket: " + e);
			}
		}

	}
	
	public void end()
	{
		isRunning = false;
	}

	@ChannelPipelineCoverage("all")
	private class ConnectionHandler extends SimpleChannelHandler implements
			Runnable {
		Socket socket = null;
		String filename, directory;
		boolean isSynced = false;

		@SuppressWarnings("unused")
		ConnectionHandler(Socket socket) {
			this.socket = socket;
		}

		ConnectionHandler(Socket socket, String directory, String filename) {
			this.socket = socket;
			this.filename = filename;
			this.directory = directory;
		}

		/**
		 * Send data to an OutputStream
		 * 
		 * @param out
		 *            The OuptutStream
		 * @param message
		 *            The message to be sent
		 * @throws IOException
		 */
		public void sendMessage(DataOutputStream out, String message)
				throws IOException {
			System.err.println("Sending message to Student");
			out.writeUTF(message);
			out.flush();
		}

		public void sendInteger(DataOutputStream out, int key)
				throws IOException {
			System.err.println("Sending the key to student");
			out.writeInt(key);
			out.flush();
		}

		public void sendFile(DataOutputStream out, String filename)
				throws IOException {
			System.err.println("Sending the File to student");
			File file = new File(filename);
			if(!file.exists())
			{
				System.err.println("Couldn't send file:"+filename);
				return;
			}
			FileInputStream fstream = new FileInputStream(file);

			byte[] buffer = new byte[1024];
			int bytesRead, blocks = 0;
			while ((bytesRead = fstream.read(buffer)) != -1) {
				blocks++;
			}
			fstream.close();
			System.err.println("#Packets:" + blocks);
			sendInteger(out, blocks);

			fstream = new FileInputStream(file);
			buffer = new byte[1024];
			bytesRead = 0;
			while ((bytesRead = fstream.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
				out.flush();
				try {
					Thread.sleep(250);
				}
				catch (InterruptedException e) {
				}
			}

			file = null;
			fstream.close();
			out.flush();

			System.err.println("File sent");
		}

		/**
		 * Send data over socket
		 * 
		 * @param socket
		 * @throws IOException
		 */
		private void sendAndReceiveData(Socket socket) {
			try {
				// get socket output stream
				OutputStream out = socket.getOutputStream();
				DataOutputStream dout = new DataOutputStream(out);

				while (isRunning) {
					// first sync the files
					if (!isSynced) {
						isSynced = true;
						sendInteger(dout, message.DOWNLOAD_FILE);
						sendMessage(dout, filename);
						sendFile(dout, directory);
					}
				}
				socket.close();
				System.err.println("Connection closed");
			}
			catch (Exception ie) {
				System.err.println("Error trying to run the Lecturer Server: "
						+ ie);
			}
		}

		public void run() {
			sendAndReceiveData(socket);
		}
	}

	public static void main(String[] args) {
		System.setProperty(Logging.JXTA_LOGGING_PROPERTY, Level.OFF.toString());
		try {
			Thread.currentThread()
					.setName(Lecturer.class.getName() + ".main()");
			Lecturer lecturerSocket = new Lecturer("SocketServer");
			lecturerSocket
					.setFileDirectory("C:\\Users\\evan\\workspace\\FileStructure\\earth.jpg");
			lecturerSocket.setFilename("earth.jpg");
			lecturerSocket.run();
		}
		catch (Throwable e) {
			System.err.println("Failed : " + e);
		}
	}
}