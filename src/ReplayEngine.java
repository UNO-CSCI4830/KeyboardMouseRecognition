public class ReplayEngine {

    public void replay(List<Action> actions) {
        for (Action action : actions) {
            execute(action);
        }
    }

    private void execute(Action action) {
        
    }
}
