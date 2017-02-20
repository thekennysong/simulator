/** TernaryLogic.java
 * @author Douglas Jones
 * @author Kenny Song
 * @version 2017-02-07
 */
import java.util.LinkedList;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

/** This class is for handling errors */
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

/** Will eventually fill this out with code */
class Wire{}

/** Contains code needs for 2 a and b
*More specifically, the setting of name and delay in 41 and 42 is for part a
*Lines 49-51 is for part b
*/
class Gate{
    String name;
    float delay;
    String type;
    int totalInputs;

    int output;
    LinkedList <Wire> wires;


    public Gate(Scanner sc){
        this.name = sc.next();
        this.type = sc.next();
        this.totalInputs = Integer.parseInteger(sc.next());
        this.delay = Float.parseFloat(sc.next());


    }

    /**
     *  toString method is for part b answer!
     * @return part b answer here
     */
    public String toString(){
        return "Name of gate: " + name + " Delay is: " + delay;
    }


}

/** Main class, where we initialize the gates read from the text file */
class TernaryLogic{

    //static LinkedList <Gate> gates = new LinkedList <Gate> ();

    /**
     * Part a answer, rest of answer for part a can be seen in the Gate class.
     * @param sc
     */
    public static void initializeGate( Scanner sc ){

        while(sc.hasNextLine()){
            String command  = sc.next();

            if(command.equals("gate")){

                //gates.add( new Gate(sc) );
                //System.out.println( new Gate(sc) );

            }
        }

        //System.out.println(gates.toString());




    }

    public static void main(String[] args) {
        if( args.length < 1){
            Errors.fatal( "Missing file name on command line" );
        } else if(args.length > 1){
            Errors.fatal( "Unexpected command line args" );
        } else try{
            /**
             * This is where the program takes in the input file, the initalizer is called here (part a)
             * @param  args[0]
             */
            Scanner sc = new Scanner( new File( args[0] ) );
            initializeGate( sc );
        } catch(FileNotFoundException e){
            Errors.fatal( "Could not read '" + args[0] + "'");
        }
    }
}