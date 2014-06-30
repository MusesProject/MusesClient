package eu.musesproject.client.connectionmanager;
/*
 * #%L
 * MUSES Client
 * %%
 * Copyright (C) 2013 - 2014 Sweden Connectivity
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * Stores Server statuses which are sent to other module using status callback
 * 
 * @author Yasir Ali
 * @version Jan 27, 2014
 */

public class Statuses {
	
	public static final int OFFLINE = 0;
	public static final int ONLINE = 1;
	public static int CURRENT_STATUS = 0; 
	public static boolean STATUS_CHANGED = false;
	public static final int DATA_SEND_FAILED = 3;
	public static final int DATA_SEND_OK = 4;
	public static final int CONNECTION_FAILED = 5;
	public static final int CONNECTION_OK = 6;
	public static final int DISCONNECTED = 7;

}
