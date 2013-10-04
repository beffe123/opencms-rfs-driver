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

package com.metamesh.opencms.driver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.CmsAlias;
import org.opencms.db.CmsAliasFilter;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsResourceState;
import org.opencms.db.CmsRewriteAlias;
import org.opencms.db.CmsRewriteAliasFilter;
import org.opencms.db.CmsSqlManager;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.db.urlname.CmsUrlNameMappingEntry;
import org.opencms.db.urlname.CmsUrlNameMappingFilter;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsPropertyDefinition.CmsPropertyType;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.util.CmsUUID;

public class VfsDriverWrapper implements I_CmsDriver, I_CmsVfsDriver {

  protected I_CmsVfsDriver nextDriver;

  protected CmsSqlManager sqlManager;

  protected CmsDriverManager driverManager;

  @Override
  public void addUrlNameMappingEntry(CmsDbContext dbc, boolean online,
      CmsUrlNameMappingEntry entry) throws CmsDataAccessException {
    nextDriver.addUrlNameMappingEntry(dbc, online, entry);
  }

  @Override
  public int countSiblings(CmsDbContext dbc, CmsUUID projectId,
      CmsUUID resourceId) throws CmsDataAccessException {
    return nextDriver.countSiblings(dbc, projectId, resourceId);
  }

  @Override
  public void createContent(CmsDbContext dbc, CmsUUID projectId,
      CmsUUID resourceId, byte[] content) throws CmsDataAccessException {
    nextDriver.createContent(dbc, projectId, resourceId, content);
  }

  @Override
  public CmsFile createFile(ResultSet res, CmsUUID projectId)
      throws SQLException {
    return nextDriver.createFile(res, projectId);
  }

  @Override
  public CmsFile createFile(ResultSet res, CmsUUID projectId,
      boolean hasFileContentInResultSet) throws SQLException {

    return nextDriver.createFile(res, projectId, hasFileContentInResultSet);
  }

  @Override
  public CmsFolder createFolder(ResultSet res, CmsUUID projectId,
      boolean hasProjectIdInResultSet) throws SQLException {

    return nextDriver.createFolder(res, projectId, hasProjectIdInResultSet);
  }

  @Override
  public void createOnlineContent(CmsDbContext dbc, CmsUUID resourceId,
      byte[] contents, int publishTag, boolean keepOnline,
      boolean needToUpdateContent) throws CmsDataAccessException {
    nextDriver.createOnlineContent(dbc, resourceId, contents, publishTag,
        keepOnline, needToUpdateContent);
  }

  @Override
  public CmsPropertyDefinition createPropertyDefinition(CmsDbContext dbc,
      CmsUUID projectId, String name, CmsPropertyType type)
      throws CmsDataAccessException {

    return nextDriver.createPropertyDefinition(dbc, projectId, name, type);
  }

  @Override
  public void createRelation(CmsDbContext dbc, CmsUUID projectId,
      CmsRelation relation) throws CmsDataAccessException {

    nextDriver.createRelation(dbc, projectId, relation);
  }

  @Override
  public CmsResource createResource(CmsDbContext dbc, CmsUUID projectId,
      CmsResource resource, byte[] content) throws CmsDataAccessException {

    return nextDriver.createResource(dbc, projectId, resource, content);
  }

  @Override
  public CmsResource createResource(ResultSet res, CmsUUID projectId)
      throws SQLException {

    return nextDriver.createResource(res, projectId);
  }

  @Override
  public void createSibling(CmsDbContext dbc, CmsProject project,
      CmsResource resource) throws CmsDataAccessException {

    nextDriver.createSibling(dbc, project, resource);
  }

  @Override
  public void deleteAliases(CmsDbContext dbc, CmsProject project,
      CmsAliasFilter filter) throws CmsDataAccessException {

    nextDriver.deleteAliases(dbc, project, filter);
  }

  @Override
  public void deletePropertyDefinition(CmsDbContext dbc,
      CmsPropertyDefinition name) throws CmsDataAccessException {

    nextDriver.deletePropertyDefinition(dbc, name);
  }

  @Override
  public void deletePropertyObjects(CmsDbContext dbc, CmsUUID projectId,
      CmsResource resource, int deleteOption) throws CmsDataAccessException {

    nextDriver.deletePropertyObjects(dbc, projectId, resource, deleteOption);
  }

  @Override
  public void deleteRelations(CmsDbContext dbc, CmsUUID projectId,
      CmsResource resource, CmsRelationFilter filter)
      throws CmsDataAccessException {

    nextDriver.deleteRelations(dbc, projectId, resource, filter);
  }

  @Override
  public void deleteRewriteAliases(CmsDbContext dbc,
      CmsRewriteAliasFilter filter) throws CmsDataAccessException {

    nextDriver.deleteRewriteAliases(dbc, filter);
  }

  @Override
  public void deleteUrlNameMappingEntries(CmsDbContext dbc, boolean online,
      CmsUrlNameMappingFilter filter) throws CmsDataAccessException {

    nextDriver.deleteUrlNameMappingEntries(dbc, online, filter);
  }

  @Override
  public void destroy() throws Throwable {

    nextDriver.destroy();
  }

  @Override
  public List<CmsOrganizationalUnit> getResourceOus(CmsDbContext dbc,
      CmsUUID projectId, CmsResource resource) throws CmsDataAccessException {

    return nextDriver.getResourceOus(dbc, projectId, resource);
  }

  @Override
  public CmsSqlManager getSqlManager() {

    return sqlManager;
  }

  @Override
  public int incrementCounter(CmsDbContext dbc, String name)
      throws CmsDataAccessException {

    return nextDriver.incrementCounter(dbc, name);
  }

  @Override
  public CmsSqlManager initSqlManager(String classname) {

    return nextDriver.initSqlManager(classname);
  }

  @Override
  public void insertAlias(CmsDbContext dbc, CmsProject project, CmsAlias alias)
      throws CmsDataAccessException {

    nextDriver.insertAlias(dbc, project, alias);
  }

  @Override
  public void insertRewriteAliases(CmsDbContext dbc,
      Collection<CmsRewriteAlias> rewriteAliases) throws CmsDataAccessException {

    nextDriver.insertRewriteAliases(dbc, rewriteAliases);
  }

  @Override
  public void moveResource(CmsDbContext dbc, CmsUUID projectId,
      CmsResource source, String destinationPath) throws CmsDataAccessException {

    nextDriver.moveResource(dbc, projectId, source, destinationPath);
  }

  @Override
  public void publishResource(CmsDbContext dbc, CmsProject onlineProject,
      CmsResource onlineResource, CmsResource offlineResource)
      throws CmsDataAccessException {

    nextDriver.publishResource(dbc, onlineProject, onlineResource,
        offlineResource);
  }

  @Override
  public void publishVersions(CmsDbContext dbc, CmsResource resource,
      boolean firstSibling) throws CmsDataAccessException {

    nextDriver.publishVersions(dbc, resource, firstSibling);
  }

  @Override
  public List<CmsAlias> readAliases(CmsDbContext dbc, CmsProject project,
      CmsAliasFilter filter) throws CmsDataAccessException {

    return nextDriver.readAliases(dbc, project, filter);
  }

  @Override
  public List<CmsResource> readChildResources(CmsDbContext dbc,
      CmsProject currentProject, CmsResource resource, boolean getFolders,
      boolean getFiles) throws CmsDataAccessException {

    return nextDriver.readChildResources(dbc, currentProject, resource,
        getFolders, getFiles);
  }

  @Override
  public byte[] readContent(CmsDbContext dbc, CmsUUID projectId,
      CmsUUID resourceId) throws CmsDataAccessException {

    return nextDriver.readContent(dbc, projectId, resourceId);
  }

  @Override
  public CmsFolder readFolder(CmsDbContext dbc, CmsUUID projectId,
      CmsUUID folderId) throws CmsDataAccessException {

    return nextDriver.readFolder(dbc, projectId, folderId);
  }

  @Override
  public CmsFolder readFolder(CmsDbContext dbc, CmsUUID projectId,
      String foldername) throws CmsDataAccessException {

    return nextDriver.readFolder(dbc, projectId, foldername);
  }

  @Override
  public CmsFolder readParentFolder(CmsDbContext dbc, CmsUUID projectId,
      CmsUUID structureId) throws CmsDataAccessException {

    return nextDriver.readParentFolder(dbc, projectId, structureId);
  }

  @Override
  public CmsPropertyDefinition readPropertyDefinition(CmsDbContext dbc,
      String name, CmsUUID projectId) throws CmsDataAccessException {

    return nextDriver.readPropertyDefinition(dbc, name, projectId);
  }

  @Override
  public List<CmsPropertyDefinition> readPropertyDefinitions(CmsDbContext dbc,
      CmsUUID projectId) throws CmsDataAccessException {

    return nextDriver.readPropertyDefinitions(dbc, projectId);
  }

  @Override
  public CmsProperty readPropertyObject(CmsDbContext dbc, String key,
      CmsProject project, CmsResource resource) throws CmsDataAccessException {

    return nextDriver.readPropertyObject(dbc, key, project, resource);
  }

  @Override
  public List<CmsProperty> readPropertyObjects(CmsDbContext dbc,
      CmsProject project, CmsResource resource) throws CmsDataAccessException {

    return nextDriver.readPropertyObjects(dbc, project, resource);
  }

  @Override
  public List<CmsRelation> readRelations(CmsDbContext dbc, CmsUUID projectId,
      CmsResource resource, CmsRelationFilter filter)
      throws CmsDataAccessException {

    return nextDriver.readRelations(dbc, projectId, resource, filter);
  }

  @Override
  public CmsResource readResource(CmsDbContext dbc, CmsUUID projectId,
      CmsUUID structureId, boolean includeDeleted)
      throws CmsDataAccessException {

    return nextDriver.readResource(dbc, projectId, structureId, includeDeleted);
  }

  @Override
  public CmsResource readResource(CmsDbContext dbc, CmsUUID projectId,
      String filename, boolean includeDeleted) throws CmsDataAccessException {

    return nextDriver.readResource(dbc, projectId, filename, includeDeleted);
  }

  @Override
  public List<CmsResource> readResources(CmsDbContext dbc,
      CmsUUID currentProject, CmsResourceState state, int mode)
      throws CmsDataAccessException {

    return nextDriver.readResources(dbc, currentProject, state, mode);
  }

  @Override
  public List<CmsResource> readResourcesForPrincipalACE(CmsDbContext dbc,
      CmsProject project, CmsUUID principalId) throws CmsDataAccessException {

    return nextDriver.readResourcesForPrincipalACE(dbc, project, principalId);
  }

  @Override
  public List<CmsResource> readResourcesForPrincipalAttr(CmsDbContext dbc,
      CmsProject project, CmsUUID principalId) throws CmsDataAccessException {

    return nextDriver.readResourcesForPrincipalAttr(dbc, project, principalId);
  }

  @Override
  public List<CmsResource> readResourcesWithProperty(CmsDbContext dbc,
      CmsUUID projectId, CmsUUID propertyDefinition, String path, String value)
      throws CmsDataAccessException {

    return nextDriver.readResourcesWithProperty(dbc, projectId,
        propertyDefinition, path, value);
  }

  @Override
  public List<CmsResource> readResourceTree(CmsDbContext dbc,
      CmsUUID projectId, String parent, int type, CmsResourceState state,
      long startTime, long endTime, long releasedAfter, long releasedBefore,
      long expiredAfter, long expiredBefore, int mode)
      throws CmsDataAccessException {

    return nextDriver.readResourceTree(dbc, projectId, parent, type, state,
        startTime, endTime, releasedAfter, releasedBefore, expiredAfter,
        expiredBefore, mode);
  }

  @Override
  public List<CmsRewriteAlias> readRewriteAliases(CmsDbContext dbc,
      CmsRewriteAliasFilter filter) throws CmsDataAccessException {

    return nextDriver.readRewriteAliases(dbc, filter);
  }

  @Override
  public List<CmsResource> readSiblings(CmsDbContext dbc, CmsUUID projectId,
      CmsResource resource, boolean includeDeleted)
      throws CmsDataAccessException {

    return nextDriver.readSiblings(dbc, projectId, resource, includeDeleted);
  }

  @Override
  public List<CmsUrlNameMappingEntry> readUrlNameMappingEntries(
      CmsDbContext dbc, boolean online, CmsUrlNameMappingFilter filter)
      throws CmsDataAccessException {

    return nextDriver.readUrlNameMappingEntries(dbc, online, filter);
  }

  @Override
  public Map<String, Integer> readVersions(CmsDbContext dbc, CmsUUID projectId,
      CmsUUID resourceId, CmsUUID structureId) throws CmsDataAccessException {

    return nextDriver.readVersions(dbc, projectId, resourceId, structureId);
  }

  @Override
  public void removeFile(CmsDbContext dbc, CmsUUID projectId,
      CmsResource resource) throws CmsDataAccessException {

    nextDriver.removeFile(dbc, projectId, resource);
  }

  @Override
  public void removeFolder(CmsDbContext dbc, CmsProject currentProject,
      CmsResource resource) throws CmsDataAccessException {

    nextDriver.removeFolder(dbc, currentProject, resource);
  }

  @Override
  public void replaceResource(CmsDbContext dbc, CmsResource newResource,
      byte[] newResourceContent, int newResourceType)
      throws CmsDataAccessException {

    nextDriver.replaceResource(dbc, newResource, newResourceContent,
        newResourceType);
  }

  @Override
  public void setDriverManager(CmsDriverManager driverManager) {

    this.driverManager = driverManager;
  }

  @Override
  public void setSqlManager(CmsSqlManager sqlManager) {

    this.sqlManager = sqlManager;
  }

  @Override
  public void transferResource(CmsDbContext dbc, CmsProject project,
      CmsResource resource, CmsUUID createdUser, CmsUUID lastModifiedUser)
      throws CmsDataAccessException {

    nextDriver.transferResource(dbc, project, resource, createdUser,
        lastModifiedUser);
  }

  @Override
  public void updateRelations(CmsDbContext dbc, CmsProject onlineProject,
      CmsResource offlineResource) throws CmsDataAccessException {

    nextDriver.updateRelations(dbc, onlineProject, offlineResource);
  }

  @Override
  public boolean validateResourceIdExists(CmsDbContext dbc, CmsUUID projectId,
      CmsUUID resourceId) throws CmsDataAccessException {

    return nextDriver.validateResourceIdExists(dbc, projectId, resourceId);
  }

  @Override
  public boolean validateStructureIdExists(CmsDbContext dbc, CmsUUID projectId,
      CmsUUID structureId) throws CmsDataAccessException {

    return nextDriver.validateStructureIdExists(dbc, projectId, structureId);
  }

  @Override
  public void writeContent(CmsDbContext dbc, CmsUUID resourceId, byte[] content)
      throws CmsDataAccessException {

    nextDriver.writeContent(dbc, resourceId, content);
  }

  @Override
  public void writeLastModifiedProjectId(CmsDbContext dbc, CmsProject project,
      CmsUUID projectId, CmsResource resource) throws CmsDataAccessException {

    nextDriver.writeLastModifiedProjectId(dbc, project, projectId, resource);
  }

  @Override
  public void writePropertyObject(CmsDbContext dbc, CmsProject project,
      CmsResource resource, CmsProperty property) throws CmsDataAccessException {

    nextDriver.writePropertyObject(dbc, project, resource, property);
  }

  @Override
  public void writePropertyObjects(CmsDbContext dbc, CmsProject project,
      CmsResource resource, List<CmsProperty> properties)
      throws CmsDataAccessException {

    nextDriver.writePropertyObjects(dbc, project, resource, properties);
  }

  @Override
  public void writeResource(CmsDbContext dbc, CmsUUID projectId,
      CmsResource resource, int changed) throws CmsDataAccessException {

    nextDriver.writeResource(dbc, projectId, resource, changed);
  }

  @Override
  public void writeResourceState(CmsDbContext dbc, CmsProject project,
      CmsResource resource, int changed, boolean isPublishing)
      throws CmsDataAccessException {

    nextDriver
        .writeResourceState(dbc, project, resource, changed, isPublishing);
  }

  @Override
  public void init(CmsDbContext dbc,
      CmsConfigurationManager configurationManager,
      List<String> successiveDrivers, CmsDriverManager driverManager)
      throws CmsException {

    CmsParameterConfiguration config = configurationManager.getConfiguration();
    
    if (successiveDrivers != null && successiveDrivers.size() > 0) {
      String driverKey = (String)successiveDrivers.get(0) + ".vfs.driver";
      String driverName = config.get(driverKey);
      List<String> drivers = (successiveDrivers.size() > 1) ? successiveDrivers.subList(1, successiveDrivers.size()) : null;
  
      nextDriver = (I_CmsVfsDriver)driverManager.newDriverInstance(dbc, configurationManager, driverName, drivers);
    }
  }

}
