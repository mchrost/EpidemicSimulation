import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Main {

    public static void main(String[] args){
        Runtime r = Runtime.instance();
        ProfileImpl p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.GUI, "true");
        p.setParameter("jade_domain_df_maxresult", "1000");
        //Argument do runtime i tak
        int numberOfAgents = 1000;

        ContainerController cc = r.createMainContainer(p);
        AgentController[] ac;

        ac = InitializeAgents(cc, numberOfAgents);
        ActivateAgents(ac);

    }

    private static AgentController[] InitializeAgents(ContainerController cc, int size){
        AgentController[] arr = new AgentController[size];
        for (int i=0; i<1000; i++){
            arr[i] = createAgent(cc, i);
        }
        return arr;
    }
    private static AgentController createAgent(ContainerController cc, int i){
        AgentController ac = null;
        try {
            ac = cc.createNewAgent("agent"+i,"Agents.Human", null);
        }catch (StaleProxyException e) {
            e.printStackTrace();
        }
        return ac;
    }

    private static void ActivateAgents(AgentController[] ac) {
        for(AgentController agent: ac){
            try{
                agent.start();
            }catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }
}