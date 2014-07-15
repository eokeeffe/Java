/*
 * Copyright (c) 2006-2007 Sun Microsystems, Inc.  All rights reserved.
 *  
 *  The Sun Project JXTA(TM) Software License
 *  
 *  Redistribution and use in source and binary forms, with or without 
 *  modification, are permitted provided that the following conditions are met:
 *  
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  
 *  2. Redistributions in binary form must reproduce the above copyright notice, 
 *     this list of conditions and the following disclaimer in the documentation 
 *     and/or other materials provided with the distribution.
 *  
 *  3. The end-user documentation included with the redistribution, if any, must 
 *     include the following acknowledgment: "This product includes software 
 *     developed by Sun Microsystems, Inc. for JXTA(TM) technology." 
 *     Alternately, this acknowledgment may appear in the software itself, if 
 *     and wherever such third-party acknowledgments normally appear.
 *  
 *  4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must 
 *     not be used to endorse or promote products derived from this software 
 *     without prior written permission. For written permission, please contact 
 *     Project JXTA at http://www.jxta.org.
 *  
 *  5. Products derived from this software may not be called "JXTA", nor may 
 *     "JXTA" appear in their name, without prior written permission of Sun.
 *  
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SUN 
 *  MICROSYSTEMS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 *  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  JXTA is a registered trademark of Sun Microsystems, Inc. in the United 
 *  States and other countries.
 *  
 *  Please see the license information page at :
 *  <http://www.jxta.org/project/www/license.html> for instructions on use of 
 *  the license in source files.
 *  
 *  ====================================================================
 *  
 *  This software consists of voluntary contributions made by many individuals 
 *  on behalf of Project JXTA. For more information on Project JXTA, please see 
 *  http://www.jxta.org.
 *  
 *  This license is based on the BSD license adopted by the Apache Foundation. 
 */

package netConfigurator;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import messages.message;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.XMLDocument;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.endpoint.TextDocumentMessageElement;
import net.jxta.endpoint.router.RouteController;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.protocol.RouteAdvertisement;

/**
 * Simple example to illustrate the use of propagated pipes
 */
public class ViewerServer extends Thread implements PipeMsgListener {
	public InputPipe inputPipe;
	public MessageElement routeAdvElement = null;
	public RouteController routeControl = null;
	public static final String ROUTEADV = "ROUTE";
	private boolean receivedPong = false;
	private String serviceName = null;
	private String user;
	private boolean pingIt = true;
	private boolean isRunning = true;
	private int key = -1;

	public boolean serverConnected() {
		return receivedPong;
	}

	public void setPing() {
		pingIt = true;
	}

	synchronized public boolean hasService() {
		if (serviceName != null) {
			return true;
		}
		return false;
	}

	public void setUserName(String user) {
		this.user = user;
	}

	synchronized public String getServiceName() {
		return serviceName;
	}

	public PipeAdvertisement getPipeAdvertisement() {
		PipeID pipeID = null;

		try {
			pipeID = (PipeID) IDFactory.fromURI(new URI(
					PresenterServer.PIPEIDSTR));
		}
		catch (URISyntaxException use) {
			use.printStackTrace();
		}
		PipeAdvertisement advertisement = (PipeAdvertisement) AdvertisementFactory
				.newAdvertisement(PipeAdvertisement.getAdvertisementType());

		advertisement.setPipeID(pipeID);
		advertisement.setType(PipeService.PropagateType);
		advertisement.setName("Socket");
		return advertisement;
	}

	public void run() {
		NetworkManager manager = null;
		try {
			manager = new NetworkManager(NetworkManager.ConfigMode.EDGE,
					"PropagatedPipeClient", new File(new File(".cache"),
							"PropagatedPipeClient").toURI());
			manager.startNetwork();
		}
		catch (Exception e) {
			System.err.println(e);
		}
		PeerGroup netPeerGroup = manager.getNetPeerGroup();
		PipeAdvertisement pipeAdv = this.getPipeAdvertisement();
		PipeService pipeService = netPeerGroup.getPipeService();

		System.err.println("Creating Propagated InputPipe for "
				+ pipeAdv.getPipeID());
		try {
			this.inputPipe = pipeService.createInputPipe(pipeAdv, this);
		}
		catch (IOException e) {
			System.err.println(e);
		}

		this.routeControl = netPeerGroup.getEndpointService()
				.getEndpointRouter().getRouteController();
		RouteAdvertisement route = this.routeControl.getLocalPeerRoute();

		if (route != null) {
			this.routeAdvElement = new TextDocumentMessageElement(
					ViewerServer.ROUTEADV,
					(XMLDocument) route.getDocument(MimeMediaType.XMLUTF8),
					null);
		}

		System.out.println("Creating Propagated OutputPipe for "
				+ pipeAdv.getPipeID());
		OutputPipe output = null;

		try {
			output = pipeService.createOutputPipe(pipeAdv, 1);
		}
		catch (IOException e) {
			System.err.println(e);
		}
		while (isRunning) {
			try {
				if (pingIt) {
					pingIt = false;
					Message ping = new Message();
					ping.addMessageElement(PresenterServer.NAMESPACE,
							new StringMessageElement(message.SRCIDTAG,
									netPeerGroup.getPeerID().toString(), null));
					ping.addMessageElement(PresenterServer.NAMESPACE,
							new StringMessageElement(message.SRCNAMETAG, user,
									null));
					if (this.routeAdvElement != null
							&& this.routeControl != null) {
						ping.addMessageElement(PresenterServer.NAMESPACE,
								this.routeAdvElement);
					}

					System.err.println("Sending message :" + ping);
					boolean sucess = output.send(ping);
					System.err.println("Send ping message status :" + sucess);
				}
				Thread.sleep(15000);

			}
			catch (Exception e) {
				System.err.println(e);
			}
		}
		manager.stopNetwork();
		System.out.println("Received Necessary Information");
	}
	
	/**
	 * End the Viewer Server Cycle
	 */
	public void end()
	{
		isRunning = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pipeMsgEvent(PipeMsgEvent event) {

		Message msg = event.getMessage();

		if (msg == null) {
			return;
		}

		MessageElement sel = msg.getMessageElement(PresenterServer.NAMESPACE,
				message.PONGTAG);
		MessageElement nel = msg.getMessageElement(PresenterServer.NAMESPACE,
				message.SRCNAMETAG);

		MessageElement serverName = msg.getMessageElement(
				PresenterServer.NAMESPACE, message.SERVICE_INFO);
		MessageElement serviceName = msg.getMessageElement(
				PresenterServer.NAMESPACE, message.SERVICE_NAME_AD);

		MessageElement dfile = msg.getMessageElement(PresenterServer.NAMESPACE,
				message.DFILE);
		MessageElement file = msg.getMessageElement(PresenterServer.NAMESPACE,
				message.FNAME);
		MessageElement length = msg.getMessageElement(
				PresenterServer.NAMESPACE, message.CLENGHT);
		
		MessageElement pg = msg.getMessageElement(PresenterServer.NAMESPACE,
				message.PAGEEVENT);
		MessageElement pkey = msg.getMessageElement(PresenterServer.NAMESPACE,
				message.PAGEKEY);

		// Since propagation relies on ip multicast whenever possible, it is to
		// to be expected that a unicasted message can be intercepted through ip
		// multicast

		if (sel != null) {
			System.out.println("Received a pong from :" + nel.toString() + " "
					+ sel.toString());
		}
		if (dfile != null) {
			System.out.println(dfile.toString());
			System.out.println("File name:" + file.toString() + " Size:"
					+ length.toString());
		}
		if (serverName != null) {
			this.serviceName = serviceName.toString();
			System.out.println("Service Information:" + serverName + " @"
					+ serviceName);
		}
		if(pg != null)
		{
			key = Integer.parseInt(pkey.toString());
			System.out.println("PageEvent:" + serverName + " Key:"
					+ key);
		}
		receivedPong = true;
	}
	
	synchronized public int getKey()
	{
		int temp = key;
		key = -1;
		return temp;
	}

	/**
	 * main
	 * 
	 * @param args
	 *            command line args
	 */
	@SuppressWarnings("rawtypes")
	public static void main(String args[]) {
		ViewerServer client = new ViewerServer();
		NetworkManager manager = null;

		Logger jxtaLogger = Logger.getLogger("net.jxta");
		jxtaLogger.setLevel(Level.SEVERE);

		try {
			manager = new NetworkManager(NetworkManager.ConfigMode.EDGE,
					"PropagatedPipeClient", new File(new File(".cache"),
							"PropagatedPipeClient").toURI());
			manager.startNetwork();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		PeerGroup netPeerGroup = manager.getNetPeerGroup();
		PipeAdvertisement pipeAdv = client.getPipeAdvertisement();
		PipeService pipeService = netPeerGroup.getPipeService();

		System.out.println("Creating Propagated InputPipe for "
				+ pipeAdv.getPipeID());
		try {
			client.inputPipe = pipeService.createInputPipe(pipeAdv, client);
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		client.routeControl = netPeerGroup.getEndpointService()
				.getEndpointRouter().getRouteController();
		RouteAdvertisement route = client.routeControl.getLocalPeerRoute();

		if (route != null) {
			client.routeAdvElement = new TextDocumentMessageElement(ROUTEADV,
					(XMLDocument) route.getDocument(MimeMediaType.XMLUTF8),
					null);
		}

		System.out.println("Creating Propagated OutputPipe for "
				+ pipeAdv.getPipeID());
		OutputPipe output = null;

		try {
			output = pipeService.createOutputPipe(pipeAdv, 1);
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		boolean isRunning = true;
		while (isRunning) {
			try {
				if (client.pingIt) {
					client.pingIt = false;
					Message ping = new Message();
					ping.addMessageElement(PresenterServer.NAMESPACE,
							new StringMessageElement(message.SRCIDTAG,
									netPeerGroup.getPeerID().toString(), null));
					ping.addMessageElement(PresenterServer.NAMESPACE,
							new StringMessageElement(message.SRCNAMETAG,
									" evan", null));
					if (client.routeAdvElement != null
							&& client.routeControl != null) {
						ping.addMessageElement(PresenterServer.NAMESPACE,
								client.routeAdvElement);
					}

					System.out.println("Sending message :" + ping);
					boolean sucess = output.send(ping);
					System.out.println("Send ping message status :" + sucess);
				}
				Thread.sleep(8000);

			}
			catch (Exception e) {
				System.err.println("Ping error in ViewServer: " + e);
			}
		}
		manager.stopNetwork();
		System.out.println("Tests Done");
	}
}
