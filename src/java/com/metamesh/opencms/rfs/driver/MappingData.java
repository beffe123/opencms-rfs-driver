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

package com.metamesh.opencms.rfs.driver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opencms.file.CmsResource;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;

import com.metamesh.opencms.rfs.ManifestInfo;
import com.metamesh.opencms.rfs.file.RfsCmsFolder;

public class MappingData {

  private File rfsMapping;
  
  private String webappBasePath;
  
  private String vfsMountPath;
  
  private ResourceCreator rc;

  ManifestInfo manifestInfo;

  private HashMap<String, Map<String, Object>> manifestEntries = null;
  private HashMap<String, RfsCmsFolder> rootPaths = new HashMap<String, RfsCmsFolder>();
  private HashMap<CmsUUID, CmsResource> resourcesByStructureId = new HashMap<CmsUUID, CmsResource>();
  private HashMap<CmsUUID, CmsResource> resourcesByResourceId = new HashMap<CmsUUID, CmsResource>();
  private HashMap<String, CmsResource> resourcesByPath = new HashMap<String, CmsResource>();
  private HashMap<String, List<CmsResource>> resourcesByParent = new HashMap<String, List<CmsResource>>();
  private HashMap<String, Set<CmsResource>> resourcesByProperty = new HashMap<String, Set<CmsResource>>();
  
  private HashMap<CmsUUID, Map<String, String>> propertiesByStructureId = new HashMap<CmsUUID, Map<String,String>>();

  public MappingData(File rfsMapping, String webappBasePath, String vfsMountPath) {
    this.rfsMapping = rfsMapping;
    this.webappBasePath = webappBasePath;
    this.vfsMountPath = vfsMountPath;
    this.rc = new ResourceCreator(this);
  }
  
  public RfsCmsFolder getRootPathForSubPath(String subPath) {
    for (Map.Entry<String, RfsCmsFolder> entry: rootPaths.entrySet()) {
      if (subPath.startsWith(entry.getKey())) return entry.getValue();
    }
    return null;
  }
  
  public File getRfsMapping() {
    return rfsMapping;
  }

  public void setRfsMapping(File rfsMapping) {
    this.rfsMapping = rfsMapping;
  }

  public ManifestInfo getManifestInfo() {
    return manifestInfo;
  }

  public void setManifestInfo(ManifestInfo manifestInfo) {
    this.manifestInfo = manifestInfo;
    manifestEntries = new HashMap<String, Map<String,Object>>();
  }

  public HashMap<CmsUUID, CmsResource> getResourcesByStructureId() {
    return resourcesByStructureId;
  }

  public void setResourcesByStructureId(
      HashMap<CmsUUID, CmsResource> resourcesByStructureId) {
    this.resourcesByStructureId = resourcesByStructureId;
  }

  public HashMap<CmsUUID, CmsResource> getResourcesByResourceId() {
    return resourcesByResourceId;
  }

  public void setResourcesByResourceId(
      HashMap<CmsUUID, CmsResource> resourcesByResourceId) {
    this.resourcesByResourceId = resourcesByResourceId;
  }

  public HashMap<String, CmsResource> getResourcesByPath() {
    return resourcesByPath;
  }

  public void setResourcesByPath(HashMap<String, CmsResource> resourcesByPath) {
    this.resourcesByPath = resourcesByPath;
  }

  public HashMap<String, List<CmsResource>> getResourcesByParent() {
    return resourcesByParent;
  }

  public void setResourcesByParent(
      HashMap<String, List<CmsResource>> resourcesByParent) {
    this.resourcesByParent = resourcesByParent;
  }

  public HashMap<String, Set<CmsResource>> getResourcesByProperty() {
    return resourcesByProperty;
  }

  public void setResourcesByProperty(
      HashMap<String, Set<CmsResource>> resourcesByProperty) {
    this.resourcesByProperty = resourcesByProperty;
  }

  public HashMap<CmsUUID, Map<String, String>> getPropertiesByStructureId() {
    return propertiesByStructureId;
  }

  public void setPropertiesByStructureId(
      HashMap<CmsUUID, Map<String, String>> propertiesByStructureId) {
    this.propertiesByStructureId = propertiesByStructureId;
  }

  public HashMap<String, RfsCmsFolder> getRootPaths() {
    return rootPaths;
  }

  public void setRootPaths(HashMap<String, RfsCmsFolder> rootPaths) {
    this.rootPaths = rootPaths;
  }

  public void setHasModuleConfig() {
    if (manifestInfo != null) {
      manifestInfo.setHasModuleConfig(true);
    }
  }
  
  public void addManifestEntry(Map<String, Object> entry) {
    String path = (String) entry.get("destination");
    manifestEntries.put(path, entry);
  }
  
  public HashMap<String, Map<String, Object>> getManifestEntries() {
    return manifestEntries;
  }

  public String getWebappBasePath() {
    return webappBasePath;
  }

  public String getVfsMountPath() {
    return vfsMountPath;
  }
  
  public void sync(boolean override) {
    if (rfsMapping.isDirectory()) {
      syncFolder(vfsMountPath, rfsMapping, rfsMapping, webappBasePath, true, override);
    }
    else {
      syncFile();
    }
  }
  
  private void syncFile() {
    Map<String, Object> values = null;
    
    values = new HashMap<String, Object>();
    values.put("source", "");
    values.put("destination", vfsMountPath);
    values.put("isOne2one", "true");
    rc.addResource(values);
  }
  
  private void syncFolder(RfsCmsFolder folder, boolean recursive, boolean override) {
    syncFolder(folder.getRootPath(), folder.getRfsFile(), folder.getRfsBaseFolder(), folder.getWebappPath(),
        recursive, override);
  }

  private void syncFolder(String vfsFolderPath, File folder, File baseFolder, String foldersWebappPath,
      boolean recursive, boolean override) {
    
    CmsResource resource = null;
    
    String path = CmsFileUtil.removeTrailingSeparator(vfsFolderPath);
    
    if (override || !resourcesByPath.containsKey(path)) {
      
      resource = rc.addResource(folder);
    }
    else {
      resource = resourcesByPath.get(path);
    }
    
    if (folder.isDirectory()) {
      // add sub resources

      File[] subFilesAndFolders = folder.listFiles();
      if (subFilesAndFolders != null) {
        List<CmsResource> addedChildResources = new ArrayList<CmsResource>(subFilesAndFolders.length);
        
        for (File sub: subFilesAndFolders) {
          
          String name = sub.getName();
          String vfsPath = vfsFolderPath + name;
          if (sub.isDirectory()) {
            // TODO: make configurable
            if (".svn".equals(sub.getName())) continue;
            vfsPath = CmsFileUtil.addTrailingSeparator(vfsPath);
          }
          
          if ("manifest.xml".equals(sub.getName())) continue;
          
          CmsResource subResource = null;
          if (override || !resourcesByPath.containsKey(CmsFileUtil.removeTrailingSeparator(vfsPath))) {
            
            subResource = rc.addResource(sub);
                
          }
          else {
            subResource = resourcesByPath.get(CmsFileUtil.removeTrailingSeparator(vfsPath));
          }
          if (sub.isDirectory() && recursive) {
            syncFolder((RfsCmsFolder)subResource, recursive, override);
          }
          addedChildResources.add(subResource);
        }
        
        List<CmsResource> children = resourcesByParent.get(vfsFolderPath);
        if (children != null && children.size() > 0) {
          Iterator<CmsResource> it = children.iterator();
          while (it.hasNext()) {
            CmsResource r = it.next();
            if (r != null && !addedChildResources.contains(r)) {
              resourcesByPath.remove(CmsFileUtil.removeTrailingSeparator(r.getRootPath()));
              propertiesByStructureId.remove(r.getStructureId());
              resourcesByStructureId.remove(r.getStructureId());
              resourcesByResourceId.remove(r.getResourceId());
              it.remove();
            }
          }
        }
      }
      
    }
    
  }
}
