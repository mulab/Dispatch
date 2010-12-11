package mathmu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author XiaoR
 */
public class Main {

    public static void main(String[] args) {
    	if(args.length>0) System.out.println(args[0]);
        Core core = new Core();
        core.start();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            in.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void parseConfig(){
    	
    }

}
