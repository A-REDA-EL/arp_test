package com.example.arp_scanner_test;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "arp.flutter.dev/callArpTable";

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            if (call.method.equals("getFileArp")) {
                                String arpLine = getMacAddress();
                                result.success(arpLine);
                            } else {
                                result.notImplemented();
                            }
                        }
                );
    }

    protected String getMacAddress() {

        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while((line = br.readLine()) != null) {
                System.out.println(line);
            }
            return br.readLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Couldn't read it";
    }
}