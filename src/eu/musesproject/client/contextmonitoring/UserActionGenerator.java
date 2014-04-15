package eu.musesproject.client.contextmonitoring;

import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.client.model.decisiontable.ActionType;
import eu.musesproject.contextmodel.ContextEvent;

import java.util.Map;

/**
 * Created by christophstanik on 4/14/14.
 *
 * Class to create a User action based on context events and to transform a {@link eu.musesproject.client.contextmonitoring.service.aidl.Action}
 * from a MUSES aware app to an {@link eu.musesproject.client.model.decisiontable.Action} that is readable for
 * the decision maker
 */
public class UserActionGenerator {
    /**
     * Method to create a user action based on the fired context events from the Sensors
     * ({@link eu.musesproject.client.contextmonitoring.sensors.ISensor}) by aggregating the context events to higher
     * level context events
     * @param contextEventTrigger the context event that triggered this method
     * @param lastFiredContextEvents all other available context events as well as the last context event of the trigger
     *                               if it exists
     * @return {@link eu.musesproject.client.model.decisiontable.Action}
     */
    public static Action createUserAction(ContextEvent contextEventTrigger, Map<String, ContextEvent> lastFiredContextEvents) {
        Action action = new Action();
        // dummy action
        action.setTimestamp(System.currentTimeMillis());
        action.setActionType(ActionType.ACCESS);

        return action;
    }

    /**
     * Method to transform a {@link eu.musesproject.client.contextmonitoring.service.aidl.Action} from a MUSES
     * aware application to an {@link eu.musesproject.client.model.decisiontable.Action}
     * @return {@link eu.musesproject.client.model.decisiontable.Action}
     */
    public static Action transformUserAction(eu.musesproject.client.contextmonitoring.service.aidl.Action musesAwareAction) {
        Action transformedAction = new Action();
        transformedAction.setActionType(transformToActionType(musesAwareAction.getType()));
        transformedAction.setTimestamp(musesAwareAction.getTimestamp());

        return transformedAction;
    }

    private static String transformToActionType(String type) {
        if(type.equals(ActionType.ACCESS)) {
            return ActionType.ACCESS;
        }
        else if(type.equals(ActionType.DELETE)) {
            return ActionType.DELETE;
        }
        else if(type.equals(ActionType.INSTALL)) {
            return ActionType.INSTALL;
        }
        else if(type.equals(ActionType.OPEN)) {
            return ActionType.OPEN;
        }
        else if(type.equals(ActionType.RUN)) {
            return ActionType.RUN;
        }
        else if(type.equals(ActionType.SEND)) {
            return ActionType.SEND;
        }
        else {
            return null;
        }
    }
}