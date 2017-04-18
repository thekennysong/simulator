/** Gate.java
 * @author Douglas Jones (idea of 2-D Array)
 * @author Kenny Song
 * @version MP5
 */
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Scanner;


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

        //2-D array helps us avoid a bunch of if statements/switch statements
        //2-D array to determine the value row 1 is when output was false, row 2 unknown, row 3 true.
        //column 1 is now false, column 2 is now unknown, column 3 is now true.
        String[][] values = {
            {"|    ", "|_   ",  "|___ "},
            {" _|  ", "  |  ", "  |_ "},
            {" ___|",  "   _|", "    |"}
        };

        String returnValue = values[lastPrinted][output];
        //store last printed value, this is important
        lastPrinted = output;

        return returnValue;

    }

}