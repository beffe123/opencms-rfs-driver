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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import com.metamesh.opencms.rfs.file.RfsCmsFile;
import com.metamesh.opencms.rfs.file.RfsCmsFolder;

public class ResourceCreator {
  private static HashMap<String, Integer> resourceTypeMap = new HashMap<String, Integer>();
  static {
    resourceTypeMap.put("folder", 0);
    resourceTypeMap.put("plain", 1);
    resourceTypeMap.put("binary", 2);
    resourceTypeMap.put("image", 3);
    resourceTypeMap.put("jsp", 4);
    resourceTypeMap.put("pointer", 5);
    resourceTypeMap.put("xmlpage", 6);
    resourceTypeMap.put("xmlcontent", 7);
    resourceTypeMap.put("imagegallery", 8);
    resourceTypeMap.put("downloadgallery", 9);
    resourceTypeMap.put("linkgallery", 10);
    resourceTypeMap.put("htmlgallery", 11);
    resourceTypeMap.put("tablegallery", 12);
    resourceTypeMap.put("containerpage", 13);
    resourceTypeMap.put("sitemap_config", 14);
    resourceTypeMap.put("htmlredirect", 15);
    resourceTypeMap.put("groupcontainer", 17);
    resourceTypeMap.put("containerpage_template", 21);
    resourceTypeMap.put("subsitemap", 23);
    resourceTypeMap.put("content_folder", 24);
    resourceTypeMap.put("module_config", 28);
    resourceTypeMap.put("inheritance_group", 303);
    resourceTypeMap.put("inheritance_config", 304);
    resourceTypeMap.put("seo_file", 305);
  }
  
  private MappingData md;
  
  public ResourceCreator(MappingData md) {
    this.md = md;
  }
  
  public CmsResource addResource(File f) {
  
    Map<String, Object> values = null;
    
    values = new HashMap<String, Object>();
    String source = f.getAbsolutePath().substring(md.getRfsMapping().getAbsolutePath().length());
    if (source.startsWith("/") && source.length() > 1) {
      source = source.substring(1);
    }
    if (md.getManifestEntries() != null &&  md.getManifestEntries().containsKey(source)) {
      values.putAll(md.getManifestEntries().get(source));
    }
    else {
      values.put("source", source);
      values.put("destination", source);
    }
    
    return addResource(values);
  }
    
  public CmsResource addResource(Map<String, Object> values) {
    String destination = (String)values.get("destination");
    String source = (String)values.get("source");
    if (source == null) source = destination;
    String sType = (String)values.get("type");
    String sStructureUuid = (String)values.get("uuidstructure");
    String sResourceUuid = (String)values.get("uuidresource");
    String sFlags = (String)values.get("flags");
    Map<String, String> properties = (Map<String, String>) values.get("properties");
    boolean isOne2one = "true".equals(values.get("isOne2one"));
    
    boolean isFolder = false;
    
    
    String path = destination;
    
    if (!isOne2one) {
      String mp = md.getVfsMountPath();
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(destination)) {
        path = mp;
      }
      else if ((mp.charAt(mp.length() - 1) == '/' && destination.charAt(0) != '/')
          ||
          (mp.charAt(mp.length() - 1) != '/' && destination.charAt(0) == '/')) {
        path = mp + destination;
      }
      else if (mp.charAt(mp.length() - 1) != '/' && destination.charAt(0) != '/') {
        path = mp + "/" + destination;
      }
      else {
        path = mp + destination.substring(1);
      }
    }
    
    int flags = 0;
    try {
      flags = Integer.parseInt(sFlags);
    } catch (NumberFormatException nfe) {
      //nfe.printStackTrace();
    }
    
    File f = null;
    if (isOne2one) {
      f = md.getRfsMapping();
    }
    else {
      f = new File(md.getRfsMapping(), source);
    }
    
    CmsUUID structureUuid = null;
    if (sStructureUuid != null) {
      structureUuid = new CmsUUID(sStructureUuid);
    }
    else {
      structureUuid = CmsUUID.getConstantUUID(path);
    }
    CmsUUID resourceUuid = null;
    if (sResourceUuid != null) {
      resourceUuid = new CmsUUID(sResourceUuid);
    }
    else {
      resourceUuid = CmsUUID.getConstantUUID(f.getAbsolutePath());
    }
    
    
    CmsUUID user = CmsUUID.getNullUUID();

    int type = getType(sType, f);
    
    String webappPath = null;
    if (md.getWebappBasePath() != null) {
      if (isOne2one) {
        webappPath = "/" + md.getWebappBasePath();
      }
      else {
        webappPath = "/" + md.getWebappBasePath() + "/" + source;
      }
    }
    
    CmsResource resource = null;
    
    resource = addResourceInternal(f, path, structureUuid, resourceUuid, md.getRfsMapping(), 
        webappPath, type, flags, user, source);
    
    if (resource == null) return null;
    
    String parentPath = CmsResource.getParentFolder(path);
    
    addChildToParent(parentPath, resource);
    
    if (properties != null) {
      md.getPropertiesByStructureId().put(structureUuid, properties);
      
      for (String propertyName: properties.keySet()) {
        addResourceByProperty(propertyName, resource);
      }
    }
    return resource;
  }
  
  private CmsResource addResourceInternal(File f, String path, CmsUUID structureUuid, CmsUUID resourceUuid,
      File baseFolder, String webappPath, int type, int flags, CmsUUID user, String sourcePath) {
    CmsResource resource = null;
    
    if (!f.exists()) return null;
    
    if (f.isDirectory()) {
      path = CmsFileUtil.addTrailingSeparator(path);
      RfsCmsFolder folder = new RfsCmsFolder(structureUuid, resourceUuid,
          path, type, flags, CmsProject.ONLINE_PROJECT_ID, CmsResourceState.STATE_UNCHANGED,
          0l, user, f.lastModified(), user, 0, Long.MAX_VALUE, 1, baseFolder,
          f, sourcePath, webappPath);
      
      resource = folder;
      
      // check if this is a root folder
      RfsCmsFolder correspondingRootPath = md.getRootPathForSubPath(path);
      if (correspondingRootPath == null) {
        md.getRootPaths().put(path, folder);
      }
      
    }
    else {
      RfsCmsFile file = new RfsCmsFile(structureUuid, resourceUuid, path, type,
          flags, CmsProject.ONLINE_PROJECT_ID, CmsResourceState.STATE_UNCHANGED, 
          0l, user, f.lastModified(), user, 0, Long.MAX_VALUE, 
          1, (int)f.length(), f.lastModified(), 1, new byte[]{}, baseFolder, 
          f, sourcePath, webappPath);
      
      resource = file;
    }

    md.getResourcesByStructureId().put(resource.getStructureId(), resource);
    md.getResourcesByResourceId().put(resource.getResourceId(), resource);
    md.getResourcesByPath().put(CmsFileUtil.removeTrailingSeparator(resource.getRootPath()), resource);
    
    return resource;
  }
  
  private void addResourceByProperty(String property, CmsResource resource) {
    Set<CmsResource> resources = md.getResourcesByProperty().get(property);
    if (resources == null) {
      resources = new TreeSet<CmsResource>();
      md.getResourcesByProperty().put(property, resources);
    }
    resources.add(resource);
  }
  
  private void addChildToParent(CmsResource parentFolder, CmsResource child) {
    List<CmsResource> children = md.getResourcesByParent().get(parentFolder.getRootPath());
    if (children == null) {
      children = new ArrayList<CmsResource>();
      md.getResourcesByParent().put(parentFolder.getRootPath(), children);
    }
    children.add(child);
  }
  
  private void addChildToParent(String parentFolder, CmsResource child) {
    List<CmsResource> children = md.getResourcesByParent().get(parentFolder);
    if (children == null) {
      children = new ArrayList<CmsResource>();
      md.getResourcesByParent().put(parentFolder, children);
    }
    children.add(child);
  }

  public static int getType(String sType, File file) {
    Integer type = null;
    
    CmsSystemInfo csi = OpenCms.getSystemInfo();
    if (csi != null) {
      try {
        type = OpenCms.getResourceManager().getResourceType(sType).getTypeId();
        
        // map unknown to plain
        if (type.intValue() == -1) type = Integer.valueOf(1);
      } catch (CmsLoaderException e) {
        // try internal map
        type = resourceTypeMap.get(sType);
      }
    }
    if (type != null)
      return type.intValue();
    
    return getTypeFromFile(file);
  }
  
  public static int getTypeFromFile(File file) {
    
    if (file.isDirectory()) return CmsResourceTypeFolder.getStaticTypeId();
    
    return getTypeFromFilenameExtension(file.getName());
  }
  
  public static int getTypeFromFilenameExtension(String filename) {
    filename = filename.toLowerCase();
    
    if (filename.endsWith(".jsp"))
      return CmsResourceTypeJsp.getJSPTypeId();
    
    int type = -1;
    
    List<I_CmsResourceType> rts = OpenCms.getResourceManager().getResourceTypes();
    for (I_CmsResourceType rt: rts) {
      List<String> mappings = rt.getConfiguredMappings();
      for (String mapping: mappings) {
        if (filename.endsWith(mapping)) {
          type = rt.getTypeId();
          break;
        }
      }
    }
    
    if (type >= 0) {
      return type;
    }
    
    if (filename.endsWith(".css"))
      return CmsResourceTypePlain.getStaticTypeId();
    
    if (filename.endsWith(".js"))
      return CmsResourceTypePlain.getStaticTypeId();
    
    return CmsResourceTypePlain.getStaticTypeId();
  }
  
  public void setHasModuleConfig() {
    md.setHasModuleConfig();
  }
}
