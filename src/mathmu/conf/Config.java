package mathmu.conf;

import java.util.ArrayList;
import java.util.List;

public class Config {
	// these are default values
    public static int ServerListenPort = 5010;
    public static int ClientSendPort = 5009;
    public static int CORE_INTERVAL = 10;
    public static List<String> ClientIPList = new ArrayList();
    static{    	
//        ClientIPList.add("10.0.10.3");
//        ClientIPList.add("10.0.10.4");
//        ClientIPList.add("10.0.10.5");
    	ClientIPList.add("127.0.0.1");
    }


    public static int TASKQUEUE_CAPACITY = 10000;
}

// addNode 127.0.0.1 5009 node1
