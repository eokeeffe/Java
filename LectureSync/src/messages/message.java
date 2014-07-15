package messages;

public enum message {
	message;
	//propagation messages
	public static String SRCIDTAG = "SRCID";
	public static String SRCNAMETAG = "SRCNAME";
	public static String PONGTAG = "PONG";
	
	public static String DFILE = "DOWNLOAD_FILE";
	public static String CLENGHT = "CONTENT_LENGHT";
	public static String FNAME = "FILE_NAME";
	
	public static String PAGEEVENT = "PAGE_EVENT";
	public static String PAGEKEY = "PAGE_KEY";
	
	public static String SERVICE_INFO = "SERVICE_INFO";
	public static String SERVICE_NAME_AD = "SERVICE_NAME";
	
	
	//socket messages
	public final static int SERVICE_NAME = 1;
	public final static int SERVER_INFO = 2;
	public final static int DOWNLOAD_FILE = 3;
	public final static int PAGE_EVENT = 4;
}
