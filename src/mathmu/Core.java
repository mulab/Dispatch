package mathmu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Timer;

import mathmu.conf.Config;
import mathmu.conf.Const;
import mathmu.data.Task;
import mathmu.interf.*;
import mathmu.util.ZLog;

public class Core implements ServerCallback, ClientCallback, ActionListener{
    private static Logger logger = Logger.getLogger(Core.class.getName());
    private BlockingQueue<Task> waitList, doingList;
    private List<Client> clientList;
    private Server server;
    private Timer timer;
    private HashMap<Long,Client> contextMap=new HashMap();
    
    public Core(){
        waitList = new ArrayBlockingQueue(Config.TASKQUEUE_CAPACITY);
        doingList = new ArrayBlockingQueue(Config.TASKQUEUE_CAPACITY);
        clientList = new ArrayList();
    }
    
    public void endServer(){
        if (server != null) server.endListen();
        server = null;
    }

    public boolean startServer(){
        endServer();
        try {
            server = new Server(Config.ServerListenPort);
            server.addNewTaskArriveCallback(this);
            server.startListen();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public void endClients(){
        for (Client c : clientList) c.endConnect();
        clientList.clear();
    }
    
    public int startClients(){
        int ret = 0;
        for (String ip : Config.ClientIPList){
            try {
                Client c = new Client(ip, Config.ClientSendPort, ip);
                c.startClient();
                c.addNewTaskFinishCallback(this);
                //if (c.isConnected()) {
                    ret ++;
                    this.clientList.add(c);
                //} else {
                    //TODO
                //}
            } catch (UnknownHostException ex) {
                logger.log(Level.SEVERE, null, ex);
                continue;
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
                continue;
            }
        }
        return ret;
    }

    public synchronized void arrange(){
        if (waitList.isEmpty()) return;        
        Task t=waitList.peek();
        String s=t.getExp();
        
    	if (s.toLowerCase().contains("node")){//control command
            try{
            	String[]ary=s.split("[ ]");
            	if(ary.length<3){
            		logger.info("invalid command: "+s);
            		waitList.poll();
            		return;
            	}
            	String op = ary[1];
            	String ip = ary[2];
            	int port=Config.ClientSendPort;
            	if(ary.length>3){
            		port = Integer.parseInt(ary[3]);
            	}
                String name = ""+ip;
                if (ary.length>4) name = ary[4];
                
            	if(Const.NODE_ADD_SET.contains(op.toLowerCase())){
            		addNodeWithOwner(ip, port, name, t.getOwner());
            	}else if(Const.NODE_RM_SET.contains(op.toLowerCase())){
            		Client toRm = new Client(ip,port,ip);
            		String resp;
            		int toRmIdx=clientList.indexOf(toRm);	
            		if(toRmIdx >= 0){
            			clientList.get(toRmIdx).disconnect();
            			clientList.remove(toRmIdx);
            			resp="node "+ip+" removed";
            		}
            		else
            			resp="node "+ip+" not removed";
            		taskFinish(new Task(resp,t.getOwner()));
            	}else{
            		taskFinish(new Task(Const.NotSupportedCommand,t.getOwner()));
            	}
            }catch(Exception e){
                logger.log(Level.SEVERE, null, e);
            }
            doingList.add(waitList.poll());
            return;
        }
        
    	// if no calc node available
    	if(clientList.isEmpty()){
    		taskFinish(new Task(Const.NoCalcNodeAvailable,t.getOwner()));
    		waitList.poll();
    		return;
    	}
        
        Client old=contextMap.get(t.getOwner());
        if(old==null || !clientList.contains(old)){	// no privious route or the old is dead
        	for (Client c : clientList) if (c.isFree()){
                if (c.sendNewTask(t)) {
                	contextMap.put(t.getOwner(), c);
                    logger.info("Start task @" + c.getName());
                    doingList.add(waitList.poll());
                }
                break;
            }
        }else if(old.isFree()==false){	// there exists previous route, but busy
        	waitList.add(waitList.poll());
        }else{			// there exists previous route, and free!
    		old.sendNewTask(t);
    		doingList.add(waitList.poll());
        }
    }

    public synchronized void arrive(Task task) {
//        logger.info("Task arrive : " + task.getId() + " s:" + task.getExp());
        waitList.offer(task);
//        arrange();
    }

    public boolean addNode(String ip, int port, String name){
        Client c;
        try {
            c = new Client(ip, port, name);
            c.startClient();
            c.addNewTaskFinishCallback(this);
            this.clientList.add(c);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    public boolean addNodeWithOwner(String ip, int port, String name, Long owner){
        Client c;
        try {
            c = new Client(ip, port, name,owner);
            c.startClient();
            c.addNewTaskFinishCallback(this);
            this.clientList.add(c);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    public void removeNode(Client c){
    	if(clientList.contains(c)){
	    	this.clientList.remove(c);
	    	ZLog.info("@Core.removeNode:: calc Node "+c.getName()+" removed");
    	}
    }

    public synchronized void taskFinish(Task task) {
        logger.info("@Core.taskFinish:: Task finish : " + task.getId() + " s:" + task.getExp());
        if(server==null){
        	ZLog.error("@Core.taskFinish:: seems you are too early, our server is not there");
        	return;
        }
        if (server.sendResponse(task)){
            for (Task t : doingList) if (t.getId() == task.getId()) {
                doingList.remove(t);
                return;
            }
        }else{
        	ZLog.error("@Core.taskFinish:: send Response failed!");
        }
    }

    public void start(){
        startClients();
        startServer();
        timer=new Timer(Config.CORE_INTERVAL,this);
        timer.start();
    }

    public void end(){
        endServer();
        endClients();
    }
    
    public void actionPerformed(ActionEvent e){
    	this.arrange();
    }
}
