package mathmu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import mathmu.conf.Config;

/**
 *
 * @author XiaoR
 */
public class Main {

    public static void main(String[] args) {
    	if(args.length>0){
    		parseOption(args);
    	}
        Core core = new Core();
        core.start();
    }
    
    public static void parseOption(String args[]){
	    for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-h")) {
				System.out.print(usage());
			} else if (args[i].equals("-p")) {
				Config.ServerListenPort=Integer.parseInt(args[++i]);
			}
		}
    }

    private static String usage() {
		return ("\n"
				+ "Usage:  java -jar [-h] [-p portNumber]"
				+ "\n");
	}

    
    public static void parseConfig(){
    	
    }

}
