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

import java.util.logging.Level;

/**
 * @author Franck WOLFF
 */
public class JdkLogger extends Logger {

    ///////////////////////////////////////////////////////////////////////////
    // Constructor.

    public JdkLogger(String name, LoggingFormatter formatter) {
        super(java.util.logging.Logger.getLogger(name), formatter);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utility getter.

    @Override
	protected java.util.logging.Logger getLoggerImpl() {
    	return (java.util.logging.Logger)super.getLoggerImpl();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Logging methods.

    @Override
	public void info(String message, Object... args) {
        if (isInfoEnabled())
            getLoggerImpl().log(Level.INFO, getFormatter().format(message, args));
    }

    @Override
    public void info(Throwable t, String message, Object... args) {
        if (isInfoEnabled())
            getLoggerImpl().log(Level.INFO, getFormatter().format(message, args), t);
    }

    @Override
    public void trace(String message, Object... args) {
        if (isTraceEnabled())
            getLoggerImpl().log(Level.FINE, getFormatter().format(message, args));
    }

    @Override
    public void trace(Throwable t, String message, Object... args) {
        if (isTraceEnabled())
            getLoggerImpl().log(Level.FINE, getFormatter().format(message, args), t);
    }

    @Override
    public void warn(String message, Object... args) {
        if (isWarnEnabled())
            getLoggerImpl().log(Level.WARNING, getFormatter().format(message, args));
    }

    @Override
    public void warn(Throwable t, String message, Object... args) {
        if (isWarnEnabled())
            getLoggerImpl().log(Level.WARNING, getFormatter().format(message, args), t);
    }

    @Override
    public void debug(String message, Object... args) {
        if (isDebugEnabled())
            getLoggerImpl().log(Level.FINER, getFormatter().format(message, args));
    }

    @Override
    public void debug(Throwable t, String message, Object... args) {
        if (isDebugEnabled())
            getLoggerImpl().log(Level.FINER, getFormatter().format(message, args), t);
    }

    @Override
    public void error(String message, Object... args) {
        if (isErrorEnabled())
            getLoggerImpl().log(Level.SEVERE, getFormatter().format(message, args));
    }

    @Override
    public void error(Throwable t, String message, Object... args) {
        if (isErrorEnabled())
            getLoggerImpl().log(Level.SEVERE, getFormatter().format(message, args), t);
    }

    @Override
    public void fatal(String message, Object... args) {
        if (isFatalEnabled())
            getLoggerImpl().log(Level.SEVERE, getFormatter().format(message, args));
    }

    @Override
    public void fatal(Throwable t, String message, Object... args) {
        if (isFatalEnabled())
            getLoggerImpl().log(Level.SEVERE, getFormatter().format(message, args), t);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Configuration.
    
    @Override
    public void setLevel(org.granite.logging.Level level) {
    	Level jdkLevel = null;
    	switch (level) {
	    	case FATAL: jdkLevel = Level.SEVERE; break;
	    	case ERROR: jdkLevel = Level.SEVERE; break;
	    	case INFO: jdkLevel = Level.INFO; break;
	    	case TRACE: jdkLevel = Level.FINER; break;
	    	case DEBUG: jdkLevel = Level.FINE; break;
	    	case WARN: jdkLevel = Level.WARNING; break;
	    	default: throw new IllegalArgumentException("Unknown logging level: " + level);
    	}
        getLoggerImpl().setLevel(jdkLevel);
    }

    @Override
    public boolean isDebugEnabled() {
        return getLoggerImpl().isLoggable(Level.FINE);
    }

    @Override
    public boolean isErrorEnabled() {
        return getLoggerImpl().isLoggable(Level.SEVERE);
    }

    @Override
    public boolean isFatalEnabled() {
        return getLoggerImpl().isLoggable(Level.SEVERE);
    }

    @Override
    public boolean isInfoEnabled() {
        return getLoggerImpl().isLoggable(Level.INFO);
    }

    @Override
    public boolean isTraceEnabled() {
        return getLoggerImpl().isLoggable(Level.FINER);
    }

    @Override
    public boolean isWarnEnabled() {
        return getLoggerImpl().isLoggable(Level.WARNING);
    }
}
