/** TernaryLogic.java
 * @author Douglas Jones (for Errors class and some handling)
 * @author Kenny Song
 * @version MP3
 */
import java.util.LinkedList;
import java.util.ArrayList;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Pattern;
import java.util.NoSuchElementException;

/** This class is for handling errors. Credit to Douglas Jones */
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

/** Support methods for scanning
 *  @see Errors
 *  @author Douglas Jones
 */
class ScanSupport {

    /** Pattern for identifers
     */
    public static final Pattern name
        = Pattern.compile( "[a-zA-Z0-9_]*" );

    /** Pattern for whitespace excluding things like newline
     */
    public static final Pattern whitespace
        = Pattern.compile( "[ \t]*" );

    /** Get next name without skipping to next line (unlike sc.Next())
     *  @param sc the scanner from which end of line is scanned
     *  @return the name, if there was one, or an empty string
     */
    public static String nextName( Scanner sc ) {
        sc.skip( whitespace );

        // the following is weird code, it skips the name
        // and then returns the string that matched what was skipped
        sc.skip( name );
        return sc.match().group();
    }

    /** Advance to next line and complain if is junk at the line end
     *  @see Errors
     *  @param message gives a prefix to give context to error messages
     *  @param sc the scanner from which end of line is scanned
     */
    public static void lineEnd( Scanner sc, String message ) {
        sc.skip( whitespace );
        if(sc.hasNextLine()){
            String lineEnd = sc.nextLine();
            //System.out.println(lineEnd);
            if ( !lineEnd.equals( "" )) {
                Errors.warn(
                    message +
                    " followed unexpected by '" + lineEnd + "'"
                );
            }
        } else{
            try{
                String lineEnd = sc.nextLine();
                if ( !lineEnd.equals( "" )) {
                    Errors.warn(
                        message +
                        " followed unexpected by '" + lineEnd + "'"
                    );
                }

            } catch(NoSuchElementException e){
                //do nothing
            }
        }
    }
}


class Wire{

    String source;
    String destination;
    float delay;

    public Wire(String source, String destination, Scanner sc){

        this.source = source;
        this.destination = destination;

        if(!sc.hasNextFloat()){
            Errors.fatal("wire " + this.source + " " + this.destination + " delay not entered correctly");
        }
        delay = sc.nextFloat();

        if(delay <= 0){
            Errors.warn("wire " + this.source + " " + this.destination + " delay must be greater than 0");
        }

        ScanSupport.lineEnd( sc,
            "Wire '" + this.source +
            "' '" + this.destination + "'"
        );
    }

    public String toString(){
        return "wire " + source + " " + destination + " " + delay;
    }


}

class MinGate extends Gate {
    public MinGate( String name, Scanner sc, Float delay, int totalInputsPermitted ) {
        super( name, "min", delay, totalInputsPermitted );

        ScanSupport.lineEnd( sc,
            "MinGate '" + name + "'"
        );

    }

    /** output this Intersection in a format like that used for input
     */
    public String toString() {
        return(
            super.toString() +
            totalInputsPermitted + " " +
            delay
        );
    }

}
class MaxGate extends Gate {



    public MaxGate( String name, Scanner sc, Float delay, int totalInputsPermitted ) {
        super( name, "max", delay, totalInputsPermitted );

        ScanSupport.lineEnd( sc,
            "MaxGate '" + name + "'"
        );


    }

    /** output this Intersection in a format like that used for input
     */
    public String toString() {
        return(
            super.toString() +
            totalInputsPermitted + " " +
            delay
        );
    }



}
class NegGate extends Gate {
    public NegGate( String name, Scanner sc, Float delay ) {
        super( name, "neg", delay, 1 );

        ScanSupport.lineEnd( sc,
            "NegGate '" + name + "'"
        );

    }

    /** output this Intersection in a format like that used for input
     */
    public String toString() {
        return(
            super.toString() +
            delay
        );
    }


}
class TrueGate extends Gate {
    public TrueGate( String name, Scanner sc, Float delay ) {
        super( name, "istrue", delay, 1 );

        ScanSupport.lineEnd( sc,
            "TrueGate '" + name + "'"
        );

    }

    /** output this Intersection in a format like that used for input
     */
    public String toString() {
        return(
            super.toString() +
            delay
        );
    }


}
class FalseGate extends Gate {
    public FalseGate( String name, Scanner sc, Float delay ) {
        super( name, "isfalse", delay, 1 );

        ScanSupport.lineEnd( sc,
            "FalseGate '" + name + "'"
        );

    }

    /** output this Intersection in a format like that used for input
     */
    public String toString() {
        return(
            super.toString() +
            delay
        );
    }


}
class UnknownGate extends Gate {
    public UnknownGate( String name, Scanner sc, Float delay ) {
        super( name, "isunknown", delay, 1 );

        ScanSupport.lineEnd( sc,
            "UnknownGate '" + name + "'"
        );


    }
    /** output this Intersection in a format like that used for input
     */
    public String toString() {
        return(
            super.toString() +
            delay
        );
    }

}

abstract class Gate{
    public final String name;
    float delay;
    public final String type;
    int totalInputs = 0;
    int totalInputsPermitted = 0;


    int output;
    LinkedList <Wire> wires;

    protected Gate( String name, String type, Float delay, int totalInputsPermitted ) {
        this.name = name;
        this.type = type;
        this.delay = delay;
        this.totalInputsPermitted = totalInputsPermitted;

    }

    public static Gate newGate(String name, Scanner sc, String type){
        //String gateName = name;
        int totalInputsPermitted = 0;
        float delay;

        if("min".equals(type) || "max".equals(type) ){
            if( !sc.hasNextInt() ){
                Errors.warn(name + " permitted inputs not entered correctly");
                return null;
            } else{

                totalInputsPermitted  = sc.nextInt();
                if( totalInputsPermitted <= 0 ){
                    Errors.warn(name + " permitted inputs must be greater than 0");
                    return null;
                }

            }


        }

        if(!sc.hasNextFloat()){
            Errors.fatal(name + " delay not entered correctly");
            return null;
        } else{

            delay = sc.nextFloat();
            if(delay <= 0){
                Errors.warn(name + " delay must be greater than 0");
                return null;
            }

        }


        switch (type) {
            case "min":
                return new MinGate( name, sc, delay, totalInputsPermitted );
            case "max":
                return new MaxGate( name, sc, delay, totalInputsPermitted );
            case "neg":
                return new NegGate( name, sc, delay );
            case "istrue":
                return new TrueGate( name, sc, delay );
            case "isfalse":
                return new FalseGate( name, sc, delay );
            case "isunknown":
                return new UnknownGate( name, sc, delay );
            default:
                Errors.fatal("entered type: " + type + " is not a gate type/entered correctly");
                return null;
        }

    }

    /**
     *  toString method is for part b answer!
     * @return part b answer here
     */
    public String toString(){
        return "gate " + name + " " + type + " ";
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

            if("gate".equals(command)){
                String name = sc.next();

                if(gateNames.contains(name)){
                   Errors.fatal("Gate: " + name + " already exists");
                }

                String type = sc.next();
                if( name.length() > 0 && isNumericLetters(name) ){
                    Gate gate = Gate.newGate(name, sc, type );
                    if (gate != null) {
                        gates.add( gate );
                    }
                } else{
                     Errors.fatal(
                        "Gate: " + name + " is not entered correctly. Only letters and numbers."
                    );
                }


            } else if("wire".equals(command)){

                String source = sc.next();
                String destination = sc.next();


                if(gateNames.contains(source) && gateNames.contains(destination) && !source.equals(destination)){
                    wires.add( new Wire( source, destination, sc) );
                } else{
                    if(!gateNames.contains(source) || !gateNames.contains(destination)){
                        Errors.fatal(
                            "wire " + source + " " + destination +  " destination and/or source not entered correctly"
                        );
                    } else if(source.equals(destination)){
                        Errors.fatal(
                            "wire " + source + " " + destination +  " cannot have same source and destination"
                        );
                    }
                }

            } else{
                Errors.fatal(command + " found where 'gate' or 'wire' was suppose to be");
            }
        }

    /**
     * Please ignore this comment block.
     */
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

    public static boolean isNumericLetters(String s) {
        return s.matches("[a-zA-Z0-9]*");
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