/** Simulator.java
 * @author Douglas Jones
 * @author Kenny Song
 * @version 2017-02-07
 */
import java.util.LinkedList;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;


class Errors{
    private Errors(){};

    public static void warn(String message){
        System.out.println( "Warning: " + message );
    }

    public static void fatal(String message){
        System.out.println( "Fatal error: " + message );
        System.exit(-1);
    }
}

class Wire{}

class Gate(Scanner sc){
    String name;
    float delay;

    int output;
    LinkedList <Wire> wires;

}
class Simulator{



    public static void initializeGate( Scanner sc ){

        while(sc.hasNext()){
            String command  = sc.next();

            if(command.equals("gate")){
                //comma

            }
        }

    }

    public static void main(String[] args) {
        if( args.length < 1){
            Errors.fatal( "Missing file name on command line" );
        } else if(args.length > 1){
            Errors.fatal( "Unexpected command line args" );
        } else try{
            Scanner sc = new Scanner( new File( args[0] ) );
            initializeGate( sc );
        } catch(FileNotFoundException e){
            Errors.fatal( "Could not read '" + args[0] + "'");
        }
    }
}