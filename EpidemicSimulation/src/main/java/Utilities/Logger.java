package Utilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {

    private long simulationStartDate;
    private long dayDuration;
    private String folderPath;

    public Logger(long dayDuration, String folderPath) {
        this.dayDuration = dayDuration * 1000;
        this.folderPath = folderPath;
    }

    public void logAgentStateChange(String agentName, AgentState agentState, long eventTime) {
        String filePath = folderPath + agentName + ".csv";
        try (FileWriter fw = new FileWriter(filePath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            int day = Math.toIntExact(((eventTime - simulationStartDate) / dayDuration));
            String state = getStateString(agentState);
            String textToWrite = String.format("%s,%s,%d", agentName, state, day);

            out.println(textToWrite);
        } catch (IOException e) {
            System.out.println("Error occurred during saving data to file from agent " + agentName);
        }
    }

    public void setSimulationStartDate(long simulationStartDate) {
        this.simulationStartDate = simulationStartDate;
    }

    private String getStateString(AgentState agentState) {
        String state = "";
        switch (agentState) {
            case CURED:
                state = "CURED";
                break;
            case INFECTED:
                state = "INFECTED";
                break;
            case DEAD:
                state = "DEAD";
                break;
        }
        return state;
    }
}