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

package com.metamesh.opencms.rfs;

import java.io.File;

public class ManifestInfo {

  File moduleFolder;
  String webappBasePath;
  File manifest;
  long parseDate;
  boolean hasModuleConfig;
  
  public ManifestInfo(File moduleFolder, String webappBasePath, File manifest,
      long parseDate) {
    super();
    this.moduleFolder = moduleFolder;
    this.webappBasePath = webappBasePath;
    this.manifest = manifest;
    this.parseDate = parseDate;
  }
  public File getModuleFolder() {
    return moduleFolder;
  }
  public void setModuleFolder(File moduleFolder) {
    this.moduleFolder = moduleFolder;
  }
  public String getWebappBasePath() {
    return webappBasePath;
  }
  public void setWebappBasePath(String webappBasePath) {
    this.webappBasePath = webappBasePath;
  }
  public File getManifest() {
    return manifest;
  }
  public void setManifest(File manifest) {
    this.manifest = manifest;
  }
  public long getParseDate() {
    return parseDate;
  }
  public void setParseDate(long parseDate) {
    this.parseDate = parseDate;
  }
  public boolean hasModuleConfig() {
    return hasModuleConfig;
  }
  public void setHasModuleConfig(boolean hasModuleConfig) {
    this.hasModuleConfig = hasModuleConfig;
  }

  
}
