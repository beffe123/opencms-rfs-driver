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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsModuleConfiguration;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleImportExportHandler;
import org.opencms.module.CmsModuleManager;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.metamesh.opencms.rfs.ManifestInfo;
import com.metamesh.opencms.rfs.RfsAwareModuleConfiguration;
import com.metamesh.opencms.rfs.file.RfsCmsFile;
import com.metamesh.opencms.rfs.file.RfsCmsFolder;
import com.metamesh.opencms.rfs.parser.ManifestParser;

public class RfsDriverService {

  CmsConfigurationManager configurationManager;
  
  CmsParameterConfiguration config;
  
  List<MappingData> mappingDatas = new ArrayList<MappingData>();
  
  String configPath;
  
  boolean autoSync;
  
  private class EventListener implements I_CmsEventListener {

    @Override
    public void cmsEvent(CmsEvent event) {
      if (event.getType() == I_CmsEventListener.EVENT_CLEAR_CACHES) {
        try {
          init();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      
    }
    
  }
  
  public static RfsDriverService newInstance(CmsConfigurationManager configurationManager,
      String configPath) throws IOException {
    
    RfsDriverService rds = new RfsDriverService();
    rds.configPath = configPath;
    rds.init();
    
    return rds;
  }
  
  private RfsDriverService() {
    OpenCms.getEventManager().addCmsEventListener(new EventListener());
  }
  
  private synchronized void init() throws IOException {
    
    config = new CmsParameterConfiguration(configPath);
    
    autoSync = config.getBoolean("auto-sync", false);
    
    mappingDatas.clear();
    
    List<String> mappings;
    // read mappings inside of webapp folder
    // e.g. "mappings/modules", will add all sub-folders of ${webapp}/mappings/modules/
    // as a mapping
    mappings = config.getList("root-mappings.folder.webapp-relative");
    for (String mapping: mappings) {
      List<MappingData> md = initRootFolders(mapping, true);
      if (md != null)
        mappingDatas.addAll(md);
    }
    
    // read mappings outside of webapp
    mappings = config.getList("root-mappings.folder");
    
    // read one-to-one mappings
    mappings = config.getList("root-mapping.webapp-relative");
    
    mappings = config.getList("root-mapping");
    
    // one-2-one-mappings of form "<rfs-path>:<vfs-root-path>"
    // e.g. "mappings/videos:/sites/video-center/videos"
    mappings = config.getList("one-2-one-mapping.webapp-relative");
    for (String mapping: mappings) {
      String[] s = mapping.split(":");
      if (s.length == 2) {
        MappingData md = initOne2OneMapping(s[0], s[1], true);
        if (md != null) mappingDatas.add(md);
      }
    }
    
    mappings = config.getList("one-2-one-mapping");
    for (String mapping: mappings) {
      String[] s = mapping.split(":");
      if (s.length == 2) {
        MappingData md = initOne2OneMapping(s[0], s[1], false);
        if (md != null) mappingDatas.add(md);
      }
    }
    
  }
  
  private static MappingData initOne2OneMapping(String rfsPath, String vfsPath, boolean relativeToWebapp) {

    File rfsFile = null;
    if (relativeToWebapp) {
      String path = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebApplication(rfsPath);
      rfsFile = new File(path);
    }
    else {
      rfsFile = new File(rfsPath);
    }
    if (rfsFile.exists()) {

      String webappBasePath = null;
      if (relativeToWebapp) {
        webappBasePath = rfsPath;
      }
      
      MappingData md = new MappingData(rfsFile, webappBasePath, vfsPath);
      md.sync(false);
      
      return md;
    }
    return null;
  }

  private static List<MappingData> initRootFolders(String rfsParentPath, boolean relativeToWebapp) {

    List<MappingData> result = new ArrayList<MappingData>();
    
    File dbFolder = null;
    if (relativeToWebapp) {
      String path = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebApplication(rfsParentPath);
      dbFolder = new File(path);
    }
    else {
      dbFolder = new File(rfsParentPath);
    }
    if (dbFolder.exists() && dbFolder.isDirectory()) {
      for (String sub: dbFolder.list()) {
        File f = new File(sub);
        MappingData md = null;
        if (relativeToWebapp) {
          md = initRootFolder(rfsParentPath + File.separator + f.getName(), "/", true);
        }
        else {
          md = initRootFolder(sub, "/", false);
        }
        if (md != null) result.add(md);
      }
      
    }
    return result;
  }
  

  private static MappingData initRootFolder(String rfsPath, String vfsMountPath, boolean relativeToWebapp) {
    long startTime = System.currentTimeMillis();

    CmsModuleConfiguration mcfg = (CmsModuleConfiguration)RfsDriver
        .getConfigurationManager().getConfiguration(CmsModuleConfiguration.class);
    
    RfsAwareModuleConfiguration ramcfg = null;
    if (mcfg instanceof RfsAwareModuleConfiguration) {
      ramcfg = (RfsAwareModuleConfiguration)mcfg;
    }
    
    // initialization of this object must not fail due to Lazy Holder pattern
    File dbFolder = null;
    try {
      if (relativeToWebapp) {
        String modulesPath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebApplication(rfsPath);
        dbFolder = new File(modulesPath);
        
      }
      else {
        // absolute path
        dbFolder = new File(rfsPath);
      }
      if (dbFolder.exists() && dbFolder.isDirectory()) {
        
        String webappBasePath = null;
        if (relativeToWebapp) {
          webappBasePath = rfsPath;
        }
        
        MappingData md = new MappingData(dbFolder, webappBasePath, vfsMountPath);
        
        File manifest = new File(dbFolder, "manifest.xml");
        
        if (manifest.exists()) {
          
          md.setManifestInfo(new ManifestInfo(dbFolder, webappBasePath, manifest, manifest.lastModified()));
          parseDoc(dbFolder, webappBasePath, manifest, md);

          CmsModule importedModule = CmsModuleImportExportHandler.readModuleFromImport(dbFolder.getAbsolutePath());
          
          addModuleToManager(importedModule);

          if (ramcfg != null) {
            ramcfg.addReadonlyModule(importedModule);
          }
        }
        
        md.sync(false);
        
        return md;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    System.err.println("parsing took " + (System.currentTimeMillis() - startTime) + " millis");
    return null;
  }

  private static void parseDoc(File baseFolder, String webappBasePath, File manifest,
      MappingData md) {
    XMLReader xr;
    try {
      xr = XMLReaderFactory.createXMLReader();
      ManifestParser mp = new ManifestParser(baseFolder, md);
      xr.setContentHandler(mp);
      
      FileInputStream fis = new FileInputStream(manifest);
      
      xr.parse(new InputSource(fis));
      
      fis.close();
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  
  private static void addModuleToManager(CmsModule module) {
    Class clazz = CmsModuleManager.class;
    Field mModules;
    try {
      mModules = clazz.getDeclaredField("m_modules");
      mModules.setAccessible(true);
      Map<String, CmsModule> modules = (Map<String, CmsModule>)mModules.get(OpenCms.getModuleManager());
      modules.put(module.getName(), module);
    } catch (SecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
  

  
  public Set<CmsResource> getResourcesByPropertyName(String propertyName) {
    Set<CmsResource> result = new HashSet<CmsResource>();
    for (MappingData md: mappingDatas) {
      result.addAll(md.getResourcesByProperty().get(propertyName));
    }
    return result;
  }
  
  public List<CmsResource> getChildResources(CmsResource resource, boolean getFolders, boolean getFiles) {
    
    //TODO
    //checkManifests();
    
    List<CmsResource> result = null;
    
    // read in files that are not part of the manifest or that are new
    if (resource instanceof RfsCmsFolder) {
      RfsCmsFolder folder = (RfsCmsFolder)resource;
      //TODO
      //syncFolder(folder, false);
    }

    Set<CmsResource> set = new TreeSet<CmsResource>(CmsResource.COMPARE_ROOT_PATH_IGNORE_CASE_FOLDERS_FIRST);
    
    for (MappingData md: mappingDatas) {
      Map<String, List<CmsResource>> resourcesByParent = md.getResourcesByParent();
      if (resourcesByParent.containsKey(resource.getRootPath())) {
        if (autoSync) {
          checkManifest(md);
          md.sync(true);
        }
        for (CmsResource res: resourcesByParent.get(resource.getRootPath())) {
          if (res != null && (getFiles && res.isFile() || getFolders && res.isFolder())) {
            set.add(res);
          } 
        }
      }
    }
    result = new ArrayList<CmsResource>(set);
    return result;
  }
  
  public boolean hasChildrenForResource(CmsResource res) {
    for (MappingData md: mappingDatas) {
      if (md.getResourcesByParent().containsKey(res.getRootPath()))
        return true;
    }
    return false;
  }
  
  public boolean isRfsStructureId(CmsUUID id) {
    for (MappingData md: mappingDatas) {
      if (md.getResourcesByStructureId().containsKey(id))
        return true;
    }
    return false;
  }
  
  public boolean isRfsResourceId(CmsUUID id) {
    for (MappingData md: mappingDatas) {
      if (md.getResourcesByResourceId().containsKey(id))
        return true;
    }
    return false;
  }
  
  public CmsResource getResourceByStructureId(CmsUUID id) {
    
    //TODO
    //checkManifests();

    for (MappingData md: mappingDatas) {
      if (md.getResourcesByStructureId().containsKey(id))
        return md.getResourcesByStructureId().get(id);
    }
    return null;
  }
  
  public CmsResource getResourceByResourceId(CmsUUID id) {
    for (MappingData md: mappingDatas) {
      if (md.getResourcesByResourceId().containsKey(id))
        return md.getResourcesByResourceId().get(id);
    }
    return null;
  }
  
  public boolean isRfsResource(String path) {
    if (path == null || "/".equals(path)) return false;

    for (MappingData md: mappingDatas) {
      if (md.getResourcesByPath().containsKey(CmsFileUtil.removeTrailingSeparator(path))) {
        return true;
      }
    }

    for (MappingData md: mappingDatas) {
      RfsCmsFolder rootPath = md.getRootPathForSubPath(path);
      
      if (rootPath != null) {
  
        File rfsFile = rootPath.getRfsFile();
        File sub = new File(rfsFile, path.substring(rootPath.getRootPath().length()));
        
        if (sub.exists()) {
          
          String parentPath = CmsResource.getParentFolder(path);
          
          if (!rootPath.getRootPath().equals(path)) {
            isRfsResource(parentPath);
          }
          
          CmsUUID structureUuid = CmsUUID.getConstantUUID(path);
          CmsUUID resourceUuid = structureUuid;
          int flags = 0;
          CmsUUID user = CmsUUID.getNullUUID();
          String sourcePath = path;
          Map<String, Object> values = new HashMap<String, Object>();
          values.put("destination", path);
          values.put("source", sourcePath);
          values.put("flags", "0");
          values.put("source", sourcePath);
    
          ResourceCreator rc = new ResourceCreator(md);
          return true;
        }
        
        /*
        boolean autoSync = isAutoSync();
        boolean recursiveSync = isRecursiveSync();
        
        if (autoSync)
          syncFolder(rootPath, recursiveSync);
        
        if (resourcesByPath.containsKey(CmsFileUtil.removeTrailingSeparator(path))) {
          return true;
        }
        */
      }
      /*
      else {
        if (false) { // if not a vfs resource
          // try all base folders
          for (File baseFolder: moduleFolders) {
            File sub = new File(baseFolder, path);
            
            if (sub.exists()) {
              
              String parentPath = CmsResource.getParentFolder(path);
              
              isRfsResource(parentPath);
              
              CmsUUID structureUuid = CmsUUID.getConstantUUID(path);
              CmsUUID resourceUuid = structureUuid;
              int flags = 0;
              int type = getTypeFromFile(sub);
              CmsUUID user = CmsUUID.getNullUUID();
              String sourcePath = path;
              CmsResource res = addResourceInternal(sub, path, structureUuid, resourceUuid, baseFolder, null, type, flags, user, sourcePath);
              addChildToParent(parentPath, res);
              return true;
            }
            
            boolean autoSync = isAutoSync();
            boolean recursiveSync = isRecursiveSync();
            
            //if (autoSync)
              //syncFolder(xxx, recursiveSync);
            
            if (resourcesByPath.containsKey(CmsFileUtil.removeTrailingSeparator(path))) {
              return true;
            }
          }
        }
      }
      */
    }
    return false;
  }
  
  public Collection<CmsResource> getAllRfsResources() {
    Set<CmsResource> allRfsResources = new HashSet<CmsResource>();
    for (MappingData md: mappingDatas) {
      allRfsResources.addAll(md.getResourcesByPath().values());
    }
    return allRfsResources;
  }
  
  public CmsResource getResourceByPath(String path) {
    
    path = CmsFileUtil.removeTrailingSeparator(path);
    
    for (MappingData md: mappingDatas) {
      if (md.getResourcesByPath().containsKey(path)) {
        return md.getResourcesByPath().get(path);
      }
    }
    return null;
  }
  
  public byte[] readContent(CmsUUID resourceId) {
    CmsResource res = getResourceByResourceId(resourceId);
    if (res != null) {
      RfsCmsFile file = (RfsCmsFile)res;
      File f = file.getRfsFile();
      FileInputStream fis;
      try {
        fis = new FileInputStream(f);
        FileChannel fc = fis.getChannel();
        long size = fc.size();
        ByteBuffer buffy = ByteBuffer.allocate((int)size);
        fc.read(buffy);
        fc.close();
        fis.close();
        return buffy.array();
        
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return new byte[]{};
  }

  public List<CmsProperty>  getProperties(CmsResource res) {
    
    for (MappingData md: mappingDatas) {
      
      if (md.getResourcesByStructureId().containsKey(res.getStructureId()) && md.getManifestInfo() != null) {
        checkManifest(md);
      }
      
      if (md.getPropertiesByStructureId().containsKey(res.getStructureId())) {
        
        Map<String, String> properties = md.getPropertiesByStructureId().get(res.getStructureId());
        if (properties == null) {
          return Collections.<CmsProperty>emptyList();
        }
        else {
          List<CmsProperty> result = new ArrayList<CmsProperty>();
          for (Map.Entry<String, String> entry: properties.entrySet()) {
            CmsProperty prop = new CmsProperty(entry.getKey(), entry.getValue(), null);
            result.add(prop);
          }
          return result;
        }
      }
    }
    return Collections.<CmsProperty>emptyList();
  }
  
  public CmsProperty getProperty(CmsResource res, String propertyName) {
    
    for (MappingData md: mappingDatas) {
      
      if (md.getResourcesByStructureId().containsKey(res.getStructureId()) && md.getManifestInfo() != null) {
        checkManifest(md);
      }
      
      if (md.getPropertiesByStructureId().containsKey(res.getStructureId())) {
        
        Map<String, String> properties = md.getPropertiesByStructureId().get(res.getStructureId());
        
        if (properties == null || !properties.containsKey(propertyName)) {
          return CmsProperty.getNullProperty();
        }
        else {
          CmsProperty prop = new CmsProperty(propertyName, properties.get(propertyName), null);
          return prop;
        }
      }
    }
    return CmsProperty.getNullProperty();
  }

  private void checkManifest(MappingData md) {
    
    boolean changesMade = false;
    
    ManifestInfo mi = md.getManifestInfo();
    
    if (mi != null) {
        
        if (mi.getManifest().lastModified() > mi.getParseDate()) {

          md.setManifestInfo(new ManifestInfo(mi.getModuleFolder(), mi.getWebappBasePath(), 
              mi.getManifest(), mi.getManifest().lastModified()));
          parseDoc(mi.getModuleFolder(), mi.getWebappBasePath(), mi.getManifest(), md);
          changesMade = true;
        }
    }
    
    if (changesMade) {
      OpenCms.getMemoryMonitor().flushProperties();
      OpenCms.getMemoryMonitor().flushPropertyLists();
    }
  }
  
}
