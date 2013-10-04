opencms-rfs-driver
==================

Installation
------------
1. copy JAR file from release folder to your WEB-INF/lib folder
2. edit WEB-INF/config/opencms-vfs.xml (see src/conf/opencms-vfs.xml, backup your original!!!)
  * comment CmsDumpLoader out and add RfsAwareDumpLoader
  * comment opening tag of CmsJspLoader out and add new opening tag for RfsAwareJspLoader
3. edit WEB-INF/config/opencms.properties (see src/conf/opencms.properties, backup your original!!!)
  * add rfs.vfs.driver=com.metamesh.opencms.rfs.driver.RfsDriver
  * edit driver.vfs to use the new rfs driver before the db driver
4. add metamesh_rfs.properties file to WEB-INF/config/ (example can be found in src/conf/)

Usage
-----
See comments in example metamesh_rfs.properties

License
-------
GPL v3
