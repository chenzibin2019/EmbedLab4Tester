//
//  Try.java
//  EmbedLab4Tester
//
//  Creater by Zibin Chen on 4/27/2019
//  Copyright c 2019 Zibin Chen. All rights reserved.
//

import java.io.*;
import java.lang.Runtime;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;

class Constant {
    public static String SPEXMODEL_FILE = "BB_TPL_SPEX.tpl";
    public static String PHVRMODEL_FILE = "BB_TPL_PHVR.tpl";
    public static String MODEL_DIR = "model_dir/";
    public static String MODEL_NAME_TPL = Constant.MODEL_DIR + "BB_MDL_%s_%s.%s";        //BB_MDL_PHA_0.5.pha
    public static String SPEX_EXEC_CMD = "sspaceex --model-file %s --config %s --sampling-time %s --output-file %s --forbidden \"%s\"";
    //sspaceex --model-file model_dir/xxx.xml --configure spex_cfg.cfg --sampling-time 0.3 --output-file c_0.3.raw --forbidden "v>=1.05"
    public static String PHAV_EXEC_CMD = "phaver %s > %s";
    public static String BUFFER_FILE_NAME = "anabuf.tmp";
    public static String SPACEX_CFG_FILE = "try.cfg";
    public static String BASH_FLIE_NAME = "phv_bash.sh";
    //phaver model_dir/xxx.pha
    public static double DLTA = 0.05;
    public static int INIT_SPEED = 2;
    public static String PHAVER_TARGET_STR = "cond_v_excd_105 is reachable.";
    public static double PRECISION = 0.02;
    public static double PHAVER_LBOUND = 0.1;
    public static double PHAVER_UBOUND = 1;
    public static double SPACE_LBOUND = 0.1;
    public static double SPACE_UBOUND = 1;
}

class ExecResult {
    public int statusCode;
    public String execResult;
    public long execTime;

    public ExecResult(int code, String res, long time) {
        this.statusCode = code;
        this.execResult = res;
        this.execTime = time;
    }
}

class Utils {

    public static String readToString(String fileName) {  
        String encoding = "UTF-8";  
        File file = new File(fileName);  
        Long filelength = file.length();  
        byte[] filecontent = new byte[filelength.intValue()];  
        try {  
            FileInputStream in = new FileInputStream(file);  
            in.read(filecontent);  
            in.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        try {  
            return new String(filecontent, encoding);  
        } catch (UnsupportedEncodingException e) {  
            System.err.println("The OS does not support " + encoding);  
            e.printStackTrace();  
            return null;  
        }  
    }

    public static void writeStringToFile(String fileName, String str) {
        try (FileWriter writer = new FileWriter(fileName);
             BufferedWriter bw = new BufferedWriter(writer)) {

            bw.write(str);

        } catch (IOException e) {
            System.err.format("IOException: %s\n", e);
        }
    }

    public static void print(String type, String str) {
        System.out.println(
            String.format(
                "[%s] %s : %s",
                type,
                str,
                (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date())
            )
        );
    }

    public static void print(int type, String str) {
        String[] tbl = {"ERROR", "INFO", "WARNING"}; //, "DEBUG", "TEST"};
        try {
            Utils.print(tbl[type + 1], str);
        }catch(Exception e) {}
    }

    public static double formatDecimal(double d) {
        //return d;
        DecimalFormat df = new DecimalFormat("0.0000");
        return Double.valueOf(df.format(d));
        //return (new DecimalFormat("0.00")).format(d);
    }
}

abstract class ModelBuilder {
    protected String modelStr;
    protected double c;
    protected double cond;

    public ModelBuilder(double c) {
        this.cond = c * Constant.INIT_SPEED + Constant.DLTA;
        this.c = c;
    }

    abstract public void setPartition(double partition);
    abstract public void writeToFile();

    public void writeToFile(String fileName) {
        Utils.writeStringToFile(fileName, this.modelStr);
    }

    public ExecResult execCmd(String cmd, String buf) {
        long stTime = System.currentTimeMillis();
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            Utils.print(2, "CMD To Run: " + cmd + "; Instance is " + this.getClass().toString());
            Utils.print(0, this.getClass().toString() + " launched, c = " + this.c +" . At");
            process.waitFor();
            String result = Utils.readToString(buf);
            long edTime = System.currentTimeMillis();
            Utils.print(0, this.getClass().toString() + " exit. At");
            return new ExecResult(0, result, (long)(edTime - stTime));
        }catch (Exception e) {
            return new ExecResult(1, e.getMessage(), 0);
        }
    }

    abstract public ExecResult execAnalyse();
}

abstract class ModelTester {
    double upperBound;
    double lowerBound;
    double runningPere;
    boolean isReachableU;
    boolean isReachableL;
    ModelBuilder mb;
    private boolean modified;

    public ModelTester(ModelBuilder mb, double lb, double ub) {
        this.mb = mb;
        this.upperBound = ub;
        this.lowerBound = lb;
        this.modified = false;
        //this.runningPere = lb;
    }

    abstract public boolean reachablity(String res);

    public long exec() {
        ExecResult rst = new ExecResult(1, "", 0);
        Utils.print(0, "Currently running " + this.getClass().toString() + " with partition " + this.upperBound);
        //Test is reach with upper
        this.mb.setPartition(this.upperBound);
        this.mb.writeToFile();
        ExecResult rst1 = mb.execAnalyse();
        Utils.print(0, "Currently running " + this.getClass().toString() + " with partition " + this.lowerBound);
        //Test is reach with lower
        this.mb.setPartition(this.lowerBound);
        this.mb.writeToFile();
        ExecResult rst2 = mb.execAnalyse();
        //if both reach or not reach, print warning, set running time to be average
        if(rst1.statusCode == 1) {
            Utils.print("ERROR", this.getClass().toString() + "exec failed!" + rst1.execResult);
            return 0;
        }
        if(rst2.statusCode == 1) {
            Utils.print("ERROR", this.getClass().toString() + "exec failed!" + rst2.execResult);
            return 0;
        }
        Utils.print(2, "RST1:" + rst1.execTime + "\tRST2:" + rst2.execTime);
        this.isReachableU = this.reachablity(rst1.execResult);
        this.isReachableL = this.reachablity(rst2.execResult);
        if (this.isReachableL == this.isReachableU) {
            Utils.print("WARNING", "Upper and lower has the are both " + (this.isReachableL?"":"un") + "reachable, c = " + this.mb.c);
            return (rst1.execTime + rst2.execTime)/2;
        }
        this.runningPere = Utils.formatDecimal((double) (this.upperBound + this.lowerBound) / 2);
        //System.out.println(this.upperBound - this.lowerBound);
        while(this.upperBound - this.lowerBound > Constant.PRECISION) {
            if (this.runningPere > this.upperBound || this.runningPere < this.lowerBound ) break;
            // Test middle
            this.mb.setPartition(this.runningPere);
            this.mb.writeToFile();
            Utils.print(0, "Currently running " + this.getClass().toString() + " with partition " + this.runningPere);
            rst = mb.execAnalyse();
            if(rst.statusCode == 1) {
                Utils.print(1, "Exec failed, plus 0.01 and try again, partition = " + this.runningPere);
                this.runningPere += 0.1;
                continue;
            }
            boolean result = this.reachablity(rst.execResult);
            if(result == isReachableL) {
                //going up
                Utils.print(0, "Going Up and Continue Test partition = " + this.runningPere);
                this.lowerBound = this.runningPere;
                this.isReachableL = result;
            }else {
                //going down
                Utils.print(0, "Going Down and Continue Test partition = " + this.runningPere);
                this.upperBound = this.runningPere;
                this.isReachableU = result;
            }
            this.runningPere = Utils.formatDecimal((double) (this.upperBound + this.lowerBound) / 2);
            //break;
        }
        return rst.execTime;
    }
}

class SpaceExModelTester extends ModelTester {
    public SpaceExModelTester(SpaceExModelBuilder sp) {
        super(sp, Constant.SPACE_LBOUND, Constant.SPACE_UBOUND);
    } 

    public boolean reachablity(String res) {
        String trimedRes = res.replaceAll(" ", "").replaceAll("\n", "");
        return !trimedRes.equals("");
        //return true;
    }
    
}

class PhaverModelTester extends ModelTester {
    public PhaverModelTester(PhaverModelBuilder ph) {
        super(ph, Constant.PHAVER_LBOUND, Constant.PHAVER_UBOUND);
    }

    public boolean reachablity(String res) {
        return res.indexOf(Constant.PHAVER_TARGET_STR) != -1;
        //return true;
    }
}

class SpaceExModelBuilder extends ModelBuilder {

    private double samplingTime = 0.1;      //default  sampling time is 0.1
    public SpaceExModelBuilder(String modelFileName, double c) {
        super(c);
        modelStr = String.format(Utils.readToString(modelFileName), String.valueOf(c));
    }

    public void setPartition(double partition) {
        this.samplingTime = partition;
    }

    public SpaceExModelBuilder(double c) {
        this(Constant.SPEXMODEL_FILE, c);
    }

    public void writeToFile() {
        super.writeToFile(String.format(Constant.MODEL_NAME_TPL, "SPX", String.valueOf(this.c), "xml"));
    }

    public ExecResult execAnalyse() {
        return super.execCmd(String.format(
            Constant.SPEX_EXEC_CMD, 
            String.format(Constant.MODEL_NAME_TPL, "SPX", String.valueOf(this.c), "xml"),
            Constant.SPACEX_CFG_FILE,
            String.valueOf(this.samplingTime),
            "phvr_output_" + Constant.BUFFER_FILE_NAME,
            String.format("v>=%s", String.valueOf(this.cond))
        ), "phvr_output_" + Constant.BUFFER_FILE_NAME);
    }
}

class PhaverModelBuilder extends ModelBuilder {

    private double pc = 0;
    public PhaverModelBuilder(String modelFileName, double c, double pc) {
        super(c);
        this.pc = pc;
        modelStr = String.format(Utils.readToString(modelFileName), String.valueOf(c), String.valueOf(pc), String.valueOf(this.cond));
    }

    public PhaverModelBuilder(double c, double pc) {
        this(Constant.PHVRMODEL_FILE, c, pc);
    }

    public void setPartition(double partition) {
        //need to rebuild a model
        //this = NULL;
        this.modelStr =  (new PhaverModelBuilder(this.c, partition)).modelStr;
        //Utils.print(2, "modelStr change to \"" + this.modelStr + "\"");
    }

    public void writeToFile() {
        super.writeToFile(String.format(Constant.MODEL_NAME_TPL, "PHA", String.valueOf(this.c), "pha"));
    }

    public ExecResult execAnalyse() {
        String bs = String.format(
            Constant.PHAV_EXEC_CMD, 
            String.format(Constant.MODEL_NAME_TPL, "PHA", String.valueOf(this.c), "pha"),
            "phvr_output_" + Constant.BUFFER_FILE_NAME
        );
        Utils.writeStringToFile(Constant.BASH_FLIE_NAME, bs);
        return super.execCmd("sh " + Constant.BASH_FLIE_NAME, "phvr_output_" + Constant.BUFFER_FILE_NAME);
    }
}

public class Tester {
    static public void main(String args[]) {
        Utils.print("INFO", "Program begin");
        // define input format as c c c c c c 
        long startTime = System.currentTimeMillis();
        SpaceExModelTester sp;
        PhaverModelTester pv;
        SpaceExModelBuilder spModel;
        PhaverModelBuilder pvModel;
        String csvBuf = "c,time_space_ex,time_phaver\n";
        for(String s:args) {
            try {
                if(s.indexOf("--space-lower") != -1) {
                    Constant.SPACE_LBOUND = Utils.formatDecimal(Float.valueOf(s.replaceAll("--space-lower", "")));
                    Utils.print("NOTICE", "You have set the lower bound of SpaceEx tester to " + Constant.SPACE_LBOUND);
                    continue;
                }
                if(s.indexOf("--space-upper") != -1) {
                    Constant.SPACE_UBOUND = Utils.formatDecimal(Float.valueOf(s.replaceAll("--space-upper", "")));
                    Utils.print("NOTICE", "You have set the upper bound of SpaceEx tester to " + Constant.SPACE_UBOUND);
                    continue;
                }
                if(s.indexOf("--phaver-lower") != -1) {
                    Constant.PHAVER_LBOUND = Utils.formatDecimal(Float.valueOf(s.replaceAll("--phaver-lower", "")));
                    Utils.print("NOTICE", "You have set the lower bound of Phaver tester to " + Constant.PHAVER_LBOUND);
                    continue;
                }
                if(s.indexOf("--phaver-upper") != -1) {
                    Constant.PHAVER_UBOUND = Utils.formatDecimal(Float.valueOf(s.replaceAll("--phaver-upper", "")));
                    Utils.print("NOTICE", "You have set the upper bound of Phaver tester to " + Constant.PHAVER_UBOUND);
                    continue;
                }
                double c = Double.valueOf(s);
                // create a spaceex model using c
                System.out.println("--------------------");
                Utils.print(0, "Testing Start partition = " + s);
                spModel = new SpaceExModelBuilder(c);
                sp = new SpaceExModelTester(spModel);
                pvModel = new PhaverModelBuilder(c, 0.1);
                pv = new PhaverModelTester(pvModel);
                long timePv = pv.exec();
                Utils.print(0, pv.getClass().toString() + " - exec time : " + String.valueOf(timePv) + "ms");
                long timeSp = sp.exec();
                Utils.print(0, sp.getClass().toString() + " - exec time : " + String.valueOf(timeSp) + "ms");
                csvBuf += String.format("%s,%s,%s\n", s, String.valueOf(timeSp), String.valueOf(timePv));
            }catch(Exception e) {
                Utils.print(-1, "Exception while parsing command " + s + " :" + e.getMessage().toString() + ", escaped!");
                continue;
            }
            
            //System.out.println("--------------------");
        }
        Utils.writeStringToFile("result.csv", csvBuf);
        long endTime = System.currentTimeMillis();
        System.out.println("--------------------");
        Utils.print("INFO", "Program exits with running time " + String.valueOf(endTime - startTime) + "ms");
    }
}