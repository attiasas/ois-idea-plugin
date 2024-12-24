package %PROJECT_GROUP%;

import org.ois.core.state.SimpleState;

public class BasicState extends SimpleState {

    /**
     * Called when entering the state.
     * Providing optional parameters passed by StateManager.changeState("StateKey", parameters...)
     */
    @Override
    public void enter(Object... objects) {

    }

    /** Called when exiting the state. */
    @Override
    public void exit() {

    }

    /** Called every frame to update the state. if the state should continue 'true' should be returned */
    @Override
    public boolean update(float v) {
        return true;
    }

    /** Called every frame to render the state */
    @Override
    public void render() {

    }

    /** Called once when the simulation stops to dispose of resources used by the state. */
    @Override
    public void dispose() {

    }
}
