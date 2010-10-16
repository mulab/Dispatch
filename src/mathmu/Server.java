
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
import mathmu.interf.ServerCallback;

/**
 *
 * @author XiaoR
 */
public class Server {
    private static Logger logger =  Logger.getLogger(Server.class.getName());
    private ServerSocket ss = null;
    private int listenPort = -1;
    private List<ServerCallback> callbacks;
    private Runner runner;
    private boolean isConnected;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    class Runner extends Thread{
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
                }
                isConnected = true;
                String recv;
                try {
                    recv = in.readLine();
                    if (recv == null) {
                        isConnected = false;
                        logger.info("Server Connect failed!");
                        continue;
                    }
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    continue;
                }
                if (recv.contains("addNode")){
                    try{
                        String ip = recv.split("[ ]")[1];
                        int port = Integer.parseInt(recv.split("[ ]")[2]);
                        String name = "";
                        if (recv.split("[ ]").length > 3){
                            name = recv.split("[ ]")[3];
                        }
                        for (ServerCallback nta : callbacks) nta.addNode(ip, port, name);
                    }catch(Exception e){
                        logger.log(Level.SEVERE, null, e);
                    }
                }else{
                    Task task = new Task(recv);
                    for (ServerCallback nta : callbacks) nta.arrive(task);
                }
                //for ()
            }
        }
    }
    public Server(int port) throws IOException{
        this.listenPort = port;
        this.isConnected = false;
        callbacks = new ArrayList();
        ss = new ServerSocket(port);
        logger.info("Server create @ " + port);
    }

    public void addNewTaskArriveCallback(ServerCallback nta){
        this.callbacks.add(nta);
    }

    public boolean removeNewTaskArriveCallback(ServerCallback nta){
        return this.callbacks.remove(nta);
    }

    public boolean startListen(){
        endListen();        
        runner = new Runner();
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
}
