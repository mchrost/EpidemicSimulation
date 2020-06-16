import Agents.Human;
import Models.InputParameters;
import Utilities.Logger;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Main {

    public static void main(String[] args) {
        try {
            long simulationStartDate;
            InputParameters inputParameters = new InputParameters(args);
            Logger logger = inputParameters.getLogger();

            Runtime r = Runtime.instance();
            ProfileImpl p = new ProfileImpl();
            p.setParameter(Profile.MAIN_HOST, "localhost");
            p.setParameter(Profile.GUI, "true");
            p.setParameter("jade_domain_df_maxresult", "1000");
            //Argument do runtime i tak
            int numberOfAgents = 1000;

            ContainerController cc = r.createMainContainer(p);
            AgentController[] ac;

            ac = InitializeAgents(cc, numberOfAgents, inputParameters);

            simulationStartDate = System.currentTimeMillis();
            ActivateAgents(ac, simulationStartDate, logger);

            while (!((Human.deadHumans == inputParameters.getNumberOfPeople()) ||
                    (Human.healthyHumans == inputParameters.getNumberOfPeople() - Human.deadHumans) ||
                    isSimulationTimePassed(simulationStartDate, inputParameters))) {
            }

            r.shutDown();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occured during simulation.");
        }
    }

    private static AgentController[] InitializeAgents(ContainerController cc, int size, InputParameters inputParameters) {
        AgentController[] arr = new AgentController[size];

        int agentsToInfectCount = inputParameters.getNumberOfIInitiallyInfectedPeople();
        boolean isAgentInfected = true;
        for (int i = 0; i < inputParameters.getNumberOfPeople(); i++) {
            if (agentsToInfectCount <= 0) {
                isAgentInfected = false;
            } else {
                agentsToInfectCount--;
            }

            arr[i] = createAgent(cc, i, inputParameters, isAgentInfected);
        }
        return arr;
    }

    private static AgentController createAgent(ContainerController cc, int i, InputParameters inputParameters, boolean isAgentInfected) {
        AgentController ac = null;
        try {
            ac = cc.createNewAgent("agent" + i, "Agents.Human", inputParameters.parseToAgentArguments(isAgentInfected));
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        return ac;
    }

    private static void ActivateAgents(AgentController[] ac, long simulationStartDate, Logger logger) {
        logger.setSimulationStartDate(simulationStartDate);
        for (AgentController agent : ac) {
            try {
                agent.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isSimulationTimePassed(long simulationStartDate, InputParameters inputParameters) {
        if (System.currentTimeMillis() - simulationStartDate > inputParameters.getSimulationLength() * 1000) {
            return true;
        }
        return false;
    }
}