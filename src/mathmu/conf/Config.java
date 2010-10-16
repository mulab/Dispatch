package mathmu.conf;

import java.util.ArrayList;
import java.util.List;

public class Config {
	// these are default values
    public static int ServerListenPort = 5009;
    public static int ClientSendPort = 5009;
    public static List<String> ClientIPList = new ArrayList();
//    static{
//        ClientIPList.add("10.0.10.3");
//        ClientIPList.add("10.0.10.4");
//        ClientIPList.add("10.0.10.5");
//    }


    public static int TASKQUEUE_CAPACITY = 10000;
}
