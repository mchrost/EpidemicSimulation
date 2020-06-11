package Agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

import javax.swing.*;

public class Person2 extends Agent {

    protected void setup() {
        addBehaviour(new CyclicBehaviour(){
            @Override
            public void action() {

                ACLMessage msg = receive();
                if (msg!= null){
                    JOptionPane.showMessageDialog(null, "Message received "
                            +msg.getContent());
                }else block();
            }
        });

    }


}
