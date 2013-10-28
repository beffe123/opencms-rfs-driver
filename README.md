Metamesh's RFS Driver for OpenCms
=================================

Purpose
-------
1. Fast Development
  * Make changes to your JSPs and other module files in your IDE without having to import your module or to synchronize
    and restart
  * Better debugging of JSPs - JSPs are executed from their webapp path and so your IDE can find them for debugging
2. Fast Deployment
  * Just unzip your module to your webapp folder and restart OpenCms - no time consuming module updates anymore where 
    your application is not available for minutes (deleting all resources, publishing, importing all resources, 
    publishing, exporting, restart). 
    The RFS driver will read in all files, their properties and even resource types and explorer types
  * In automated deployments there is no need to script with CmsShell, where the process is also as time consuming
    as with manual updates
3. Support for large files like videos
  * It is wise to keep large files out of the DB. The normal process in OpenCms for serving a binary file is to load 
    the file content at first completely from the DB into memory (a byte array) and then write it to 
    the output stream. The drawbacks are that it takes some time (the larger the file the longer it takes) 
    until OpenCms starts to send data to the client and that it requires much heap space.
  * The RFS driver comes with an improved dump loader that streams those large files directly from the RFS. Playback
    of videos can start immediately and it uses only 4 KB of heap.

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

Notes
-----
  * The RFS driver does not distinguish between online and offline projects - all resources are available in both
    without difference
  * You cannot write to RFS resources from within OpenCms
  * You cannot set properties for RFS resources from within OpenCms (but you can set properites within a manifest.xml)
  * I keep my Java classes (the JAR file) completely out of the module and copy it manually (or automatically)
    to WEB-INF/lib
  * Allthough the driver comes with an improved dump loader for large files, you should configure the webserver
    to serve them directly, e.g. everything under the URI "/videos/*"
  * The Driver has been devoloped mainly for developing purpose - there still may be some bugs
  * Most OpenCms modules will not work as unzipped modules because OpenCms comes with some proprietary
    non-standard JSP-extensions (sigh), e.g. a taglibs attribute of the JSP page directive. 
    Those JSPs won't compile. Other extensions may compile, but the JSP won't work as expected.

History
-------
- releases/metamesh_rfs-8.5.0.7.jar is compatible with OpenCms 8.5.x

License
-------
GPL v3
