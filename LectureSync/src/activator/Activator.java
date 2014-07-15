package activator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import launcher.Launcher;
import net.jxta.exception.PeerGroupException;
import net.jxta.logging.Logging;
import net.jxta.platform.NetworkManager;
import netConfigurator.PresenterServer;
import netConfigurator.ViewerServer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import downloadSystem.Lecturer;
import downloadSystem.StudentPeer;

public class Activator implements BundleActivator {

	@SuppressWarnings("unused")
	private static BundleContext context;

	Launcher launcher = new Launcher();
	Lecturer lecturerServer;
	StudentPeer studentServer;
	PresenterServer presenter;
	ViewerServer viewer;
	NetworkManager manager;
	Timer scheduler = new Timer();

	JFrame frame;
	String filename, service, user;

	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		// turn off jxta logging
		System.setProperty(Logging.JXTA_LOGGING_PROPERTY, Level.OFF.toString());

		Activator.context = bundleContext;
		getDisplay();

		/*
		 * Create the Gnutella file agent
		 */
		// if (!isGnutellaAlive()) {cms = new Simpella();}
	}

	public void initNetManager() {
		String fileLog = "";
		if (launcher.getIsPeer()) {
			fileLog = launcher.getUserName();
		}
		else {
			fileLog = launcher.getServiceName();
		}

		try {
			manager = new NetworkManager(NetworkManager.ConfigMode.EDGE,
					fileLog, new File(new File(".cache"), fileLog).toURI());
		}
		catch (IOException e) {
			System.err.println("Error starting net manager: " + e);
		}
		if (manager != null) {
			try {
				manager.startNetwork();
			}
			catch (PeerGroupException e) {
				System.err.println("Error Running net manager: " + e);
			}
			catch (IOException e) {
				System.err.println("Error Running net manager: " + e);
			}
		}

	}

	public void startMessengers() {
		Logger jxtaLogger = Logger.getLogger("net.jxta");
		jxtaLogger.setLevel(Level.SEVERE);

		if (!launcher.getIsPeer()) {
			String dir = launcher.getFilePath();
			String fn;
			if (System.getProperty("os.name").startsWith("Windows")) {
				fn = dir.replace('\\', ' ');
			}
			else {
				fn = dir.replace('/', ' ');
			}
			String[] ls = fn.split(" ");
			fn = ls[ls.length - 1];

			System.err.println(dir);
			System.err.println(fn);
			presenter = new PresenterServer();
			presenter.setServiceName(service);
			presenter.setFile(dir, fn);
			presenter.start();
		}
		else {
			System.err.println("Launching viewer as :" + user);
			viewer = new ViewerServer();
			viewer.setUserName(user);
			viewer.start();
		}
	}

	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		launcher.setVisible(false);
	}

	public void getDisplay() {
		frame = new JFrame("Lync");
		frame.add(launcher);
		frame.pack();
		frame.setMaximumSize(java.awt.Toolkit.getDefaultToolkit()
				.getScreenSize());
		frame.setVisible(true);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		launcher.getLaunchButton().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Run the p2p Networking
				if (e.getSource() == launcher.getLaunchButton()) {
					user = launcher.getUserName();
					service = launcher.getServiceName();

					System.err.println("User:" + user);
					System.err.println("Service:" + service);

					startMessengers();

					if (launcher.getIsPeer()) {
						try {
							String value = System.getProperty("RDVWait",
									"false");
							boolean waitForRendezvous = Boolean.valueOf(value);
							System.err.println("Starting:"
									+ StudentPeer.class.getName());
							studentServer = new StudentPeer(viewer
									.getServiceName(), waitForRendezvous);
							studentServer.setName(StudentPeer.class.getName());
							studentServer.setPriority(8);
							studentServer.start();
							while (!studentServer.fileDownloaded()) {
							}
							filename = studentServer.getFile();
							studentServer.end();

							System.err.println("Received file:" + filename);
							File file = new File(filename);
							if (!file.exists()) {
								System.err.println("File:" + filename
										+ " doesn't exist locally");
							}
							else {
								System.err.println("Opening File:" + filename);
								launcher.startStudent(filename);

								TimerTask task = new TimerTask() {
									int ckey = -1;

									@Override
									public void run() {
										ckey = viewer.getKey();
										if (ckey >= 0
												&& ckey != launcher.screenviewer
														.getPage()) {
											if (launcher.screenviewer != null) {
												launcher.screenviewer
														.setPage(ckey);
											}
										}
										else {
											ckey = -1;
										}
									}
								};
								scheduler.schedule(task, 1500);

							}
						}
						catch (Throwable a) {
							System.err
									.println("Failed to launch Peer Download Server : "
											+ a);
						}
					}
					else {
						if (launcher.getFilePath() != null
								&& launcher.getFilePath().matches(".+\\.pdf")) {
							System.err
									.println("Starting Student\\Lecturer Server");

							try {
								System.err.println("Starting :" + service);
								String dir = launcher.getFilePath();
								String fn;
								if (System.getProperty("os.name").startsWith(
										"Windows")) {
									fn = dir.replace('\\', ' ');
								}
								else {
									fn = dir.replace('/', ' ');
								}
								String[] ls = fn.split(" ");
								fn = ls[ls.length - 1];
								System.err.println(dir);
								System.err.println(fn);

								lecturerServer = new Lecturer(service);
								lecturerServer.setFileDirectory(dir);
								lecturerServer.setFilename(fn);
								lecturerServer.setName("Lecturer Service");
								lecturerServer.start();
							}
							catch (Throwable a) {
								System.err
										.println("Failed to launch Presenter File Server : "
												+ a);
							}
							// publish the key events to the network
							TimerTask task = new TimerTask() {
								@Override
								public void run() {
									if (launcher.screenviewer != null) {
										int ckey = launcher.screenviewer
												.getPage();
										presenter.broadCastPageChange(ckey);
									}
								}
							};
							scheduler.schedule(task, 1500);

						}
					}
				}
			}
		});
	}

}
