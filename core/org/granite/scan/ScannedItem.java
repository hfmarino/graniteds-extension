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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Franck WOLFF
 */
public interface ScannedItem {

    public Scanner getScanner();
    public ScannedItem getMarker();

    public String getName();
    public String getExtension();
    public String getRelativePath();
    public String getAbsolutePath();
    public String getClassName();

    public long getSize();
    public InputStream getInputStream() throws IOException;
    public byte[] getContent() throws IOException;

    public Properties loadAsProperties() throws IOException, IllegalArgumentException;
    public Class<?> loadAsClass() throws ClassNotFoundException, IOException, ClassFormatError;
}
