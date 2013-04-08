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
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public interface AccessFlagValues {

    //
    public final static int ACC_PUBLIC = 0x0001; //Declared public; may be accessed from outside its package.
    public final static int ACC_PRIVATE = 0x0002; //Declared private; usable only within the defining class.
    public final static int ACC_PROTECTED = 0x0004; //Declared protected; may be accessed within subclasses.
    public final static int ACC_STATIC = 0x0008; // 	Declared static.
    public final static int ACC_FINAL = 0x0010; //Declared final; no subclasses allowed.
    public final static int ACC_SYNCHRONIZED = 0x0020; // declared sync; invocation in wrapped in a monitor lock
    public final static int ACC_NATIVE = 0x0100; //native
    public final static int ACC_ABSTRACT = 0x0400; // 	Declared abstract; may not be instantiated.
    //
    public final static int ACC_SUPER = 0x0020; // Treat superclass methods specially when invoked by the invokespecial instruction.
    public final static int ACC_VOLATILE = 0x0040; // Declared volatile; cannot be cached.
    public final static int ACC_TRANSIENT = 0x0080;
    public final static int ACC_INTERFACE = 0x0200; // 	Is an interface, not a class.
}
