package Agents;

import jade.core.Agent;

public class Person extends Agent {

    private int age;

    protected void setup() {
        System.out.println("Person-agent "+getAID().getName()+" is ready.");
    }
}
