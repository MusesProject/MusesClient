package eu.musesproject.client.actuators;

/*
 * #%L
 * musesclient
 * %%
 * Copyright (C) 2013 - 2014 HITEC
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

import android.util.Log;

import java.io.File;

/**
 * Created by christophstanik on 4/22/15.
 *
 * The purpose of this actuator is to erase company sensitive
 * files in a specific folder on the SD card.
 */
public class FileEraserActuator {
    private static final String TAG = FileEraserActuator.class.getSimpleName();

    /**
     * Method to erase files from a given folder path.
     * The folder structure (sub-folders) are not deleted, just the files itself
     * @param folderPath
     */
    public void eraseFolderContent(String folderPath) {
        File[] folder = new File(folderPath).listFiles();
        for (File file : folder) {
            if(file.isDirectory()) {
                eraseFolderContent(file.getAbsolutePath());
            }
            else if(file.isFile()) {
                String fileName = file.getName();
                boolean deleted = file.delete();
                Log.d(TAG, fileName + " is deleted = " + deleted);
            }
        }
    }
}