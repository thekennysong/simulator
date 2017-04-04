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
    //input change, check if we need to change the wire value and call a corresponding input change for gates
    public void inputChange(float t, int o, int n){

        //o and this.value are the same value, in that it is the old wire value
        if(o != n){
            // Simulator.schedule(
            //     t+delay,
            //     (float time)->outputChange( time, this.value, n )
            // );
            //
            //Doing lambda and regular would take same amoutn of code
            Simulation.schedule(
                t+delay,
                new Simulation.Action(){
                    public void trigger(float time){
                        outputChange(time, o, n);
                    }
            });
            this.value = n;

        }
    }
    public void outputChange(float time, int o, int n){


        //are able to trigger input change for wire that has changed. don't have to worry about direction since can use destination variable
        Simulation.schedule(
            time,
            new Simulation.Action(){
                public void trigger(float t){
                    destination.inputChange(t, o, n);
                }
        });

        // Simulator.schedule(
        //     time,
        //     (float t)->destination.inputChange( t, o, n )
        // );
    }
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

        //Before the simulation begins, each such gate should immediately schedule an output transition from unknown to false (represented by the value 0) at a time equal to the gate's delay.
        Simulation.schedule(
                delay,
                (float t) -> this.outputChange( t, output, 0 )
        );

    }

    public void inputChange( float time, int o, int n ) {

        //System.out.println("min gate");
        for(int i = 0; i < 3; i++){
            if(o == i){
                inputCounts[i] = inputCounts[i] - 1;
            }
            if(n == i){
                inputCounts[i] = inputCounts[i] + 1;
            }
        }
        final int newOutput;
        if(inputCounts[0] > 0){
            newOutput = 0;
        } else if(inputCounts[1] > 0){
            newOutput = 1;
        } else{
            newOutput = 2;
        }

        //if current out is not the same as new, we will proceed with changing wires coming from this gate
        if (output != newOutput) {
                final int old = output;
                final int n1 = newOutput;

                Simulation.schedule(
                        time + delay,
                        (float t) -> this.outputChange( t, old, n1 )
                );
                this.output = newOutput;

                //TernaryLogic.printGatesOutputs();
        }
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

        //before simulation scheudle output transition from 1 to 0.
        Simulation.schedule(
                delay,
                (float t) -> this.outputChange( t, output, 0 )
        );
       // output = 0;

    }
    //do our check here whether we need to change gate value based on new wire input value
    public void inputChange( float time, int o, int n ) {

        //System.out.println("max gate");
        for(int i = 0; i < 3; i++){
            if(o == i){
                inputCounts[i] = inputCounts[i] - 1;
            }
            if(n == i){
                inputCounts[i] = inputCounts[i] + 1;
            }
        }

        final int newOutput;
        if(inputCounts[2] > 0){
            newOutput = 0;
        } else if(inputCounts[1] > 0){
            newOutput = 1;
        } else{
            newOutput = 0;
        }
        //similar to the min gate, just starting from index 0 to 2
        if (output != newOutput) {
                final int old = output;
                final int n1 = newOutput;

                Simulation.schedule(
                        time + delay,
                        (float t) -> outputChange( t, old, n1 )
                );
                this.output = newOutput;

                //TernaryLogic.printGatesOutputs();
        }
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

        Simulation.schedule(
                delay,
                (float t) -> this.outputChange( t, output, 0 )
        );
        //output = 0;

    }
    //do our check here whether we need to change gate value based on new wire input value
    public void inputChange( float time, int o, int n ) {
        //System.out.println(o + " neg gate " + output + " "+ n);

            //take the inverse of the value, unless unknown
            final int newOutput;
            if(n == 0){
                newOutput = 2;
            } else if(n == 1){
                newOutput = 1;
            } else{
                newOutput = 0;
            }
            //check if new output is different since we don't need to do anything otherwise
            if(output != newOutput){
                final int old = output;
                Simulation.schedule(
                        time + delay,
                        (float t) -> this.outputChange( t, old, newOutput )
                );
                this.output = newOutput;
            }


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

        ScanSupport.lineEnd( sc,
            "TrueGate '" + name + "'"
        );
        //must change to 0 for the output transition
        Simulation.schedule(
                delay,
                (float t) -> this.outputChange( t, output, 0 )
        );
        //output = 0;
    }
    //do our check here whether we need to change gate value based on new wire input value
    public void inputChange( float time, int o, int n ) {
        //System.out.println(o + " true gate " + output + " "+ n);
      //  if(o != n){
            final int newOutput;
            if( n == 2 ){
                newOutput = 2;
            } else{
                newOutput = 0;
            }
            if(output != newOutput){
                final int old = output;
                Simulation.schedule(
                        time + delay,
                        (float t) -> this.outputChange( t, old, newOutput )
                );
                this.output = newOutput;
            }
            //TernaryLogic.printGatesOutputs();
       // }

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
        //output transition schedule method call
        Simulation.schedule(
                delay,
                (float t) -> this.outputChange( t, output, 0 )
        );
        //output = 0;
    }
    //do our check here whether we need to change gate value based on new wire input value
    public void inputChange( float time, int o, int n ) {
        //o is old input wire value, n is new input wire value, output is old gate value

            final int newOutput;
            if( n == 0 ){
                newOutput = 2;
            } else{
                newOutput = 0;
            }
            if(output != newOutput){
                final int old = output;
                Simulation.schedule(
                        time + delay,
                        (float t) -> this.outputChange( t, old, newOutput )
                );
                this.output = newOutput;
            }


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
        //NOTE, this is 2 because when 1 it is true. Unique from the other schedule calls in the constructor
        Simulation.schedule(
                delay,
                (float t) -> this.outputChange( t, output, 2 )
        );


    }
    //do our check here whether we need to change gate value based on new wire input value
    public void inputChange( float time, int o, int n ) {
        //System.out.println(o + " unknown gate " + output + " "+ n);
       // if(o != n){
            final int newOutput;
            if( n == 1 ){
                newOutput = 2;
            } else{
                newOutput = 0;
            }
            if(output != newOutput){
                final int old = output;
                Simulation.schedule(
                        time + delay,
                        (float t) -> outputChange( t, old, newOutput )
                );
                this.output = newOutput;
            }
            //System.out.println("outp");

            //TernaryLogic.printGatesOutputs();
        //}

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

    public int[] inputCounts = {0, 0, 0};

    public int output = 1;
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
        if (incoming.size() > totalInputsPermitted ) {
            Errors.warn(
                this.toString() +
                ": too many incoming wires"
            );
        }
    }
    public void inputChange( float time, int o, int n ){
        System.out.println(" test"); //this will never print, will go into subclass inputChange functions
    }
    //this is the main output change for all Gate classes. VERY IMPORTANT. This is what prints when a gate val changes.
    public void outputChange(float time, int old, int n){

        this.output = n;
        System.out.println("time: " + time + " gate name: " + this.name + " old val: " + old + " new val: " + n);
        //update all the outgoing wires when the gate value changes
        for(Wire w: outgoing){
            //System.out.println("wire wire for loop" + w.toString());
            Simulation.schedule(
                    time,
                    (float t) -> w.inputChange( t, w.value, n )
            );
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
    public static void printGatesOutputs(){
        for (Gate g: gates) {
            System.out.println( g.name + " " + g.output );
        }

        for (Wire r: wires) {
            System.out.println( r );
        }
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
        } else if(args.length > 1){
            Errors.fatal( "Unexpected command line args" );
        } else try{
            /**
             * This is where the program takes in the input file, the initalizer is called here (part a)
             * @param  args[0]
             */
            Scanner sc = new Scanner( new File( args[0] ) );
            initializeGatesAndWires( sc );
            checkNetwork();
            if (Errors.count() > 0) {
                printGatesAndWires();
            } else {
                //System.out.println("here");
                Simulation.run();
            }
            //printGatesAndWires();
        } catch(FileNotFoundException e){
            Errors.fatal( "Could not read '" + args[0] + "'");
        }
    }
}