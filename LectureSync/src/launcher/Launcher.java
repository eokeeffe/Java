package launcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import pdfviewer.FullScreenViewerBasic;
import pdfviewer.ServiceObject;

public class Launcher extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JButton btnFileChooser;
	private JButton openFile;
	private JTabbedPane tabbedPane;
	private JTextArea txtServiceName;
	private JTextArea txtName;
	private JFileChooser fileChooser;
	private JScrollPane serviceScrollPane;
	private JPanel Console;
	private JScrollPane ConsoleScroll;
	private JTextArea consoleText;
	private JButton btnLaunch;
	private JToggleButton btnPresenterViewer;
	private boolean isPeer = false;
	private String filePath;
	private JPanel pnlStartService;
	private JPanel pnlConnectService;
	private JPanel pnlButtonContainer;
	public FullScreenViewerBasic screenviewer;
	private String serviceName = "Enter a Service Name";
	private String user = "Enter your name";
	private boolean wipeUser = true;
	private boolean wipeService = true;

	public boolean getIsPeer() {
		return this.isPeer;
	}

	public JButton getLaunchButton() {
		return this.btnLaunch;
	}

	public JTabbedPane getTabbedPane() {
		return this.tabbedPane;
	}

	public String getFilePath() {
		return this.filePath;
	}

	synchronized public String getServiceName() {
		return this.serviceName;
	}

	synchronized public String getUserName() {
		return user;
	}

	public void setFilePath(String filepath) {
		this.filePath = filepath;
	}

	@SuppressWarnings("unused")
	private final int START_SERVICE_TAB = 0;

	public Launcher() {
		super();
		tabbedPane = new JTabbedPane();

		pnlStartService = new JPanel(new GridLayout(10, 2, 15, 5));
		pnlConnectService = new JPanel();

		serviceScrollPane = new JScrollPane();
		serviceScrollPane.setBounds(32, 22, 358, 266);

		Console = new JPanel(new FlowLayout());

		txtServiceName = new JTextArea(serviceName);
		txtServiceName.setBorder(BorderFactory.createLineBorder(Color.black));
		txtServiceName.setEditable(true);
		txtServiceName.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (wipeService) {
					wipeService = false;
					serviceName = new String();
				}
				if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
				{
					String temp = new String();
					for(int i=0;i<serviceName.length()-1;i++)
					{
						temp += serviceName.charAt(i);
					}
					serviceName = temp;
				}
				else
				{
					serviceName += e.getKeyChar();
				}

				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String temp = new String();
					serviceName += e.getKeyChar();
					for (int i = 0; i < serviceName.length(); i++) {
						char c = serviceName.charAt(i);
						if (Character.isDigit(c) || Character.isLetter(c)) {
							temp += c;
						}
					}
					serviceName = temp;
				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});

		txtName = new JTextArea(user);
		txtName.setBorder(BorderFactory.createLineBorder(Color.black));
		txtName.setVisible(false);
		txtName.setEditable(true);
		txtName.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (wipeUser) {
					wipeUser = false;
					user = new String();
				}
				if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
				{
					String temp = new String();
					for(int i=0;i<user.length()-1;i++)
					{
						temp += user.charAt(i);
					}
					user = temp;
				}
				else
				{
					user += e.getKeyChar();
				}

				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String temp = new String();
					user += e.getKeyChar();
					for (int i = 0; i < user.length(); i++) {
						char c = user.charAt(i);
						if (Character.isLetter(c) || Character.isDigit(c)) {
							temp += c;
						}
					}
					user = temp;
				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});

		consoleText = new JTextArea("Running Debug Lync\n", 25, 50);
		consoleText.setBorder(BorderFactory.createLineBorder(Color.black));

		consoleText.setLineWrap(true);
		ConsoleScroll = new JScrollPane(consoleText);
		ConsoleScroll
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		Console.add(ConsoleScroll);

		fileChooser = new JFileChooser();
		this.setPreferredSize(new Dimension(800, 600));

		btnFileChooser = new JButton("Choose File");
		btnFileChooser.addActionListener(this);
		btnFileChooser.setPreferredSize(new Dimension(200, 100));

		btnLaunch = new JButton("Launch Service");
		btnLaunch.addActionListener(this);
		btnLaunch.setPreferredSize(new Dimension(200, 100));
		btnLaunch.setEnabled(false);

		openFile = new JButton("Open File");
		openFile.addActionListener(this);
		openFile.setPreferredSize(new Dimension(200, 100));
		openFile.setEnabled(false);

		/*
		 * Check if the user is looking to listen to a lecture
		 */
		btnPresenterViewer = new JToggleButton("Present");
		btnPresenterViewer.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (arg0.getStateChange() == ItemEvent.SELECTED) {
					btnPresenterViewer.setText("Listen");
					btnFileChooser.setVisible(false);
					txtServiceName.setVisible(false);
					openFile.setVisible(true);
					btnLaunch.setVisible(true);
					btnLaunch.setEnabled(true);
					txtName.setVisible(true);
					isPeer = true;
				}
				else {
					btnPresenterViewer.setText("Present");
					btnFileChooser.setVisible(true);
					txtServiceName.setVisible(true);
					txtName.setVisible(false);
					openFile.setVisible(true);
					if (serviceName != null || serviceName.equals("")) {
						btnLaunch.setVisible(false);
					}
					isPeer = false;
				}
			}
		});

		pnlConnectService.add(serviceScrollPane);
		pnlConnectService.setSize(new Dimension(800, 600));

		pnlStartService.add(txtName);
		pnlStartService.add(txtServiceName);
		pnlStartService.add(btnPresenterViewer);
		pnlStartService.add(btnFileChooser);
		pnlStartService.add(btnLaunch);
		pnlStartService.add(openFile);
		pnlStartService.setSize(new Dimension(800, 600));

		tabbedPane.setPreferredSize(new Dimension(800, 600));
		tabbedPane.addTab("Launch Service", null, pnlStartService);
		tabbedPane.addTab("Connect to Service", serviceScrollPane);
		tabbedPane.addTab("Console", null, Console);
		add(tabbedPane);

		PipedInputStream inPipe = new PipedInputStream();
		final PipedInputStream outPipe = new PipedInputStream();
		System.setIn(inPipe);
		try {
			System.setOut(new PrintStream(new PipedOutputStream(outPipe), true));
		}
		catch (IOException e1) {
			System.err.println(e1);
		}

		new SwingWorker<Void, String>() {
			private Scanner s;

			protected Void doInBackground() throws Exception {
				s = new Scanner(outPipe);
				while (s.hasNextLine()) {
					String line = s.nextLine();
					publish(line);
				}
				return null;
			}

			@Override
			protected void process(java.util.List<String> chunks) {
				for (String line : chunks) {
					// consoleText.setText(line);
					consoleText.append(line);
				}
				consoleText.append("\n");
			}
		}.execute();
	}

	public void refreshServiceList(Vector<ServiceObject> services) {
		if (pnlButtonContainer == null) {
			pnlButtonContainer = new JPanel();
			serviceScrollPane.add(pnlButtonContainer);
		}
		if (pnlButtonContainer.getComponentCount() > 0) {
			pnlButtonContainer.removeAll();
		}

		// Vector<ServiceObject> services = getDummyList();
		ServiceObject currentService;
		pnlButtonContainer.setLayout(new GridLayout(services.size(), 1));

		for (int i = 0; i < services.size(); i++) {
			currentService = services.get(i);
			JButton currentButton = new JButton();
			currentButton.setPreferredSize(new Dimension(pnlButtonContainer
					.getWidth(), 50));
			currentButton.setSize(pnlButtonContainer.getWidth(),
					pnlButtonContainer.getHeight());
			currentButton.setText(currentService.getName());
			currentButton.setName("service: " + currentService.getName());
			currentButton.addActionListener(this);
			pnlButtonContainer.add(currentButton);
		}

		serviceScrollPane.setViewportView(pnlButtonContainer);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		if (arg0.getSource() == openFile) {
			if (!isPeer) {
				if (filePath != null && filePath.matches(".+\\.pdf")) {
					serviceName = txtServiceName.getText();
					System.out.println("Starting Service:" + serviceName);
					screenviewer = new FullScreenViewerBasic(filePath,
							FullScreenViewerBasic.MODE_LECTURER);
				}
			}
		}
		if (arg0.getSource() == btnFileChooser) {
			int ret = fileChooser.showOpenDialog(Launcher.this);

			if (ret == JFileChooser.APPROVE_OPTION) {
				filePath = fileChooser.getSelectedFile().getAbsolutePath();
				if (filePath.matches(".+\\.pdf")) {
					String fileName = filePath.substring(filePath
							.lastIndexOf("/") + 1);
					btnFileChooser.setText(fileName);
					btnLaunch.setEnabled(true);
					openFile.setEnabled(true);
				}
				else {
					if (btnLaunch.isEnabled()) {
						btnLaunch.setEnabled(false);
					}
					if (openFile.isEnabled()) {
						openFile.setEnabled(false);
					}
				}
			}
		}

		if (arg0.getSource() == btnLaunch) {

		}
		else if (arg0.getSource().getClass().getSimpleName().equals("JButton")) {
			JButton button = (JButton) arg0.getSource();
			String buttonName = button.getName();
			if (buttonName != null) {
				if (buttonName.startsWith("service")) {
					String serviceName = buttonName.substring(
							buttonName.indexOf(' ') + 1, buttonName.length());
					System.out.println(serviceName);
				}
			}
		}
	}

	public void startStudent(String filename) {
		screenviewer = new FullScreenViewerBasic(filename,
				FullScreenViewerBasic.MODE_STUDENT);
	}

	public JFrame getDisplay() {
		return screenviewer.getDisplay();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame("Lync");
				frame.add(new Launcher());
				frame.pack();
				frame.setMaximumSize(java.awt.Toolkit.getDefaultToolkit()
						.getScreenSize());
				frame.setVisible(true);

				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		});
	}

}
