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
package takatuka.classreader.logic.constants;

/**
 * <p>Title: </p>
 * <p>Description:
 * It defines sizes (in bytes) of different sections of class file
 * It is based on section 4.1 at http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public final class SectionSizes {
    private SectionSizes() {
        super();
    }

    public final static int MAGIC_NUMBER_SIZE = 4; //bytes
    public final static int MINOR_VERSION_SIZE = 2;
    public final static int MAGOR_VERSION_SIZE = 2;
    public final static int CONSTANT_POOL_COUNT_SIZE = 2;
    public final static int ACCESS_FLAG_SIZE = 2;
    public final static int THIS_CLASS_SIZE = 2;
    public final static int SUPER_CLASS_SIZE = 2;
    public final static int INTERFACES_COUNT_SIZE = 2;
    public final static int FIELD_COUNT_SIZE = 2;
    public final static int METHOD_COUNT_SIZE = 2;
    public final static int ATTRIBUTES_COUNT_SIZE = 2;

}
