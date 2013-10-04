/*  
    Copyright (c) Stephan Hartmann (www.metamesh.de)
    
    This file is part of Metamesh's RFS driver for OpenCms.

    Metamesh's RFS driver for OpenCms is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Metamesh's RFS driver for OpenCms is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Metamesh's RFS driver for OpenCms. 
    If not, see <http://www.gnu.org/licenses/>.

    Diese Datei ist Teil von Metamesh's RFS Treiber für OpenCms.

    Metamesh's RFS Treiber für OpenCms ist Freie Software: Sie können es unter den Bedingungen
    der GNU General Public License, wie von der Free Software Foundation,
    Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
    veröffentlichten Version, weiterverbreiten und/oder modifizieren.

    Metamesh's RFS Treiber für OpenCms wird in der Hoffnung, dass es nützlich sein wird, aber
    OHNE JEDE GEWÄHELEISTUNG, bereitgestellt; sogar ohne die implizite
    Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Siehe die GNU General Public License für weitere Details.

    Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
    Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */

package com.metamesh.opencms.rfs.file;

import java.io.File;
import java.io.Serializable;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.util.CmsUUID;

public class RfsCmsFile extends CmsFile implements RfsCmsResource, Serializable {

  private static final long serialVersionUID = 8465404974838705568L;

  private File baseFolder;
  
  private File rfsFile;
  
  private String sourcePath;
  
  private String webappPath;

  public RfsCmsFile(CmsUUID structureId, CmsUUID resourceId, 
      String path,
      int type, int flags, CmsUUID projectId, CmsResourceState state,
      long dateCreated, CmsUUID userCreated, long dateLastModified,
      CmsUUID userLastModified, long dateReleased, long dateExpired,
      int linkCount, int length, long dateContent, int version, byte[] content,
      File rfsBaseFolder, File rfsFile, String sourcePath, String webappPath) {
    super(structureId, resourceId, path, type, flags, projectId, state,
        dateCreated, userCreated, dateLastModified, userLastModified, dateReleased,
        dateExpired, linkCount, length, dateContent, version, content);
    baseFolder = rfsBaseFolder;
    this.webappPath = webappPath;
    this.sourcePath = sourcePath;
    this.rfsFile = rfsFile;
  }

  @Override
  public File getRfsBaseFolder() {
    return baseFolder;
  }

  @Override
  public String getWebappPath() {
    return webappPath;
  }

  @Override
  public File getRfsFile() {
    return rfsFile;
  }

  public String getSourcePath() {
    return sourcePath;
  }

}
