/*
 * Copyright (C) 2013 The Android Open Source Project
 *
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
 */

package com.seeun.devsign;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();

    //Service
    public static String DEVSIGN1 = "0000ffe0-0000-1000-8000-00805f9b34fb";
    //Chararteristics
    public static String NUMBER = "0000ffe1-0000-1000-8000-00805f9b34fb";

    public static String UUID_WRITE="0000ffe1-0000-1000-8000-00805f9b34fb";
   /* public static String CHAR1 = ""*/


    static {
        //Services.
        attributes.put(DEVSIGN1, "First Service");
        //Characteristics.
        attributes.put(NUMBER, "characteristics");
        //WriteCharacteristics.
        attributes.put(UUID_WRITE, "writecharacteristis");


    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
