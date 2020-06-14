package Models;

import Utilities.Constants;

public class InputParameters {

    private int simulationLength;
    private int numberOfPeople;
    private int numberOfIInitiallyInfectedPeople;

    private int averageIllnessDuration;
    private int averageInfectionProbability;
    private int mortalityRate;
    private int contactAttemptInterval;

    public InputParameters(String[] args) throws Exception {
        if(args.length != Constants.SIMULATION_INPUT_PARAMETERS_COUNT)
        {
            throw new Exception(
                    String.format("Number of input parameters is invalid. There should be %d input parameters.",
                            Constants.SIMULATION_INPUT_PARAMETERS_COUNT));
        }

        simulationLength = Integer.parseInt(args[0]);
        numberOfPeople = Integer.parseInt(args[1]);
        numberOfIInitiallyInfectedPeople = Integer.parseInt(args[2]);

        averageIllnessDuration = Integer.parseInt(args[3]);
        averageInfectionProbability = Integer.parseInt(args[4]);
        mortalityRate = Integer.parseInt(args[5]);
        contactAttemptInterval = Integer.parseInt(args[6]);
    }

    public Object[] parseToAgentArguments(boolean isInfected)
    {
        Object[] agentArguments = new Object[Constants.AGENT_INPUT_PARAMETERS_COUNT];
        Disease disease = new Disease(mortalityRate, averageInfectionProbability, averageIllnessDuration);

        agentArguments[0] = disease;
        agentArguments[1] = isInfected;
        agentArguments[2] = contactAttemptInterval;
        agentArguments[3] = numberOfPeople;

        return agentArguments;
    }

    public int getSimulationLength() {
        return simulationLength;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public int getNumberOfIInitiallyInfectedPeople() {
        return numberOfIInitiallyInfectedPeople;
    }
}