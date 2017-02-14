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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor.CacheType;
import org.opencms.util.CmsUUID;

import com.metamesh.opencms.driver.VfsDriverWrapper;
import com.metamesh.opencms.rfs.file.RfsCmsFolder;

public class RfsDriver extends VfsDriverWrapper {
  
  private boolean initialized = false;
  
  private static String rfsBasePath = null;
  
  private static CmsParameterConfiguration config = null;
  
  private static CmsConfigurationManager configurationManager;
  
  private RfsDriverService service;
  
  @Override
  public List<CmsResource> readChildResources(CmsDbContext dbc,
      CmsProject currentProject, CmsResource resource, boolean getFolders,
      boolean getFiles) throws CmsDataAccessException {
    
    List<CmsResource> vfsChildren = nextDriver.readChildResources(dbc, currentProject, resource, getFolders,
        getFiles);
    
    if (!isInitialized()) return vfsChildren;
    
    Set<String> vfsPaths = new java.util.HashSet<String>(vfsChildren.size());
    for (CmsResource vfsChild: vfsChildren) {
      vfsPaths.add(vfsChild.getRootPath());
    }
    
    if (resource instanceof RfsCmsFolder) {
      return service.getChildResources(resource, getFolders, getFiles);
    }
    
    else if (service.hasChildrenForResource(resource)) {
      List<CmsResource> rfsChildren = service.getChildResources(resource, getFolders, getFiles);
      ArrayList<CmsResource> result = new ArrayList<CmsResource>(vfsChildren.size() + rfsChildren.size());
      for (CmsResource rfsChild: rfsChildren) {
        if (!vfsPaths.contains(rfsChild.getRootPath())) {
          result.add(rfsChild);
        }
      }
      
      result.addAll(vfsChildren);
      
      Collections.sort(result, CmsResource.COMPARE_ROOT_PATH_IGNORE_CASE_FOLDERS_FIRST);
      return result;
    }
    else {
      return vfsChildren;
    }
  }

  @Override
  public byte[] readContent(CmsDbContext dbc, CmsUUID projectId,
      CmsUUID resourceId) throws CmsDataAccessException {
    
    if (!isInitialized()) return super.readContent(dbc, projectId, resourceId);
    
    if (service.isRfsResourceId(resourceId)) {
      CmsResource file = service.getResourceByResourceId(resourceId);
      if (file.isFile()) {
        byte[] result = service.readContent(resourceId);
        return result;
      }
    }
    return super.readContent(dbc, projectId, resourceId);
  }

  @Override
  public CmsFolder readFolder(CmsDbContext dbc, CmsUUID projectId,
      CmsUUID folderId) throws CmsDataAccessException {
    
    if (!isInitialized()) return super.readFolder(dbc, projectId, folderId);
      
    if (service.isRfsStructureId(folderId)) {
      return (CmsFolder)service.getResourceByStructureId(folderId);
    }
    else {
      return super.readFolder(dbc, projectId, folderId);
    }
  }

  @Override
  public CmsFolder readFolder(CmsDbContext dbc, CmsUUID projectId,
      String foldername) throws CmsDataAccessException {
    
    if (!isInitialized()) return super.readFolder(dbc, projectId, foldername);
    
    // vfs resources go first
    try {
      return super.readFolder(dbc, projectId, foldername);
    }
    catch (CmsDataAccessException cdae) {
      if (service.isRfsResource(foldername)) {
        return (CmsFolder)service.getResourceByPath(foldername);
      }
      throw cdae;
    }
  }

  @Override
  public CmsFolder readParentFolder(CmsDbContext dbc, CmsUUID projectId,
      CmsUUID structureId) throws CmsDataAccessException {

    if (!isInitialized()) return super.readParentFolder(dbc, projectId, structureId);
    

    if (service.isRfsStructureId(structureId)) {
      CmsResource res = service.getResourceByStructureId(structureId);
      String parentPath = CmsResource.getParentFolder(res.getRootPath());
      try {
        return nextDriver.readFolder(dbc, projectId, parentPath);
      }
      catch (CmsDataAccessException cdae) {
        if (service.isRfsResource(parentPath))
          return (CmsFolder)service.getResourceByPath(parentPath);
        
        throw cdae;
      }
    }
    return super.readParentFolder(dbc, projectId, structureId);
  }

  @Override
  public CmsResource readResource(CmsDbContext dbc, CmsUUID projectId,
      CmsUUID structureId, boolean includeDeleted)
      throws CmsDataAccessException {
    
    if (!isInitialized()) return super.readResource(dbc, projectId, structureId, includeDeleted);
      
    if (service.isRfsStructureId(structureId)) {
      return service.getResourceByStructureId(structureId);
    }
    return super.readResource(dbc, projectId, structureId, includeDeleted);
  }

  @Override
  public CmsResource readResource(CmsDbContext dbc, CmsUUID projectId,
      String filename, boolean includeDeleted) throws CmsDataAccessException {
    
    if (!isInitialized()) return super.readResource(dbc, projectId, filename, includeDeleted);
      
    // vfs resources go first
    
    try {
      return super.readResource(dbc, projectId, filename, includeDeleted);
    }
    catch (CmsDataAccessException cdae) {
      if (service.isRfsResource(filename)) {
        return service.getResourceByPath(filename);
      }
      throw cdae;
    }
  }

  @Override
  public CmsProperty readPropertyObject(CmsDbContext dbc, String key,
      CmsProject project, CmsResource resource) throws CmsDataAccessException {
    
    if (!isInitialized()) return super.readPropertyObject(dbc, key, project, resource);
      
    if (service.isRfsStructureId(resource.getStructureId())) {
      return service.getProperty(resource, key);
    }
    return super.readPropertyObject(dbc, key, project, resource);
  }

  @Override
  public List<CmsProperty> readPropertyObjects(CmsDbContext dbc,
      CmsProject project, CmsResource resource) throws CmsDataAccessException {
    
    if (!isInitialized()) return super.readPropertyObjects(dbc, project, resource);
      
    if (service.isRfsStructureId(resource.getStructureId())) {
      return service.getProperties(resource);
    }
    return super.readPropertyObjects(dbc, project, resource);
  }
  
  @Override
  public List<CmsResource> readResourceTree(CmsDbContext dbc,
      CmsUUID projectId, String parent, int type, CmsResourceState state,
      long startTime, long endTime, long releasedAfter, long releasedBefore,
      long expiredAfter, long expiredBefore, int mode)
      throws CmsDataAccessException {

    List<CmsResource> result = new ArrayList<CmsResource>();
    
    if (parent == null || "/".equals(parent)) {
      result.addAll(nextDriver.readResourceTree(dbc, projectId, parent, type, state,
          startTime, endTime, releasedAfter, releasedBefore, expiredAfter,
          expiredBefore, mode));
      Collection<CmsResource> rfsResources =service.getAllRfsResources(); 
      result.addAll(rfsResources);
    }
    else {
      CmsUUID parentId = null;
      boolean directChildrenOnly = false;
      boolean parentIsRfsResource = false;
      CmsResource parentResource = null;
      
      if ((mode & CmsDriverManager.READMODE_EXCLUDE_TREE) > 0) {
        // parent is a string representation of a UUID and it should only be returned direct children
        directChildrenOnly = true;
        parentId = new CmsUUID(parent);
        parentIsRfsResource = service.isRfsStructureId(parentId);
        if (parentIsRfsResource) {
          parentResource = service.getResourceByStructureId(parentId);
        }
        else {
          parentResource = nextDriver.readResource(dbc, projectId, parentId, true);
        }
      }
      else {
        // parent is a path
        try {
          parentResource = nextDriver.readResource(dbc, projectId, parent, true);
        }
        catch (CmsDataAccessException cdae) {
          if (service.isRfsResource(parent)) {
            parentResource = service.getResourceByPath(parent);
            parentIsRfsResource = true;
          }
          else {
            throw cdae;
          }
        }
      }
  
      boolean getFiles = ((mode & CmsDriverManager.READMODE_ONLY_FOLDERS) > 0) ? false : true;
      boolean getFolders = ((mode & CmsDriverManager.READMODE_ONLY_FILES) > 0) ? false : true;
      
      if (parentIsRfsResource) {
        
        List<CmsResource> children = service.getChildResources(parentResource, getFolders, getFiles);
        
        if (!directChildrenOnly) {
          for (CmsResource child: children) {
            if (child.isFolder() && getFolders) {
              result.addAll(readResourceTree(dbc, projectId, child.getStructureId().toString(), type, state, startTime, endTime, 
                  releasedAfter, releasedBefore, expiredAfter, expiredBefore, mode));
            }
          }
        }
        result.addAll(children);
      }
      else {
        result.addAll(nextDriver.readResourceTree(dbc, projectId, parent, type, state,
            startTime, endTime, releasedAfter, releasedBefore, expiredAfter,
            expiredBefore, mode));
        
        if (service.hasChildrenForResource(parentResource)) {
          List<CmsResource> children = service.getChildResources(parentResource, getFolders, getFiles);
  
          for (CmsResource child: children) {
            CmsResource check = null;
            try {
              check = nextDriver.readResource(dbc, projectId, child.getRootPath(), true);
            }
            catch (CmsDataAccessException cdae) {
              
            }
            if (check == null) {
              // only add children that do not exist in VFS
              result.add(child);
            }
            if (!directChildrenOnly && child.isFolder() && getFolders) {
              result.addAll(readResourceTree(dbc, projectId, child.getStructureId().toString(), type, state, startTime, endTime, 
                  releasedAfter, releasedBefore, expiredAfter, expiredBefore, mode));
            }
          }
        }
      }
    }
    
    Collections.sort(result,  CmsResource.COMPARE_ROOT_PATH_IGNORE_CASE_FOLDERS_FIRST);
    
    return result;
  }
  
  @Override
  public List<CmsResource> readResourcesWithProperty(CmsDbContext dbc,
      CmsUUID projectId, CmsUUID propertyDefinition, String path, String value)
      throws CmsDataAccessException {
    
    List<CmsResource> result = new ArrayList<CmsResource>();
    result.addAll(nextDriver.readResourcesWithProperty(dbc, projectId, propertyDefinition,
        path, value));
    
    CmsResource pathRes = readResource(dbc, projectId, path, true);
    
    if (pathRes instanceof RfsCmsFolder || service.hasChildrenForResource(pathRes)) {
      CmsPropertyDefinition pd = getPropertyDefinitions(dbc, projectId, propertyDefinition);
      
      if (pd != null) {
        Set<CmsResource> resources = service.getResourcesByPropertyName(pd.getName());
        
        for (CmsResource res: resources) {
          if (res.getRootPath().startsWith(path)) {
            if (value == null || value.equals(service.getProperty(res, pd.getName()))) {
              result.add(res);
            }
          }
        }
      }
    }
    
    Collections.sort(result,  CmsResource.COMPARE_ROOT_PATH_IGNORE_CASE_FOLDERS_FIRST);
    
    return result;
    
  }

  private CmsPropertyDefinition getPropertyDefinitions(CmsDbContext dbc, CmsUUID projectId, CmsUUID pdId) {
    try {
      List<CmsPropertyDefinition> pds = nextDriver.readPropertyDefinitions(dbc, projectId);
      for (CmsPropertyDefinition pd: pds) {
        if (pd.getId().equals(pdId))
          return pd;
      }
    } catch (CmsDataAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
  
  public boolean isInitialized() {
    return true;
  }
  
  public static String getRfsBasePath() {
    return rfsBasePath;
  }
  
  public static CmsConfigurationManager getConfigurationManager() {
    return configurationManager;
  }

  @Override
  public void init(CmsDbContext dbc,
      CmsConfigurationManager configurationManager,
      List<String> successiveDrivers, CmsDriverManager driverManager)
      throws CmsException {
    
    RfsDriver.configurationManager = configurationManager;

    String configPath = OpenCms.getSystemInfo()
        .getAbsoluteRfsPathRelativeToWebInf("config/metamesh_rfs.properties");
    
    try {
      CmsParameterConfiguration configuration = new CmsParameterConfiguration(OpenCms.getSystemInfo()
          .getAbsoluteRfsPathRelativeToWebInf("config/metamesh_rfs.properties"));
      
      config = configuration;
      
      boolean autoSync = config.getBoolean("auto-sync", false);
      
      if (autoSync) {
        OpenCms.getMemoryMonitor().disableCache(CacheType.RESOURCE_LIST);
      }
      
      service = RfsDriverService.newInstance(configurationManager, configPath);

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    super.init(dbc, configurationManager, successiveDrivers, driverManager);
  }
}
