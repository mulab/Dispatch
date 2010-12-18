package mathmu.conf;

import java.util.*;

public class Const{
	// return message
	public static final String NotSupportedCommand="not supported command.";
	public static final String NoCalcNodeAvailable="no calc node available.";
	
	// node operation
	public static final String NODE_ADD="add";
	public static final String NODE_DEL="del";
	public static final String NODE_RM="rm";
	public static final String NODE_STOP="stop";
	public static final String NODE_PAUSE="pause";
	public static final String NODE_RESUME="resume";	
	
	public static Set<String> NODE_ADD_SET= new HashSet<String>();
	public static Set<String> NODE_RM_SET = new HashSet<String>();
	public static Set<String> NODE_STOP_SET = new HashSet<String>();
	public static Set<String> NODE_RESUME_SET = new HashSet<String>();
	
	static{
		// add
		NODE_ADD_SET.add(NODE_ADD);
		// remove
		NODE_RM_SET.add(NODE_DEL);
		NODE_RM_SET.add(NODE_RM);
		// stop
		NODE_STOP_SET.add(NODE_PAUSE);
		NODE_STOP_SET.add(NODE_STOP);
		// resume
		NODE_RESUME_SET.add(NODE_RESUME);
	}
}