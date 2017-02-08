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

class Gate{
    String name;
    float delay;

    int output;
    LinkedList <Wire> wires;

    public Gate(Scanner sc){
        this.name = sc.next();
        this.delay = Float.parseFloat(sc.next());


    }


    public String toString(){
        return "Name of gate: " + name + "Delay is: " + delay;
    }


}

class Simulator{

    static LinkedList <Gate> gates = new LinkedList <Gate> ();

    public static void initializeGate( Scanner sc ){

        while(sc.hasNextLine()){
            String command  = sc.next();

            if(command.equals("gate")){

                gates.add( new Gate(sc) );
                //System.out.println( new Gate(sc) );

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