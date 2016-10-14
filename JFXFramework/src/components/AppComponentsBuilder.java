package components;

/**
 * This interface provides the structure required for a builder
 * object used for initializing all components for this application.
 * This is one means of employing a component hierarchy.
 *
 * @author Richard McKenna
 * @author ?
 * @version 1.0
 */
public interface AppComponentsBuilder {
    AppDataComponent buildDataComponent() throws Exception;

    AppFileComponent buildFileComponent() throws Exception;

    AppWorkspaceComponent buildWorkspaceComponent() throws Exception;
}
