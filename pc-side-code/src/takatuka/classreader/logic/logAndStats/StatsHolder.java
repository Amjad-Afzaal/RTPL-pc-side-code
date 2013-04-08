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
package takatuka.classreader.logic.logAndStats;

import java.io.*;
import java.util.*;
import takatuka.classreader.logic.*;
import takatuka.classreader.logic.util.*;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class StatsHolder {

    private final static StatsHolder statHolder = new StatsHolder();
     
    /**
     * the map will have groups.
     * The key of the map is groupId (String) and null will always point to a same group.
     * The value of the map is Properties object
     */
    private static TreeMap map = new TreeMap();

    private StatsHolder() {
    }

    public static StatsHolder getInstanceOf() {
        return statHolder;
    }

    public void addStatArgs(String[] args) {
        addArgument(args);
    }

    private Properties getGroup(String group) {
        if (group == null) {
            group = "@default";
        }
        Properties prop = (Properties) map.get(group);
        if (prop == null) {
            prop = new Properties();
            map.put(group, prop);
        }
        return prop;
    }

    public final void addStat(String key, int value) {
        addStat(null, key, value);
    }

    public final void addStat(String key, Object value) {
        addStat(null, key, value);
    }

    public final void addStat(String group, String key, Object value) {
        Properties prop = getGroup(group);
        prop.setProperty(key, value.toString());
    }

    private final void addArgument(String args[]) {
        String key = "args";
        String value = "[";
        for (int loop = 0; loop < args.length; loop++) {
            value += args[loop];
            if (loop + 1 < args.length) {
                value += " ";
            }
        }
        value += "]";
        addStat(key, key, value);
    }

    private void writeGroup(Properties prop, RandomAccessFile rm)
            throws Exception {
        //
        rm.writeBytes("\n");
        rm.writeBytes("\t" + prop.toString());
    //prop.store(new FileOutputStream(propertyOutFile), null);
    }

    public void writeProperties() {
        try {
            String propertyOutFile = StartMeAbstract.outputWriter.
            getOutputDirectory()+"/stat.properties";
            Vector keys = new Vector(map.keySet());
            File file = new File(propertyOutFile);
            if (file.exists()) {
                file.delete();
            }
            RandomAccessFile rm = new RandomAccessFile(file, "rw");
            for (int loop = 0; loop < keys.size(); loop++) {
                String groupId = (String) keys.elementAt(loop);
                Properties group = (Properties) map.get(groupId);
                rm.writeBytes("\n[" + groupId + ":");
                writeGroup(group, rm);
                rm.writeBytes("]\n");
            }
            rm.writeBytes("\n\n");
            rm.close();
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }
}
