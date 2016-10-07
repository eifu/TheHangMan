package settings;

/**
 * This enum provides properties that are to be loaded via
 * XML files to be used for setting up the application.
 *
 * @author Richard McKenna
 * @author ?
 * @version 1.0
 */
public enum AppPropertyType {

    // from app-properties.xml
    APP_WINDOW_WIDTH,
    APP_WINDOW_HEIGHT,
    APP_TITLE,
    APP_LOGO,
    APP_CSS,
    APP_PATH_CSS,

    // APPLICATION ICONS
    NEW_ICON,
    SAVE_ICON,
    LOAD_ICON,
    EXIT_ICON,

    // APPLICATION TOOLTIPS FOR BUTTONS
    NEW_TOOLTIP,
    SAVE_TOOLTIP,
    LOAD_TOOLTIP,
    EXIT_TOOLTIP,

    // ERROR MESSAGES
    NEW_ERROR_MESSAGE,
    SAVE_ERROR_MESSAGE,
    PROPERTIES_LOAD_ERROR_MESSAGE,

    // ERROR TITLES
    NEW_ERROR_TITLE,
    SAVE_ERROR_TITLE,
    PROPERTIES_LOAD_ERROR_TITLE,

    // AND VERIFICATION MESSAGES AND TITLES
    SAVE_COMPLETED_MESSAGE,
    SAVE_COMPLETED_TITLE,
    SAVE_UNSAVED_WORK_TITLE,
    SAVE_UNSAVED_WORK_MESSAGE,

    SAVE_WORK_TITLE,
    LOAD_WORK_TITLE,
    WORK_FILE_EXT,
    WORK_FILE_EXT_DESC,
    LOAD_COMPLETED_MESSAGE,
    LOAD_COMPLETED_TITLE,

    GAME_WON_MESSAGE,
    GAME_LOST_MESSAGE,
    GAME_OVER_TITLE
}
