/*
  GRANITE DATA SERVICES
  Copyright (C) 2007-2010 ADEQUATE SYSTEMS SARL

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.scan;

import java.io.InputStream;
import java.util.Properties;

import org.granite.logging.Logger;

/**
 * @author Franck WOLFF
 */
public class ScannerFactory {
	
	private static final Logger log = Logger.getLogger(ScannerFactory.class);

	
	public static Scanner createScanner(ScannedItemHandler handler, String marker) {
        if (isVFSAvailable() && !isEmbedded() && isJBoss(5)) {
        	log.debug("Using VFS aware scanner");
        	try {
        		Class<?> vfsScannerClass = ScannerFactory.class.getClassLoader().loadClass("org.granite.scan.VFSScanner");
        		return (Scanner)vfsScannerClass.getConstructor(ScannedItemHandler.class, String.class).newInstance(handler, marker);        		
        	}
        	catch (Exception e) {
        		throw new RuntimeException("Could not create VFSScanner", e);
        	}
        }
        if (isVFS3Available() && !isEmbedded() && isJBoss(6)) {
        	log.debug("Using VFS3 aware scanner");
        	try {
        		Class<?> vfsScannerClass = ScannerFactory.class.getClassLoader().loadClass("org.granite.scan.VFS3Scanner");
        		return (Scanner)vfsScannerClass.getConstructor(ScannedItemHandler.class, String.class).newInstance(handler, marker);        		
        	}
        	catch (Exception e) {
        		throw new RuntimeException("Could not create VFS3Scanner", e);
        	}
        }
        
    	log.debug("Using default Scanner");
    	return new URLScanner(handler, marker);
	}
   
	private static boolean isVFSAvailable() {
		try {
			Class.forName("org.jboss.virtual.VFS");
			log.trace("VFS detected");
			return true;
		}
		catch (Throwable t) {
			return false;
		}
	}
	   
	private static boolean isVFS3Available() {
		try {
			Class.forName("org.jboss.vfs.VFS");
			log.trace("VFS3 detected");
			return true;
		}
		catch (Throwable t) {
			return false;
		}
	}
	
	private static boolean isJBoss(int version) {
		try {
			Properties props = new Properties();
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/org/jboss/version.properties");
			if (is == null)
				is = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/jboss/version.properties");
			props.load(is);
			is.close();
			int major = Integer.parseInt(props.getProperty("version.major"));
			int minor = Integer.parseInt(props.getProperty("version.minor"));
			
			boolean isJBossVersion = major >= version && minor >= 0;
			if (isJBossVersion)
				log.trace("JBoss " + major + "." + minor + " detected");
			
			return isJBossVersion;
		}
		catch (Throwable t) {
			return false;
		}
	}
	
	private static boolean isEmbedded() {
		try {
			Class.forName("org.jboss.embedded.Bootstrap");
			log.trace("JBoss Embedded detected");
			return true;
		}
		catch (Throwable t) {
			return false;
		}
	}
}
