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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Core core = new Core();
        core.start();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            in.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } finally{            
            core.end();
        }
    }

}
