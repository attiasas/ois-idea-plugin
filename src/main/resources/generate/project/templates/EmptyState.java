package %PROJECT_GROUP%;

import org.ois.core.state.IState;

public class EmptyState implements IState {

    /**
     * Called when entering the state.
     * Providing optional parameters passed by StateManager.changeState("StateKey", parameters...)
     */
    @Override
    public void enter(Object... parameters) {

    }

    /** Called when exiting the state. */
    @Override
    public void exit() {

    }

    /** Called to pause the state. */
    @Override
    public void pause() {

    }

    /** Called to resume the state after it has been paused. */
    @Override
    public void resume() {

    }

    /** Called to resize the state (typically in response to window resizing) */
    @Override
    public void resize(int i, int i1) {

    }

    /** Called every frame to render the state */
    @Override
    public void render() {

    }

    /** Called every frame to update the state. if the state should continue 'true' should be returned */
    @Override
    public boolean update(float v) {
        return true;
    }

    /** Called once when the simulation stops to dispose of resources used by the state. */
    @Override
    public void dispose() {

    }
}
