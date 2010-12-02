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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Franck WOLFF
 */
public class ZipScannedItem extends AbstractScannedItem {

    private final ZipFile file;
    private final ZipEntry entry;

    public ZipScannedItem(Scanner scanner, ZipScannedItem marker, ZipFile file, ZipEntry entry) {
        super(scanner, marker);

        this.file = file;
        this.entry = entry;
    }

    public ZipFile getFile() {
        return file;
    }

    public ZipEntry getEntry() {
        return entry;
    }

    public String getName() {
        String path = entry.getName();
        int lastSlash = path.lastIndexOf('/');
        return (lastSlash >= 0 ? path.substring(lastSlash + 1) : path);
    }

    public String getRelativePath() {
        return entry.getName();
    }

    public String getAbsolutePath() {
        return new File(file.getName()).getAbsolutePath() + '!' + entry.getName();
    }

    public long getSize() {
        return entry.getSize();
    }

    public InputStream getInputStream() throws IOException {
        return file.getInputStream(entry);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof ZipScannedItem))
            return false;
        return file.equals(((ZipScannedItem)obj).file) && entry.equals(((ZipScannedItem)obj).entry);
    }

    @Override
    public int hashCode() {
        return file.hashCode() + (31 * entry.hashCode());
    }
}
