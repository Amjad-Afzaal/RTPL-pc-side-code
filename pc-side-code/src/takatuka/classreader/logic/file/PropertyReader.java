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

import java.io.*;
import java.util.*;
import takatuka.classreader.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class PropertyReader {

    private static HashMap cache = new HashMap();
    private static final PropertyReader pReader = new PropertyReader();
    public static String path = null;

    protected PropertyReader() {
        super();
    }

    public static PropertyReader getInstanceOf() {
        return pReader;
    }

    public static void setPropertiesPath(String path) {
        PropertyReader.path = path;
    }

    public static String createName(String name) {
        if (path != null) {
            if (!path.endsWith("/") && !path.endsWith("\\")) {
                path += "/";
            }
            return new File(path+name).getAbsolutePath();
        } else {
            return name;
        }
    }

    public final Properties loadProperties(String name) {

        Properties prop = (Properties) cache.get(createName(name)); //load one properties only once.
        if (prop != null) {
            return (Properties) prop;
        }
        try {
            prop = new Properties();
            FileInputStream in = new FileInputStream(createName(name));
            prop.load(in);
            in.close();

        } catch (Exception d) {
            Miscellaneous.printlnErr("Error while reading property file named ="+name);
            Miscellaneous.printlnErr("Check if the file named "+name+ " is missing?");
            Miscellaneous.exit();
        }
        cache.put(createName(name), prop.clone());
        return prop;
    }

    public final Properties getProperties(String name) {
        if (cache.get(createName(name)) == null) {
            loadProperties(name);
        }
        return (Properties) cache.get(createName(name));
    }

    /*
    public static void main(String args[]) {
    // Read properties file.
    Properties pro = PropertyReader.getInstanceOf().
    loadConfigProperties();
    Vector keys = new Vector(pro.keySet());
    Miscellaneous.println(StringUtil.getString(keys, "\n"));
    }*/
}
