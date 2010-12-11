package mathmu.interf;

import mathmu.Client;
import mathmu.data.*;

public interface ClientCallback{
	void taskFinish(Task t);
	void arrive(Task t);
	void removeNode(Client c);
}
