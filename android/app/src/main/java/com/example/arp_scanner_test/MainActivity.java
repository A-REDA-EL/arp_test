package com.example.arp_scanner_test;

import androidx.annotation.NonNull;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.flutter.Log;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;


public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "arp.flutter.dev/callArpTable";
    private static final String LOG_TAG = "ANDROID LOG:";

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            if (call.method.equals("getFileArp")) {
                                String value = getMacAddress();
                                result.success("Scanned");
                            } else {
                                result.notImplemented();
                            }
                        }
                );
    }

    protected String getMacAddress() {


        try {
            Process execWithBor = Runtime.getRuntime().exec("ip neighbor");
            Process execWithoutBor = Runtime.getRuntime().exec("ip neigh");

            Log.i("fing:arp-proc", "Reading ARP table from '/proc/net/arp'");
//            FileInputStream fileInputStream = new FileInputStream(new File("/proc/net/arp"));
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream), 4096);
            InputStreamReader reader = new InputStreamReader(execWithBor.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(reader);

//            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
            return bufferedReader.readLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Couldn't read it";
    }

    private static final int NB_THREADS = 10;

    public void doScan() {
        Log.i(LOG_TAG, "Start scanning");

        ExecutorService executor = Executors.newFixedThreadPool(NB_THREADS);
        for(int dest=0; dest<255; dest++) {
            String host = "192.168.1." + dest;
            executor.execute(pingRunnable(host));
        }

        Log.i(LOG_TAG, "Waiting for executor to terminate...");
        executor.shutdown();
        try { executor.awaitTermination(60*1000, TimeUnit.MILLISECONDS); } catch (InterruptedException ignored) { }

        Log.i(LOG_TAG, "Scan finished");
    }

    private Runnable pingRunnable(final String host) {
        return new Runnable() {
            public void run() {
                Log.d(LOG_TAG, "Pinging " + host + "...");
                try {
                    InetAddress inet = InetAddress.getByName(host);
                    boolean reachable = inet.isReachable(1000);
                    Log.d(LOG_TAG, "=> Result: " + (reachable ? "reachable" : "not reachable"));
                } catch (UnknownHostException e) {
                    Log.e(LOG_TAG, "Not found", e);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "IO Error", e);
                }
            }
        };
    }

}