package components;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

/**
 * This abstract class provides the structure for workspace components in
 * our applications. Note that by doing so we make it possible
 * for custom-provided descendant classes to have their methods
 * called from this framework.
 *
 * @author Richard McKenna
 * @author ?
 * @version 1.0
 */
public abstract class AppWorkspaceComponent implements AppStyleArbiter {

    protected Pane    workspace;          // The workspace that can be customized depending on what the app needs
    protected boolean workspaceActivated; // Denotes whether or not the workspace is activated
    
    /**
     * When called this function puts the workspace into the window, revealing the controls for editing work.
     *
     * @param appPane The pane that contains all the controls in the
     *                entire application, including the file toolbar controls, which
     *                this framework manages, as well as the customly provided workspace,
     *                which would be different for each app.
     */
    public void activateWorkspace(BorderPane appPane) {
        if (!workspaceActivated) {
            appPane.setCenter(workspace);
            workspaceActivated = true;
        }
    }
    
    /**
     * Mutator method for setting the custom workspace.
     *
     * @param initWorkspace The workspace to set as the user interface's workspace.
     */
    public void setWorkspace(Pane initWorkspace) {
        workspace = initWorkspace;
    }
    
    /**
     * Accessor method for getting the workspace.
     *
     * @return The workspace pane for this app.
     */
    public Pane getWorkspace() { return workspace; }
    
    /**
     * This method is defined completely at the concrete implementation level.
     */
    public abstract void reloadWorkspace();
}
