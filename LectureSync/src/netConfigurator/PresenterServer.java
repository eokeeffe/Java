/*
 * Copyright (c) 2001-2007 Sun Microsystems, Inc.  All rights reserved.
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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import messages.message;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLDocument;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.endpoint.TextDocumentMessageElement;
import net.jxta.endpoint.router.RouteController;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
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

public class PresenterServer extends Thread implements PipeMsgListener {

	/**
	 * Tutorial message name space
	 */
	public final static String NAMESPACE = "PROPTUT";
	public PeerGroup netPeerGroup = null;

	/**
	 * Common propagated pipe id
	 */
	public final static String PIPEIDSTR = "urn:jxta:uuid-59616261646162614E504720503250336FA944D18E8A4131AA74CE6F4BF85DEF04";
	private final static String completeLock = "completeLock";
	private static PipeAdvertisement pipeAdv = null;
	private static PipeService pipeService = null;
	public InputPipe inputPipe = null;
	private transient Map<PeerID, OutputPipe> pipeCache = new Hashtable<PeerID, OutputPipe>();
	public static final String ROUTEADV = "ROUTE";
	public RouteController routeControl = null;
	public MessageElement routeAdvElement;
	public String dir,filename,serviceName;
	
	public PresenterServer() {
		routeAdvElement = null;
	}

	public void setFile(String dir,String filename)
	{
		this.dir = dir;
		this.filename = filename;
	}
	
	public void setServiceName(String serviceName)
	{
		this.serviceName = serviceName;
	}
	
	/**
	 * Gets the pipeAdvertisement attribute of the PropagatedPipeServer class
	 * 
	 * @return The pipeAdvertisement value
	 */
	public PipeAdvertisement getPipeAdvertisement() {
		PipeID pipeID = null;

		try {
			pipeID = (PipeID) IDFactory.fromURI(new URI(PIPEIDSTR));
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pipeMsgEvent(PipeMsgEvent event) {

		Message msg = event.getMessage();

		if (msg == null) {
			return;
		}
		MessageElement sel = msg.getMessageElement(NAMESPACE, message.SRCIDTAG);
		MessageElement nel = msg.getMessageElement(NAMESPACE,
				message.SRCNAMETAG);

		// check for a route advertisement and train the endpoint router with
		// the new route
		processRoute(msg);
		if (sel == null) {
			return;
		}
		System.out.println("Received a Ping from :" + nel.toString());
		System.out.println("Source PeerID :" + sel.toString());
		Message pong = new Message();
		Message sync = new Message();

		pong.addMessageElement(NAMESPACE, new StringMessageElement(
				message.PONGTAG, nel.toString(), null));
		pong.addMessageElement(NAMESPACE, new StringMessageElement(
				message.SRCNAMETAG, netPeerGroup.getPeerName(), null));

		OutputPipe outputPipe = null;
		PeerID pid = null;

		try {
			pid = (PeerID) IDFactory.fromURI(new URI(sel.toString()));
			if (pid != null) {
				// Unicast the Message back. One should expect this to be
				// unicast
				// in Rendezvous only propagation mode.
				// create a op pipe to the destination peer
				if (!pipeCache.containsKey(pid)) {
					// Unicast datagram
					// create a op pipe to the destination peer
					outputPipe = pipeService.createOutputPipe(pipeAdv,
							Collections.singleton(pid), 1);
					pipeCache.put(pid, outputPipe);
				}
				else {
					outputPipe = pipeCache.get(pid);
				}
				// boolean sucess = outputPipe.send(pong);
				boolean sucess = true;

				//String filename = "C:\\Users\\evan\\workspace\\FileStructure\\earth.jpg";
				File file = new File(dir);
				FileInputStream fstream = new FileInputStream(file);

				byte[] buffer = new byte[1024];
				int blocks = 0;
				while ((fstream.read(buffer)) != -1) {
					blocks++;
				}
				fstream.close();
				System.err.println("#Packets:" + blocks);

				sync.addMessageElement(NAMESPACE, new StringMessageElement(
						message.DFILE, "sync devices", null));
				sync.addMessageElement(NAMESPACE, new StringMessageElement(
						message.FNAME, filename, null));
				sync.addMessageElement(NAMESPACE, new StringMessageElement(
						message.CLENGHT, String.valueOf(blocks), null));
				sync.addMessageElement(NAMESPACE, new StringMessageElement(
						message.SERVICE_INFO, "Presenter", null));
				sync.addMessageElement(NAMESPACE, new StringMessageElement(
						message.SERVICE_NAME_AD, serviceName, null));

				System.out.println(sync);

				while (!outputPipe.send(sync)) {
				}

				System.out.println("File sync message sent");
				System.out.println("Send pong message status :" + sucess);
			}
			else {
				// send it to all
				System.out.println("unable to create a peerID from :"
						+ sel.toString());
				outputPipe = pipeService.createOutputPipe(pipeAdv, 1000);
				boolean sucess = outputPipe.send(pong);
				System.out.println("Send pong message status :" + sucess);

			}
		}
		catch (IOException ex) {
			if (pid != null && outputPipe != null) {
				outputPipe.close();
				outputPipe = null;
				pipeCache.remove(pid);
			}
			ex.printStackTrace();
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public void broadCastPageChange(int Page)
	{
		OutputPipe outputPipe;
		Message pageEvent = new Message();
		pageEvent.addMessageElement(NAMESPACE, new StringMessageElement(
				message.PAGEEVENT, "broadcasting change", null));
		pageEvent.addMessageElement(NAMESPACE, new StringMessageElement(
				message.PAGEKEY, String.valueOf(Page), null));
		
		for(Entry<PeerID, OutputPipe> pip : pipeCache.entrySet())
		{
			outputPipe = pipeCache.get(pip.getKey());
			try {
				outputPipe.send(pageEvent);
			}
			catch (IOException e) {
			}
		}
	}

	/**
	 * Keep running, avoids existing
	 */
	public void waitForever() {
		try {
			System.out.println("Waiting for Messages.");
			synchronized (completeLock) {
				completeLock.wait();
			}
			System.out.println("Done.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processRoute(final Message msg) {
		try {
			final MessageElement routeElement = msg.getMessageElement(
					NAMESPACE, ROUTEADV);
			if (routeElement != null && routeControl != null) {
				XMLDocument<?> asDoc = (XMLDocument<?>) StructuredDocumentFactory
						.newStructuredDocument(routeElement.getMimeType(),
								routeElement.getStream());
				final RouteAdvertisement route = (RouteAdvertisement) AdvertisementFactory
						.newAdvertisement(asDoc);
				routeControl.addRoute(route);
			}
		}
		catch (IOException io) {
			io.printStackTrace();
		}
	}

	public void run()
	{
		PipeAdvertisement pipeAdv = this.getPipeAdvertisement();
		NetworkManager manager = null;
		
		try {
			manager = new NetworkManager(NetworkManager.ConfigMode.EDGE,
					"PropagatedPipeServer", new File(new File(".cache"),
							"PropagatedPipeServer").toURI());
			manager.startNetwork();
		}
		catch (Exception e) {
			System.err.println(e);
		}
		this.netPeerGroup = manager.getNetPeerGroup();
		PipeService pipeService = this.netPeerGroup.getPipeService();

		this.routeControl = this.netPeerGroup
				.getEndpointService().getEndpointRouter()
				.getRouteController();
		RouteAdvertisement route = (RouteAdvertisement) this.routeControl
				.getLocalPeerRoute();

		if (route != null) {
			this.routeAdvElement = new TextDocumentMessageElement(
					PresenterServer.ROUTEADV,
					(XMLDocument<?>) route
							.getDocument(MimeMediaType.XMLUTF8), null);
		}

		System.out.println("Creating Propagated InputPipe for "
				+ pipeAdv.getPipeID());
		try {
			this.inputPipe = pipeService.createInputPipe(pipeAdv,
					this);
		}
		catch (IOException e) {
			System.err.println(e);
		}
		this.waitForever();
		this.inputPipe.close();
		manager.stopNetwork();
	}
	
	/**
	 * main
	 * 
	 * @param args
	 *            command line args
	 */
	public static void main(String args[]) {

		Logger jxtaLogger = Logger.getLogger("net.jxta");
		jxtaLogger.setLevel(Level.SEVERE);

		PresenterServer server = new PresenterServer();
		server.setServiceName("SocketServer");
		server.setFile("C:\\Users\\evan\\Documents\\a\\a.pdf", "a.pdf");
		
		pipeAdv = server.getPipeAdvertisement();
		NetworkManager manager = null;

		try {
			manager = new NetworkManager(NetworkManager.ConfigMode.EDGE,
					"PropagatedPipeServer", new File(new File(".cache"),
							"PropagatedPipeServer").toURI());
			manager.startNetwork();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		server.netPeerGroup = manager.getNetPeerGroup();
		pipeService = server.netPeerGroup.getPipeService();

		server.routeControl = server.netPeerGroup.getEndpointService()
				.getEndpointRouter().getRouteController();
		RouteAdvertisement route = (RouteAdvertisement) server.routeControl
				.getLocalPeerRoute();

		if (route != null) {
			server.routeAdvElement = new TextDocumentMessageElement(ROUTEADV,
					(XMLDocument<?>) route.getDocument(MimeMediaType.XMLUTF8),
					null);
		}

		System.out.println("Creating Propagated InputPipe for "
				+ pipeAdv.getPipeID());
		try {
			server.inputPipe = pipeService.createInputPipe(pipeAdv, server);
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		server.waitForever();
		server.inputPipe.close();
		manager.stopNetwork();
	}
}