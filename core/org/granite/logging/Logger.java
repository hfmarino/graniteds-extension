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

package org.granite.logging;

import java.lang.reflect.Constructor;

import org.granite.util.ClassUtil;

/**
 * @author Franck WOLFF
 */
public abstract class Logger {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

	public static final String LOGGER_IMPL_SYSTEM_PROPERTY = "org.granite.logger.impl";
	
	private static final boolean log4jAvailable;
	static {
		boolean available = false;
		try {
			ClassUtil.forName("org.apache.log4j.Logger");
			available = true;
		} catch (Exception e) {
		}
		log4jAvailable = available;
	}
	
    private final Object loggerImpl;
    private final LoggingFormatter formatter;

    ///////////////////////////////////////////////////////////////////////////
    // Constructor.
    
    protected Logger(Object loggerImpl, LoggingFormatter formatter) {
        this.loggerImpl = loggerImpl;
        this.formatter = formatter;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Getters.
    
    protected Object getLoggerImpl() {
    	return loggerImpl;
    }
    
    protected LoggingFormatter getFormatter() {
		return formatter;
	}

    ///////////////////////////////////////////////////////////////////////////
    // Static initializers.

	public static Logger getLogger() {
        return getLogger(new DefaultLoggingFormatter());
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName(), new DefaultLoggingFormatter());
    }

    public static Logger getLogger(String name) {
        return getLogger(name, new DefaultLoggingFormatter());
    }

    public static Logger getLogger(LoggingFormatter formatter) {
        Throwable t = new Throwable();
        StackTraceElement[] stes = t.getStackTrace();
        if (stes.length < 2)
            throw new RuntimeException("Illegal instantiation context (stacktrace elements should be of length >= 2)", t);
        return getLogger(stes[1].getClassName());
    }

    public static Logger getLogger(Class<?> clazz, LoggingFormatter formatter) {
        return getLogger(clazz.getName(), formatter);
    }

    public static Logger getLogger(String name, LoggingFormatter formatter) {
    	String loggerImplClass = System.getProperty(LOGGER_IMPL_SYSTEM_PROPERTY);
    	if (loggerImplClass != null) {
    		try {
        		Class<? extends Logger> clazz = ClassUtil.forName(loggerImplClass, Logger.class);
        		Constructor<? extends Logger> constructor = clazz.getConstructor(String.class, LoggingFormatter.class);
        		return constructor.newInstance(name, formatter);
			} catch (Exception e) {
				throw new RuntimeException(
					"Could not create instance of: " + loggerImplClass +
					" (" + LOGGER_IMPL_SYSTEM_PROPERTY + " system property)", e);
			}
    	}
        return log4jAvailable ? new Log4jLogger(name, formatter) : new JdkLogger(name, formatter);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Logging methods.

    public abstract void info(String message, Object... args);
    public abstract void info(Throwable t, String message, Object... args);

    public abstract void trace(String message, Object... args);
    public abstract void trace(Throwable t, String message, Object... args);
    
    public abstract void warn(String message, Object... args);
    public abstract void warn(Throwable t, String message, Object... args);

    public abstract void debug(String message, Object... args);
    public abstract void debug(Throwable t, String message, Object... args);

    public abstract void error(String message, Object... args);
    public abstract void error(Throwable t, String message, Object... args);

    public abstract void fatal(String message, Object... args);
    public abstract void fatal(Throwable t, String message, Object... args);

    ///////////////////////////////////////////////////////////////////////////
    // Configuration.

    public abstract void setLevel(Level level);

    public abstract boolean isDebugEnabled();

    public abstract boolean isErrorEnabled();

    public abstract boolean isFatalEnabled();

    public abstract boolean isInfoEnabled();

    public abstract boolean isTraceEnabled();

    public abstract boolean isWarnEnabled();
}
