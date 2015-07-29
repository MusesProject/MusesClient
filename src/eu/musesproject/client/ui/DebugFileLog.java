/*
 * #%L
 * MUSES Client
 * %%
 * Copyright (C) 2013 - 2015 Sweden Connectivity
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

package eu.musesproject.client.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;

public class DebugFileLog {

	private static FileWriter fWriter = null;
	private static boolean fileOpen = false;
	
	private static boolean enableWrite = true;
	
	public synchronized static void write(String data)
	{
		if (!enableWrite)
			return;

		Date now=new Date();
		String currentTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss-").format(now);
		if (!fileOpen)
		{
			fileOpen = true;
			try {
				/* Open and append */
				String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
				String filename = "MUSES_debug.log";
				fWriter = new FileWriter(baseDir+File.separator+filename, true);
				fWriter.write("");
				fWriter.write(currentTimeString+"Logfile reopened,,\n");
			} catch (IOException e) {
				fileOpen = false;
				e.printStackTrace();
				return;
			}
			
		}
		
		try {
			fWriter.write(currentTimeString+data+"\n");
			fWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	public static void close()
	{
		if (fileOpen)
		{
			try {
				fWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			fileOpen = false;
		}
	}
	
}



