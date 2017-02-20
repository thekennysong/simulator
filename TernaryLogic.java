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

    public Wire(Scanner sc){

        source = sc.next();
        destination = sc.next();


        if (!sc.hasNextFloat() && sc.nextFloat() <= 0 ) {
            Errors.fatal(
                "travel time invalid. Must be a float greater than 0."
            );
        }
        delay = sc.nextFloat();
    }

}


class Gate{
    String name;
    float delay;
    String type;
    int totalInputs;

    int output;
    LinkedList <Wire> wires;


    public Gate(Scanner sc){
        name = sc.next();

        if(gateNames.contains(name)){
           Errors.fatal("gate name " + name + " already exists");
        }

        type = sc.next();
        if(Integer.parseInt(sc.next()) > 0){
            totalInputs = Integer.parseInt(sc.next());
        } else{
            Errors.fatal("inputs must be gerater than 0");
        }
        if(sc.hasNextFloat() && sc.nextFloat() > 0){
            delay = sc.nextFloat();
        }


    }

    /**
     *  toString method is for part b answer!
     * @return part b answer here
     */
    public String toString(){
        return "Gate " + name + delay;
    }


}

class MinGate extends Gate {
    public MinGate( Scanner sc ) {
        super( sc );
    }
    public void checkCorrectness() {

        if ( wires.size() == 0 ) {
            Errors.fatal(
                "No wires to gate"
            );
        }
    }



}
class MaxGate extends Gate {
    public MaxGate( Scanner sc ) {
        super( sc );
    }

}
class NegGate extends Gate {
    public NegGate( Scanner sc ) {
        super( sc );
    }
}
class TrueGate extends Gate {
    public TrueGate( Scanner sc ) {
        super( sc );
    }
}
class FalseGate extends Gate {
    public FalseGate( Scanner sc ) {
        super( sc );
    }
}
class UnknownGate extends Gate {
    public UnknownGate( Scanner sc ) {
        super( sc );
    }
}
/** Main class, where we initialize the gates read from the text file */
public class TernaryLogic{

    static LinkedList <Gate> gates = new LinkedList <Gate> ();
    static LinkedList <Wire> wires = new LinkedList <Wire> ();
    ArrayList<String> gateNames = new ArrayList<String>();
    String[] gateTypes = {"min", "max", "neg", "istrue", "isfalse", "isunknown"};

    /**
     * Part a answer, rest of answer for part a can be seen in the Gate class.
     * @param sc
     */
    public static void initializeGatesAndWires( Scanner sc ){

        while(sc.hasNextLine()){
            String command  = sc.next();

            if(command.equals("gate")){
                String name = sc.next();
                String type = sc.next();
                if( name.length() > 0 || ( isNumeric(name) && isEven(name) ) ){
                     switch (type) {
                        case "min": gates.add( new MinGate(sc) ); break;
                        case "max": gates.add( new MaxGate(sc) ); break;
                        case "neg": gates.add( new NegGate(sc) ); break;
                        case "istrue": gates.add( new TrueGate(sc) ); break;
                        case "isfalse": gates.add( new FalseGate(sc) ); break;
                        case "isunknown": gates.add( new UnknownGate(sc) ); break;
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
                String destination = sc.next();
                //for (Integer vertex : entry.getValue()) {

                for(Gate gate: gates){
                    gateNames.add(gate.name);
                }
                if(gateNames.contains(source) && gateNames.contains(destination) && !source.equals(destination)){
                    wires.add( new Wire(sc) );
                } else{
                     Errors.fatal(
                        "wire destination/source not entered correctly"
                    );
                }

            }
        }

    }

    private static void printGatesAndWires(){

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