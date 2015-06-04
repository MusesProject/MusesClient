//package eu.musesproject.client.connectionmanager.test;
//
///*
// * #%L
// * MUSES Client
// * %%
// * Copyright (C) 2013 - 2014 Sweden Connectivity
// * %%
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * 
// *      http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * #L%
// */
//
//import static org.mockito.Mockito.atMost;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import org.apache.http.Header;
//import org.apache.http.HeaderElement;
//import org.apache.http.HttpResponse;
//import org.apache.http.ParseException;
//import org.apache.http.StatusLine;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.mockito.invocation.InvocationOnMock;
//import org.mockito.stubbing.Answer;
//import eu.musesproject.client.connectionmanager.ConnectionManager;
//import eu.musesproject.client.connectionmanager.DetailedStatuses;
//import eu.musesproject.client.connectionmanager.IConnectionCallbacks;
//import android.test.AndroidTestCase;
//
//
//public class IConnectionCallbacksTest extends AndroidTestCase {
//	@Mock
//	private HttpResponse httpResponse;
//	@Mock 
//	private StatusLine statusLine;
//	
//	private ConnectionManager comManager;
//	String url;
//	int pollInterval;
//	int sleepPollInterval;
//	String data;
//	IConnectionCallbacks callbacks;
//	
//	
//	@Override
//	protected void setUp() throws Exception {
//		super.setUp();
//	    MockitoAnnotations.initMocks(this);
//		callbacks = null; // FIXME new StubConnectionManagerCallbacks();
//		comManager = new ConnectionManager();
//		url = "http://192.168.44.101:8888/server-0.0.1-SNAPSHOT/commain";
//		pollInterval = 10000;
//		sleepPollInterval = 20000;
//		data = "this is some test data";
//	}
//
//	@Override
//	protected void tearDown() throws Exception {
//		super.tearDown();
//	}
//
//	public void testStatusCB() throws Exception {
//		when(httpResponse.getStatusLine()).thenReturn(statusLine);
//		when(statusLine.getStatusCode()).thenReturn(DetailedStatuses.SUCCESS);
//		when(httpResponse.getAllHeaders()).thenAnswer(new Answer<Header[]>() {
//			Header [] headers = new Header[1024];
//			@Override
//			public Header[] answer(InvocationOnMock invocation)
//					throws Throwable {
//				headers[0] = new Header() {
//					
//					@Override
//					public String getValue() {
//						return "Some response from the Server..";
//					}
//					@Override
//					public String getName() {
//						return "data";
//					}
//					@Override
//					public HeaderElement[] getElements() throws ParseException {
//						return null;
//					}
//				};
//				return headers;
//			}
//		});
//		comManager.connect(url, pollInterval, sleepPollInterval, callbacks, getContext());
//		verify(httpResponse,atMost(10)).getStatusLine();
//		verify(statusLine, atMost(10)).getStatusCode();
//		verify(httpResponse, atMost(10)).getAllHeaders();
//	}
//}
