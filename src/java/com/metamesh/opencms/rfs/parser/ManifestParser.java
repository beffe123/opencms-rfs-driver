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

package com.metamesh.opencms.rfs.parser;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.metamesh.opencms.rfs.driver.MappingData;


public class ManifestParser extends DefaultHandler {

  private MappingData md;
  
  private File baseFolder;
  private Map<String, Object> currentFile;
  private Map<String, String> currentProperties;
  String currentPropertyName;
  String currentPropertyValue;
  private StringBuilder bob;
  private boolean bodyTagOpen = false;

  private static final String TAG_FILE = "file";
  private static final String TAG_SOURCE = "source";
  private static final String TAG_DESTINATION = "destination";
  private static final String TAG_UUIDSTRUCTURE = "uuidstructure";
  private static final String TAG_UUIDRESOURCE = "uuidresource";
  private static final String TAG_TYPE = "type";
  private static final String TAG_FLAGS = "flags";
  private static final String TAG_NAME = "name";
  private static final String TAG_VALUE = "value";
  private static final String TAG_PROPERTIES = "properties";
  private static final String TAG_PROPERTY = "property";

  private static final String TAG_MODULE = "module";
  
  private static final String[] BODY_TAGS_ARRAY = {TAG_SOURCE,
    TAG_DESTINATION, TAG_UUIDRESOURCE, TAG_UUIDSTRUCTURE, TAG_TYPE,
    TAG_FLAGS, TAG_NAME, TAG_VALUE
  };
  
  private static final String[] FILE_ATTR_TAGS_ARRAY = {TAG_SOURCE,
    TAG_DESTINATION, TAG_UUIDRESOURCE, TAG_UUIDSTRUCTURE, TAG_TYPE,
    TAG_FLAGS
  };
  
  private static final Collection<String> BODY_TAGS = Arrays.asList(BODY_TAGS_ARRAY);
  
  private static final Collection<String> FILE_ATTR_TAGS = Arrays.asList(FILE_ATTR_TAGS_ARRAY);

  public ManifestParser(File baseFolder, MappingData md) {
    this.baseFolder = baseFolder;
    this.md = md;
  }
  
  @Override
  public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws SAXException {
    if (TAG_FILE.equals(localName)) {
      currentFile = new HashMap<String, Object>();
    }
    else if (BODY_TAGS.contains(localName)) {
      bodyTagOpen = true;
      bob = new StringBuilder();
    }
    else if (TAG_PROPERTIES.equals(localName)) {
      currentProperties = new HashMap<String, String>();
    }
    else if (TAG_MODULE.equals(localName)) {
      md.setHasModuleConfig();
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (bodyTagOpen) {
      bob.append(ch, start, length);
    }
  }
  
  @Override
  public void endElement(String uri, String localName, String qName)
      throws SAXException {
    if (TAG_FILE.equals(localName)) {
      md.addManifestEntry(currentFile);
      currentFile = null;
    }
    else if (FILE_ATTR_TAGS.contains(localName)) {
      if (currentFile != null) {
        currentFile.put(localName, bob.toString().trim());
      }
      bodyTagOpen = false;
    }
    else if (TAG_NAME.equals(localName)) {
      currentPropertyName = bob.toString().trim();
      bodyTagOpen = false;
    }
    else if (TAG_VALUE.equals(localName)) {
      currentPropertyValue = bob.toString().trim();
      bodyTagOpen = false;
    }
    else if (TAG_PROPERTY.equals(localName)) {
      currentProperties.put(currentPropertyName, currentPropertyValue);
    }
    else if (TAG_PROPERTIES.equals(localName)) {
      if (currentFile != null) {
        currentFile.put("properties", currentProperties);
      }
    }
  }

  
}
