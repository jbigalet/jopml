package jop_simulator;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Timer;
import java.util.TimerTask;

import java.awt.*;
import javax.swing.*;

public class Main extends JFrame {
//hop
    public Main() {}

    public static TreeMap<String, String[]> ParsedArgs = new TreeMap<String,String[]>();

    public static int varMaxTotal;
    public static boolean[] RegValues;
    public static boolean isClockLimit;
    public static long clockLimit;
    public static boolean isBrief;
    public static List<Gate> GateList;
    public static boolean toPrintRam;
    public static List<Gate> RegGates;
    public static List<Gate> RamGates;
    public static List<Gate> OutputGates;
    public static int iState;
    public static int NumberOfStates;
    public static Timer simulationTimer;
    public static long startingTime;
    public static boolean isVisual;
    public static boolean isRamGraph = true;

    public static TreeMap<String,JLabel> VisualOutputs;

    public static void main(final String[] args) {
        Runnable runThread = new Runnable() {
            public void run() {
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        try {
                            internMain(args, new Main());
                        } catch( Exception e) {e.printStackTrace();}
                    }
                });
            }
        };
        new Thread(runThread).start();
    }

    public static Main frame;

    public static void internMain(String[] args, Main tmpFrame) throws IOException, InterruptedException {
        startingTime = System.currentTimeMillis();
        frame = tmpFrame;
        
        ArgParsing(args);
        
        String FileName = ParsedArgs.get("NetListFile")[0];
        String InputFileName = ( ParsedArgs.containsKey("f") )
                                 ? ParsedArgs.get("f")[0] : null;
        isBrief = ParsedArgs.containsKey("brief");
        toPrintRam = ParsedArgs.containsKey("printram");
        isVisual = ParsedArgs.containsKey("visual");

        List<String> UnparsedGateList = ReadFile(FileName);
        System.out.println("Netlist \"" + FileName + "\" loaded.");

        GateList = ExtractGateList(UnparsedGateList, isBrief);
        System.out.println("\t-> " + GateList.size() + " gates found.\n");

        varMaxTotal = varMax(GateList);
        RegGates = GetAndRemoveTypedGates(new String[] {"Reg"}, GateList, true);
        int varMaxInReg = varMax(RegGates);
        List<Gate> InputGates = GetAndRemoveTypedGates(new String[] {"Input", "Zero", "One"}, GateList, true);
        RamGates = GetAndRemoveTypedGates(new String[] {"Ram", "Ram_Graphic"}, GateList, false);
        OutputGates = GetAndRemoveTypedGates(new String[] {"Output"}, GateList, true);
        //boolean[] RegValues = new boolean[varMaxInReg + 1];
        RegValues = initRegValues(RegGates, varMaxInReg);

        NumberOfStates = 0;
        if( ParsedArgs.containsKey("f")) NumberOfStates = GetInputsFromFile(InputGates, InputFileName);
        else GetInputsFromArgs( InputGates );

        if( ParsedArgs.containsKey("tick")){
            try{
                NumberOfStates = Integer.parseInt( ParsedArgs.get("tick")[0] );
            } catch (NumberFormatException e) {
                System.out.println("The number of tick entered is not a a valid integer.");
                System.exit(0);
            }
        }

        if( ParsedArgs.containsKey("ram") ){
            String[] RamFileArray = ParsedArgs.get("ram");
            for(String S : RamFileArray)
                PutFileInRam(S,RamGates);
        }

        isClockLimit = false;
        long clockLimit = 0;
        if( ParsedArgs.containsKey("clocklimit")){
            isClockLimit = true;
            clockLimit = (long)Integer.parseInt(ParsedArgs.get("clocklimit")[0]);
        }

        /*Runnable SimulationThread = new Runnable() {
            public void run() {
                try {
                    MakeSimulation();
                } catch( Exception e) {}
            }
        };
        new Thread(SimulationThread).start();*/

        if(isVisual){
            isBrief = false;
            frame.setTitle("Simulation : " + FileName);
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

            JPanel panel;
            panel = new JPanel(new GridBagLayout());
            //panel.setLayout(new BorderLayout(10, 10));

            frame.add(panel);

            VisualOutputs = new TreeMap<String,JLabel>();

            int column = 0;
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;

            constraints.gridy = column++;
            panel.add(new JLabel(" "), constraints);
            
            JLabel label_tick = new JLabel("Tick Number :");
            constraints.gridy = column++;
            panel.add(label_tick, constraints);
            VisualOutputs.put("tick", label_tick);

            constraints.gridy = column++;
            panel.add(new JLabel(" "), constraints);

            for(Gate g : OutputGates){
                JLabel tmp = new JLabel(g.Alias + " :   ");
                tmp.setHorizontalAlignment(JLabel.LEFT);
                constraints.gridy = column++;
                constraints.fill = GridBagConstraints.HORIZONTAL;
                panel.add(tmp, constraints);
                VisualOutputs.put(g.Alias, tmp);
            }

            constraints.gridy = column++;
            panel.add(new JLabel(" "), constraints);

            if(toPrintRam){
                for(Gate g : RamGates){
                    JLabel tmp = new JLabel("Ram " + g.Alias + " :   ");
                    constraints.gridy = column++;
                    panel.add(tmp, constraints);
                    VisualOutputs.put(g.Alias, tmp);
                }
            }

            constraints.gridy = column++;
            panel.add(new JLabel(" "), constraints);

            frame.pack();
            frame.setSize(500, frame.getHeight());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }

        iState = 0;
        simulationTimer = new Timer();
        if(isClockLimit)
            simulationTimer.schedule( new simTimerEx(), 0, clockLimit );
        else
            simulationTimer.schedule( new simTimerEx(), 0, 1 );
    }

    public void paintComponent(Graphics g) {
        
    }

    public static class simTimerEx extends TimerTask {
        public void run (  ) {
            try {
                MakeSimulation();
            } catch (InterruptedException e) {}
        }
    }

    public static void MakeSimulation() throws InterruptedException {

        if( iState < NumberOfStates ) {
            //long endClockLimit = System.nanoTime() + clockLimit;

            if( !isBrief ){
                if( !isVisual )
                    System.out.println("\n### State n°" + (iState+1) + " ###");
                else
                    VisualOutputs.get("tick").setText("### State n°" + (iState+1) + " ###");
            }

            boolean[] VarTable = InitValues(iState, RegValues, RegGates, varMaxTotal);

            /*
            List<Gate> tempGateList = new ArrayList<Gate>();
            tempGateList.addAll(GateList);
            while(!tempGateList.isEmpty())
                for(Gate g : tempGateList)
                    if(isCalculable(g, VarTable)){
                        FullGateSimulation(g, VarTable);
                        tempGateList.remove(g);
                        break;
                    }
                    else System.out.println("Erreur dans le tri topo");
            */

                // Topologic sort done by Oscar, so no need to check, the gates are sorted.
            for(Gate g : GateList)
                FullGateSimulation(g,VarTable);

            for(Gate g: OutputGates){
                if(isVisual)
                    VisualOutputs.get(g.Alias).setText(((Output)g).getOutput(getValues(g.InputArray,VarTable)));
                else
                    g.Simulate(getValues(g.InputArray,VarTable));
            }

            RegValuesActualisation(RegGates, RegValues, VarTable);

            if( !isBrief && toPrintRam ){
                if(!isVisual)
                    for(Gate RamG : RamGates){
                        System.out.print("\t");
                        ((AbstractRam)RamG).printRam();
                    }
                else
                    for(Gate RamG : RamGates)
                        VisualOutputs.get(RamG.Alias).setText(((AbstractRam)RamG).getToPrint());
            }

            /*if(isClockLimit){
                long diffTime = endClockLimit - System.nanoTime();
                if( diffTime > 0 ){
                    long tmpMillis = diffTime / 1000000L;
                    int tmpNanos = (int)(diffTime % 1000000L);
                    Thread.sleep(tmpMillis,tmpNanos);
                }
            }*/

            iState++;
        }

        else {
            if( isBrief ) PrintAllOutputs( GetAndRemoveTypedGates( new String[] {"Output","SSD"}, GateList, false) ) ;

            if( isBrief && toPrintRam )
                for(Gate RamG : RamGates)
                    ((AbstractRam)RamG).printRam();

            System.out.println("\nExecuted in " + (System.currentTimeMillis() - startingTime) + " ms.");
            simulationTimer.cancel();
        }
    }

    public static void PutFileInRam(String RamAliasAndFile, List<Gate> RamGateList){
        int ind = RamAliasAndFile.indexOf("=");
        String RamAlias = RamAliasAndFile.substring(0, ind);
        String File = RamAliasAndFile.substring( ind+1 );

        AbstractRam FoundGate = null;
        boolean isFound = false;
        for( Gate CurrentGate : RamGateList )
            if(CurrentGate.Alias.equals(RamAlias)){
                isFound = true;
                FoundGate = (AbstractRam)CurrentGate;
                break;
            }

        if( !isFound ){
            System.out.println("Warning : Ram with the alias " + RamAlias + " was not found. Nothing is done.");
            return ;
        }

        try{
            String RamContent = ReadFile(File).get(0);
            char[] CContent = new char[RamContent.length()];
            RamContent.getChars(0, RamContent.length(), CContent, 0);
            int i=0, j=0;
            for(char C : CContent){
                if( i == FoundGate.RamArray.length ){
                    System.out.println("Warning : To many information to add in the ram " + RamAlias + ". We're overflowing it.");
                    return ;
                }

                if( C == '0')
                    FoundGate.RamArray[i][j] = false;
                else if( C == '1')
                    FoundGate.RamArray[i][j] = true;
                else {
                    System.out.println("Error : In " + File + ", \"" + C + "\" is not a valid bit (0 or 1).");
                    System.exit(0);
                }

                j++;
                if( j == FoundGate.WordSize ){
                    j=0;
                    i++;
                }
            }
        }
        catch (Exception e) {
            if(e instanceof FileNotFoundException)
                System.out.println("Warning : The file " + File + " to put in a ram was not found. Nothing is done.");
            return ;
        }
    }

    public static int varMax( List<Gate> GateList ){
        int m = 0;
        for(Gate g : GateList){
            for(int var : g.InputArray)
                if(var > m) m = var;
            for(int var : g.OutputArray)
                if(var > m) m = var;
        }
        return m;
    }

    public static void ArgParsing( String[] args ){
        if(args.length == 0){
            System.out.println("Simulator use : ... (args) File\n");
            System.out.println("File : Complete or relative NetList path. NetList extension : .nl");
            System.out.println("-f input : Complete or relative Input file path, must be in standard defined format.");
            System.out.println("-i input : Manual command-line input entering (a=0 1 0 b=1 ...).\n\tMust be used with -tick argument");
            System.out.println("\tWarning : One of the -f or -i option must be defined\n");
            System.out.println("-tick : Define the number of clock tick to simulate.\n\tMust be an positive integer. Optional if -f defined");
            System.out.println("-brief : If defined, output are written in brief mode.");
            System.out.println("-ram alias=file : Put the files in alias-rams (-ram r1=test.ram r2=t2.r");
            System.out.println("-printram : Print the content of every ram, after every tick if not -isBrief, after the end else.");
            System.out.println("-onlyshow out1 out2 ... : Only show defined outputs.");
            System.out.println("-clocklimit limitInMS : Limit the simulator to do one tick every limitInMs millisecond.");
            System.out.println("-visual : To got a visual interface, to see the outputs in live.");
            System.exit(0);
        }
        
        String NetListFile = args[args.length -1];
        if( !NetListFile.endsWith(".nl") ){
            System.out.println("There must be a NetList file definition, with nl extension.");
            System.exit(0);
        }
        ParsedArgs.put("NetListFile", new String[] {NetListFile});

        if(args.length == 1){
            System.out.println("There must be either a -f or a -i definition for the input.");
            System.exit(0);
        }

        String Option = args[0];
        if( !Option.startsWith("-") ){
            System.out.println("Error in argument parsing : Don't forget the '-'");
            System.exit(0);
        }
        
        int i=1;
        while( i < args.length-1 ){
            Option = args[i-1].substring(1);
            List<String> FollowingArgs = new ArrayList<String>();
            for( ; !args[i].startsWith("-") && i < args.length -1 ; i++)
                FollowingArgs.add(args[i]);
            ParsedArgs.put(Option, ListToArray(FollowingArgs) );
            i++;
        }

        if(ParsedArgs.containsKey("i")){
            if( !ParsedArgs.containsKey("tick")){
                System.out.println("There must be a -tick definition if using -i argument.");
                System.exit(0);
            }
            if(ParsedArgs.containsKey("f")){
                System.out.println("You can't use -f and -i argument at the same time.");
                System.exit(0);
            }
        }
        else if( !ParsedArgs.containsKey("f") ){
            System.out.println("There must be either a -f or a -i argument for an input definition.");
            System.exit(0);
        }

        if( ParsedArgs.containsKey("brief") )
            if( ParsedArgs.get("brief").length != 0 )
                System.out.println("The is no arguments needed after the brief option. It will be ignored");

        if( ParsedArgs.containsKey("visual") )
            if( ParsedArgs.get("visual").length != 0 )
                System.out.println("The is no arguments needed after the visual option. It will be ignored");

        if( ParsedArgs.containsKey("printram") )
            if( ParsedArgs.get("printram").length != 0 )
                System.out.println("The is no arguments needed after the printram option. It will be ignored");


        if( ParsedArgs.containsKey("f") )
            if( ParsedArgs.get("f").length == 0 ){
                System.out.println("You must specified a file name after the -f option.");
                System.exit(0);
            }
            else if( ParsedArgs.get("f").length > 1)
                System.out.println("There is no more than one argument needed after the -f option. It will be ignored.");

        if( ParsedArgs.containsKey("tick") )
            if( ParsedArgs.get("tick").length == 0 ){
                System.out.println("You must specified a number of tick after the -tick option.");
                System.exit(0);
            }
            else if(ParsedArgs.get("tick").length > 1)
                System.out.println("There is no more than one argument needed after the -tick option. It will be ignored.");

        if( ParsedArgs.containsKey("clocklimit") )
            if( ParsedArgs.get("clocklimit").length == 0 ){
                System.out.println("You must specified a number of milliseconds after the -clocklimit option.");
                System.exit(0);
            }
            else if(ParsedArgs.get("clocklimit").length > 1)
                System.out.println("There is no more than one argument needed after the -clocklimit option. It will be ignored.");


        if( ParsedArgs.containsKey("ram") )
            if( ParsedArgs.get("ram").length == 0 )
                System.out.println("You must specified the alias of the ram and the file to put after the -ram option.");

        for( String S : ParsedArgs.navigableKeySet() )
            if( !S.equals("NetListFile") && !S.equals("brief") && !S.equals("f")
             && !S.equals("tick") && !S.equals("i") && !S.equals("ram")
             && !S.equals("printram") && !S.equals("onlyshow") && !S.equals("clocklimit")
             && !S.equals("visual"))
                System.out.println("Warning : Argument " + S + " is unknow. It will be ignore.");
        
    }

    public static Boolean[] BooleanListToArray(List<Boolean> L){
        Boolean[] R = new Boolean[L.size()];
        int pos=0;
        for(boolean S : L){
            R[pos] = S;
            pos++;
        }
        return R;
    }

    public static String[] ListToArray(List<String> L){
        String[] R = new String[L.size()];
        int pos=0;
        for(String S : L){
            R[pos] = S;
            pos++;
        }
        return R;
    }

    public static void PrintAllOutputs( List<Gate> OutputGates ){
        for(Gate g : OutputGates)
            if(g instanceof Output)
                ((Output)g).printAll();
            else
                ((SSD)g).printAll();
    }

    public static void FullGateSimulation(Gate g, boolean[] VarTable){
        putValues(g, g.OutputArray, g.Simulate(getValues(g.InputArray,VarTable)), VarTable);
    }

    public static void putValues(Gate g, int[] varArray, boolean[] valueArray, boolean[] VarTable){
        try {
            for(int i=0 ; i<varArray.length ; i++)
                VarTable[varArray[i]] = valueArray[i];
        }
        catch (Exception e) {
            System.out.println("Error in putValues, with the gate of type " + g.GateType + " and with the first input " + (g.InputArray != null ? g.InputArray[0] : ""));
            System.exit(0);
        }
    }

    public static boolean[] getValues(int[] varArray, boolean[] VarTable){
        boolean[] values = new boolean[varArray.length];
        for(int i=0 ; i<varArray.length ; i++)
            values[i] = VarTable[varArray[i]];
        return values;
    }

    /*public static boolean isCalculable(Gate g, TreeMap<String,Boolean> VarTable){
        for(int i=0 ; i<g.InputArray.length ; i++)
            if(!VarTable.containsKey(g.InputArray[i]))
                return false;
        return true;
    }*/

    public static List<Gate> GetAndRemoveTypedGates(String[] GateType, List<Gate> GateList, boolean wantToRemove){
        List<Gate> KeepGates = new ArrayList<Gate>();
        for(Gate g : GateList)
            if(isIn(g.GateType,GateType))
                KeepGates.add(g);

        if( wantToRemove )
            for(Gate g : KeepGates)
                GateList.remove(g);

        return KeepGates;
    }

    public static boolean isIn(String toCheck, String[] SArray){
        for(int i=0 ; i<SArray.length ; i++)
            if(SArray[i].equals(toCheck))
                return true;
        return false;
    }

    public static void RegValuesActualisation(List<Gate> RegGates, boolean[] RegValues, boolean[] VarTable){
        for(Gate g : RegGates)
            RegValues[g.OutputArray[0]] = VarTable[g.InputArray[0]];
    }

    public static boolean[] initRegValues(List<Gate> RegGates, int varMaxInReg){
        boolean[] RegValues = new boolean[varMaxInReg + 1];
        for(Gate g : RegGates)
            RegValues[g.OutputArray[0]] = ((Reg)g).startingValue;
        return RegValues;
    }

    public static boolean[] InitValues(int iState, boolean[] RegValues, List<Gate> RegGates, int varMaxTotal){
            // We start by an initialization of the Input, Zero and One variables.
        boolean[] VarTable = new boolean[varMaxTotal + 1];
        for(int S : InputValues.navigableKeySet()){
            Boolean[] bArray = InputValues.get(S);
            VarTable[S] = bArray[iState % bArray.length];
        }

            // Then, the we put the register values.
        for(Gate RegGate : RegGates){
            int var = RegGate.OutputArray[0];
            VarTable[var] = RegValues[var];
        }
            
        return VarTable;
    }

    public static TreeMap<Integer,Boolean[]> InputValues = new TreeMap<Integer,Boolean[]>();

    public static void GetInputsFromArgs(List<Gate> InputGateList){
        // -f C:\\CounterMod24.jopnl.input.txt
        TreeMap<String,Integer[]> tempVarTable = new TreeMap<String,Integer[]>();
        String[] UnparsedInputs = ParsedArgs.get("i");

        int i = 0;
        while( i < UnparsedInputs.length ){
            int ind = UnparsedInputs[i].indexOf("=");
            String tempVar = UnparsedInputs[i].substring(0, ind);
            String firstVal = UnparsedInputs[i].substring( ind+1 );

            List<Integer> toPutValues = new ArrayList<Integer>();
            toPutValues.add( StringToInteger(firstVal) );
            i++;
            for( ; i < UnparsedInputs.length && !UnparsedInputs[i].contains("=") ; i++ )
                toPutValues.add( StringToInteger(UnparsedInputs[i]) );

            Integer[] ArrayToPut = new Integer[toPutValues.size()];
            toPutValues.toArray( ArrayToPut );
            tempVarTable.put(tempVar, ArrayToPut );
        }

        InputValues = CheckRealInputWithInputGates(InputGateList, tempVarTable);
    }

    public static int StringToInteger( String SToParse ){
        int i = 0;
        try {
            i = Integer.parseInt( SToParse );
        }
        catch ( NumberFormatException e ) {
            System.out.println("Error in input parsing : \"" + SToParse + "\" receveid as a value, but it must be an integer.");
            System.exit(0);
        }
        return i;
    }

    public static boolean StringToBoolean(String SToParse){
        try{
            int i = Integer.parseInt( SToParse );
            if( i != 0 & i != 1 ) {
                System.out.println("Error in input parsing : " + i + " is not a valid value (must be 0 or 1)." );
                System.exit(0);
            }
            else return (i == 1);
        }
        catch( NumberFormatException e) {
            System.out.println("Error in input parsing : \"" + SToParse + "\" is not a integer valid value" );
            System.exit(0);
        }
        return true;
    }

    public static int GetInputsFromFile(List<Gate> InputGateList, String InputFileName) throws IOException {
        TreeMap<String,Integer[]> tempVarTable = new TreeMap<String,Integer[]>();

        List<String> InputFile = ReadFile(InputFileName);
        int n = Integer.parseInt(InputFile.get(0));
        InputFile.remove(0);
        for(String S:InputFile){
            String[] Spl = S.split(" ");
            Integer[] toAdd = new Integer[Spl.length - 1];
            for(int i=1 ; i<Spl.length ; i++)
                toAdd[i-1] = StringToInteger(Spl[i]);
            tempVarTable.put(Spl[0], toAdd);
        }

        InputValues = CheckRealInputWithInputGates(InputGateList, tempVarTable);

        return n;
    }

    public static Boolean[][] UncompressInputValues(Integer[] CompressedValues, int nOutput, String GateAlias){
        Boolean[][] UncompressedValues = new Boolean[nOutput][CompressedValues.length];
        for(int i=0 ; i<CompressedValues.length ; i++){
            int Current = CompressedValues[i];
            for(int j=0 ; j<nOutput ; j++){
                UncompressedValues[j][i] = (Current % 2 == 1);
                Current /= 2;
            }
                // If Overflow :
            if(Current != 0)
                System.out.println("Warning : The " + i + "-th value of the input gate \"" + GateAlias +
                                   "\" is overflow defined at " + CompressedValues[i] + ". It will be used truncated.");
        }
        return UncompressedValues;
    }

    public static TreeMap<Integer, Boolean[]> CheckRealInputWithInputGates( List<Gate> InputGateList, TreeMap<String,Integer[]> tempVarTable ){
        TreeMap<Integer,Boolean[]> VarTable = new TreeMap<Integer,Boolean[]>();
        String var;
        for(Gate g:InputGateList){
                if(g.GateType.equals("One"))
                    VarTable.put(g.OutputArray[0], new Boolean[] {true});
                else if(g.GateType.equals("Zero"))
                    VarTable.put(g.OutputArray[0], new Boolean[] {false});
                else if(tempVarTable.containsKey(var = (g.Alias + ""))){
                    Boolean[][] BtoAdd = UncompressInputValues(tempVarTable.get(var),g.OutputArray.length,var);
                    for(int i=0 ; i<g.OutputArray.length ; i++)
                        VarTable.put(g.OutputArray[i], BtoAdd[i]);
                }
                else {
                    System.out.println("Error : The input " + var + " is not defined in the input file.");
                    System.exit(0);
                }
        }
        return VarTable;
    }

    public static List<Gate> ExtractGateList(List<String> UnparsedGateList, boolean isBrief){
        List<Gate> GateList = new ArrayList<Gate>();
        Gate NewGate;
        for(String S:UnparsedGateList){
            String[] ParsingArray = S.split(" ");
            NewGate = null;
            if(ParsingArray[0].equals("not"))
                NewGate = new Not(Integer.parseInt(ParsingArray[1]),Integer.parseInt(ParsingArray[2]));
            else if(ParsingArray[0].equals("and"))
                NewGate = new And(Integer.parseInt(ParsingArray[1]),Integer.parseInt(ParsingArray[2]),Integer.parseInt(ParsingArray[3]));
            else if(ParsingArray[0].equals("or"))
                NewGate = new Or(Integer.parseInt(ParsingArray[1]),Integer.parseInt(ParsingArray[2]),Integer.parseInt(ParsingArray[3]));
            else if(ParsingArray[0].equals("xor"))
                NewGate = new Xor(Integer.parseInt(ParsingArray[1]),Integer.parseInt(ParsingArray[2]),Integer.parseInt(ParsingArray[3]));
            else if(ParsingArray[0].equals("mux"))
                NewGate = new Mux(Integer.parseInt(ParsingArray[1]),Integer.parseInt(ParsingArray[2]),Integer.parseInt(ParsingArray[3]),Integer.parseInt(ParsingArray[4]));
            else if(ParsingArray[0].equals("input"))
                NewGate = new Input(ParsingArray[1], ParsingArray );
            else if(ParsingArray[0].equals("output"))
                NewGate = new Output(ParsingArray, isBrief);
            else if(ParsingArray[0].equals("ssd"))
                NewGate = new SSD(ParsingArray, isBrief);
            else if(ParsingArray[0].equals("ram"))
                NewGate = new Ram(ParsingArray);
            else if(ParsingArray[0].equals("random"))
                NewGate = new Random(Integer.parseInt(ParsingArray[1]));
            else if(ParsingArray[0].equals("ramgraph")){
                isRamGraph = true;
                //isVisual = true;
                NewGate = new Ram_Graphic(ParsingArray);
            }
            else if(ParsingArray[0].equals("zero"))
                NewGate = new Zero(Integer.parseInt(ParsingArray[1]));
            else if(ParsingArray[0].equals("one"))
                NewGate = new One(Integer.parseInt(ParsingArray[1]));
            else if(ParsingArray[0].equals("reg"))
                NewGate = new Reg(Integer.parseInt(ParsingArray[1]),Integer.parseInt(ParsingArray[2]),false);
            else if(ParsingArray[0].equals("nreg"))
                NewGate = new Reg(Integer.parseInt(ParsingArray[1]),Integer.parseInt(ParsingArray[2]),true);
            else if(!S.contains("//") && !S.equals("")) // Comments on the netlist, or blank line for visibility.
                System.out.println("Warning : '" + S + "' does not define a gate");

            if(NewGate != null && verifyOutputToShow(NewGate) ) GateList.add(NewGate);
        }
        return GateList;
    }

    public static boolean verifyOutputToShow(Gate g){
        if((!g.GateType.equals("Output") && !g.GateType.equals("SSD"))
            || !ParsedArgs.containsKey("onlyshow"))
            return true;
        for(String S : ParsedArgs.get("onlyshow"))
            if(S.equals(g.Alias))
                return true;
        return false;
    }

    public static List<String> ReadFile(String FileName) throws IOException {
        List<String> FList = new ArrayList<String>();

        BufferedReader BR = null;
        try { BR = new BufferedReader (new FileReader(FileName)); }
        catch(FileNotFoundException e) { System.out.println("File " + FileName + " not found."); }

        String line;
        while ((line = BR.readLine()) != null)
            FList.add(line);

        BR.close();
        return FList;
    }

}
