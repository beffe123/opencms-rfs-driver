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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ExtendedProperties;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.loader.CmsDumpLoader;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.loader.Messages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceManager;

import com.metamesh.opencms.rfs.file.RfsCmsResource;

public class RfsAwareDumpLoader extends CmsDumpLoader implements I_CmsResourceLoader {

  /** The id of this loader. */
  public static final int RESOURCE_LOADER_ID = 1;

  /** The maximum age for dumped contents in the clients cache. */
  private static long m_clientCacheMaxAge;

  /** The resource loader configuration. */
  private CmsParameterConfiguration m_configuration;

  /**
   * The constructor of the class is empty and does nothing.<p>
   */
  public RfsAwareDumpLoader() {

      m_configuration = new CmsParameterConfiguration();
  }

  /**
   * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
   */
  public void addConfigurationParameter(String paramName, String paramValue) {

      m_configuration.put(paramName, paramValue);
  }

  /** 
   * Destroy this ResourceLoder, this is a NOOP so far.<p>
   */
  public void destroy() {

      // NOOP
  }

  /**
   * @see org.opencms.loader.I_CmsResourceLoader#dump(org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.lang.String, java.util.Locale, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public byte[] dump(
      CmsObject cms,
      CmsResource resource,
      String element,
      Locale locale,
      HttpServletRequest req,
      HttpServletResponse res) throws CmsException {

      return super.dump(cms, resource, element, locale, req, res);
  }

  /**
   * @see org.opencms.loader.I_CmsResourceLoader#export(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public byte[] export(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
  throws IOException, CmsException {

      return super.export(cms, resource, req, res);
  }

  /**
   * Will always return <code>null</code> since this loader does not 
   * need to be configured.<p>
   * 
   * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
   */
  public CmsParameterConfiguration getConfiguration() {

      // return the configuration in an immutable form
      return m_configuration;
  }

  /**
   * @see org.opencms.loader.I_CmsResourceLoader#getLoaderId()
   */
  public int getLoaderId() {

      return RESOURCE_LOADER_ID;
  }

  public String getResourceLoaderInfo() {

      return "RFS aware Dump Loader";
  }

  /**
   * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
   */
  public void initConfiguration() {

      ExtendedProperties config = new ExtendedProperties();
      config.putAll(m_configuration);

      String maxAge = config.getString("client.cache.maxage");
      if (maxAge == null) {
          m_clientCacheMaxAge = -1;
      } else {
          m_clientCacheMaxAge = Long.parseLong(maxAge);
      }

      if (CmsLog.INIT.isInfoEnabled()) {
          if (maxAge != null) {
              CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_CLIENT_CACHE_MAX_AGE_1, maxAge));
          }
          CmsLog.INIT.info(Messages.get().getBundle().key(
              Messages.INIT_LOADER_INITIALIZED_1,
              this.getClass().getName()));
      }
  }

  /**
   * @see org.opencms.loader.I_CmsResourceLoader#isStaticExportEnabled()
   */
  public boolean isStaticExportEnabled() {

      return false;
  }

  /**
   * @see org.opencms.loader.I_CmsResourceLoader#isStaticExportProcessable()
   */
  public boolean isStaticExportProcessable() {

      return false;
  }

  /**
   * @see org.opencms.loader.I_CmsResourceLoader#isUsableForTemplates()
   */
  public boolean isUsableForTemplates() {

      return false;
  }

  /**
   * @see org.opencms.loader.I_CmsResourceLoader#isUsingUriWhenLoadingTemplate()
   */
  public boolean isUsingUriWhenLoadingTemplate() {

      return false;
  }

  /**
   * @see org.opencms.loader.I_CmsResourceLoader#load(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public void load(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
  throws IOException, CmsException {

      super.load(cms, resource, req, res);
  }

  /**
   * @see org.opencms.loader.I_CmsResourceLoader#service(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
   */
  public void service(CmsObject cms, CmsResource resource, ServletRequest req, ServletResponse res)
  throws CmsException, IOException {

    if (resource instanceof RfsCmsResource) {
      RfsCmsResource rfsFile = (RfsCmsResource)resource;
      FileInputStream fis = null;
      try {
        fis = new FileInputStream(rfsFile.getRfsFile());
        byte[] buffy = new byte[4096];
        int bytesRead;
        while((bytesRead = fis.read(buffy)) >= 0) {
          res.getOutputStream().write(buffy, 0, bytesRead);
          res.getOutputStream().flush();
        }
      }
      finally {
        if (fis != null) {
          fis.close();
        }
      }
    }
    else {
      super.service(cms, resource, req, res);
    }
  }
}
