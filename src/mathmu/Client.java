
package mathmu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mathmu.data.Task;
import mathmu.interf.ClientCallback;
import mathmu.util.*;

/**
 *
 * @author XiaoR
 */
public class Client {
    private static Logger logger = Logger.getLogger(Client.class.getName());
    private BufferedReader in = null;
    private PrintWriter out = null;
    private Socket socket = null;
    private List<ClientCallback> callbacks;
    private boolean isTasking;
    private Runner runner = null;
    private Long taskID = new Long(-1);	// if calc node return sth before asking him, then it will be treated as the -1 task
    private Long ownerID= new Long(-1);
    private String name;
    private InetAddress addr;
    private int port;
    private Client self=this;
    
    class Runner extends Thread{
        @Override
        public void run(){
            try {
            	ZLog.info("@Client.Runner:: trying to connect to " + addr.getHostAddress() + ":" + port);
                socket = new Socket(addr, port);                
            }catch(IOException e){
            	ZLog.error("@Client.Runner:: cannot connect to " + addr.getHostAddress() + ":" + port);
            	suicide();
            	return;
            }            
            if(ownerID!=-1){
        		Task t=new Task("client "+addr.toString()+":"+port+" connected",ownerID);
        		for (ClientCallback ntf : callbacks) ntf.taskFinish(t);
        		ownerID=new Long(-1);
        	}
            try{
                ZLog.info("Client connected @" + addr.getHostAddress() + ":" + port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                while (true){
                    String recv = in.readLine();
                    ZLog.info("@Client.Runner.run:: msg got from calc node"+ZLog.LINE+recv+ZLog.LINE);
                    if (recv == null){
                    	suicide();
                    	break;
                    }
                    if(ownerID==-1)continue;
                    Task task = new Task(recv, ownerID);
                    task.setId(taskID);
                    if(callbacks==null)ZLog.error("@Client.Runner.run::callbacks null");
                    else for (ClientCallback ntf : callbacks) ntf.taskFinish(task);
                    isTasking = false;
                    ownerID=new Long(-1);
                    taskID=new Long(-1);
                }
            } catch (IOException ex) {

            }
        }
    }

    public String getName(){
        return this.name+":"+port;
    }
    
    public String getIP(){
    	return addr.getHostAddress();
    }
    
    public int getPort(){
    	return this.port;
    }

    public Client(String ip, int port, String name) throws UnknownHostException {
        this.name = name;                
        callbacks = new ArrayList();
        isTasking = false;
        this.port = port;
        addr = InetAddress.getByName(ip);
    }
    
    public Client(String ip, int port, String name, Long taskOwner) throws UnknownHostException {
        this.name = name;                
        callbacks = new ArrayList();
        isTasking = false;
        this.port = port;
        this.ownerID=taskOwner;
        addr = InetAddress.getByName(ip);
    }
    
    public void startClient(){
    	runner = new Runner();
        runner.start();
    }
    
    public boolean disconnect(){
    	if(runner!=null && runner.isAlive()){
    		runner.interrupt();
    		runner=null;
    	}
    	if(socket!=null){
    		try{
        		socket.close();
        		ZLog.info("@Client.suicide:: client "+addr.toString()+":"+port+" closed");
        	}catch(Exception e){
        		ZLog.error("@Client.suicide::What? Even suicide is throwing an exception!!!");
        		return false;
        	}
    	}
    	return true;
    }
    
    public void suicide(){
    	disconnect();
    	if(ownerID!=-1){
    		Task t=new Task("client "+addr.toString()+":"+port+" closed",ownerID);
    		for (ClientCallback ntf : callbacks) ntf.taskFinish(t);
    	}
    	for (ClientCallback ntf : callbacks) ntf.removeNode(this);
    }

    public void addNewTaskFinishCallback(ClientCallback ntf){
        callbacks.add(ntf);
    }

    public boolean removeNewTaskFinishCallback(ClientCallback ntf){
        return callbacks.remove(ntf);
    }
    
    public void endConnect(){
        if (socket != null && socket.isConnected()) try {
            socket.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        socket = null;
    }

    public boolean isFree(){
        return !isTasking;
    }
    
    public boolean isConnected(){
        return socket != null && socket.isConnected();
    }
    
    public boolean sendNewTask(Task task){
    	ZLog.info(""+isConnected()+" "+(out==null));
        if (isConnected() && out != null) {
            out.println(task.getExp());
            out.flush();
            taskID = task.getId();
            ownerID = task.getOwner();
            isTasking = true;
            ZLog.info("@Client.sendNewTask:: "+socket.getInetAddress()+":"+socket.getPort()+" get new task, owner: "+ownerID+" ,exp: "+task.getExp());
            return true;
        }
        return false;
    }

    public boolean equals(Object o){
    	Client c = (Client) o;
    	if (c.getIP().equals(this.getIP()) && c.getPort()==this.getPort()) return true;
    	return false;
    }
    
    public int hashCode(){
    	return 0;
    }
}
