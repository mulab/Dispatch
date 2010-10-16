package mathmu.interf;

import mathmu.data.*;

public interface ServerCallback{
	void addNode(String ip,int port,String name);
	void arrive(Task t);
}
