package eu.musesproject.client.contextmonitoring.sensors;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import eu.musesproject.client.contextmonitoring.ContextListener;
import eu.musesproject.contextmodel.ContextEvent;




public class OSSensor implements ComputingEnvironmentContextSensor {
	private static final String TAG = OSSensor.class.getSimpleName();
	
	public static final String TYPE = "CONTEXT_SENSOR_OS";

    private Context context;
    private ContextListener listener;
	
    // history of fired context events
    List<ContextEvent> contextEventHistory;

	public OSSensor(Context context) {
        this.context = context;
        contextEventHistory = new ArrayList<ContextEvent>();
	}

	@Override
	public void enable() {
		// TODO Auto-generated method stub

	}

	@Override
	public void disable() {
		// TODO Auto-generated method stub

	}
	

	@Override
	public void addContextListener(ContextListener listener) {
        this.listener = listener;
	}
	

	@Override
	public ContextEvent getLastFiredContextEvent() {
        if(contextEventHistory.size() > 0) {
            return contextEventHistory.get(contextEventHistory.size() - 1);
        }
        else {
            return null;
        }
	}
}
