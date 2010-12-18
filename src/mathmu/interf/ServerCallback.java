package mathmu.interf;

import mathmu.data.*;

public interface ServerCallback{
	boolean addNode(String ip,int port,String name);
	void arrive(Task t);
}
