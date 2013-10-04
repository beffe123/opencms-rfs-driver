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

package org.opencms.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.dom4j.Element;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleManager;
import org.opencms.module.CmsModuleXmlHandler;

import com.metamesh.opencms.rfs.RfsAwareModuleConfiguration;

public class CmsModuleConfiguration extends A_CmsXmlConfiguration 
    implements RfsAwareModuleConfiguration {

  /** The name of the DTD for this configuration. */
  public static final String CONFIGURATION_DTD_NAME = "opencms-modules.dtd";

  /** The name of the default XML file for this configuration. */
  public static final String DEFAULT_XML_FILE_NAME = "opencms-modules.xml";

  /** The node name for the modules top node. */
  public static final String N_MODULES = "modules";

  /** The module manager generated from the configuration. */
  private CmsModuleManager m_moduleManager;

  /** The configured list of module descriptions. */
  private List<CmsModule> m_modules;

  private List<CmsModule> m_readOnlyModules;
  /**
   * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester.Digester)
   */
  public void addXmlDigesterRules(Digester digester) {
    
      // add finish rule
      digester.addCallMethod("*/" + N_MODULES, "initializeFinished");

      // add the module rules for the module digester
      CmsModuleXmlHandler.addXmlDigesterRules(digester);
  }

  /**
   * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
   */
  public Element generateXml(Element parent) {

      List<CmsModule> modules;
      if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
          modules = new ArrayList<CmsModule>();
          Iterator<String> names = OpenCms.getModuleManager().getModuleNames().iterator();
          while (names.hasNext()) {
              CmsModule module = OpenCms.getModuleManager().getModule(names.next());
              if (module != null) {
                  modules.add(module);
              }
          }
          Collections.sort(modules);
      } else {
          // simple unit tests
          modules = m_modules;
      }

      // generate modules node and sub nodes
      Element modulesNode = parent.addElement(N_MODULES);

      for (int i = 0; i < modules.size(); i++) {
          // append all configured modules except read-only modules
          CmsModule module = modules.get(i);
          if (!m_readOnlyModules.contains(module)) {
            Element moduleNode = CmsModuleXmlHandler.generateXml(module);
            modulesNode.add(moduleNode);
          }
      }

      // return the modules node
      return modulesNode;
  }

  /**
   * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
   */
  public String getDtdFilename() {

      return CONFIGURATION_DTD_NAME;
  }

  /**
   * Returns the configured module manager.<p>
   *
   * @return the configured module manager
   */
  public CmsModuleManager getModuleManager() {

      return m_moduleManager;
  }

  /**
   * Will be called when configuration of this object is finished.<p> 
   */
  public void initializeFinished() {
    
      // create the module manager with the configured modules
      m_moduleManager = new CmsModuleManager(m_modules);
      if (CmsLog.INIT.isInfoEnabled()) {
          CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_MODULE_CONFIG_FINISHED_0));
      }
  }
  
  public void addReadonlyModule(CmsModule module) {
    m_modules.add(module);
    m_readOnlyModules.add(module);
  }

  /**
   * Adds a new module to the list of configured modules.<p>
   * 
   * @param moduleHandler contains the imported module
   */
  public void setModule(CmsModuleXmlHandler moduleHandler) {

      // add the module info to the list of configured modules
      m_modules.add(moduleHandler.getModule());
  }

  /**
   * @see org.opencms.configuration.A_CmsXmlConfiguration#initMembers()
   */
  @Override
  protected void initMembers() {

      setXmlFileName(DEFAULT_XML_FILE_NAME);
      m_modules = new ArrayList<CmsModule>();
      m_readOnlyModules = new ArrayList<CmsModule>();
      if (CmsLog.INIT.isInfoEnabled()) {
          CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_MODULE_CONFIG_INIT_0));
      }
  }
}