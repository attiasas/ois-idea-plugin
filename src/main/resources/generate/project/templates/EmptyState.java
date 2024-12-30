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

    /**
    * Called to resize the state, typically in response to window resizing.
    *
    * @param width  The new width of the state.
    * @param height The new height of the state.
    */
    @Override
    public void resize(int width, int height) {

    }

    /** Called every frame to render the state */
    @Override
    public void render() {

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

    /** Called once when the simulation stops to dispose of resources used by the state. */
    @Override
    public void dispose() {

    }
}
