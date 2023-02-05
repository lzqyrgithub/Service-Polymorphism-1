package com.example.jni222;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

enum ExecPattern
{
    //Microservice, Sequential, Speculative Parallel
    Preselected, CostOpt, Balance, LatencyOpt;
}

public class PolymorphicWebService {

    public List<Double> combined_lat_list = new ArrayList<>();

    public Context context;

    public String execPatern;
    public ExecPlan execStrategy;

    public String strategy;

    public String result;

    public String defaultStrategy;

    PloymorphicInvocationRes rep_exec;

    int probe_flag = 0;
    int default_flag = 0;

    public List<EqvService> eqvServiceList = new ArrayList<EqvService>();

    public Map<String, EqvService> eqvServiceMap = new LinkedHashMap<String, EqvService>();

    public Map<Integer, String> eqvServiceNameMap = new LinkedHashMap<Integer, String>();

//    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public static List<PloymorphicInvocationRes> rep_exec_list = new ArrayList<>();

    public static Map<String, PloymorphicInvocationRes> res_success_list = new HashMap<>();

    public int dns_time;

    public float dns_time_c;

    public int status_code;

    static {
        System.loadLibrary("jni222");
    }

    /**
     * A native method that is implemented by the 'jni222' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();


    public static native int initialize_native(ConnectivityManager connectivity_manager);

    //    private static native int getipbyhostname(String hostname);
    public static native String getipbyhostname(String hostname);


    public static native String getiphyhostname_our(String hostname);

    public static native String dnsQueryQoS(String hostnames);

    public static native String dnsQueryQoS_single(String hostnames);



    public static native void ReportQoS(String hash_url, int latency);

//    public static native void dnsQueryQoS_null();

    public static void initialize(Context context) {
//        Log.d("AsyncDnsQos myTag", "begin initialize***************************************");

        initialize_native((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));

//        Log.d("AsyncDnsQos initialize_native res:", String.valueOf(res));

//        Log.d("myTag", "end initialize***************************************");
    }



    public PolymorphicWebService(Context context){
        this.context = context;
    }

//    public void addEqvFromMapToList(){
//        for (Map.Entry<String, EqvService> item : this.eqvServiceMap.entrySet()){
//            this.eqvServiceList.add(item.getValue());
//        }
//    }

    public void addEqvService(EqvService... eqv_services){
        int id = 0;
        for (EqvService eqv : eqv_services){
            this.eqvServiceNameMap.put(id, eqv.host_name);
            eqv.setWsID(id++);
            this.eqvServiceMap.put(eqv.host_name, eqv);
        }

    }

    // which will be used when we issue the requests to a unmodified dns server but we still set the LatOpt pattern
    // then we just exe the default service
    public void setDefaultStrategy(String str){
        this.defaultStrategy = str;
    }

    // e.g., Preselected, A-B-C
    public void setPattern(String... args){
        if (args.length == 1){
            this.execPatern = args[0];
        } else {
            this.execPatern = args[0];
            this.strategy = args[1];
        }
    }


    public ExecPlan generatePlanFromStr(String epStr) {
        //System.out.println("epStr"+epStr);
        //epStr = epStr.trim();
        char[] exc = tools.toCharArrayTrimOutParenthes(epStr);
        if (!tools.hasOperation(exc)) {
            //System.out.println("epStr:"+epStr+" has no operation code");
            int branchID = Character.getNumericValue(exc[0]);
            //System.out.println("branchID"+branchID);
            return new Leaf(this.eqvServiceMap.get(this.eqvServiceNameMap.get(branchID)));
        }else {
            //System.out.println("epStr:"+epStr+" has operation code");
            int parenthes = 0;
            int index = 0;
            List<Integer> paralIndexs = new ArrayList<Integer>();
            for(int i=exc.length-1;i>=0;i--) {
                if(exc[i]=='*') {
                    index = i;
                    paralIndexs.add(0,i);
                }
            }
            if (exc[index]=='*') {
                //println(paralIndexs)
                List<ExecPlan> children = new ArrayList<ExecPlan>();
                StringBuilder start = new StringBuilder();
                StringBuilder end = new StringBuilder();
                for(int i=0; i<paralIndexs.get(0);i++) {
                    start.append(exc[i]);
                }
                for(int i=paralIndexs.get(paralIndexs.size()-1)+1;i<exc.length;i++) {
                    end.append(exc[i]);
                }

                children.add(generatePlanFromStr(start.toString()));
                children.add(generatePlanFromStr(end.toString()));

                //System.out.println("start"+start.toString());
                //System.out.println("end"+end.toString());

                //System.out.println("paralIndexs"+paralIndexs);
                if(paralIndexs.size()>1){
                    for(int i=0; i<paralIndexs.size()-1;i++){
                        StringBuilder childStr = new StringBuilder();
                        for(int j = paralIndexs.get(i)+1; j< paralIndexs.get(i+1);j++) {
                            childStr.append(exc[j]);
                        }
                        //System.out.println("childstr"+childStr.toString());
                        children.add(generatePlanFromStr(childStr.toString()));
                    }
                }
                return new Par(children);
            }
        }
        return null;
    }


    public void sendLatency(int latency, String strategy, List<Double> combined_lat_list) {
        int port = 20001;
        String ip = "192.168.1.120";

        Log.d("", "************************sendLatency*********************************");

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    DatagramSocket udpSocket = new DatagramSocket(port);
                    InetAddress serverAddr = InetAddress.getByName(ip);

                    String lat = String.valueOf(latency);


                    if (!strategy.equals("") && combined_lat_list.size() > 0){

                        String comb_lat1 = String.valueOf(combined_lat_list.get(0).intValue());
                        String comb_lat2 = String.valueOf(combined_lat_list.get(1).intValue());
                        String comb_lat3 = String.valueOf(combined_lat_list.get(2).intValue());
                        String comb_lat4 = String.valueOf(combined_lat_list.get(3).intValue());

                        String data = lat + "," + strategy + "," + comb_lat1 + "," + comb_lat2 + "," + comb_lat3 + "," + comb_lat4;

                        byte[] buf = data.getBytes();
                        DatagramPacket packet = new DatagramPacket(buf, buf.length,serverAddr, port);
                        udpSocket.send(packet);

                        Log.d("", "************************finish sendLatency*********************************");

                    } else{
                        byte[] buf = lat.getBytes();
                        DatagramPacket packet = new DatagramPacket(buf, buf.length,serverAddr, port);
                        udpSocket.send(packet);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

 


    public String md5Str(String plaintext) throws NoSuchAlgorithmException {

//        String plaintext = "your text here";

        MessageDigest m = MessageDigest.getInstance("MD5");
        m.reset();
        m.update(plaintext.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        String hashtext = bigInt.toString(16);
// Now we need to zero pad it if you actually want the full 32 chars.
        while(hashtext.length() < 32 ){
            hashtext = "0"+hashtext;
        }

        return hashtext;
    }

    public void issueDNSQuery2() throws NoSuchAlgorithmException {




        StringBuilder urls_buf = new StringBuilder();
//        int i = 0;
        for (Map.Entry<String, EqvService> item : this.eqvServiceMap.entrySet()){

//            urls_buf.append(hash_urls[i] + item.getValue().host_name);
            urls_buf.append(md5Str(item.getValue().dns_url).toUpperCase() + item.getValue().host_name);


            Log.d("urls_buf: ", md5Str(item.getValue().dns_url));

            urls_buf.append(";");
//            i++;
        }

        urls_buf.setLength(urls_buf.length() - 1);



        String urls_str = urls_buf.toString();



        long startTime = System.nanoTime();

        initialize(this.context);
        String dns_ip_qos_result = dnsQueryQoS(urls_str);


        long endTime = System.nanoTime();
        long timeElapsed = (endTime - startTime) / 1000000;


        this.dns_time = (int) timeElapsed;



//        assert (0 == 1);

        String[] ws = dns_ip_qos_result.split("\\|");


//        String c_dns_time = ws[ws.length - 1];


//        sendLatency(c_dns_time, "", null);

        for (String s : ws){
//            Log.d("ws", s);

            String[] items = s.split("\\$");

            if (items.length == 4){ // dns response with qos response
                String hostname = items[0];
                String ip = items[1];
                String[] qos = items[2].split(",");
                double loc = Double.parseDouble(qos[0]);
                double lambda = Double.parseDouble(qos[1]);
                String hash_url = items[3];

//            Log.d("hostname: ", hostname);
//            Log.d("ip: ", ip);
//            Log.d("loc: ", String.valueOf(loc));
//            Log.d("lambda: ", String.valueOf(lambda));
//            Log.d("hash_url: ", hash_url);


                EqvService eqv_ws = eqvServiceMap.get(hostname);
                String eqv_ws_url = eqv_ws.service_url.replaceFirst(hostname, ip);
                eqv_ws.resetExecParam(eqv_ws_url, loc, lambda, hash_url);
            } else if (items.length == 3){ // dns response without qos response
                String hostname = items[0];
                String ip = items[1];
                String hash_url = items[2];
                EqvService eqv_ws = eqvServiceMap.get(hostname);
                String eqv_ws_url = eqv_ws.service_url.replaceFirst(hostname, ip);
                eqv_ws.resetExecParam(eqv_ws_url, 0, 0, hash_url);

                this.probe_flag++; // as long as there is service without qos, we probe
            } else if (items.length == 2){ // dns response from a unmodified dns server
                String hostname = items[0];
                String ip = items[1];
                EqvService eqv_ws = eqvServiceMap.get(hostname);
                String eqv_ws_url = eqv_ws.service_url.replaceFirst(hostname, ip);
                eqv_ws.resetExecParam(eqv_ws_url, 0, 0, "");
                this.default_flag++;





            }

        }

        // copy eqvServiceMap to eqvServiceList, it is good for computing strategy
        for (Map.Entry<String, EqvService> item : this.eqvServiceMap.entrySet()){
            eqvServiceList.add(item.getValue());
        }

    }

    public void sendLatency(String latency, String strategy, List<Double> combined_lat_list) {
        int port = 20001;
        String ip = "192.168.1.120";

        Log.d("", "************************sendLatency*********************************");

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    DatagramSocket udpSocket = new DatagramSocket(port);
                    InetAddress serverAddr = InetAddress.getByName(ip);

//                    String lat = String.valueOf(latency);
                    String lat = latency;


                    if (!strategy.equals("") && combined_lat_list.size() > 0){

                        String comb_lat1 = String.valueOf(combined_lat_list.get(0).intValue());
                        String comb_lat2 = String.valueOf(combined_lat_list.get(1).intValue());
                        String comb_lat3 = String.valueOf(combined_lat_list.get(2).intValue());
                        String comb_lat4 = String.valueOf(combined_lat_list.get(3).intValue());

                        String data = lat + "," + strategy + "," + comb_lat1 + "," + comb_lat2 + "," + comb_lat3 + "," + comb_lat4;

                        byte[] buf = data.getBytes();
                        DatagramPacket packet = new DatagramPacket(buf, buf.length,serverAddr, port);
                        udpSocket.send(packet);

                        Log.d("", "************************finish sendLatency*********************************");

                    } else{
                        byte[] buf = lat.getBytes();
                        DatagramPacket packet = new DatagramPacket(buf, buf.length,serverAddr, port);
                        udpSocket.send(packet);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static int indexOfSmallest(List<Double>  array){

        if (array.size() == 0)
            return -1;

        int index = 0;
        Double min = array.get(index);

        for (int i = 1; i < array.size(); i++){
            if (array.get(i) < min){
                min = array.get(i);
                index = i;
            }
        }
        return index;
    }

    public ExecPlan calculate_Balance(){
        List<Double> lat = new ArrayList<Double>();
        for (Map.Entry<String, EqvService> item : this.eqvServiceMap.entrySet()){
            Double avg_latency = item.getValue().loc + item.getValue().lambda;
            lat.add(avg_latency);
        }
        int idx = indexOfSmallest(lat);

        return generatePlanFromStr(Integer.toString(idx)); // choose the service with minimum average latency
    }


    public double l_c_metric(double lat, double tail, double cost){
        int a = 10;
        int b = 1;

        double res = Math.pow(lat, a) * Math.pow(cost, b) * Math.pow(tail, a);

        Log.d("lat, tail, cost, l_c_metric_res: ", String.valueOf(lat) + ", " + tail + ", " + String.valueOf(cost)  + ", " + String.valueOf(res));

        return Math.pow(lat, a) * Math.pow(cost, b);
    }


    public Double estimateLatency(int[] service_arr){
        double res = 0;
        if (service_arr.length == 2){
            double loc1 = this.eqvServiceList.get(service_arr[0]).loc;
            double lambda1 = this.eqvServiceList.get(service_arr[0]).lambda;
            double loc2 = this.eqvServiceList.get(service_arr[1]).loc;
            double lambda2 = this.eqvServiceList.get(service_arr[1]).lambda;

            double l1 = loc1;
            double k1 = 1 / lambda1;
            double l2 = loc2;
            double k2 = 1/ lambda2;

            if (loc2 < loc1){
                l1 = loc2;
                k1 = 1/ lambda2;
                l2 = loc1;
                k2 = 1/ lambda1;
            }

            double res1 = (-(k1*Math.exp(k1*l1)*l2 + Math.exp(k1*l1)) * Math.exp(-k1*l2) + k1*l1 + 1) / k1;
            double res2 = ((k2+k1)*l2 + 1)*Math.exp(k1*l1-k1*l2) / (k2 + k1);
            res = res1 + res2;

        } else if (service_arr.length == 3){
            double loc1 = this.eqvServiceList.get(service_arr[0]).loc;
            double lambda1 = this.eqvServiceList.get(service_arr[0]).lambda;
            double loc2 = this.eqvServiceList.get(service_arr[1]).loc;
            double lambda2 = this.eqvServiceList.get(service_arr[1]).lambda;
            double loc3 = this.eqvServiceList.get(service_arr[2]).loc;
            double lambda3 = this.eqvServiceList.get(service_arr[2]).lambda;

            Map<Double, Double> l_k_map = new TreeMap<Double, Double>();
            l_k_map.put(loc1, lambda1);
            l_k_map.put(loc2, lambda2);
            l_k_map.put(loc3, lambda3);

            double[] loc_arr = new double[3];
            double[] lambda_arr = new double[3];
            int idx = 0;
            for (Map.Entry<Double, Double> item : l_k_map.entrySet()){
                loc_arr[idx] = item.getKey();
                lambda_arr[idx] = item.getValue();
                idx++;
            }
            double l1 = loc_arr[0];
            double l2 = loc_arr[1];
            double l3 = loc_arr[2];

            double k1 = 1 / lambda_arr[0];
            double k2 = 1 / lambda_arr[1];
            double k3 = 1 / lambda_arr[2];

            double res1 = (-(k1*Math.exp(k1*l1)*l2 + Math.exp(k1*l1)) * Math.exp(-k1*l2) + k1*l1 + 1) / k1;
            double res2 = ((((k2+k1)*l2+1)*Math.exp((k2+k1)*l3)-Math.exp((k2+k1)*l2)*((k2+k1)*l3+1))*Math.exp(-k2*l3-k1*l3-k1*l2+k1*l1))/(k2+k1);
            double res3 = (((k3+k2+k1)*l3+1)*Math.exp(-k2*l3-k1*l3+k2*l2+k1*l1))/(k3+k2+k1);
            res = res1 + res2 + res3;


        } else if(service_arr.length == 1){

            double loc1 = this.eqvServiceList.get(service_arr[0]).loc;
            double lambda1 = this.eqvServiceList.get(service_arr[0]).lambda;

            res = loc1 + 1/lambda1;

        }

        this.combined_lat_list.add(res);

        return res;
    }

    public static double findMin(double a, double b, double c) {
        return Math.min(Math.min(a, b), c);
    }

    public Double estimateTailLatency(int[] service_arr) {

        double res = 0;

        if (service_arr.length == 2){
            double loc1 = this.eqvServiceList.get(service_arr[0]).loc;
            double lambda1 = this.eqvServiceList.get(service_arr[0]).lambda;
            double loc2 = this.eqvServiceList.get(service_arr[1]).loc;
            double lambda2 = this.eqvServiceList.get(service_arr[1]).lambda;

            double l1 = loc1;
            double k1 = 1 / lambda1;
            double l2 = loc2;
            double k2 = 1/ lambda2;

            if (loc2 < loc1){
                l1 = loc2;
                k1 = 1/ lambda2;
                l2 = loc1;
                k2 = 1/ lambda1;
            }

            double P = 0.95;

            double x1 = ( Math.log(1-P) - k1*l1  ) / (-1 * k1);
            double x2 = (Math.log(1 - P) - (k1 * l1 + k2 * l2)) / (-1 * (k1 + k2));

            if (x1 < l2)
                res = x1;
            else
                res = x2;


        } else if (service_arr.length == 3){


            double loc1 = this.eqvServiceList.get(service_arr[0]).loc;
            double lam1 = this.eqvServiceList.get(service_arr[0]).lambda;
            double loc2 = this.eqvServiceList.get(service_arr[1]).loc;
            double lam2 = this.eqvServiceList.get(service_arr[1]).lambda;
            double loc3 = this.eqvServiceList.get(service_arr[2]).loc;
            double lam3 = this.eqvServiceList.get(service_arr[2]).lambda;

            double k1 = 1 / lam1;
            double k2 = 1 / lam2;
            double k3 = 1 / lam3;

            double l1 = loc1;
            double l2 = loc2;
            double l3 = loc3;


            List<Double> list_l = new ArrayList<>();
            List<Double> list_k = new ArrayList<>();

            list_l.add(l1);
            list_l.add(l2);
            list_l.add(l3);

            list_k.add(k1);
            list_k.add(k2);
            list_k.add(k3);

            int minIndex = list_l.indexOf(Collections.min(list_l));
            l1 = list_l.get(minIndex);
            k1 = list_k.get(minIndex);


            List<Double> list_l_2 = new ArrayList<>();
            List<Double> list_k_2 = new ArrayList<>();

            for (int i = 0; i < 3; i++){
                if (i != minIndex){
                    list_l_2.add(list_l.get(i));
                    list_k_2.add(list_k.get(i));
                }
            }

            int minIndex2 = list_l_2.indexOf(Collections.min(list_l_2));
            l2 = list_l_2.get(minIndex2);
            k2 = list_k_2.get(minIndex2);


            int minIndex3 = list_l.indexOf(Collections.max(list_l));
            l3 = list_l.get(minIndex3);
            k3 = list_k.get(minIndex3);



            double P = 0.95;

            double x1 = ( Math.log(1-P) - k1*l1  ) / (-1 * k1);
            if (x1 < l2)
                res = x1;
            else{
                double x2 = ( Math.log(1-P) - (k1*l1+k2*l2) ) / ( -1 * (k1+k2) );
                if (x2 < l3)
                    res = x2;
                else{
                    double x3 = ( Math.log(1-P) - (k1*l1+k2*l2+k3*l3) ) / ( -1 * (k1+k2+k3) );
                    res = x3;
                }

            }


        } else if(service_arr.length == 1){



            double loc1 = this.eqvServiceList.get(service_arr[0]).loc;
            double lam1 = this.eqvServiceList.get(service_arr[0]).lambda;
            double P = 0.95;
            double k1 = 1/lam1;
            double l1 = loc1;


            res = ( Math.log(1-P) - k1*l1  ) / (-1 * k1);


        }



        return res;


    }

    // Method for getting the minimum value
    public double getMin(double[] inputArray){
        double minValue = inputArray[0];
        for(int i=1;i<inputArray.length;i++){
            if(inputArray[i] < minValue){
                minValue = inputArray[i];
            }
        }
        return minValue;
    }

    //    public ExecPlan calculate_Combination(){
    public String calculate_Combination_lat_opt(){
        // we assume there are 3 web services, but we also can generalize this implementation to n services in the future
        Map<Double, String> services_latency_map1 = new TreeMap<>();
        Map<Double, String> services_latency_map2 = new TreeMap<>();
//        Map<Double, String> services_latency_map3 = new TreeMap<>();


        List<Double> avg_cost1 = new ArrayList<Double>();
        List<Double> avg_cost2 = new ArrayList<Double>();


        List<Double> tail_cost1 = new ArrayList<Double>();
        List<Double> tail_cost2 = new ArrayList<Double>();





        double avg_lat_0_1 = estimateLatency(new int[]{0, 1});
        double avg_lat_0_2 = estimateLatency(new int[]{0, 2});
        double avg_lat_1_2 = estimateLatency(new int[]{1, 2});


        Log.d("", "avg_lat_0_1: " + avg_lat_0_1);
        Log.d("", "avg_lat_0_2: " + avg_lat_0_2);
        Log.d("", "avg_lat_1_2: " + avg_lat_1_2);


        services_latency_map2.put(avg_lat_0_1, "01" );
        services_latency_map2.put(avg_lat_0_2, "02" );
        services_latency_map2.put(avg_lat_1_2, "12" );

        avg_cost2.add(avg_lat_0_1);
        avg_cost2.add(avg_lat_0_2);
        avg_cost2.add(avg_lat_1_2);



        tail_cost2.add(estimateTailLatency(new int[]{0, 1}));
        tail_cost2.add(estimateTailLatency(new int[]{0, 2}));
        tail_cost2.add(estimateTailLatency(new int[]{1, 2}));



//        services_latency_map3.put(best_lat, "012");

        double avg_lat_0 = estimateLatency(new int[]{0});
        double avg_lat_1 = estimateLatency(new int[]{1});
        double avg_lat_2 = estimateLatency(new int[]{2});


        Log.d("", "avg_lat_0: " + avg_lat_0);
        Log.d("", "avg_lat_1: " + avg_lat_1);
        Log.d("", "avg_lat_2: " + avg_lat_2);


        services_latency_map1.put( avg_lat_0, "0" );
        services_latency_map1.put( avg_lat_1, "1" );
        services_latency_map1.put( avg_lat_2, "2" );

        avg_cost1.add(avg_lat_0);
        avg_cost1.add(avg_lat_1);
        avg_cost1.add(avg_lat_2);

        tail_cost1.add(estimateTailLatency(new int[]{0}));
        tail_cost1.add(estimateTailLatency(new int[]{1}));
        tail_cost1.add(estimateTailLatency(new int[]{2}));




        double min_lat_s1 = services_latency_map1.entrySet().iterator().next().getKey();
        int idx_s1 = avg_cost1.indexOf(min_lat_s1);
        double min_lat_tail_s1 = tail_cost1.get(idx_s1);



        double min_lat_s2 = services_latency_map2.entrySet().iterator().next().getKey();
        int idx_s2 = avg_cost2.indexOf(min_lat_s2);
        double min_lat_tail_s2 = tail_cost2.get(idx_s2);



        double min_lat_s3 = estimateLatency(new int[]{0, 1, 2});
        Log.d("", "min_lat_s3: " + min_lat_s3);


        double min_lat_tail_s3 = estimateTailLatency(new int[]{0, 1, 2});




        double l_c_metric_1 = l_c_metric(min_lat_s1, min_lat_tail_s1, 1);
        double l_c_metric_2 = l_c_metric(min_lat_s2, min_lat_tail_s2, 2);
        double l_c_metric_3 = l_c_metric(min_lat_s3, min_lat_tail_s3, 3);

        double[] metrics_array = new double[]{l_c_metric_1, l_c_metric_2, l_c_metric_3};
        double min_metric = getMin(metrics_array);

        if (min_metric == l_c_metric_1){
            String strategy_1 = services_latency_map1.entrySet().iterator().next().getValue();
//            strategy_1 = "01";
            strategy_1 = "12";
            return strategy_1;
        } else if (min_metric == l_c_metric_2){
            String strategy_2 = services_latency_map2.entrySet().iterator().next().getValue();
//            strategy_2 = "01";
            strategy_2 = "12";
            return strategy_2;
        } else if (min_metric == l_c_metric_3){
            String strategy_3 = "012";
//            strategy_3 = "01";
            strategy_3 = "12";
            return strategy_3;
        }

        return "error";




    }


    public String calculate_Combination_cost_effecitive(){
        // we assume there are 3 web services, but we also can generalize this implementation to n services in the future
        Map<String, Double> services_latency_map = new LinkedHashMap<String, Double>();
        double best_lat = estimateLatency(new int[]{0, 1, 2});
        services_latency_map.put("01", estimateLatency(new int[]{0, 1}));
        services_latency_map.put("02", estimateLatency(new int[]{0, 2}));
        services_latency_map.put("12", estimateLatency(new int[]{1, 2}));
        services_latency_map.put("012", best_lat);

        String[] keys = {"01", "02", "12", "012"};

        Map<Double, String> cost_strategy_map = new TreeMap<Double, String>();

//        PriorityQueue<Double, String> cost_strategy_queue = new PriorityQueue<>();

        for(String key : keys) {
            Double latency = services_latency_map.get(key);
            Log.d(key, String.valueOf(latency));
            double cost = 0;
            if (key.length() == 2){
                double cost1 = this.eqvServiceList.get(key.charAt(0) - 48).cost;
                double cost2 = this.eqvServiceList.get(key.charAt(1) - 48).cost;
                cost = cost1 + cost2;
            } else { // ==3
                double cost1 = this.eqvServiceList.get(key.charAt(0) - 48).cost;
                double cost2 = this.eqvServiceList.get(key.charAt(1) - 48).cost;
                double cost3 = this.eqvServiceList.get(key.charAt(2) - 48).cost;
                cost = cost1 + cost2 + cost3;
            }
            Log.d("cost, latency: ", String.valueOf(cost) + ", " + String.valueOf(latency));
//            Log.d("cost * latency: ", String.valueOf(0.01*cost + 0.99*latency));

//            cost_strategy_map.put(0.01*cost + 0.99*latency, key);
            cost_strategy_map.put(latency * cost, key);
        }

        Map.Entry<Double, String> entry = cost_strategy_map.entrySet().iterator().next();
        return entry.getValue();

    }


    public String CalculateOptimizeStrategy(){

        if (this.probe_flag != 0)
//            return generatePlanFromStr("0*1*2");
            return "012";

        if (this.default_flag != 0)
            return this.defaultStrategy;

        ExecPlan strategy = null;
        switch (this.execPatern) {
            case "Preselected":
//                return this.execStrategy;
                return this.strategy;
            case "CostOpt":
                return "012";
//                return this.generatePlanFromStr("0*1*2");
            case "Balance":
//                return calculate_Balance();
                return "012";
            default: // "Lat_Opt"
                return calculate_Combination_lat_opt();
        }

    }


    public PloymorphicInvocationRes invokeWS() throws  InterruptedException {


        int size_ws = this.strategy.length();

        for (int i = 0; i < size_ws; i++){
            String idx_ser = this.strategy.charAt(i) + "";
            this.eqvServiceList.get(Character.getNumericValue(idx_ser.charAt(0))).start();
        }

        while (true){
            Thread.sleep(1);
//                Log.d("res_success_list.size(): ", String.valueOf(res_success_list.size()));
            if (res_success_list.size() > 0){ // once there is at least one response, we return the results to parse
                return res_success_list.entrySet().iterator().next().getValue();
            }
        }
    }


    public void exec() throws ExecutionException, InterruptedException, NoSuchAlgorithmException {
        // 1. first issue dns to obtain ip and qos
        if (!this.execPatern.equals("Preselected")){
            issueDNSQuery2();
        } else{
            for (Map.Entry<String, EqvService> item : this.eqvServiceMap.entrySet()){
                eqvServiceList.add(item.getValue());
            }
        }

//        // 2. optimize the invocation strategy
////        this.execStrategy = CalculateOptimizeStrategy();
//
        this.strategy = CalculateOptimizeStrategy();
////
//        Log.d("strategy: ", this.strategy);
//
        // 3. invoke and processOutput
        rep_exec = invokeWS();
    }

    public String[] GetRes(){

        this.status_code = Objects.requireNonNull(eqvServiceMap.get(rep_exec.host_name)).responseCode;
        return Objects.requireNonNull(eqvServiceMap.get(rep_exec.host_name)).execute_praseOutput();
    }


    public void sendLatency_each_indivual(int latency1, int latency2, int latency3, int code1, int code2, int code3) {
        int port = 20002;
        String ip = "192.168.1.121";

        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());

        Log.d("", "************************sendLatency*********************************");

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    DatagramSocket udpSocket = new DatagramSocket(port);
                    InetAddress serverAddr = InetAddress.getByName(ip);

//                    String lat = String.valueOf(latency);

                    String tmp = latency1 + ", " + latency2 + ", " + latency3 + "," + code1 + "," + code2 + "," + code3 + "," + timeStamp;
                    byte[] buf = tmp.getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length,serverAddr, port);
                    udpSocket.send(packet);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }



    public void ReportQoS() throws InterruptedException {

        Log.d("", "***************begin ReportQoS***************");
		
//        String str = this.strategy.replace("*", "");
        if (this.default_flag != 0)
            return;

        for (int i = 0; i < this.strategy.length(); i++){
            String idx_ser = this.strategy.charAt(i) + "";
            this.eqvServiceList.get(Character.getNumericValue(idx_ser.charAt(0))).join();
        }

        // Map<String, PloymorphicInvocationRes> res_success_list
        int[] lat = new int[3];
        int[] status = new int[3];

        int i = 0;
        for (Map.Entry<String, PloymorphicInvocationRes> item : res_success_list.entrySet()){
            ReportQoS(item.getValue().hash_url, (int)item.getValue().reporting_latency);
            lat[i] = (int)item.getValue().reporting_latency;
            status[i] = (int)item.getValue().response_code;
            i++;
        }
        Log.d("", "***************all qos reported***************");

        sendLatency_each_indivual(lat[0], lat[1], lat[2], status[0], status[1], status[2]);


    }

}
