package mathmu;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import mathmu.conf.Config;
import mathmu.data.Task;
import mathmu.interf.*;

public class Core implements ServerCallback, ClientCallback{
    private static Logger logger = Logger.getLogger(Core.class.getName());
    private BlockingQueue<Task> waitList, doingList;
    private List<Client> clientList;
    private Server server;
    
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
        for (Client c : clientList) if (c.isFree()){
            Task t = waitList.peek();
            if (c.sendNewTask(t)) {
                logger.info("Start task @" + c.getName());
                doingList.add(waitList.poll());
                return;
            }            
        }
    }

    public void arrive(Task task) {
        logger.info("Task arrive : " + task.getId() + " s:" + task.getExp());
        waitList.offer(task);
        arrange();
    }

    public void addNode(String ip, int port, String name){
        Client c;
        try {
            c = new Client(ip, port, name);
            this.clientList.add(c);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void taskFinish(Task task) {
        logger.info("Task finish : " + task.getId() + " s:" + task.getExp());
        if (server.sendResponse(task.getExp())){
            for (Task t : doingList) if (t.getId() == task.getId()) {
                doingList.remove(t);
                return;
            }
        }
    }

    public void start(){
        startClients();
        startServer();        
    }

    public void end(){
        endServer();
        endClients();
    }

}
