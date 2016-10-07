package components;

/**
 * This interface serves as a family of type that will initialize the style for some set of controls, like the
 * workspace, for example.
 *
 * @author Richard McKenna
 * @author ?
 * @version 1.0
 */
public interface AppStyleArbiter {

    String CLASS_BORDERED_PANE = "bordered_pane";

    enum BUTTON_TYPE {
        NEW, SAVE, LOAD, EXIT;
    }
    
    void initStyle();
}
