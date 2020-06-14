package Agents;

import Models.Disease;
import Utilities.Constants;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Human extends Agent {

    private int illnessDuration = Constants.MIN_ILLNESS_DURATION;
    private int infectionProbability = Constants.MIN_INFECTION_PROBABILITY;
    private boolean isDying = false;

    private Disease disease;
    private Random random;

    private boolean diseased;
    private AID[] offeredHumans;
    //TODO: Z linii polecen argument
    private int interval = 10000;
    private int numberOfAgents = 0;

    protected void setup() {
        Object[] args = getArguments();

        if (args.length == Constants.AGENT_INPUT_PARAMETERS_COUNT)
        {
            random = new Random();
            disease = (Disease)args[0];
            diseased = (boolean)args[1];
            interval = (int)args[2];
            numberOfAgents = (int)args[3];

            setIllnessDuration();
            setInfectionProbability();
        }
        //Rejestracja descriptora do nasluchiwania innych agentów:
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("meeting");
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        //End rejestracja
        //Dwa zachowania - zawsze obecne, block w zaleznosci od diseased
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
                    offeredHumans = new AID[result.length];
                    for (int i = 0; i < result.length; ++i)
                    {
                        offeredHumans[i] = result[i].getName();
                    }
                    System.out.println(String.format("%s :", myAgent.getLocalName()));
                    System.out.println(result.length);
                }
                catch (FIPAException e){
                    System.out.println(e.toString());
                    e.printStackTrace();
                }
                //if(offeredHumans.length == numberOfAgents)
                    //myAgent.addBehaviour(new MeetingRequest());
            }
        });
        //Zachowania zdrowej osoby
        //if(offeredHumans.length == numberOfAgents){
            //addBehaviour(new MeetingRequestAnswer());
            //addBehaviour(new Meeting());
        //}

    }

    //Propozycja spotkania oraz spotkanie
    private class MeetingRequest extends Behaviour{
        private List<AID> acceptedHumans = new ArrayList<>();
        private int bestPrice;
        private int repliesCnt = 0;
        private int meetingID = 0;
        private MessageTemplate mt;
        private int step = 0;

        public void action() {
            switch (step) {
                case 0:
                    //call for proposal do innych ludzi - z losowego przedzialu
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < offeredHumans.length; ++i) {
                        cfp.addReceiver(offeredHumans[i]);
                    }
                    cfp.setContent(String.format("Meeting no %s", myAgent.getAID().toString()));
                    cfp.setConversationId(String.format("Meeting ID: %s", myAgent.getAID().toString()));
                    cfp.setReplyWith("cfp"+System.currentTimeMillis()); //unikalna wartosc
                    myAgent.send(cfp);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId(String.format("Meeting ID: %s", myAgent.getAID().toString())),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    //odbior odpowiedzi od osob na spotkanie
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            //Przyszla odpowiedz pozytywna - dodać do listy
                            AID human = reply.getSender();
                            if(!acceptedHumans.contains(human))
                                acceptedHumans.add(human);
                        }
                        //Licznik osob up
                        repliesCnt++;
                        if (repliesCnt >= offeredHumans.length) {
                            //otrzymano wszystkie oferty -> nastepny krok
                            step = 2;
                        }
                    }
                    else {
                        block();
                    }
                    break;
                case 2:
                    //"Wyslanie" spotkania do kazdego zaakceptowanego osobnika - zrobic podobnie jak w step 2
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    //TODO: Obsłużyć wszystkie osoby z acceptedHumans
/*                    order.addReceiver(bestSeller);
                    order.setContent(targetBookTitle);
                    order.setConversationId(String.format("Meeting ID: %s", myAgent.getAID().toString()));
                    order.setReplyWith("order"+System.currentTimeMillis());
                    myAgent.send(order);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId(String.format("Meeting ID: %s", myAgent.getAID().toString())),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));*/
                    step = 3;
                    break;
                case 3:
                    //otrzymanie potwierdzenia spotkania przez osobę spytaną
                    //TODO: Odpowiednia pasująca nam logika co zrobić po udanym spotkaniu po stronie chorego
/*                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            //zakup zakonczony powodzeniem
                            System.out.println(targetBookTitle+" kupiona za "+bestPrice+" od "+reply.getSender().getLocalName());
                            System.out.println("Czekam na nowa dyspozycje kupna.");
                            targetBookTitle = "";
                            //myAgent.doDelete();
                        }
                        else {
                            System.out.println("Zakup nieudany. "+targetBookTitle+" zostala sprzedana w miedzyczasie.");
                        }
                        step = 4;	//konczy cala interakcje, ktorej celem jest kupno
                    }
                    else {
                        block();
                    }
                    break;*/
            }
        }

        public boolean done() {
            if (step == 2 && acceptedHumans.size() == 0) {
                System.out.println("Zero akceptacji spotkań");
            }
            //Koniec jesli nie ma akceptacji lub spotkania sie odbyly
            return ((step == 2 && acceptedHumans.size() == 0) || step == 4);
        }
    }

    //Behaviour wstępnie akceptujący propozycję spotkania
    private class MeetingRequestAnswer extends CyclicBehaviour{

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();
                //if (WARUNEK na akceptacje spotkania) {
                    reply.setPerformative(ACLMessage.PROPOSE);
                    //reply.setContent(Wiadomosc jakaś);
                //}
                //else {
                    //pozycji nie ma w katalogu
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                //}
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }
    //Akceptacja - co się dzieje po spotkaniu
    private class Meeting extends CyclicBehaviour{

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();
//                if (price != null) {
//                    reply.setPerformative(ACLMessage.INFORM);
//                    System.out.println(title+" sprzedana agentowi "+msg.getSender().getLocalName());
//                }
//                else {
//                    //pozycji nie ma w katalogu, poniewaz w miedzyczasie (juz po zlozeniu oferty) zostala sprzedana innemu agentowi
//                    reply.setPerformative(ACLMessage.FAILURE);
//                    reply.setContent("not-available");
//                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }

    private void setIsDying()
    {
        if (diseased && !isDying)
        {
            if(random.nextInt(100) + 1 < disease.getMortalityRate())
            {
                isDying = true;
            }
        }
    }

    private int getMultiplier()
    {
        boolean isPositive =  random.nextBoolean();

        if (!isPositive)
        {
            return -1;
        }
        return 1;
    }

    private void setIllnessDuration()
    {
        int illnessDurationModifier = random.nextInt(Constants.ILLNESS_DURATION_MODIFIER);
        int multiplier = getMultiplier();

        illnessDurationModifier *= multiplier;
        illnessDuration = disease.getAverageIllnessDuration() + illnessDurationModifier;

        if (illnessDuration < Constants.MIN_ILLNESS_DURATION)
        {
            illnessDuration = Constants.MIN_ILLNESS_DURATION;
        }
        else if (illnessDuration > Constants.MAX_ILLNESS_DURATION)
        {
            illnessDuration = Constants.MAX_ILLNESS_DURATION;
        }
    }

    private void setInterval(int interval)
    {
        this.interval = interval;
    }

    private void setInfectionProbability()
    {
        int infectionProbabilityModifier = random.nextInt(Constants.INFECTION_PROBABILITY_MODIFIER);
        int multiplier = getMultiplier();

        infectionProbabilityModifier *= multiplier;
        infectionProbability = disease.getAverageInfectionProbability() + infectionProbabilityModifier;

        if (illnessDuration < Constants.MIN_INFECTION_PROBABILITY)
        {
            infectionProbability = Constants.MIN_INFECTION_PROBABILITY;
        }
        else if (illnessDuration > Constants.MAX_INFECTION_PROBABILITY)
        {
            infectionProbability = Constants.MAX_INFECTION_PROBABILITY;
        }
    }
}