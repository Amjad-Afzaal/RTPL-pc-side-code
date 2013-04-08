/*
 * Copyright 2010 Christian Schindelhauer, Peter Thiemann, Faisal Aslam, Luminous Fennell and Gidon Ernst.
 * All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 3 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 3 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Faisal Aslam 
 * (aslam AT informatik.uni-freibug.de or studentresearcher AT gmail.com)
 * if you need additional information or have any questions.
 */
package takatuka.classreader.logic.file;

import java.util.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class ConfigPropertyReader extends PropertyReader {

    private final static ConfigPropertyReader configReader =
            new ConfigPropertyReader();
    public final static String CONFIG_PROPERTY = "config.properties";
    private final static String GENERATE_TUK_TEXT_FOR_DEBUGING =
            "GENERATE_TUK_TEXT_FOR_DEBUGING";
    private final Properties configProp = getProperties(CONFIG_PROPERTY);
    private static int shouldGenTUKTextFileForDebugging = -1;
    private HashMap<String, String> cache = new HashMap();
    
    public static ConfigPropertyReader getInstanceOf() {
        return configReader;
    }

    public boolean isGenerateTukTextForDebuging() {
        if (shouldGenTUKTextFileForDebugging == 0) {
            return false;
        } else if (shouldGenTUKTextFileForDebugging == 1) {
            return true;
        }

        String prop = configProp.getProperty(GENERATE_TUK_TEXT_FOR_DEBUGING);
        if (prop != null && prop.equalsIgnoreCase("true")) {
            shouldGenTUKTextFileForDebugging = 1;
            return true;
        } else {
            shouldGenTUKTextFileForDebugging = 0;
            return false;
        }
    }

    public String getConfigProperty(String property) {
        String ret = cache.get(property);
        if (ret != null) {
            return ret;
        }
        ConfigPropertyReader configPropReader =
                ConfigPropertyReader.getInstanceOf();
        Properties prop = configPropReader.getProperties(ConfigPropertyReader.
                CONFIG_PROPERTY);
        ret = (String) prop.get(property);
        cache.put(property, ret);
        return ret;
    }
}
