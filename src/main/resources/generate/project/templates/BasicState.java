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

    /**
    * Called to update the state.
    *
    * @param dt The delta time since the last update.
    * @return True if the state should continue, false otherwise (will cause the state to exit).
    */
    @Override
    public boolean update(float dt) {
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
