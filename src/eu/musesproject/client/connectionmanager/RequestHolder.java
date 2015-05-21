package eu.musesproject.client.connectionmanager;

/*
 * #%L
 * musesclient
 * %%
 * Copyright (C) 2013 - 2014 HITEC
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import eu.musesproject.client.model.decisiontable.Action;
import eu.musesproject.contextmodel.ContextEvent;

import java.util.List;
import java.util.Map;

public class RequestHolder {
	private int id;
	private Action action;
	private Map<String, String> actionProperties;
	private List<ContextEvent> contextEvents;
	private RequestTimeoutTimer requestTimeoutTimer;
	
	public RequestHolder() {
	}
	
	public RequestHolder(Action action, Map<String, String> actionProperties, List<ContextEvent> contextEvents) {
		setAction(action);
		setActionProperties(actionProperties);
		setContextEvents(contextEvents);
		setId(hashCode());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime
				* result
				+ ((actionProperties == null) ? 0 : actionProperties.hashCode());
		result = prime * result
				+ ((contextEvents == null) ? 0 : contextEvents.hashCode());
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequestHolder other = (RequestHolder) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (actionProperties == null) {
			if (other.actionProperties != null)
				return false;
		} else if (!actionProperties.equals(other.actionProperties))
			return false;
		if (contextEvents == null) {
			if (other.contextEvents != null)
				return false;
		} else if (!contextEvents.equals(other.contextEvents))
			return false;
		if (id != other.id)
			return false;
		return true;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Map<String, String> getActionProperties() {
		return actionProperties;
	}

	public void setActionProperties(Map<String, String> actionProperties) {
		this.actionProperties = actionProperties;
	}

	public List<ContextEvent> getContextEvents() {
		return contextEvents;
	}

	public void setContextEvents(List<ContextEvent> contextEvents) {
		this.contextEvents = contextEvents;
	}

	public RequestTimeoutTimer getRequestTimeoutTimer() {
		return requestTimeoutTimer;
	}

	public void setRequestTimeoutTimer(RequestTimeoutTimer requestTimeoutTimer) {
		this.requestTimeoutTimer = requestTimeoutTimer;
	}
}