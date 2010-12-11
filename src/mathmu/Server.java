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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mathmu.data.Task;
import mathmu.interf.*;
import mathmu.util.ZLog;

public class Server implements MsgHandle{
    private static Logger logger =  Logger.getLogger(Server.class.getName());
    private ServerSocket ss = null;
    private int listenPort = -1;
    private List<ServerCallback> callbacks;
    private WelcomeService runner;
    private boolean isConnected;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private Server self=this;
    
    class WatchDog extends Thread{
    	private BufferedReader in;
    	private MsgHandle mh;
    	private Socket skt;
    	public WatchDog(BufferedReader vin,MsgHandle vmh,Socket socket){
    		in=vin;
    		mh=vmh;
    		skt=socket;
    	}
    	@Override
    	public void run(){
    		String s=null;
    		if(in!=null)
	    		while(true){
	    			if(!socket.isConnected()){
	    				closeConnection();
	    				break;
	    			}
	    			try{
	    				s=in.readLine();
	    				if(s==null){
	    					closeConnection();
	    					break;
	    				}
	    			}catch(Exception e){
	    				s=null;
	    				closeConnection();	    				
	    				break;
	    			}
	    			mh.handleMsg(s);
	    		}
    	}
    	private void closeConnection(){
    		ZLog.info("");
    		try{
				skt.close();
				ZLog.info("@Server.WatchDog.closeConnection:: client "+skt.getInetAddress()+":"+skt.getPort()+" disconnected.");
			}catch(Exception ee){
				ZLog.error("@Server.WatchDog.closeConnection:: nani? cannot close socket!");
			}
    	}
    }
    
    class WelcomeService extends Thread{	// accept socket connect and add a dog to handle
    	List<WatchDog> dogs=new ArrayList();
        @Override
        public void run(){
            while (true){
                try {
                     socket = ss.accept();
                     logger.info("Server accept");
                     in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    continue;
                }
                WatchDog d=new WatchDog(in,self,socket);
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
        this.isConnected = false;
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

    public boolean sendResponse(String ret){
        if (this.isConnected && out != null){
            out.println(ret);
            out.flush();
            return true;
        }
        return false;
    }
    
    public void handleMsg(String s){
    	if(s==null||s=="")return;
    	logger.info("handleMsg: "+s);
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
            Task task = new Task(s);
            for (ServerCallback nta : callbacks) nta.arrive(task);
        }
    }
}
