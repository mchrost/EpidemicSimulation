package Agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

import javax.swing.*;

public class Person extends Agent {

    protected void setup() {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent("Send");
                msg.addReceiver(new AID("second", AID.ISLOCALNAME));
                send(msg);
            }
        });

    }


}


