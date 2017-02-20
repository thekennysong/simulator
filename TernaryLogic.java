/** TernaryLogic.java
 * @author Douglas Jones (for Errors class and some handling)
 * @author Kenny Song
 * @version MP2
 */
import java.util.LinkedList;
import java.util.ArrayList;

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


class Wire{

    String source;
    String destination;
    float delay;

    public Wire(String sour, String dest, Scanner sc){

        source = sour;
        destination = dest;


        if (!sc.hasNextFloat() && sc.nextFloat() <= 0 ) {
            Errors.fatal(
                "travel time invalid. Must be a float greater than 0."
            );
        }
        delay = sc.nextFloat();
    }

    public String toString(){
        return source + " " + destination + " " + delay;
    }


}

class MinGate extends Gate {
    public MinGate( String n, Scanner sc ) {
        super( n, sc, "min" );
    }

}
class MaxGate extends Gate {
    public MaxGate( String n, Scanner sc ) {
        super( n, sc, "max" );
    }

}
class NegGate extends Gate {
    public NegGate( String n, Scanner sc ) {
        super( n, sc, "neg" );
    }
}
class TrueGate extends Gate {
    public TrueGate( String n, Scanner sc ) {
        super( n, sc, "istrue" );
    }
}
class FalseGate extends Gate {
    public FalseGate( String n, Scanner sc ) {
        super( n, sc, "isfalse" );
    }
}
class UnknownGate extends Gate {
    public UnknownGate( String n, Scanner sc ) {
        super( n, sc, "isunknown" );

    }
}

class Gate{
    String name;
    float delay;
    String type;
    int totalInputs = 0;
    int totalInputsPermitted = 0;

    int output;
    LinkedList <Wire> wires;


    public Gate(String name, Scanner sc, String type){
        this.name = name;
        this.type = type;
        if( !sc.hasNextInt() ){
            Errors.fatal(name + "permitted inputs not entered correctly");
        }
        totalInputsPermitted  = sc.nextInt();
        if( totalInputsPermitted <= 0 ){
            Errors.fatal("permitted inputs must be greater than 0");
        }



        if(!sc.hasNextFloat()){
            Errors.fatal(name + " delay not entered correctly");
        }
        delay = sc.nextFloat();

        if(delay <= 0){
            Errors.fatal("permitted inputs must be greater than 0");
        }






    }

    /**
     *  toString method is for part b answer!
     * @return part b answer here
     */
    public String toString(){
        return name + " " + type + " " + totalInputsPermitted + " " + delay;
    }


}


/** Main class, where we initialize the gates read from the text file */
public class TernaryLogic{

    static LinkedList <Gate> gates = new LinkedList <Gate> ();
    static LinkedList <Wire> wires = new LinkedList <Wire> ();
    static ArrayList<String> gateNames = new ArrayList<String>();
    //String[] gateTypes = {"min", "max", "neg", "istrue", "isfalse", "isunknown"};

    /**
     * Part a answer, rest of answer for part a can be seen in the Gate class.
     * @param sc
     */
    public static void initializeGatesAndWires( Scanner sc ){

        while(sc.hasNextLine()){
            String command  = sc.next();

            for(Gate gate: gates){
                gateNames.add(gate.name);
            }

            if(command.equals("gate")){
                String name = sc.next();

                if(gateNames.contains(name)){
                   Errors.fatal("gate name " + name + " already exists");
                }

                String type = sc.next();
                if( name.length() > 0 || ( isNumeric(name) && isEven(name) ) ){
                     switch (type) {
                        case "min":  gates.add( new MinGate( name, sc) ); break;
                        case "max":  gates.add( new MaxGate( name, sc) ); break;
                        case "neg":  gates.add( new NegGate( name, sc) ); break;
                        case "istrue":  gates.add( new TrueGate( name, sc) ); break;
                        case "isfalse":  gates.add( new FalseGate( name, sc) ); break;
                        case "isunknown":  gates.add( new UnknownGate( name, sc) ); break;
                        default:  Errors.fatal("gate type not entered correctly"); break;
                     }

                } else{
                     Errors.fatal(
                        "gate name is not entered correctly"
                    );
                }
               // gates.add( new Gate(sc) );
                //System.out.println( new Gate(sc) );

            } else if(command.equals("wire")){

                String source = sc.next();

                if(source == null){
                    Errors.fatal("need wire source");
                }


                String destination = sc.next();

                if(destination == null){
                    Errors.fatal("need wire destination");
                }


                //for (Integer vertex : entry.getValue()) {


                if(gateNames.contains(source) && gateNames.contains(destination) && !source.equals(destination)){
                    wires.add( new Wire( source, destination, sc) );
                } else{
                     Errors.fatal(
                        "wire " + source + " " + destination +  "destination/source not entered correctly"
                    );
                }

            }
        }

      // for(Wire wire: wires){
      //       if(name.equals(wire.destination)){
      //           totalInputs++;
      //       }
      //   }

      //   if(totalInputs == 0){
      //       Errors.fatal("Gate has no inputs");
      //   }

      //   if(totalInputs > totalInputsPermitted){
      //       Errors.fatal("Total inputs to gate " + name + " exceeds it total input permitted");
      //   }



    }

    private static void printGatesAndWires(){
        for (Gate g: gates) {
            System.out.println( g.toString() );
        }
        for (Wire w: wires) {
            System.out.println( w.toString() );
        }
    }

    public static boolean isNumeric(String s) {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }

    public static boolean isEven(String s){
        int number = Integer.parseInt(s);
        if ( (number & 1) == 0 ) {return true; } else { return false; }
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
            initializeGatesAndWires( sc );
            printGatesAndWires();
        } catch(FileNotFoundException e){
            Errors.fatal( "Could not read '" + args[0] + "'");
        }
    }
}