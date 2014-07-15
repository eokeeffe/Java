package pdfviewer;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.icepdf.core.pobjects.Page;

public class FullScreenViewerBasic{

	public static GraphicsDevice gd;
	protected JFrame frame;
	protected LyncDoc doc;
	protected float zoom = 1f;
	protected float zoomFit;
	protected Integer currentPage;
	protected Integer yLocation;
	protected boolean visualStateChanged;
	protected LyncEvent currentEvent;
	protected JScrollPane scrollPane;
	protected JPanel container;
	protected int mode;
	
	public static final int MODE_LECTURER = 0;
	public static final int MODE_STUDENT=1;
	
	public int getMode()
	{
		return this.mode;
	}
	
	public int getPage()
	{
		return currentPage;
	}
	
	synchronized public void setPage(int Page)
	{
		currentPage = Page;
		yLocation=0;
		displayDoc();
	}
	
	public FullScreenViewerBasic(String path, int mode){
		
		doc = new LyncDoc(path);
		currentPage = 0;
		yLocation=0;
		visualStateChanged=false;
		currentEvent=null;
		this.mode = mode;
		
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        
		frame = new JFrame();	
	
		container = new JPanel();
		//frame.setUndecorated(true);
		frame.setBackground(Color.black);
		container.setBackground(Color.black);
        frame.setIgnoreRepaint(true);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setPreferredSize(new Dimension(device.getDisplayMode().getWidth(), device.getDisplayMode().getHeight()));
        
     
        
        float pageSize = doc.getDocument().getPageDimension(1,0).getHeight();
        float screenHeight= device.getDisplayMode().getHeight();
        
        container.setIgnoreRepaint(true);
  //      container.setPreferredSize(new Dimension(device.getDisplayMode().getWidth(), device.getDisplayMode().getHeight()));
        frame.add(container);
        zoomFit = zoom = screenHeight/pageSize;
        
        device.setFullScreenWindow(frame);
        
        frame.repaint();
        initializeInput();
        displayDoc();
	}

	public JFrame getDisplay()
	{
		return frame;
	}
	
	protected void displayDoc(){
		visualStateChanged=false;
		try{
	    doc.getDocument().paintPage(currentPage, container.getGraphics(), Page.BOUNDARY_MEDIABOX, 0, 0.0f, zoomFit);
		}catch(Exception e)
		{
			System.err.println("Caught Error: "+e);
		}
	}
	
	private void initializeInput(){
			frame.addKeyListener(new KeyListener() {
			
				@Override
				public void keyTyped(KeyEvent arg0) {}
			
				@Override
				public void keyReleased(KeyEvent arg0) {}
			
				@Override
				public void keyPressed(KeyEvent arg0) {
					int keyCode = arg0.getKeyCode();
				
					if(mode == FullScreenViewerBasic.MODE_LECTURER){
						if(keyCode==37){
							if(currentPage>-1){
								currentPage--;
								yLocation=0;
								visualStateChanged=true;
							currentEvent = new LyncEvent(LyncEvent.PAGE_CHANGE, currentPage.toString());
							}
						}
						else if(keyCode==39){
							if(currentPage < doc.getDocument().getNumberOfPages()-1){
								currentPage++;
								yLocation=0;
								visualStateChanged=true;
								currentEvent = new LyncEvent(LyncEvent.PAGE_CHANGE, currentPage.toString());
							}
						}
						}
					
						if(visualStateChanged){displayDoc();}
					}
				});
	}
}