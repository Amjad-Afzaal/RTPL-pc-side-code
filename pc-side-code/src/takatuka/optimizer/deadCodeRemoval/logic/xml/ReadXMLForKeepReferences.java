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
package takatuka.optimizer.deadCodeRemoval.logic.xml;

import takatuka.optimizer.deadCodeRemoval.dataObj.xml.*;
import takatuka.classreader.logic.util.*;
import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import java.util.*;
import takatuka.classreader.logic.file.*;

/**
 *
 * @author Faisla Aslam
 */
public class ReadXMLForKeepReferences {

    private Vector packages = null;
    private Vector classfiles = null;
    private static final String PACKAGES = "ns0:Package";
    private static final String CLASS_FILE = "ns0:ClassFile";
    private static final String FIELD = "ns0:Field";
    private static final String FUNCTION = "ns0:Function";
    private static final String ATT_NAME = "name";
    private static final String ATT_DESC = "description";
    private static final String ATT_INCLUDE_SUB_PACK = "includeSubPackages";
    private static final String ATT_INCLUDE_ALL_FUNCTION = "includeAllFunctions";
    private static final String ATT_INCLUDE_ALL_FIELDS = "includeAllFields";
    private final static ReadXMLForKeepReferences readXML = new ReadXMLForKeepReferences();
    private final static String XML_FOR_FILE_EXIST = "XML_FOR_KEEPING_REFERENCES";
    private final static ConfigPropertyReader cProReader = ConfigPropertyReader.getInstanceOf();
    
    public static final ReadXMLForKeepReferences getInstanceOf() {
        return readXML;
    }

    public Vector getPackagesXML() {
        if (packages == null) {
            readXML();
        }
        return (Vector) packages.clone();
    }

    public Vector getClassFileXML() {
        if (classfiles == null) {
            readXML();
        }
        return (Vector) classfiles.clone();
    }

    private void readPackages(NodeList packages) {
        int numberOfPackages = packages.getLength();
        //Miscellaneous.println(" total number of packages = " + numberOfPackages);
        PackageXML packXML = null;
        for (int index = 0; index < numberOfPackages; index++) {
            Node packageNode = packages.item(index);
            Element packageXMLElm = (Element) packageNode;
            String name = packageXMLElm.getAttribute(ATT_NAME);
            String sIncludeSubPack = packageXMLElm.getAttribute(ATT_INCLUDE_SUB_PACK);
            packXML = new PackageXML(name, Boolean.parseBoolean(sIncludeSubPack));
            this.packages.addElement(packXML);
        //Miscellaneous.println("package = " + packXML);
        }
    }

    private Vector readFunctionsAndFields(NodeList functionOrFieldList) {
        Vector ret = new Vector();
        int listSize = functionOrFieldList.getLength();
        FunctionAndFieldXML funFieldXML = null;
        for (int index = 0; index < listSize; index++) {
            Node funFieldNode = functionOrFieldList.item(index);
            Element funFieldNodeElm = (Element) funFieldNode;
            String name = funFieldNodeElm.getAttribute(ATT_NAME);
            String desc = funFieldNodeElm.getAttribute(ATT_DESC);
            funFieldXML = new FunctionAndFieldXML(name, desc);
            ret.addElement(funFieldXML);
        }
        return ret;
    }

    private void readClassFiles(NodeList classFiles, Document doc) {
        int numberOfClassFiles = classFiles.getLength();
        //Miscellaneous.println(" total number of class files " + numberOfClassFiles);
        ClassFileXML classXML = null;
        for (int index = 0; index < numberOfClassFiles; index++) {
            Node classNode = classFiles.item(index);
            Element classXMLElm = (Element) classNode;
            String name = classXMLElm.getAttribute(ATT_NAME);
            //Miscellaneous.println("name = "+name);
            String sIncludeAllFunctions = classXMLElm.getAttribute(ATT_INCLUDE_ALL_FUNCTION);
            String sIncludeAllFields = classXMLElm.getAttribute(ATT_INCLUDE_ALL_FIELDS);
            NodeList functionsList = classXMLElm.getElementsByTagName(FUNCTION);
            NodeList fieldList = classXMLElm.getElementsByTagName(FIELD);
            Vector fields = readFunctionsAndFields(fieldList);
            Vector methods = readFunctionsAndFields(functionsList);
            classXML = new ClassFileXML(name, Boolean.parseBoolean(sIncludeAllFunctions), Boolean.parseBoolean(sIncludeAllFields), methods, fields);

            this.classfiles.addElement(classXML);
        //Miscellaneous.println("class  = " + classXML);
        }

    }

    private void readXML() {
        if (packages != null) {
            return; //no reading again
        }
        packages = new Vector();
        classfiles = new Vector();
        try {
            String fileName = cProReader.getConfigProperty(XML_FOR_FILE_EXIST);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            File file = new File(fileName);
            if (!file.exists()) {
                Miscellaneous.printlnErr("Cannot find XML file  ="+fileName);
                Miscellaneous.exit();
            }
            Document doc = docBuilder.parse(new File(fileName));
            // normalize text representation
            doc.getDocumentElement().normalize();
            //Miscellaneous.println("Root element of the doc is " +
            //        doc.getDocumentElement().getNodeName());
            NodeList packagesLocal = doc.getElementsByTagName(PACKAGES);
            NodeList classFilesLocal = doc.getElementsByTagName(CLASS_FILE);
            readPackages(packagesLocal);
            readClassFiles(classFilesLocal, doc);
            return;
        } catch (SAXParseException err) {
            Miscellaneous.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
            Miscellaneous.println(" " + err.getMessage());

        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();

        } catch (Throwable t) {
            t.printStackTrace();
        }
        Miscellaneous.exit();
    }

    public static void main(String args[]) {
        ReadXMLForKeepReferences.getInstanceOf().readXML();

    }
}
