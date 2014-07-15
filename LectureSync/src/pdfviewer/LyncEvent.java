package pdfviewer;


public class LyncEvent {
	public static final int PAGE_CHANGE = 0;
	public static final int ZOOM_CHANGED = 1;
	public static final int END_SERVICE = 2;
	public static final int PAGE_POSITION_CHANGE=3;
	public static final int CLIENT_CONNECTED=4;
	public static final int CLIENT_DISCONNECTED=5;
	
	private String extra;
	
	public LyncEvent(int type, String extraInfo){
		this.extra = extraInfo;
	}
	
	public String getExtraInfo(){
		return extra;
	}
}
