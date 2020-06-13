package Agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class Human extends Agent {

    private boolean diseased;
    private AID[] metHumans;
    //TODO: Z linii polecen argument
    private int interval = 1000;

    protected void setup(){
        Object[] args = getArguments();
        if (args != null && args.length > 0){
            interval = Integer.parseInt(args[0].toString());

        }


        //myAgent type: Agent
        //The agent this behaviour belongs to. (class Human)
        addBehaviour(new TickerBehaviour(this, interval) {
            @Override
            protected void onTick() {
                //TODO: logika wymiany informacji
                System.out.println(String.format("Agent %s looking for colleagues", this.myAgent.getAID().toString()));

                DFAgentDescription description = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("meeting");
                description.addServices(sd);

                try{
                    //Znalezieni agenci - tu tylko informacyjnie
                    DFAgentDescription[] result = DFService.search(myAgent, description);
                    //TODO: wybranie odpowiedniej liczby agentów na podstawie parametru
                    metHumans = new AID[result.length];
                    for (int i = 0; i < result.length; ++i)
                    {
                        metHumans[i] = result[i].getName();
                        System.out.println(metHumans[i].getLocalName());
                    }
                }
                catch (FIPAException e){
                    e.printStackTrace();
                }
                myAgent.addBehaviour(new MeetingRequest());
            }
        });
    }
    private class MeetingRequest extends Behaviour{
        public void action(){

        }

        @Override
        public boolean done() {
            return false;
        }
    }
}
