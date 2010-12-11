package mathmu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mathmu.data.Task;
import mathmu.interf.*;
import mathmu.util.ZLog;

public class Server{
    private static Logger logger =  Logger.getLogger(Server.class.getName());
    private ServerSocket ss = null;
    private int listenPort = -1;
    private List<ServerCallback> callbacks;
    private WelcomeService runner;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private HashMap<Long,PrintWriter> map=new HashMap();
    private Long srcCnter=new Long(0);
    
    class WatchDog extends Thread{
    	private Long src;
    	private BufferedReader win;
    	private PrintWriter wout;
    	private Socket wskt;
    	public WatchDog(Long srcId, BufferedReader vin,PrintWriter vout,Socket vsocket){
    		src=srcId;
    		win=vin;
    		wout=vout;
    		wskt=vsocket;
    	}
    	@Override
    	public void run(){
    		String s=null;
    		if(win!=null)
	    		while(true){
	    			if(!wskt.isConnected()){
	    				closeConnection();
	    				break;
	    			}
	    			try{
	    				s=win.readLine();
	    				if(s==null){
	    					closeConnection();
	    					break;
	    				}
	    			}catch(Exception e){
	    				s=null;
	    				closeConnection();	    				
	    				break;
	    			}
	    			handleMsg(s);
	    		}
    	}
    	private void closeConnection(){
    		try{
				wskt.close();
				ZLog.info("@Server.WatchDog.closeConnection:: client "+wskt.getInetAddress()+":"+wskt.getPort()+" disconnected.");
			}catch(Exception ee){
				ZLog.error("@Server.WatchDog.closeConnection:: nani? cannot close socket!");
			}
    	}
    	
    	private void handleMsg(String s){
        	if(s==null||s=="")return;
        	ZLog.info("\n\n@Server.WatchDog.handleMsg: "+s);
        	if (s.contains("addNode")){//control command
                try{
                	String[]ary=s.split("[ ]");
                	if(ary.length<3){
                		logger.info("invalid command: "+s);
                		return;
                	}
                    String ip = ary[1];
                    int port = Integer.parseInt(ary[2]);
                    String name = ""+ip;
                    if (ary.length > 3) name = ary[3];
                    for (ServerCallback nta : callbacks) nta.addNode(ip, port, name);
                }catch(Exception e){
                    logger.log(Level.SEVERE, null, e);
                }
                return;
            }else{					//task dispatch
                Task task = new Task(s,src);
                map.put(src, wout);
                for (ServerCallback nta : callbacks) nta.arrive(task);
            }
        }
    }
    
    class WelcomeService extends Thread{	// accept socket connect and add a dog to handle
    	List<WatchDog> dogs=new ArrayList();
        @Override
        public void run(){
        	Long srcid;
            while (true){
                try {
                     socket = ss.accept();
                     logger.info("Server accept");
                     in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                     srcid=new Long(srcCnter);
                     srcCnter++;
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    continue;
                }
                WatchDog d=new WatchDog(srcid, in,out,socket);
                dogs.add(d);
                d.start();
            }
        }
        
        @Override
        public void interrupt(){
        	super.interrupt();
        	for(WatchDog d:dogs){
        		d.interrupt();
        	}
        	dogs.clear();
        }
    }
    public Server(int port) throws IOException{
        this.listenPort = port;
        callbacks = new ArrayList();
        ss = new ServerSocket(port);
        ZLog.info("Server create @ " + port);
    }

    public void addNewTaskArriveCallback(ServerCallback nta){
        this.callbacks.add(nta);
    }

    public boolean removeNewTaskArriveCallback(ServerCallback nta){
        return this.callbacks.remove(nta);
    }

    public boolean startListen(){
        endListen();
        runner = new WelcomeService();
        runner.start();
        return true;
    }

    public void endListen() {
        if (runner != null && runner.isAlive()) runner.interrupt();
        runner = null;
        if (out != null) out.close();
        out = null;
        if (in != null) try {
            in.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        in = null;
        if (socket != null) try {
            socket.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        socket = null;
    }

    public boolean maintainListen(){
        if (runner == null || !runner.isAlive()) return startListen();
        return true;
    }

    public boolean sendResponse(Task t){
    	PrintWriter pw=map.get(t.getOwner());
        if (pw != null){
            pw.println(t.getExp());
            pw.flush();
            return true;
        }
        return false;
    }
    
    
}
