package com.example.jni222;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.jni222.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class MainActivity extends AppCompatActivity {


    private ActivityMainBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String URL1 = "https://api.weatherstack.com/current?access_key=fd16d137c79bdcd765315117c5c0a930&query=Dearborn";
//        String URL2 = "https://api.weatherbit.io/v2.0/current?lat=35.7796&lon=-78.6382&key=a7acd8d596344f3f9893042529b25a8f&include=minutely";

        String URL2 = "";

//        String pText = "Hello MD5";
//        System.out.println(String.format(OUTPUT_FORMAT, "Input (string)", pText));
//        System.out.println(String.format(OUTPUT_FORMAT, "Input (length)", pText.length()));

//        byte[] md5InBytes1 = digest(URL1.getBytes(UTF_8));
//        System.out.println(String.format(OUTPUT_FORMAT, "MD5 (hex) ", bytesToHex(md5InBytes1)));
//        // fixed length, 16 bytes, 128 bits.
//        System.out.println(String.format(OUTPUT_FORMAT, "MD5 (length)", md5InBytes1.length));
//
//        byte[] md5InBytes2 = digest(URL2.getBytes(UTF_8));
//        System.out.println(String.format(OUTPUT_FORMAT, "MD5 (hex) ", bytesToHex(md5InBytes2)));
//        // fixed length, 16 bytes, 128 bits.
//        System.out.println(String.format(OUTPUT_FORMAT, "MD5 (length)", md5InBytes2.length));


//        String urls = URL1 + ";" + URL2;
        String urls = URL1;

        PolymorphicWebService.initialize(this.getApplicationContext());

//        String dns_ip_qos_result = AsyncDnsQos.dnsQueryQoS("www.google.com;www.facebook.com");
        String dns_ip_qos_result = PolymorphicWebService.dnsQueryQoS(urls);

        if (dns_ip_qos_result.equals("error")){
            Log.d("myapp3 dnsQueryQoS status:", "error");
        }

        Log.d("myapp3 android dns_ip_qos_result: ", String.valueOf(dns_ip_qos_result));

        Log.d("myapp3 current path: ", getApplicationContext().getFilesDir().getAbsolutePath());




//        binding = ActivityMainBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        // Example of a call to a native method
//        TextView tv = binding.sampleText;
//        tv.setText("hello c-ares library test");


    }


}