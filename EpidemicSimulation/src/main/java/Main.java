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

        ContainerController cc = r.createMainContainer(p);
        AgentController ac;
        for (int i=0; i<10; i++){
            try {
                ac = cc.createNewAgent("agent"+i,"Agents.Person", null);
                ac.start();
            }catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }


    }
}