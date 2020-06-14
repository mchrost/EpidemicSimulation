package Agents;

import Models.Disease;
import Utilities.AgentState;
import Utilities.Constants;
import Utilities.Logger;
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

import java.util.Random;

public class Human extends Agent {

    private int day = 1000;

    private int illnessDuration = Constants.MIN_ILLNESS_DURATION;
    private int infectionProbability = Constants.MIN_INFECTION_PROBABILITY;
    private boolean isDying = false;
    private boolean isDead = false;

    private Disease disease;
    private Random random;

    private boolean diseased;
    private AID offeredHuman;
    private long timeOfInfection;
    //TODO: Z linii polecen argument
    private int interval = 10000;
    private int numberOfAgents = 0;

    private Logger logger;

    protected void setup() {
        Object[] args = getArguments();

        if (args.length == Constants.AGENT_INPUT_PARAMETERS_COUNT) {
            random = new Random();
            disease = (Disease) args[0];
            diseased = (boolean) args[1];
            interval = ((int) args[2]) * day;
            numberOfAgents = (int) args[3];
            logger = (Logger) args[4];

            setIllnessDuration();
            setInfectionProbability();
        }
        //Agenci chorzy na start
        if (diseased) {
            System.out.println(getLocalName() + " is diseased");
            timeOfInfection = System.currentTimeMillis();
            logger.logAgentStateChange(this.getLocalName(), AgentState.INFECTED, timeOfInfection);
            setIsDying();
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
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        //End rejestracja
        //Dwa zachowania - zawsze obecne, block w zaleznosci od diseased
        //myAgent type: Agent
        //The agent this behaviour belongs to. (class Human)
        addBehaviour(new TickerBehaviour(this, interval) {
            @Override
            protected void onTick() {
                DFAgentDescription description = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("meeting");
                description.addServices(sd);

                try {
                    //Znalezieni agenci - jesli zostana znalezieni wszyscy - podepnij zachowania
                    if (diseased) {
                        if (!isDead) {
                            DFAgentDescription[] res = DFService.search(myAgent, description);
                            //wybieramy tylko jednego do zaoferowania spotkania
                            offeredHuman = res[random.nextInt(numberOfAgents)].getName();
                        }
                    }
                } catch (FIPAException e) {
                    e.printStackTrace();
                }

                myAgent.addBehaviour(new MeetingRequest());
            }
        });

        addBehaviour(new MeetingRequestAnswer());
        addBehaviour(new Meeting());


    }

    //Propozycja spotkania oraz spotkanie
    //TODO: Smierc agenta lub jego wyzdrowienie
    private class MeetingRequest extends Behaviour {
        private AID acceptedHuman;
        private MessageTemplate mt;
        private int step = 0;

        public void action() {
            if (diseased) {
                if (!isDead) {
                    switch (step) {
                        case 0:
                            if (setIsDead(myAgent.getLocalName()) || setIsCured(myAgent.getLocalName()))
                            {
                                break;
                            }

                            System.out.println("MeetingRequest from " + myAgent.getLocalName() + " to " + offeredHuman.getLocalName());
                            //call for proposal do tej jednej wylosowanej osoby
                            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                            cfp.addReceiver(offeredHuman);
                            cfp.setContent(String.format("Meeting no %s", myAgent.getAID().toString()));
                            cfp.setConversationId(String.format("Meeting ID: %s", myAgent.getAID().toString()));
                            cfp.setReplyWith("cfp" + System.currentTimeMillis()); //unikalna wartosc
                            myAgent.send(cfp);
                            mt = MessageTemplate.and(MessageTemplate.MatchConversationId(String.format("Meeting ID: %s", myAgent.getAID().toString())),
                                    MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                            step = 1;
                            break;
                        case 1:
                            //odbior odpowiedzi od osoby na spotkanie
                            ACLMessage reply = myAgent.receive(mt);
                            if (reply != null) {
                                if (reply.getPerformative() == ACLMessage.PROPOSE) {
                                    //Przyszla odpowiedz pozytywna - przypisać
                                    acceptedHuman = reply.getSender();
                                    System.out.println(acceptedHuman.getLocalName() + " accepted the meeting");
                                }
                                if (acceptedHuman != null) {
                                    //otrzymano wszystkie oferty -> nastepny krok
                                    step = 2;
                                }
                            } else {
                                block();
                            }
                            break;
                        case 2:
                            //"Wyslanie" spotkania do kazdego zaakceptowanego osobnika - zrobic podobnie jak w step 2
                            ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                            System.out.println(myAgent.getLocalName() + " Initiates meeting with " + acceptedHuman.getLocalName());
                            order.addReceiver(acceptedHuman);
                            order.setContent(String.format("Meeting no %s", myAgent.getAID().toString()));
                            order.setConversationId(String.format("Meeting ID: %s", myAgent.getAID().toString()));
                            order.setReplyWith("order" + System.currentTimeMillis());
                            myAgent.send(order);
                            mt = MessageTemplate.and(MessageTemplate.MatchConversationId(String.format("Meeting ID: %s", myAgent.getAID().toString())),
                                    MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                            step = 3;
                            break;
                        case 3:
                            //otrzymanie potwierdzenia spotkania przez osobę spytaną
                            //TODO: Odpowiednia pasująca nam logika co zrobić po udanym spotkaniu po stronie chorego - o ile jakas jest
                            reply = myAgent.receive(mt);
                            if (reply != null) {
                                step = 4;
                            } else {
                                block();
                            }
                            break;
                    }
                }
            } else {
                block();
            }
        }

        //TODO: dodać warunek isDead do alternatywy w return
        public boolean done() {
            if (step == 2 && acceptedHuman == null) {
                System.out.println("Zero akceptacji spotkań");
            }
            //Koniec jesli nie ma akceptacji lub spotkania sie odbyly lub po zarazeniu na poczatku nie zostal wylosowany nowy human
            return ((step == 2 && acceptedHuman == null) || step == 4 || offeredHuman == null && step == 0);
        }
    }

    //Behaviour wstępnie akceptujący propozycję spotkania
    private class MeetingRequestAnswer extends CyclicBehaviour {

        @Override
        public void action() {
            if (!diseased) {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    String title = msg.getContent();
                    ACLMessage reply = msg.createReply();
                    //80% szans na akceptacje
                    if (random.nextInt(10) + 1 > 2) {
                        reply.setPerformative(ACLMessage.PROPOSE);
                        System.out.println(myAgent.getLocalName() + " Has accepted a proposal");
                    } else {
                        System.out.println(myAgent.getLocalName() + " Has rejected a proposal");
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("not-available");
                    }
                    myAgent.send(reply);
                } else {
                    block();
                }
            } else
                block();
        }
    }

    //Akceptacja - co się dzieje po spotkaniu
    private class Meeting extends CyclicBehaviour {

        @Override
        public void action() {
            if (!diseased) {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    System.out.println(myAgent.getLocalName() + " has met with " + msg.getSender().getLocalName());
                    setIsDiseased(myAgent.getLocalName());
                    String title = msg.getContent();
                    ACLMessage reply = msg.createReply();
                    if (diseased) {
                        timeOfInfection = System.currentTimeMillis();
                        setIsDying();
                        reply.setPerformative(ACLMessage.INFORM);
                        System.out.println("Agent " + myAgent.getLocalName() + " got infected");
                        if (isDying) {
                            System.out.println("Agent " + myAgent.getLocalName() + " is dying");
                        }

                    } else {
                        System.out.println("Agent " + myAgent.getLocalName() + " did not get infected");
                        reply.setPerformative(ACLMessage.FAILURE);
                        reply.setContent("not-available");
                    }
                    myAgent.send(reply);
                } else {
                    block();
                }
            } else
                block();
        }
    }

    private void setIsDying() {
        if (diseased && !isDying) {
            if (random.nextInt(100) < disease.getMortalityRate()) {
                isDying = true;
            }
        }
    }

    private void setIsDiseased(String agentName) {
        if (random.nextInt(100)< infectionProbability) {
            diseased = true;
            setIsDying();
            timeOfInfection = System.currentTimeMillis();
            logger.logAgentStateChange(agentName, AgentState.INFECTED, timeOfInfection);
        }
    }

    private boolean setIsDead(String agentName)
    {
        if (diseased && isDying)
        {
            long currentTime = System.currentTimeMillis();
            if (currentTime - timeOfInfection > illnessDuration)
            {
                logger.logAgentStateChange(agentName, AgentState.DEAD, currentTime);
                isDead = true;
                return true;
            }
        }
        return false;
    }

    private boolean setIsCured(String agentName)
    {
        if (diseased && !isDying)
        {
            long currentTime = System.currentTimeMillis();
            if (currentTime - timeOfInfection > illnessDuration)
            {
                logger.logAgentStateChange(agentName, AgentState.CURED, currentTime);
                diseased = false;
                return true;
            }
        }
        return false;
    }

    private int getMultiplier() {
        boolean isPositive = random.nextBoolean();

        if (!isPositive) {
            return -1;
        }
        return 1;
    }

    private void setIllnessDuration() {
        int illnessDurationModifier = random.nextInt(Constants.ILLNESS_DURATION_MODIFIER);
        int multiplier = getMultiplier();

        illnessDurationModifier *= multiplier;
        illnessDuration = disease.getAverageIllnessDuration() + illnessDurationModifier;

        if (illnessDuration < Constants.MIN_ILLNESS_DURATION) {
            illnessDuration = Constants.MIN_ILLNESS_DURATION;
        } else if (illnessDuration > Constants.MAX_ILLNESS_DURATION) {
            illnessDuration = Constants.MAX_ILLNESS_DURATION;
        }

        illnessDuration *= day;
    }

    private void setInterval(int interval) {
        this.interval = interval;
    }

    private void setInfectionProbability() {
        int infectionProbabilityModifier = random.nextInt(Constants.INFECTION_PROBABILITY_MODIFIER);
        int multiplier = getMultiplier();

        infectionProbabilityModifier *= multiplier;
        infectionProbability = disease.getAverageInfectionProbability() + infectionProbabilityModifier;

        if (infectionProbability < Constants.MIN_INFECTION_PROBABILITY) {
            infectionProbability = Constants.MIN_INFECTION_PROBABILITY;
        } else if (infectionProbability > Constants.MAX_INFECTION_PROBABILITY) {
            infectionProbability = Constants.MAX_INFECTION_PROBABILITY;
        }
    }
}