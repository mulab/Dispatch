package mathmu.interf;

import mathmu.data.*;

public interface ClientCallback{
	void taskFinish(Task t);
	void arrive(Task t);
}
