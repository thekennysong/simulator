/** TernaryLogic.java
 * @author Douglas Jones (for Errors class and some handling and the lambda expression set up and also the Simulation class)
 * @author Kenny Song
 * @version MP4
 */
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Pattern;
import java.util.NoSuchElementException;

/** This class is for handling errors. Credit to Douglas Jones */
class Errors{
    private Errors(){};

    private static int count = 0; // warning count, really public read only
    /** Provide public read only access to the count of warnings. */
    public static int count() {
        return count;
    }
    public static void warn(String message){
        System.out.println( "Warning: " + message );
        count = count + 1;
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

/** Simulation support framework
 */
class Simulation {

        public interface Action {
                // actions contain the specific code of each event
                void trigger( float time );
        }

        private static class Event {
                public float time; // the time of this event
                public Action act; // what to do at that time
        }

        private static PriorityQueue <Event> eventSet
        = new PriorityQueue <Event> (
                (Event e1, Event e2) -> Float.compare( e1.time, e2.time )
        );

        /** Call schedule to make act happen at time.
         *  Users typically pass the action as a lambda expression:
         *  <PRE>
         *  Simulator.schedule(t,(float time)->method(params,time))
         *  </PRE>
         */
        static void schedule( float time, Action act ) {
               // System.out.println("schedule called " + time);

                Event e = new Event();
                e.time = time;
                e.act = act;
                eventSet.add( e );
        }

        /** Call run() after scheduling some initial events
         *  to run the simulation.
         */
        static void run() {
            while (!eventSet.isEmpty()) {
                    //System.out.println(eventSet.size() + " size");
                    //TernaryLogic.printGatesOutputs();
                    Event e = eventSet.remove();
                    //System.out.println(e.act);
                    e.act.trigger( e.time );
            }
        }
}

class Wire{

    Gate source;
    Gate destination;
    float delay;
    int value = 1; //this will hold the value of the wire*important*


    public Wire(String source, String destination, Scanner sc){
        String srcName = source;
        String dstName = destination;
        //find the source based on name
        this.source = TernaryLogic.findGate( srcName );
        if (source == null) {
            Errors.warn(
                "Gate '" + srcName +
                "' '" + dstName +
                "' source undefined."
            );
        }
        //find the destination based on name
        this.destination = TernaryLogic.findGate( dstName );
        if (destination == null) {
            Errors.warn(
                "Road '" + srcName +
                "' '" + dstName +
                "' destination undefined."
            );
        }



        if(!sc.hasNextFloat()){
            Errors.fatal("wire " + srcName + " " + dstName + " delay not entered correctly");
        }
        delay = sc.nextFloat();

        if(delay <= 0){
            Errors.warn("wire " + srcName + " " + dstName + " delay must be greater than 0");
        }

        ScanSupport.lineEnd( sc,
            "Wire '" + srcName +
            "' '" + dstName + "'"
        );

        // let the source and destination know about this wire
        if (this.destination != null){
            this.destination.addIncoming( this );
        }
        if (this.source != null) {
            this.source.addOutgoing( this );
        }

    }
    // ***** Logic Simulation *****

    /** Event service routine called when the input to a wire changes
     *  @param time the time at which the input changes
     *  @param old the previous logic value carried over this wire
     *  @param new the new logic value carried over this wire
     */
    public void inputChangeEvent( float time, int oldv, int newv ) {
        Simulation.schedule(
            time + delay,
            (float t) -> outputChangeEvent( t, oldv, newv )
        );
    };

    /** Event service routine called when the output of a wire changes
     *  @param time the time at which the output changes
     *  @param old the previous logic value carried over this wire
     *  @param new the new logic value carried over this wire
     */
    public void outputChangeEvent( float time, int oldv, int newv ) {
        // this version is optimized, we could have scheduled an event
        destination.inputChangeEvent( time, oldv, newv );
    };
    //print values of the wire
    public String toString(){
        return "wire " + source + " " + destination + " " + delay + " " + value;
    }

    /** check this road to see if it meets global sanity constraints
     */
    public void check() {
        // nothing to check.
    }
}

class MinGate extends Gate {
    public MinGate( String name, Scanner sc, Float delay, int totalInputsPermitted ) {
        super( name, "min", delay, totalInputsPermitted );

        ScanSupport.lineEnd( sc,
            "MinGate '" + name + "'"
        );



    }

    // ***** Logic Simulation for MinGate *****

    /** Every subclass of Gate must define this function;
     *  @return the new logic value, a function of <TT>inputCounts</TT>;
     */
    protected int logicValue() {
        // find the minimum of all the inputs
        int newOutput = 0;
        while (inputCounts[newOutput] == 0) newOutput++;
        return newOutput;
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


       // output = 0;

    }

    // ***** Logic Simulation for MaxGate *****

    /** Every subclass of Gate must define this function;
     *  @return the new logic value, a function of <TT>inputCounts</TT>;
     */
    protected int logicValue() {
        // find the maximum of all the inputs
        int newOutput = 2;
        while (inputCounts[newOutput] == 0) newOutput--;
        return newOutput;
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

        //output = 0;

    }


    // ***** Logic Simulation for NegGate *****

    /** Every subclass of Gate must define this function;
     *  @return the new logic value, a function of <TT>inputCounts</TT>;
     */
    protected int logicValue() {
        // Warning this is mildly tricky code
        int newOutput = 2;
        while (inputCounts[2 - newOutput] == 0) newOutput--;
        return newOutput;
    }
    //public void outputChange( float time, int o, int n ) {}
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

        //inputs = 1; // it is a one-input gate
       // totalInputsPermitted = 1;

        ScanSupport.lineEnd( sc,
            "TrueGate '" + name + "'"
        );

        //output = 0;
    }

    // ***** Logic Simulation for IsTGate *****

    /** Sanity check for IsTGate */
    public void check() {
        super.check();

        // now change the output from unknown to false
        Simulation.schedule(
            delay,
            (float t) -> this.outputChangeEvent( t, 1, 0 )
        );
        output = 0;
    }

    /** Every subclass of Gate must define this function;
     *  @return the new logic value, a function of <TT>inputCounts</TT>;
     */
    protected int logicValue() {
        int newOutput = 0;
        if (inputCounts[2] != 0) newOutput = 2;
        return newOutput;
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

        //totalInputsPermitted = 1;

        ScanSupport.lineEnd( sc,
            "FalseGate '" + name + "'"
        );

        //output = 0;
    }

    // ***** Logic Simulation for IsFGate *****

    /** Sanity check for IsFGate */
    public void check() {
        super.check();

        // now change the output from unknown to false
        Simulation.schedule(
            delay,
            (float t) -> this.outputChangeEvent( t, 1, 0 )
        );
        output = 0;
    }

    /** Every subclass of Gate must define this function;
     *  @return the new logic value, a function of <TT>inputCounts</TT>;
     */
    protected int logicValue() {
        int newOutput = 0;
        if (inputCounts[0] != 0) newOutput = 2;
        return newOutput;
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

        //totalInputsPermitted = 1;

        ScanSupport.lineEnd( sc,
            "UnknownGate '" + name + "'"
        );


    }

    // ***** Logic Simulation for IsUGate *****

    /** Sanity check for IsUGate */
    public void check() {
        super.check();

        // now change the output from unknown to true
        Simulation.schedule(
            delay,
            (float t) -> this.outputChangeEvent( t, 1, 2 )
        );
        output = 2;
    }

    /** Every subclass of Gate must define this function;
     *  @return the new logic value, a function of <TT>inputCounts</TT>;
     */
    protected int logicValue() {
        int newOutput = 0;
        if (inputCounts[1] != 0) newOutput = 2;
        return newOutput;
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

    //int totalInputs = 0;
    int totalInputsPermitted = 0;

    public int[] inputCounts = new int[3];

    public int output;
    LinkedList <Wire> incoming = new LinkedList <Wire> ();
    LinkedList <Wire> outgoing = new LinkedList <Wire> ();

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
    public void addIncoming( Wire w ) {
        incoming.add( w );
    }
    public void addOutgoing( Wire w ) {
        outgoing.add( w );
    }
    public void check() {
        if (incoming.size() < totalInputsPermitted ) {
            Errors.warn(
                this.toString() +
                ": too many incoming wires"
            );
        } else if (incoming.size() > totalInputsPermitted) {
            Errors.warn(
                this.toString() + " -- has too many inputs."
            );
        }

        // initially, all the inputs are unknown
        inputCounts[0] = 0;
        inputCounts[1] = totalInputsPermitted;
        inputCounts[2] = 0;

        // and initially, the output is unknown
        output = 1;

    }
    /** Every subclass must define this function;
     *  @return the new logic value, a function of <TT>inputCounts</TT>;
     */
    protected abstract int logicValue();

    /** Event service routine called when the input to a gate changes
     *  @param time the time at which the input changes
     *  @param old the previous logic value carried over this input
     *  @param new the new logic value carried over this input
     */
    public void inputChangeEvent( float time, int oldv, int newv ) {
        inputCounts[oldv]--;
        inputCounts[newv]++;
        final int newOut = logicValue();
        if (output != newOut) {
            final int old = output;
            Simulation.schedule(
                time + delay,
                (float t) -> outputChangeEvent( t, old, newOut )
            );
            output = newOut;
        }
    };
    /** Event service routine called when the output of a gate changes
     *  @param time the time at which the output changes
     *  @param old the previous logic value of this gate's output
     *  @param new the new logic value of this gate's output
     */
    public void outputChangeEvent( float time, int oldv, int newv ) {

        // send the new value out to all the outgoing wires
        for ( Wire w: outgoing ) {
            // this is optimized, we could have scheduled an event
            w.inputChangeEvent( time, oldv, newv );
        }
    };
    /**
     *  toString method is for part b answer!
     * @return part b answer here
     */
    public String toString(){
        return "gate " + name + " " + type + " ";
    }
    int lastPrinted = 1;

    public String printValue(){

        //System.out.println(lastPrinted + " " + output);
        String[][] values = {
            {"|    ", "|_   ",  "|___ "},
            {" _|  ", "  |  ", "  |_ "},
            {" ___|",  "   _|", "    |"}
        };

        String returnValue = values[lastPrinted][output];
        lastPrinted = output;

        return returnValue;

    }

}


/** Main class, where we initialize the gates read from the text file */
public class TernaryLogic{

    static LinkedList <Gate> gates = new LinkedList <Gate> ();
    static LinkedList <Wire> wires = new LinkedList <Wire> ();
    static ArrayList<String> gateNames = new ArrayList<String>();
    //String[] gateTypes = {"min", "max", "neg", "istrue", "isfalse", "isunknown"};
    public static Gate findGate( String s ) {
        for ( Gate g: gates ) {
            if (g.name.equals( s )) return g;
        }
        return null;
    }
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


                if(gateNames.contains(source) && gateNames.contains(destination)){
                    wires.add( new Wire( source, destination, sc) );
                } else{
                    if(!gateNames.contains(source) || !gateNames.contains(destination)){
                        Errors.fatal(
                            "wire " + source + " " + destination +  " destination and/or source not entered correctly"
                        );
                    }
                    //apparently can have same source and destination according to mp4 example
                    // else if(source.equals(destination)){
                    //     Errors.fatal(
                    //         "wire " + source + " " + destination +  " cannot have same source and destination"
                    //     );
                    // }
                }

            } else{
                //System.out.println(command);
                Errors.fatal(command + " found where 'gate' or 'wire' was suppose to be");
            }
        }

    }

    private static float printInterval;

    public static void initPrint( float i ) {
        printInterval = i;
        Simulation.schedule(
            0.0f,
            (float t) -> printGates( t )
        );

        for( Gate g: gates ) {
            String updatedName = g.name;
            if( updatedName.length() < 5){
                while(updatedName.length() < 5){
                    updatedName = updatedName + " ";
                }
            } else if (updatedName.length() > 5){
                updatedName = updatedName.substring(0, 5);
            }

            System.out.print( " " + updatedName );

        }
        System.out.println();
    }

    private static void printGates( float time ) {
        for( Gate g: gates ) {
            System.out.print( " " + g.printValue() );
        }
        System.out.println();
        //System.out.print(printInterval);
        Simulation.schedule(
            time + printInterval,
            (float t) -> printGates( t )
        );
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

    /** check the sanity of the network
    *Created by Douglas Jones
     */
    public static void checkNetwork() {
        for ( Gate g: gates ) {
            g.check();
        }
        for ( Wire w: wires ) {
            w.check();
        }
    }

    public static void main(String[] args) {
        if( args.length < 1){
            Errors.fatal( "Missing file name on command line" );
        } else if(args.length > 2){
            Errors.fatal( "Unexpected command line args" );
        } else try{
            /**
             * This is where the program takes in the input file, the initalizer is called here (part a)
             * @param  args[0]
             */
            Scanner sc = new Scanner( new File( args[0] ) );
            float p  = Float.parseFloat( args[1] );
            initializeGatesAndWires( sc );
            checkNetwork();
            if (Errors.count() > 0) {
                printGatesAndWires();
            } else {
                initPrint(p);
                //System.out.println("here");
                Simulation.run();
            }
            //printGatesAndWires();
        } catch(FileNotFoundException e){
            Errors.fatal( "Could not read '" + args[0] + "'");
        }
    }
}