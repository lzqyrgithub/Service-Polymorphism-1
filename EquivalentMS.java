package com.example.jni222;


import java.util.ArrayList;
import java.util.List;

class EquivalentMS{
    int id;
    boolean isComplete;
    double cost = 0.0;
    double latency = 0.0;
    double reliability = 100.0;

    List<Microservice> microservices = new ArrayList<Microservice>();


    EquivalentMS(Microservice ms, boolean s){
        isComplete = s;
        microservices.add(ms);
    }

    EquivalentMS(Microservice ms){
        isComplete = true;
        microservices.add(ms);
    }

    void setComplete() {
        this.isComplete = true;
    }

    void add(Microservice ms) {
        this.microservices.add(ms);
    }

    void setID(int i) {
        this.id = i;
    }
}