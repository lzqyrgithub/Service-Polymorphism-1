//package com.example.jni222;
//
//
//import android.content.Context;
//import android.icu.text.LocaleDisplayNames;
//import android.util.Log;
//
//import java.io.BufferedReader;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//public class Service{
//
//    String id;
//    //cost, latency, reliability requirements;
//    double cost;
//    double latency;
//    double reliability;
//    double cWeight = 2.73;
//    double lWeight = 2.73;
//    double rWeight = 1.05;
//    String input;
//    String output;
//    public Microservice[] microservice;
//    public List<EquivalentMS> branches = new ArrayList<EquivalentMS>();
//    List<ExecPlan> executionPlans = new ArrayList<ExecPlan>();
////    executionStatus es = new executionStatus();
//
//    private Context mContext;
//
//
//    public Service(String id, double cost, double latency, double reliability, String input, String output, Microservice[] ms,
//                   Context mContext) {
//        this.id = id;
//        this.cost = cost;
//        this.latency = latency;
//        this.reliability = reliability;
//        this.input = input;
//        this.output = output;
//        this.microservice = ms;
//        if(!this.calculateEqu()) { // here the branches is added
//            System.out.println("Error: The MS Script is Wrong");
//            System.exit(-1);
//        }
////        if(!this.getEquQoS()) {
////            System.out.println("Error: Fail to read QoS from Database!!");
////            System.exit(-1);
////        }
//
//        this.mContext = mContext;
//    }
//
//    // each qos of ms should be passed in a way of parameters
//    public boolean updateBranchQoS(List<MsInfo> msInfo) {
////        es.updateQoS();
////        this.getEquQoS();
//
//        Log.d("branches size: ", String.valueOf(branches.size()));
//        int msId = 0;
//        for(EquivalentMS branch:branches) {
//            branch.cost = 0.0;
//            branch.reliability = 100.0;
//            branch.latency = 0.0;
//            if(branch.isComplete==false) {
//                return false;
//            }
//
//
//            for(Microservice ms:branch.microservices) {
////                MsInfo msQoS = es.getMSINFO(ms.id);
//
//                Log.d("ms.id", String.valueOf(msId));
//
//
////                assert (ms.id != null);
////
////                int msId = Integer.getInteger(ms.id);
//
//                MsInfo msQoS = msInfo.get(msId++);
//
//
////                ms.url = msQoS.url;
//                Log.d("ms.url", ms.url);
//                // change url from hostname to ip
//                Log.d("msQoS.hostname", msQoS.hostname);
//                Log.d("msQoS.IP", msQoS.IP);
//                String replaceStr = ms.url.replaceFirst(msQoS.hostname, msQoS.IP);
//
//                Log.d("replaceStr url", replaceStr);
//
//                ms.url = replaceStr;
//
//                Log.d("new ms.url", ms.url);
//
//                ms.cost = msQoS.cost;
//
//
//                Log.d("cost: ", String.valueOf(msQoS.cost));
//                Log.d("latency: ", String.valueOf(msQoS.latency));
//                Log.d("reliability: ", String.valueOf(msQoS.reliability));
//
//                branch.cost += msQoS.cost * branch.reliability/100;
//                branch.latency += msQoS.latency*branch.reliability/100;
//                branch.reliability = branch.reliability * msQoS.reliability/100;
////
//
////                Log.d("ms.id", ms.id);
//
//
//
//
//            }
//
//
////            for (int i = 0; i < branch.microservices.size(); i++){
////
////            }
//
//
//
//        }
//        return true;
//    }
//
////    boolean getEquQoS() {
////        for(EquivalentMS branch:branches) {
////            branch.cost = 0.0;
////            branch.reliability = 100.0;
////            branch.latency = 0.0;
////            if(branch.isComplete==false) {
////                return false;
////            }
////            for(Microservice ms:branch.microservices) {
////                MsInfo msQoS = es.getMSINFO(ms.id);
////                ms.url = msQoS.url;
////                ms.cost = msQoS.cost;
////
////                branch.cost += msQoS.cost * branch.reliability/100;
////                branch.latency += msQoS.latency*branch.reliability/100;
////                branch.reliability = branch.reliability * msQoS.reliability/100;
////            }
////
////
////
////
////
////        }
////        return true;
////    }
//
////    void updateBranchQoS() {
//////        es.updateQoS();
//////        this.getEquQoS();
////    }
//    String greedySearch() {
//        List<Integer> sortedBranchIDs = BordaSortBranchID();
//        String executionPlan = ""+sortedBranchIDs.get(0);
//        EquivalentMS initBranch = branches.get(sortedBranchIDs.get(0));
//        double qSI = this.QoSSatisfactionIndex(initBranch.cost, initBranch.latency, initBranch.reliability);
//        for(int i=1;i<sortedBranchIDs.size();i++) {
//            String seq = executionPlan+"-"+sortedBranchIDs.get(i);
//            String par = "("+executionPlan+")*"+sortedBranchIDs.get(i);
//            PlanQoS qSeq = this.CalculateQoS(this.generatePlanFromStr(seq));
//            PlanQoS qPar = this.CalculateQoS(this.generatePlanFromStr(par));
//            double SISeq = this.QoSSatisfactionIndex(qSeq.cost, qSeq.latency, qSeq.reliability);
//            double SIPar = this.QoSSatisfactionIndex(qPar.cost, qPar.latency, qPar.reliability);
//            if(qSI>=SISeq && qSI>=SIPar) {
//                return executionPlan;
//            }else {
//                if(SISeq>SIPar) {
//                    executionPlan = executionPlan+"-"+sortedBranchIDs.get(i);
//                    qSI = SISeq;
//                }else {
//                    executionPlan = "("+executionPlan+")*"+sortedBranchIDs.get(i);
//                    qSI = SIPar;
//                }
//            }
//        }
//        return executionPlan;
//
//    }
//
//    @SuppressWarnings({ "unchecked", "rawtypes" })
//    List<Integer> BordaSortBranchID() {
//        // branch ID + score
//        Map<Integer, Integer> branchScore = new HashMap<Integer, Integer>();
//        for(int i=0; i<branches.size();i++) {
//            branchScore.put(i, 0);
//        }
//        ArrayList<EquivalentMS> branchSorted = (ArrayList<EquivalentMS>) branches;
//        Comparator<EquivalentMS> compareByCost =
//                (EquivalentMS o1, EquivalentMS o2) -> Double.compare(o1.cost, o2.cost);
//        Comparator<EquivalentMS> compareByLatency =
//                (EquivalentMS o1, EquivalentMS o2) -> Double.compare(o1.latency, o2.latency);
//        Comparator<EquivalentMS> compareByReliability =
//                (EquivalentMS o1, EquivalentMS o2) -> Double.compare(o2.reliability, o1.reliability);
//
//        Collections.sort(branchSorted, compareByCost);
//        for(int i=0; i<branchSorted.size();i++) {
//            branchScore.replace(branchSorted.get(i).id, branchScore.get(branchSorted.get(i).id)+i);
//        }
//
//        Collections.sort(branchSorted, compareByLatency);
//        for(int i=0; i<branchSorted.size();i++) {
//            branchScore.replace(branchSorted.get(i).id, branchScore.get(branchSorted.get(i).id)+i);
//        }
//
//        Collections.sort(branchSorted, compareByReliability);
//        for(int i=0; i<branchSorted.size();i++) {
//            branchScore.replace(branchSorted.get(i).id, branchScore.get(branchSorted.get(i).id)+i);
//        }
//
//        Map<Integer, Integer> sortedMap = branchScore.entrySet().stream().sorted(Map.Entry.comparingByValue())
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,(e1, e2) -> e1, LinkedHashMap::new));
//        return new ArrayList(sortedMap.keySet());
//    }
//
//    public ExecPlan ExhausiveSearch() {
//        ExecPlan SelectedPlan = null;
//        if(!generatePlans()) {
//            System.out.println("Error: too many equBranches, cannot perform Exhausive Search");
//            System.exit(-1);
//        }
////		double utility = -9999;
//        double utility = Double.NEGATIVE_INFINITY;
//        double utilityTotal = 0;
//        for(ExecPlan p: executionPlans) {
//            PlanQoS pq= CalculateQoS(p);
//            double QoSSI = QoSSatisfactionIndex(pq.cost,pq.latency,pq.reliability);
//            //System.out.println("Plan" + p + "cost" + pq.cost + "latency" + pq.latency + "reliability" + pq.reliability );
//            utilityTotal += QoSSI;
//            if(QoSSI > utility) {
//                SelectedPlan = p;
//                utility = QoSSI;
//            }
//        }
//        //System.out.println("Average Utility: " + utilityTotal/executionPlans.size() + "; Selected Plan Utility" + utility );
//        return SelectedPlan;
//
//    }
//
//    public PlanQoS CalculateQoS(ExecPlan plan) {
//        double[] costAndReliability = new double[2];
//        costAndReliability = plan.estCostandReliability();
//
////		System.out.println("CalQOS_cost:"+ costAndReliability[0]);
////		System.out.println("CalQOS_reliability:"+ costAndReliability[1]);
//
//        HashMap<EquivalentMS, Double> branchLatency = plan.estLatency();
//
//        Map<EquivalentMS, Double> sortedMap = branchLatency.entrySet().stream().sorted(Map.Entry.comparingByValue())
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,(e1, e2) -> e1, LinkedHashMap::new));
//        List<Map.Entry<EquivalentMS,Double>> entryList = new ArrayList<Map.Entry<EquivalentMS, Double>>(sortedMap.entrySet());
////		for(int i = 0; i<entryList.size();i++) {
////			System.out.println("Branch:" + entryList.get(i).getKey().microservices.get(0).id + ", lat:"+entryList.get(i).getValue());
////		}
//        double lat = 0.0;
//        if(branchLatency.size()==1) {
//            lat = entryList.get(0).getValue();
//        }else {
//            //more than 1 equivalent branches:
//            EquivalentMS bTmp = (EquivalentMS)entryList.get(0).getKey();
//            lat += entryList.get(0).getValue() * bTmp.reliability / 100;
//            if(entryList.size()>2) {
//                //this procedure can be optimized later!!!
//                for(int i=1; i<entryList.size()-1;i++) {
//                    double tempR = 1.0;
//                    for(int j =0; j<i;j++) {
//                        tempR = tempR*(100 - entryList.get(j).getKey().reliability)/100;
//                    }
//                    lat += tempR * entryList.get(i).getValue() * entryList.get(i).getKey().reliability/100;
//                }
//            }
//            double tempR = 1.0;
//            for(int j =0; j<entryList.size()-1;j++) {
//                tempR = tempR*(100 - entryList.get(j).getKey().reliability)/100;
//            }
//            lat += tempR * entryList.get(entryList.size()-1).getValue();
//        }
//
//        PlanQoS planQoS = new PlanQoS(plan.toString(), costAndReliability[0], lat, costAndReliability[1]);
//        return planQoS;
//    }
//
//
//    public ExecPlan generatePlanFromStr(String epStr) {
//        //System.out.println("epStr"+epStr);
//        //epStr = epStr.trim();
//        char[] exc = tools.toCharArrayTrimOutParenthes(epStr);
//        if (!tools.hasOperation(exc)) {
//            //System.out.println("epStr:"+epStr+" has no operation code");
//            int branchID = Character.getNumericValue(exc[0]);
//            //System.out.println("branchID"+branchID);
//            return new Leaf(branches.get(branchID));
//        }else {
//            //System.out.println("epStr:"+epStr+" has operation code");
//            int parenthes = 0;
//            int index = 0;
//            List<Integer> paralIndexs = new ArrayList<Integer>();
//            for(int i=exc.length-1;i>=0;i--) {
//                if (exc[i] == '(') parenthes --;
//                else if (exc[i]==')') parenthes ++;
//                if(parenthes ==0) {
//                    if(exc[i]=='*') {
//                        index = i;
//                        paralIndexs.add(0,i);
//                    }else if (exc[i]=='-') {
//                        index = i;
//                        break;
//                    }
//                }
//            }
//            if (exc[index]=='-'){
//                //operation is "-"
//                StringBuilder left = new StringBuilder();
//                StringBuilder right = new StringBuilder();
//                for (int i = index + 1; i < exc.length; i++) {
//                    right.append(exc[i]);
//                }
//                for (int i = 0; i < index; i++) {
//                    left.append(exc[i]);
//                }
//                ExecPlan leftTree = generatePlanFromStr(left.toString());
//                ExecPlan rightTree = generatePlanFromStr(right.toString());
//                return new Seq(leftTree, rightTree);
//            }else if (exc[index]=='*') {
//                //println(paralIndexs)
//                List<ExecPlan> children = new ArrayList<ExecPlan>();
//                StringBuilder start = new StringBuilder();
//                StringBuilder end = new StringBuilder();
//                for(int i=0; i<paralIndexs.get(0);i++) {
//                    start.append(exc[i]);
//                }
//                for(int i=paralIndexs.get(paralIndexs.size()-1)+1;i<exc.length;i++) {
//                    end.append(exc[i]);
//                }
//
//                children.add(generatePlanFromStr(start.toString()));
//                children.add(generatePlanFromStr(end.toString()));
//
//                //System.out.println("start"+start.toString());
//                //System.out.println("end"+end.toString());
//
//                //System.out.println("paralIndexs"+paralIndexs);
//                if(paralIndexs.size()>1){
//                    for(int i=0; i<paralIndexs.size()-1;i++){
//                        StringBuilder childStr = new StringBuilder();
//                        for(int j = paralIndexs.get(i)+1; j< paralIndexs.get(i+1);j++) {
//                            childStr.append(exc[j]);
//                        }
//                        //System.out.println("childstr"+childStr.toString());
//                        children.add(generatePlanFromStr(childStr.toString()));
//                    }
//                }
//                return new Par(children);
//            }
//        }
//        return null;
//    }
//
//    /*
//     * after processIO and calculateEqu, generate all execPlan (trees), and fill into executionPlans
//     */
//    boolean generatePlans() {
//        if(branches.size()<=6) {
////            InputStream in = new test().getClass().getClassLoader().getResourceAsStream(branches.size()+".equ");
//
//            InputStream in = null;
//            try {
//                assert (mContext != null);
////                in = mContext.getAssets().open(branches.size()+".equ");
//                in = mContext.getAssets().open("strategies/"+branches.size()+".equ");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//            String line = null;
//            try {
//                while ( (line = reader.readLine()) != null) {
//                    // do something with the line here
//                    ExecPlan p = generatePlanFromStr(line.replace(" ", ""));
//                    executionPlans.add(p);
//                }
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            return true;
//        }else {
//            return false;
//        }
//    }
//
//
//    /* input QoS (c, l, r)
//     * output QoS Satisfaction Index
//     */
//    double QoSSatisfactionIndex(double c, double l, double r) {
//        double SI = 0.0;
//
//        if(this.cost!=0) {
//            if(c>this.cost) {
//                SI += cWeight-Math.pow(cWeight, c/cost);
//            }else {
//                SI += Math.log(cost/c)/Math.log(cWeight);
//            }
//        }
//        if(this.latency!=0) {
//            if(l>this.latency) {
//                SI += lWeight-Math.pow(lWeight, l/latency);
//            }else {
//                SI += Math.log(latency/l)/Math.log(lWeight);
//            }
//        }
//        if(this.reliability!=0) {
//            if(r>this.reliability) {
//                SI += rWeight-Math.pow(rWeight, reliability/r);
//            }else {
//                SI += Math.log(r/reliability)/Math.log(rWeight);
//            }
//        }
//        return SI;
//    }
//
////    boolean getEquQoS() {
////        for(EquivalentMS branch:branches) {
////            branch.cost = 0.0;
////            branch.reliability = 100.0;
////            branch.latency = 0.0;
////            if(branch.isComplete==false) {
////                return false;
////            }
////            for(Microservice ms:branch.microservices) {
////                MsInfo msQoS = es.getMSINFO(ms.id);
////                ms.url = msQoS.url;
////                ms.cost = msQoS.cost;
////
////                branch.cost += msQoS.cost * branch.reliability/100;
////                branch.latency += msQoS.latency*branch.reliability/100;
////                branch.reliability = branch.reliability * msQoS.reliability/100;
////            }
////        }
////        return true;
////    }
//
//    /*
//     * calculate branches from microservices
//     * note: the input of microservice should follow a certain sequence
//     * output false happens only when a branch is not complete, which means that a microservice is missing.
//     */
//    boolean calculateEqu() {
//        for(Microservice ms:microservice) {
//            if(ms.MSInput.equals(this.input) && ms.MSOutput.equals(this.output)) {
//                branches.add(new EquivalentMS(ms));
//            }else {
//                // a sequence of microservices.
//                if(ms.MSInput.equals(this.input)) {
//                    branches.add(new EquivalentMS(ms,false));
//                }else {
//                    if(ms.MSOutput.equals(this.output)) {
//                        for(EquivalentMS branch:branches) {
//                            if(branch.isComplete)continue;
//                            else {
//                                //System.out.println(branch.microservices.get(branch.microservices.size()-1).MSOutput );
//                                if(branch.microservices.get(branch.microservices.size()-1).MSOutput.equals(ms.MSInput) ) {
//                                    //System.out.println("test");
//                                    branch.add(ms);
//                                    if(branch.microservices.get(branch.microservices.size()-1).MSOutput.equals(this.output)) {
//                                        branch.setComplete();
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        int i = 0;
//        // if any branch is not complete.
//        for(EquivalentMS branch:branches) {
//            if(branch.isComplete==false) {
//                return false;
//            }else {
//                branch.setID(i);
//                i++;
//            }
//        }
//        return true;
//    }
//}
//
//
