
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
    private Long taskID;
    private String name;
    private InetAddress addr;
    private int port;
    
    class Runner extends Thread{
        @Override
        public void run(){
            while (true){
                try {
                    logger.info("Start trying to connect to " + addr.getHostAddress() + ":" + port);
                    socket = new Socket(addr, port);                    
                    logger.info("Client connected @" + addr.getHostAddress() + ":" + port);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    while (true){
                        String recv = in.readLine();
                        if (recv == null) break;
                        Task task = new Task(recv);
                        task.setId(taskID);
                        isTasking = false;
                        for (ClientCallback ntf : callbacks) ntf.taskFinish(task);
                    }
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);                    
                }
            }
        }
    }

    public String getName(){
        return this.name;
    }

    public Client(String ip, int port, String name) throws UnknownHostException {
        this.name = name;                
        callbacks = new ArrayList();
        isTasking = false;
        this.port = port;
        addr = InetAddress.getByName(ip);
        runner = new Runner();
        runner.start();        
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
        if (isConnected() && out != null) {            
            out.println(task.getExp());
            out.flush();
            taskID = task.getId();
            isTasking = true;
            return true;
        }
        return false;
    }


}
