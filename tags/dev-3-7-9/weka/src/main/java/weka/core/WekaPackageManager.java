/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *    WekaPackageManager.java
 *    Copyright (C) 2009-2012 University of Waikato, Hamilton, New Zealand
 */

package weka.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.pentaho.packageManagement.DefaultPackageManager;
import org.pentaho.packageManagement.Dependency;
import org.pentaho.packageManagement.Package;
import org.pentaho.packageManagement.PackageConstraint;
import org.pentaho.packageManagement.PackageManager;

import weka.core.converters.ConverterUtils;
import weka.gui.GenericObjectEditor;
import weka.gui.GenericPropertiesCreator;
import weka.gui.beans.KnowledgeFlowApp;
import weka.gui.beans.PluginManager;
import weka.gui.explorer.ExplorerDefaults;

/**
 * Class providing package management and manipulation routines. Also provides a
 * command line interface for package management.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
public class WekaPackageManager {

  private static String WEKAFILES_DIR_NAME = "wekafiles";
  public static File WEKA_HOME = new File(System.getProperty("user.home")
      + File.separator + WEKAFILES_DIR_NAME);
  public static File PACKAGES_DIR = new File(System.getProperty("user.home")
      + File.separator + WEKAFILES_DIR_NAME + File.separator + "packages");

  private static String PROPERTIES_DIR_NAME = "props";
  public static File PROPERTIES_DIR = new File(WEKA_HOME.toString()
      + File.separator + PROPERTIES_DIR_NAME);

  private static PackageManager PACKAGE_MANAGER = PackageManager.create();
  private static URL REP_URL; // current repository URL to use
  private static URL CACHE_URL;
  private static boolean INITIAL_CACHE_BUILD_NEEDED = false;
  private static String PACKAGE_LIST_FILENAME = "packageListWithVersion.txt";

  private static String PRIMARY_REPOSITORY = "http://weka.sourceforge.net/packageMetaData";
  private static String REP_MIRROR;
  private static boolean USER_SET_REPO = false;

  private static String PACKAGE_MANAGER_PROPS_FILE_NAME = "PackageManager.props";
  private static Properties m_generalProps;

  public static boolean m_offline;
  private static boolean m_loadPackages = true;

  protected static boolean m_wekaHomeEstablished;
  protected static boolean m_packagesLoaded;
  public static boolean m_initialPackageLoadingInProcess = false;
  public static boolean m_noPackageMetaDataAvailable;

  static {
    establishWekaHome();
  }

  protected static boolean establishWekaHome() {
    if (m_wekaHomeEstablished) {
      return true;
    }
    // look to see if WEKA_HOME has been defined as an environment
    // variable
    Environment env = Environment.getSystemWide();
    String wh = env.getVariableValue("WEKA_HOME");
    if (wh != null) {
      WEKA_HOME = new File(wh);
      PACKAGES_DIR = new File(wh + File.separator + "packages");
      PROPERTIES_DIR = new File(wh + File.separator + PROPERTIES_DIR_NAME);
    } else {
      env.addVariableSystemWide("WEKA_HOME", WEKA_HOME.toString());
    }

    boolean ok = true;
    if (!WEKA_HOME.exists()) {
      // create it for the user
      if (!WEKA_HOME.mkdir()) {
        System.err.println("Unable to create WEKA_HOME ("
            + WEKA_HOME.getAbsolutePath() + ")");
        ok = false;
      }
    }

    if (!PACKAGES_DIR.exists()) {
      // create the packages dir
      if (!PACKAGES_DIR.mkdir()) {
        System.err.println("Unable to create packages directory ("
            + PACKAGES_DIR.getAbsolutePath() + ")");
        ok = false;
      }
    }

    m_wekaHomeEstablished = ok;
    PACKAGE_MANAGER.setPackageHome(PACKAGES_DIR);
    try {
      // setup the backup mirror first
      // establishMirror();

      // user-supplied repository URL takes precedence over anything else
      String repURL = env
          .getVariableValue("weka.core.wekaPackageRepositoryURL");
      if (repURL == null || repURL.length() == 0) {
        // See if there is a URL named in
        // $WEKA_HOME/props/PackageRepository.props
        File repPropsFile = new File(PROPERTIES_DIR.toString() + File.separator
            + "PackageRepository.props");

        if (repPropsFile.exists()) {
          Properties repProps = new Properties();
          repProps.load(new FileInputStream(repPropsFile));
          repURL = repProps.getProperty("weka.core.wekaPackageRepositoryURL");
        }
      }

      if (repURL == null || repURL.length() == 0) {
        repURL = PRIMARY_REPOSITORY;
      } else {
        log(weka.core.logging.Logger.Level.INFO,
            "[WekaPackageManager] weka.core.WekaPackageRepositoryURL = "
                + repURL);
        // System.err.println("[WekaPackageManager] weka.core.WekaPackageRepositoryURL = "
        // + repURL);
        USER_SET_REPO = true;
      }

      REP_URL = new URL(repURL);
      PACKAGE_MANAGER.setPackageRepositoryURL(REP_URL);
    } catch (MalformedURLException ex) {
      ex.printStackTrace();
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    PACKAGE_MANAGER.setBaseSystemName("weka");
    PACKAGE_MANAGER.setBaseSystemVersion(weka.core.Version.VERSION);

    // Now check the cache and establish it if necessary
    File cacheDir = new File(WEKA_HOME.toString() + File.separator + "repCache");
    try {
      String tempCacheString = "file://" + cacheDir.toString();
      // sanitize URI and fix slashes (for Windows)
      tempCacheString = tempCacheString.replace(" ", "%20");
      tempCacheString = tempCacheString.replace('\\', '/');
      if (tempCacheString.startsWith("file://")
          && !tempCacheString.startsWith("file:///")) {
        tempCacheString = tempCacheString.substring(7);
        tempCacheString = "file:///" + tempCacheString;
      }
      URI tempURI = new URI(tempCacheString);
      // System.err.println(" -- " + tempURI.toString());
      CACHE_URL = tempURI.toURL();
    } catch (Exception e) {
      e.printStackTrace();
    }

    File packagesList = new File(cacheDir.getAbsolutePath() + File.separator
        + PACKAGE_LIST_FILENAME);
    if (!cacheDir.exists()) {
      if (!cacheDir.mkdir()) {
        System.err.println("Unable to create repository cache directory ("
            + cacheDir.getAbsolutePath() + ")");
        log(weka.core.logging.Logger.Level.WARNING,
            "Unable to create repository cache directory ("
                + cacheDir.getAbsolutePath() + ")");
        CACHE_URL = null;
      } else {
        // refreshCache();
        INITIAL_CACHE_BUILD_NEEDED = true;
      }
    }

    if (!packagesList.exists()) {
      INITIAL_CACHE_BUILD_NEEDED = true;
    }

    // Package manager general properties
    // Set via system props first
    String offline = env.getVariableValue("weka.packageManager.offline");
    if (offline != null) {
      m_offline = offline.equalsIgnoreCase("true");
    }
    String loadPackages = env
        .getVariableValue("weka.packageManager.loadPackages");
    if (loadPackages == null) {
      // try legacy
      loadPackages = env.getVariableValue("weka.core.loadPackages");
    }

    if (loadPackages != null) {
      m_loadPackages = loadPackages.equalsIgnoreCase("true");
    }

    // load any general package manager properties from props file
    File generalProps = new File(PROPERTIES_DIR.toString() + File.separator
        + PACKAGE_MANAGER_PROPS_FILE_NAME);
    if (generalProps.exists()) {
      Properties gProps = new Properties();
      try {
        gProps.load(new FileInputStream(generalProps));
        m_generalProps = gProps;

        // this one takes precedence over the legacy one
        String repURL = gProps
            .getProperty("weka.core.wekaPackageRepositoryURL");
        if (repURL != null && repURL.length() > 0) {
          REP_URL = new URL(repURL);
          PACKAGE_MANAGER.setPackageRepositoryURL(REP_URL);
        }

        offline = gProps.getProperty("weka.packageManager.offline");
        if (offline != null && offline.length() > 0) {
          m_offline = offline.equalsIgnoreCase("true");
        }

        loadPackages = gProps.getProperty("weka.packageManager.loadPackages");
        if (loadPackages == null) {
          // try legacy
          loadPackages = env.getVariableValue("weka.core.loadPackages");
        }
        if (loadPackages != null) {
          m_loadPackages = loadPackages.equalsIgnoreCase("true");
        }

        String pluginManagerDisableList = gProps
            .getProperty("weka.pluginManager.disable");
        if (pluginManagerDisableList != null
            && pluginManagerDisableList.length() > 0) {
          List<String> disable = new ArrayList<String>();
          String[] parts = pluginManagerDisableList.split(",");
          for (String s : parts) {
            disable.add(s.trim());
          }
          PluginManager.addToDisabledList(disable);
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    if (INITIAL_CACHE_BUILD_NEEDED && m_offline) {
      m_noPackageMetaDataAvailable = true;
    }

    return ok;
  }

  protected static void establishMirror() {
    if (m_offline) {
      return;
    }

    try {
      String mirrorListURL = "http://www.cs.waikato.ac.nz/ml/weka/packageMetaDataMirror.txt";

      URLConnection conn = null;
      URL connURL = new URL(mirrorListURL);

      if (PACKAGE_MANAGER.setProxyAuthentication(connURL)) {
        conn = connURL.openConnection(PACKAGE_MANAGER.getProxy());
      } else {
        conn = connURL.openConnection();
      }

      conn.setConnectTimeout(10000); // timeout after 10 seconds
      conn.setReadTimeout(10000);

      BufferedReader bi = new BufferedReader(new InputStreamReader(
          conn.getInputStream()));

      REP_MIRROR = bi.readLine();

      bi.close();
      if (REP_MIRROR != null && REP_MIRROR.length() > 0) {
        // use the mirror if it is different from the primary repo
        // and the user hasn't specified an explicit repo via the
        // property
        if (!REP_MIRROR.equals(PRIMARY_REPOSITORY) && !USER_SET_REPO) {

          log(weka.core.logging.Logger.Level.INFO,
              "[WekaPackageManager] Package manager using repository mirror: "
                  + REP_MIRROR);

          REP_URL = new URL(REP_MIRROR);
        }
      }
    } catch (Exception ex) {
      log(weka.core.logging.Logger.Level.WARNING,
          "[WekaPackageManager] The repository meta data mirror file seems "
              + "to be unavailable (" + ex.getMessage() + ")");
    }
  }

  protected static void log(weka.core.logging.Logger.Level level, String message) {
    try {
      File logFile = new File(WEKA_HOME.toString() + File.separator
          + "weka.log");
      BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String linefeed = System.getProperty("line.separator");
      String m = format.format(new Date()) + " " + level + ": " + message
          + linefeed;
      writer.write(m);
      writer.flush();
      writer.close();
    } catch (Exception ex) {
    }
  }

  public static void removeExplorerProps(String installedPackageName) {
    try {
      Properties expProps = new Properties();
      String explorerProps = getPackageHome().getAbsolutePath()
          + File.separator + installedPackageName + File.separator
          + "Explorer.props";
      BufferedInputStream bi = new BufferedInputStream(new FileInputStream(
          explorerProps));
      expProps.load(bi);
      bi.close();
      bi = null;
      Set keys = expProps.keySet();
      Iterator keysI = keys.iterator();
      while (keysI.hasNext()) {
        String key = (String) keysI.next();
        if (!key.endsWith("Policy")) {
          // See if this key is in the Explorer props
          String existingVal = ExplorerDefaults.get(key, "");
          String toRemove = expProps.getProperty(key);
          if (existingVal.length() > 0) {
            // cover the case when the value to remove is at the start
            // or middle of a list
            existingVal = existingVal.replace(toRemove + ",", "");

            // the case when it's at the end
            existingVal = existingVal.replace("," + toRemove, "");
            ExplorerDefaults.set(key, existingVal);
          }
        }
      }
    } catch (Exception ex) {
    }
  }

  protected static void processGenericPropertiesCreatorProps(File propsFile) {
    try {
      Properties expProps = new Properties();
      BufferedInputStream bi = new BufferedInputStream(new FileInputStream(
          propsFile));
      expProps.load(bi);
      bi.close();
      bi = null;
      Properties GPCInputProps = GenericPropertiesCreator
          .getGlobalInputProperties();

      Set keys = expProps.keySet();
      Iterator keysI = keys.iterator();
      while (keysI.hasNext()) {
        String key = (String) keysI.next();
        // see if this key is in the GPC input props
        String existingVal = GPCInputProps.getProperty(key, "");
        if (existingVal.length() > 0) {
          // append
          String newVal = expProps.getProperty(key);
          // only append if this value is not already there!!
          if (existingVal.indexOf(newVal) < 0) {
            newVal = existingVal + "," + newVal;
            GPCInputProps.put(key, newVal);
          }
        } else {
          // simply add this new key/value combo
          String newVal = expProps.getProperty(key);
          GPCInputProps.put(key, newVal);
        }
      }
    } catch (Exception ex) {
      // ignore
    }
  }

  protected static void processExplorerProps(File propsFile) {
    try {
      Properties expProps = new Properties();
      BufferedInputStream bi = new BufferedInputStream(new FileInputStream(
          propsFile));
      expProps.load(bi);
      bi.close();
      bi = null;
      Set keys = expProps.keySet();
      Iterator keysI = keys.iterator();
      while (keysI.hasNext()) {
        String key = (String) keysI.next();
        if (!key.endsWith("Policy")) {
          // See if this key is in the Explorer props
          String existingVal = ExplorerDefaults.get(key, "");
          if (existingVal.length() > 0) {
            // get the replacement policy (if any)
            String replacePolicy = expProps.getProperty(key + "Policy");
            if (replacePolicy != null && replacePolicy.length() > 0) {
              if (replacePolicy.equalsIgnoreCase("replace")) {
                String newVal = expProps.getProperty(key);
                ExplorerDefaults.set(key, newVal);
              } else {
                // default to append
                String newVal = expProps.getProperty(key);

                // only append if this value is not already there!!
                if (existingVal.indexOf(newVal) < 0) {
                  newVal = existingVal + "," + newVal;
                  ExplorerDefaults.set(key, newVal);
                }
              }
            } else {
              // default to append
              String newVal = expProps.getProperty(key);
              // only append if this value is not already there!!
              if (existingVal.indexOf(newVal) < 0) {
                newVal = existingVal + "," + newVal;
                ExplorerDefaults.set(key, newVal);
              }
            }
          } else {
            // simply add this new key/value combo
            String newVal = expProps.getProperty(key);
            ExplorerDefaults.set(key, newVal);
          }
        }
      }
    } catch (Exception ex) {
      // ignore
    }
  }

  protected static void processGUIEditorsProps(File propsFile) {
    GenericObjectEditor.registerEditors();
    try {
      Properties editorProps = new Properties();
      BufferedInputStream bi = new BufferedInputStream(new FileInputStream(
          propsFile));
      editorProps.load(bi);
      bi.close();
      bi = null;

      Enumeration enm = editorProps.propertyNames();
      while (enm.hasMoreElements()) {
        String name = enm.nextElement().toString();
        String value = editorProps.getProperty(name, "");
        System.err.println("Registering " + name + " " + value);
        GenericObjectEditor.registerEditor(name, value);
      }

    } catch (Exception ex) {
      // ignore
    }
  }

  protected static void processPluginManagerProps(File propsFile) {
    try {
      PluginManager.addFromProperties(propsFile);
    } catch (Exception ex) {
    }
  }

  protected static void loadPackageDirectory(File directory, boolean verbose)
      throws Exception {
    File[] contents = directory.listFiles();

    // make sure that jar files and lib directory get processed first
    for (int i = 0; i < contents.length; i++) {
      if (contents[i].isFile() && contents[i].getPath().endsWith(".jar")) {
        if (verbose) {
          System.out.println("[Weka] loading " + contents[i].getPath());
        }
        ClassloaderUtil.addFile(contents[i].getPath());
      } else if (contents[i].isDirectory()
          && contents[i].getName().equalsIgnoreCase("lib")) {
        // add any jar files in the lib directory to the classpath
        loadPackageDirectory(contents[i], verbose);
      }
    }

    // now any auxilliary files
    for (int i = 0; i < contents.length; i++) {
      if (contents[i].isFile() && contents[i].getPath().endsWith("Beans.props")) {
        // KnowledgeFlow plugin -- add the Beans.props file to the list of
        // bean plugin props

        KnowledgeFlowApp.addToPluginBeanProps(contents[i]);
        KnowledgeFlowApp.disposeSingleton();

      } else if (contents[i].isFile()
          && contents[i].getPath().endsWith("Explorer.props")) {
        // Explorer tabs plugin
        // process the keys in the properties file and append/add values
        processExplorerProps(contents[i]);
      } else if (contents[i].isFile()
          && contents[i].getPath().endsWith("GUIEditors.props")) {
        // Editor for a particular component
        processGUIEditorsProps(contents[i]);
      } else if (contents[i].isFile()
          && contents[i].getPath().endsWith("GenericPropertiesCreator.props")) {
        processGenericPropertiesCreatorProps(contents[i]);
      } else if (contents[i].isFile()
          && contents[i].getPath().endsWith("PluginManager.props")) {
        processPluginManagerProps(contents[i]);
      }
    }
  }

  public static boolean loadCheck(Package toLoad, File packageRoot,
      PrintStream... progress) {

    // first check against the base version of the system
    boolean load;
    try {
      load = toLoad.isCompatibleBaseSystem();
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }

    if (!load) {
      for (PrintStream p : progress) {
        p.println("[Weka] Skipping package " + toLoad.getName()
            + " because it is not compatible with Weka "
            + PACKAGE_MANAGER.getBaseSystemVersion().toString());
      }
      return false;
    }

    // first check for missing special files/directories and
    // missing external classes (if any)

    if (!(checkForMissingClasses(toLoad, progress) && checkForMissingFiles(
        toLoad, packageRoot, progress))) {
      return false;
    }

    // check for unset environment variables
    if (!checkForUnsetEnvVar(toLoad, progress)) {
      return false;
    }

    if (m_offline) {
      return true;
    }

    // now check for missing dependencies
    try {
      List<Dependency> missing = toLoad.getMissingDependencies();
      if (missing.size() > 0) {
        for (PrintStream p : progress) {
          p.println("[Weka] " + toLoad.getName()
              + " can't be loaded because the following"
              + " packages are missing:");
          for (Dependency d : missing) {
            p.println(d.getTarget());
          }
        }
        return false;
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }

    // now check for buggered dependencies
    try {
      List<Dependency> depends = toLoad.getDependencies();
      for (Dependency d : depends) {
        if (d.getTarget().getPackage().isInstalled()) {
          if (!loadCheck(d.getTarget().getPackage(), packageRoot, progress)) {
            for (PrintStream p : progress) {
              p.println("[Weka] Can't load " + toLoad.getName() + " because "
                  + d.getTarget() + " can't be loaded.");
            }
            return false;
          }
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }

    return true;
  }

  /**
   * Checks to see if there are any environment variables or properties that
   * should be set at startup before allowing this package to be loaded. This is
   * useful for packages that might not be able to function correctly if certain
   * variables are not set correctly.
   * 
   * @param toLoad the package to check
   * @return true if good to go
   */
  public static boolean checkForUnsetEnvVar(Package toLoad,
      PrintStream... progress) {
    Object doNotLoadIfUnsetVar = toLoad
        .getPackageMetaDataElement("DoNotLoadIfEnvVarNotSet");

    boolean result = true;
    if (doNotLoadIfUnsetVar != null
        && doNotLoadIfUnsetVar.toString().length() > 0) {
      String[] elements = doNotLoadIfUnsetVar.toString().split(",");

      Environment env = Environment.getSystemWide();

      for (String var : elements) {
        if (env.getVariableValue(var.trim()) == null) {
          for (PrintStream p : progress) {
            p.println("[Weka] " + toLoad.getName()
                + " can't be loaded because " + "the environment variable "
                + var + " is not set.");
          }

          result = false;
          break;
        }
      }
    }

    if (!result) {
      // grab the message to print to the log (if any)
      Object doNotLoadMessage = toLoad
          .getPackageMetaDataElement("DoNotLoadIfEnvVarNotSetMessage");
      if (doNotLoadMessage != null && doNotLoadMessage.toString().length() > 0) {
        for (PrintStream p : progress) {
          String dnlM = doNotLoadMessage.toString();
          try {
            dnlM = Environment.getSystemWide().substitute(dnlM);
          } catch (Exception e) {
            // quietly ignore
          }
          p.println("[Weka] " + dnlM);
        }
      }
    }

    return result;
  }

  /**
   * Checks to see if there are any classes that we should try to instantiate
   * before allowing this package to be loaded. This is useful for checking to
   * see if third-party classes are accessible. An example would be Java3D,
   * which has an installer that installs into the JRE/JDK.
   * 
   * @param toLoad the package to check
   * @return true if good to go
   */
  public static boolean checkForMissingClasses(Package toLoad,
      PrintStream... progress) {
    boolean result = true;
    Object doNotLoadIfClassNotInstantiable = toLoad
        .getPackageMetaDataElement("DoNotLoadIfClassNotPresent");

    if (doNotLoadIfClassNotInstantiable != null
        && doNotLoadIfClassNotInstantiable.toString().length() > 0) {

      StringTokenizer tok = new StringTokenizer(
          doNotLoadIfClassNotInstantiable.toString(), ",");
      while (tok.hasMoreTokens()) {
        String nextT = tok.nextToken().trim();
        try {
          Class.forName(nextT);
        } catch (Exception ex) {
          for (PrintStream p : progress) {
            p.println("[Weka] " + toLoad.getName()
                + " can't be loaded because " + nextT
                + " can't be instantiated.");
          }
          result = false;
          break;
        }
      }
    }

    if (!result) {
      // grab the message to print to the log (if any)
      Object doNotLoadMessage = toLoad
          .getPackageMetaDataElement("DoNotLoadIfClassNotPresentMessage");
      if (doNotLoadMessage != null && doNotLoadMessage.toString().length() > 0) {
        for (PrintStream p : progress) {
          String dnlM = doNotLoadMessage.toString();
          try {
            dnlM = Environment.getSystemWide().substitute(dnlM);
          } catch (Exception e) {
            // quietly ignore
          }
          p.println("[Weka] " + dnlM);
        }
      }
    }

    return result;
  }

  /**
   * Checks to see if there are any missing files/directories for a given
   * package. If there are missing files, then the package can't be loaded. An
   * example would be a connector package that, for whatever reason, can't
   * include a necessary third-party jar file in its lib folder, and requires
   * the user to download and install this jar file manually.
   * 
   * @param toLoad the package to check
   * @param packageRoot the root directory of the package
   * @return true if good to go
   */
  public static boolean checkForMissingFiles(Package toLoad, File packageRoot,
      PrintStream... progress) {
    boolean result = true;

    Object doNotLoadIfFileMissing = toLoad
        .getPackageMetaDataElement("DoNotLoadIfFileNotPresent");
    String packageRootPath = packageRoot.getPath() + File.separator;

    if (doNotLoadIfFileMissing != null
        && doNotLoadIfFileMissing.toString().length() > 0) {

      StringTokenizer tok = new StringTokenizer(
          doNotLoadIfFileMissing.toString(), ",");
      while (tok.hasMoreTokens()) {
        String nextT = tok.nextToken().trim();
        File toCheck = new File(packageRootPath + nextT);
        if (!toCheck.exists()) {
          for (PrintStream p : progress) {
            p.println("[Weka] " + toLoad.getName()
                + " can't be loaded because " + toCheck.getPath()
                + " appears to be missing.");
          }
          result = false;
          break;
        }
      }
    }

    if (!result) {
      // grab the message to print to the log (if any)
      Object doNotLoadMessage = toLoad
          .getPackageMetaDataElement("DoNotLoadIfFileNotPresentMessage");
      if (doNotLoadMessage != null && doNotLoadMessage.toString().length() > 0) {
        String dnlM = doNotLoadMessage.toString();
        try {
          dnlM = Environment.getSystemWide().substitute(dnlM);
        } catch (Exception ex) {
          // quietly ignore
        }
        for (PrintStream p : progress) {
          p.println("[Weka] " + dnlM);
        }
      }
    }

    return result;
  }

  public static synchronized void loadPackages(boolean verbose) {
    loadPackages(verbose, true);
  }

  public static synchronized void loadPackages(boolean verbose,
      boolean refreshGOEProperties) {

    if (!m_loadPackages) {
      return;
    }

    if (m_packagesLoaded) {
      return;
    }

    m_packagesLoaded = true;
    m_initialPackageLoadingInProcess = true;
    if (establishWekaHome()) {
      // try and load any jar files and add to the classpath
      File[] contents = PACKAGES_DIR.listFiles();

      // if we have a non-empty packages dir then check
      // the integrity of our cache first
      if (contents.length > 0) {
        establishCacheIfNeeded(System.out);
      }

      for (int i = 0; i < contents.length; i++) {
        if (contents[i].isDirectory()) {
          try {
            Package toLoad = getInstalledPackageInfo(contents[i].getName());
            boolean load;
            // Only perform the check against the current version of Weka if
            // there exists
            // a Description.props file
            if (toLoad != null) {

              load = loadCheck(toLoad, contents[i], System.err);

              if (load) {
                if (verbose) {
                  System.out.println("[Weka] loading package "
                      + contents[i].getName());
                }
                loadPackageDirectory(contents[i], verbose);
              }
            }
          } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("[Weka] Problem loading package "
                + contents[i].getName() + " skipping...");
          }
        }
      }
    }
    m_initialPackageLoadingInProcess = false;

    // do we need to regenerate the list of available schemes for
    // the GUIs (this is not necessary when executing stuff from
    // the command line
    if (refreshGOEProperties) {
      System.err.println("Refreshing GOE props...");
      refreshGOEProperties();
    }
  }

  public static void refreshGOEProperties() {
    ClassDiscovery.clearClassCache();
    GenericPropertiesCreator.regenerateGlobalOutputProperties();
    GenericObjectEditor.determineClasses();
    ConverterUtils.initialize();
    KnowledgeFlowApp.disposeSingleton();
    KnowledgeFlowApp.reInitialize();
  }

  /**
   * Get the underlying package manager implementation
   * 
   * @return the underlying concrete package management implementation.
   */
  public static PackageManager getUnderlyingPackageManager() {
    return PACKAGE_MANAGER;
  }

  /**
   * Retrieves the size (in KB) of the repository zip archive stored on the
   * server.
   * 
   * @return the size of the repository zip archive in KB.
   */
  public static int repoZipArchiveSize() {
    int size = -1;

    try {
      PACKAGE_MANAGER.setPackageRepositoryURL(REP_URL);
      String numPackagesS = PACKAGE_MANAGER.getPackageRepositoryURL()
          .toString() + "/repoSize.txt";
      URLConnection conn = null;
      URL connURL = new URL(numPackagesS);

      if (PACKAGE_MANAGER.setProxyAuthentication(connURL)) {
        conn = connURL.openConnection(PACKAGE_MANAGER.getProxy());
      } else {
        conn = connURL.openConnection();
      }

      conn.setConnectTimeout(30000); // timeout after 30 seconds

      BufferedReader bi = new BufferedReader(new InputStreamReader(
          conn.getInputStream()));

      String n = bi.readLine();
      try {
        size = Integer.parseInt(n);
      } catch (NumberFormatException ne) {
        System.err.println("[WekaPackageManager] problem parsing the size "
            + "of repository zip archive from the server.");
      }
      bi.close();

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return size;
  }

  /**
   * Get the number of packages that are available at the repository.
   * 
   * @return the number of packages that are available (or -1 if this can't be
   *         determined for some reason.
   */
  public static int numRepositoryPackages() {
    int numPackages = -1;
    try {
      PACKAGE_MANAGER.setPackageRepositoryURL(REP_URL);
      String numPackagesS = PACKAGE_MANAGER.getPackageRepositoryURL()
          .toString() + "/numPackages.txt";
      URLConnection conn = null;
      URL connURL = new URL(numPackagesS);

      if (PACKAGE_MANAGER.setProxyAuthentication(connURL)) {
        conn = connURL.openConnection(PACKAGE_MANAGER.getProxy());
      } else {
        conn = connURL.openConnection();
      }

      conn.setConnectTimeout(30000); // timeout after 30 seconds

      BufferedReader bi = new BufferedReader(new InputStreamReader(
          conn.getInputStream()));

      String n = bi.readLine();
      try {
        numPackages = Integer.parseInt(n);
      } catch (NumberFormatException ne) {
        System.err.println("[WekaPackageManager] problem parsing number "
            + "of packages from server.");
      }
      bi.close();

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return numPackages;
  }

  /**
   * Just get a list of the package names. This is faster than calling
   * getAllPackages(), especially if fetching from the online repository, since
   * the full meta data for each package doesn't have to be read.
   * 
   * @param local true if the local package list in the cache should be read
   *          rather than the online repository
   * 
   * @return a Map<String, String> of all the package names available either
   *         locally or at the repository
   */
  public static Map<String, String> getPackageList(boolean local) {
    Map<String, String> result = new HashMap<String, String>();

    try {
      useCacheOrOnlineRepository();

      if (!local) {
        PACKAGE_MANAGER.setPackageRepositoryURL(REP_URL);
      }

      String packageListS = PACKAGE_MANAGER.getPackageRepositoryURL()
          .toString() + "/packageListWithVersion.txt";
      URLConnection conn = null;
      URL connURL = new URL(packageListS);

      if (PACKAGE_MANAGER.setProxyAuthentication(connURL)) {
        conn = connURL.openConnection(PACKAGE_MANAGER.getProxy());
      } else {
        conn = connURL.openConnection();
      }

      conn.setConnectTimeout(30000); // timeout after 30 seconds

      BufferedReader bi = new BufferedReader(new InputStreamReader(
          conn.getInputStream()));
      String l = null;
      while ((l = bi.readLine()) != null) {
        String[] parts = l.split(":");
        if (parts.length == 2) {
          result.put(parts[0], parts[1]);
        }
      }
      bi.close();

    } catch (Exception ex) {
      // ex.printStackTrace();
      result = null;
    }

    return result;
  }

  public static Exception establishCacheIfNeeded(PrintStream... progress) {
    if (m_offline) {
      return null;
    }

    if (REP_MIRROR == null) {
      establishMirror();
    }

    Exception problem = null;
    if (INITIAL_CACHE_BUILD_NEEDED) {
      for (PrintStream p : progress) {
        p.println("Caching repository meta data, please wait...");
      }

      problem = refreshCache(progress);

      INITIAL_CACHE_BUILD_NEEDED = false;
    }
    return problem;
  }

  public static Exception checkForNewPackages(PrintStream... progress) {

    if (m_offline) {
      return null;
    }

    Exception problem = null;

    Map<String, String> localPackageNameList = getPackageList(true);

    if (localPackageNameList == null) {

      System.err.println("Local package list is missing, trying a "
          + "cache refresh to restore...");
      refreshCache(progress);
      localPackageNameList = getPackageList(true);
      if (localPackageNameList == null) {
        // quietly return and see if we can continue anyway
        return null;
      }
    }

    Map<String, String> repositoryPackageNameList = getPackageList(false);

    if (repositoryPackageNameList == null) {
      // quietly return and see if we can continue anyway
      return null;
    }

    if (repositoryPackageNameList.keySet().size() != localPackageNameList
        .keySet().size()) {
      // package(s) have disappeared from the repository.
      // Force a cache refresh...
      if (repositoryPackageNameList.keySet().size() < localPackageNameList
          .keySet().size()) {
        for (PrintStream p : progress) {
          p.println("Some packages no longer exist at the repository. "
              + "Refreshing cache...");
        }
      } else {
        for (PrintStream p : progress) {
          p.println("There are new packages at the repository. "
              + "Refreshing cache...");
        }
      }
      problem = refreshCache(progress);
    } else {
      // check for new versions of packages
      boolean refresh = false;
      for (String localPackage : localPackageNameList.keySet()) {
        String localVersion = localPackageNameList.get(localPackage);

        String repoVersion = repositoryPackageNameList.get(localPackage);
        if (repoVersion == null) {
          continue;
        }

        // a difference here indicates a newer version on the server
        if (!localVersion.equals(repoVersion)) {
          refresh = true;
          break;
        }
      }

      if (refresh) {
        for (PrintStream p : progress) {
          p.println("There are newer versions of existing packages "
              + "at the repository. Refreshing cache...");
        }
        problem = refreshCache(progress);
      }
    }

    return problem;
  }

  public static Exception refreshCache(PrintStream... progress) {
    Exception problem = null;
    if (CACHE_URL == null) {
      return null;
    }

    PACKAGE_MANAGER.setPackageRepositoryURL(REP_URL);
    String cacheDir = WEKA_HOME.toString() + File.separator + "repCache";
    try {
      for (PrintStream p : progress) {
        p.println("Refresh in progress. Please wait...");
      }
      byte[] zip = PACKAGE_MANAGER
          .getRepositoryPackageMetaDataOnlyAsZip(progress);

      ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip));
      ZipEntry ze;
      final byte[] buff = new byte[1024];
      while ((ze = zis.getNextEntry()) != null) {
        // System.out.println("Cache: inflating " + ze.getName());
        if (ze.isDirectory()) {
          new File(cacheDir, ze.getName()).mkdir();
          continue;
        }
        BufferedOutputStream bo = new BufferedOutputStream(
            new FileOutputStream(new File(cacheDir, ze.getName())));
        while (true) {
          int amountRead = zis.read(buff);
          if (amountRead == -1) {
            break;
          }
          // write the data here
          bo.write(buff, 0, amountRead);
        }
        bo.close();
      }
    } catch (Exception e) {
      e.printStackTrace();

      // OK, we have a problem with the repository cache - use
      // the repository itself instead and delete repCache
      CACHE_URL = null;
      try {
        DefaultPackageManager.deleteDir(new File(cacheDir), System.out);
      } catch (Exception e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }

      return e;
    }

    return problem;
  }

  public static boolean installedPackageResourceExists(String packageName,
      String resourceName) {

    String fullResourcePath = getPackageHome().toString() + File.separator
        + packageName + File.separator + resourceName;

    return new File(fullResourcePath).exists();
  }

  private static void useCacheOrOnlineRepository() {
    if (REP_MIRROR == null) {
      establishMirror();
    }

    if (CACHE_URL != null) {
      PACKAGE_MANAGER.setPackageRepositoryURL(CACHE_URL);
    } else if (REP_URL != null) {
      PACKAGE_MANAGER.setPackageRepositoryURL(REP_URL);
    }
  }

  public static File getPackageHome() {
    return PACKAGE_MANAGER.getPackageHome();
  }

  /**
   * Find the most recent version of the package encapsulated in the supplied
   * PackageConstraint argument that satisfies the constraint
   * 
   * @param toCheck the PackageConstraint containing the package in question
   * @return the most recent version of the package satisfying the constraint
   * @throws Exception if a version can't be found that satisfies the constraint
   *           or an error occurs while communicating with the respository
   */
  public static Package mostRecentVersionWithRespectToConstraint(
      PackageConstraint toCheck) throws Exception {
    Package target = toCheck.getPackage();
    Package result = null;

    List<Object> availableVersions = PACKAGE_MANAGER
        .getRepositoryPackageVersions(target.getName());

    // version numbers will be in descending sorted order from the repository
    // we want the most recent version that meets the target constraint
    for (Object version : availableVersions) {
      Package candidate = PACKAGE_MANAGER.getRepositoryPackageInfo(
          target.getName(), version);
      if (toCheck.checkConstraint(candidate)) {
        result = candidate;
        break;
      }
    }

    if (result == null) {
      throw new Exception("[WekaPackageManager] unable to find a version of "
          + "package " + target.getName() + " that meets constraint "
          + toCheck.toString());
    }

    return result;
  }

  public static boolean installPackages(List<Package> toInstall,
      PrintStream... progress) throws Exception {

    useCacheOrOnlineRepository();
    List<Boolean> upgrades = new ArrayList<Boolean>();
    for (Package p : toInstall) {
      if (p.isInstalled()) {
        upgrades.add(new Boolean(true));
      } else {
        upgrades.add(new Boolean(false));
      }
    }
    PACKAGE_MANAGER.installPackages(toInstall, progress);
    boolean atLeastOneUpgrade = false;

    int i = 0;
    for (Package p : toInstall) {
      boolean isAnUpgrade = upgrades.get(i++);
      if (isAnUpgrade) {
        atLeastOneUpgrade = true;
      }

      String packageName = p.getName();
      File packageDir = new File(PACKAGE_MANAGER.getPackageHome().toString()
          + File.separator + packageName);

      boolean loadIt = loadCheck(p, packageDir, progress);

      if (loadIt & !isAnUpgrade) {
        loadPackageDirectory(packageDir, false);
      }
    }
    return atLeastOneUpgrade;
  }

  public static List<Object> getRepositoryPackageVersions(String packageName)
      throws Exception {
    useCacheOrOnlineRepository();
    return PACKAGE_MANAGER.getRepositoryPackageVersions(packageName);
  }

  public static URL getPackageRepositoryURL() {
    useCacheOrOnlineRepository();
    return PACKAGE_MANAGER.getPackageRepositoryURL();
  }

  public static List<Package> getAllPackages() throws Exception {
    useCacheOrOnlineRepository();
    return PACKAGE_MANAGER.getAllPackages();
  }

  public static List<Package> getAvailablePackages() throws Exception {
    useCacheOrOnlineRepository();
    return PACKAGE_MANAGER.getAvailablePackages();
  }

  public static List<Package> getInstalledPackages() throws Exception {
    useCacheOrOnlineRepository();
    return PACKAGE_MANAGER.getInstalledPackages();
  }

  public static List<Dependency> getAllDependenciesForPackage(Package target,
      Map<String, List<Dependency>> conflicts) throws Exception {
    useCacheOrOnlineRepository();
    return PACKAGE_MANAGER.getAllDependenciesForPackage(target, conflicts);
  }

  public static Package getPackageArchiveInfo(String packageArchivePath)
      throws Exception {
    useCacheOrOnlineRepository();
    return PACKAGE_MANAGER.getPackageArchiveInfo(packageArchivePath);
  }

  public static Package getInstalledPackageInfo(String packageName)
      throws Exception {
    useCacheOrOnlineRepository();
    return PACKAGE_MANAGER.getInstalledPackageInfo(packageName);
  }

  public static Package getRepositoryPackageInfo(String packageName)
      throws Exception {
    useCacheOrOnlineRepository();
    return PACKAGE_MANAGER.getRepositoryPackageInfo(packageName);
  }

  public static Package getRepositoryPackageInfo(String packageName,
      String version) throws Exception {
    useCacheOrOnlineRepository();
    return PACKAGE_MANAGER.getRepositoryPackageInfo(packageName, version);
  }

  public static boolean installPackageFromRepository(String packageName,
      String version, PrintStream... progress) throws Exception {
    useCacheOrOnlineRepository();
    Package toLoad = getRepositoryPackageInfo(packageName);

    // check to see if a version is already installed. If so, we
    // wont load the updated version into the classpath immediately in
    // order to avoid conflicts, class not found exceptions etc. The
    // user is told to restart Weka for the changes to come into affect
    // anyway, so there is no point in loading the updated package now.
    boolean isAnUpgrade = toLoad.isInstalled();

    Object specialInstallMessage = toLoad
        .getPackageMetaDataElement("MessageToDisplayOnInstallation");
    if (specialInstallMessage != null
        && specialInstallMessage.toString().length() > 0) {
      String siM = specialInstallMessage.toString();
      try {
        siM = Environment.getSystemWide().substitute(siM);
      } catch (Exception ex) {
        // quietly ignore
      }
      String message = "**** Special installation message ****\n" + siM
          + "\n**** Special installation message ****";
      for (PrintStream p : progress) {
        p.println(message);
      }
    }

    PACKAGE_MANAGER
        .installPackageFromRepository(packageName, version, progress);
    File packageDir = new File(PACKAGE_MANAGER.getPackageHome().toString()
        + File.separator + packageName);

    boolean loadIt = checkForMissingClasses(toLoad, progress);
    if (loadIt && !isAnUpgrade) {
      File packageRoot = new File(PACKAGE_MANAGER.getPackageHome()
          + File.separator + packageName);
      loadIt = checkForMissingFiles(toLoad, packageRoot, progress);
      if (loadIt) {
        loadPackageDirectory(packageDir, false);
      }
    }

    return isAnUpgrade;
  }

  public static String installPackageFromArchive(String packageArchivePath,
      PrintStream... progress) throws Exception {
    useCacheOrOnlineRepository();
    Package toInstall = PACKAGE_MANAGER
        .getPackageArchiveInfo(packageArchivePath);

    Object specialInstallMessage = toInstall
        .getPackageMetaDataElement("MessageToDisplayOnInstallation");
    if (specialInstallMessage != null
        && specialInstallMessage.toString().length() > 0) {
      String siM = specialInstallMessage.toString();
      try {
        siM = Environment.getSystemWide().substitute(siM);
      } catch (Exception ex) {
        // quietly ignore
      }
      String message = "**** Special installation message ****\n" + siM
          + "\n**** Special installation message ****";
      for (PrintStream p : progress) {
        p.println(message);
      }
    }

    PACKAGE_MANAGER.installPackageFromArchive(packageArchivePath, progress);

    boolean loadIt = checkForMissingClasses(toInstall, progress);
    if (loadIt) {
      File packageRoot = new File(PACKAGE_MANAGER.getPackageHome()
          + File.separator + toInstall.getName());
      loadIt = checkForMissingFiles(toInstall, packageRoot, progress);
      if (loadIt) {
        loadPackageDirectory(packageRoot, false);
      }
    }

    return toInstall.getName();
  }

  public static String installPackageFromURL(URL packageURL,
      PrintStream... progress) throws Exception {
    useCacheOrOnlineRepository();
    String packageName = PACKAGE_MANAGER.installPackageFromURL(packageURL,
        progress);

    Package installed = PACKAGE_MANAGER.getInstalledPackageInfo(packageName);

    Object specialInstallMessage = installed
        .getPackageMetaDataElement("MessageToDisplayOnInstallation");
    if (specialInstallMessage != null
        && specialInstallMessage.toString().length() > 0) {
      String message = "**** Special installation message ****\n"
          + specialInstallMessage.toString()
          + "\n**** Special installation message ****";
      for (PrintStream p : progress) {
        p.println(message);
      }
    }

    boolean loadIt = checkForMissingClasses(installed, progress);
    if (loadIt) {
      File packageRoot = new File(PACKAGE_MANAGER.getPackageHome()
          + File.separator + installed.getName());
      loadIt = checkForMissingFiles(installed, packageRoot, progress);
      if (loadIt) {
        loadPackageDirectory(packageRoot, false);
      }
    }
    return packageName;
  }

  public static void uninstallPackage(String packageName,
      boolean updateKnowledgeFlow, PrintStream... progress) throws Exception {

    // check to see if this is a KnowledgeFlow package (presence of Beans.props
    // file)
    if (updateKnowledgeFlow) {
      File packageToDel = new File(PACKAGE_MANAGER.getPackageHome().toString()
          + File.separator + packageName);
      if (packageToDel.exists() && packageToDel.isDirectory()) {
        File[] contents = packageToDel.listFiles();
        for (int i = 0; i < contents.length; i++) {
          if (contents[i].isFile()
              && contents[i].getPath().endsWith("Beans.props")) {
            // KnowledgeFlow plugin -- remove this properties file from the list
            // of
            // bean plugin props

            KnowledgeFlowApp.removeFromPluginBeanProps(contents[i]);
            KnowledgeFlowApp.disposeSingleton();
            break;
          }
        }
      }
    }

    PACKAGE_MANAGER.uninstallPackage(packageName, progress);
  }

  private static void printPackageInfo(Map<?, ?> packageProps) {
    Set<?> keys = packageProps.keySet();
    Iterator<?> i = keys.iterator();

    while (i.hasNext()) {
      Object key = i.next();
      Object value = packageProps.get(key);
      System.out.println(Utils.padLeft(key.toString(), 11) + ":\t"
          + value.toString());
    }
  }

  protected static void printPackageArchiveInfo(String packagePath)
      throws Exception {
    Map<?, ?> packageProps = getPackageArchiveInfo(packagePath)
        .getPackageMetaData();
    printPackageInfo(packageProps);
  }

  protected static void printInstalledPackageInfo(String packageName)
      throws Exception {
    Map<?, ?> packageProps = getInstalledPackageInfo(packageName)
        .getPackageMetaData();
    printPackageInfo(packageProps);
  }

  protected static void printRepositoryPackageInfo(String packageName,
      String version) throws Exception {
    Map<?, ?> packageProps = getRepositoryPackageInfo(packageName, version)
        .getPackageMetaData();
    printPackageInfo(packageProps);
  }

  private static String queryUser() {
    java.io.BufferedReader br = new java.io.BufferedReader(
        new java.io.InputStreamReader(System.in));

    String result = null;
    try {
      result = br.readLine();
    } catch (java.io.IOException ex) {
      // ignore
    }

    return result;
  }

  private static void removeInstalledPackage(String packageName, boolean force,
      PrintStream... progress) throws Exception {

    List<Package> compromised = new ArrayList<Package>();

    // Now check to see which other installed packages depend on this one
    List<Package> installedPackages = null;
    if (!force) {
      installedPackages = getInstalledPackages();
      for (Package p : installedPackages) {
        List<Dependency> tempDeps = p.getDependencies();

        for (Dependency d : tempDeps) {
          if (d.getTarget().getPackage().getName().equals(packageName)) {
            // add this installed package to the list
            compromised.add(p);
            break;
          }
        }
      }

      if (compromised.size() > 0) {
        System.out.println("The following installed packages depend on "
            + packageName + " :\n");
        for (Package p : compromised) {
          System.out.println("\t" + p.getName());
        }

        System.out.println("\nDo you wish to proceed [y/n]?");
        String response = queryUser();
        if (response != null
            && (response.equalsIgnoreCase("n") || response
                .equalsIgnoreCase("no"))) {
          return; // bail out here
        }
      }
    }

    if (force) {
      System.out.println("Forced uninstall.");
    }

    compromised = null;
    installedPackages = null;

    uninstallPackage(packageName, false, progress);
  }

  private static void installPackageFromRepository(String packageName,
      String version, boolean force) throws Exception {

    Package toInstall = null;
    try {
      toInstall = getRepositoryPackageInfo(packageName, version);
    } catch (Exception ex) {
      System.err.println("[WekaPackageManager] Package " + packageName
          + " at version " + version + " doesn't seem to exist!");
      // System.exit(1);
      return;
    }
    // First check to see if this package is compatible with the base system

    if (!force) {
      boolean ok = toInstall.isCompatibleBaseSystem();

      if (!ok) {
        List<Dependency> baseSysDep = toInstall.getBaseSystemDependency();
        StringBuffer depList = new StringBuffer();
        for (Dependency bd : baseSysDep) {
          depList.append(bd.getTarget().toString() + " ");
        }
        System.err.println("Can't install package " + packageName
            + " because it requires " + depList.toString());
        return;
      }

      // true if this package is already installed. In which case we need to
      // check
      // if changing this package will impact other already installed packages
      boolean upOrDowngrading = false;
      if (toInstall.isInstalled()) {
        Package installedVersion = getInstalledPackageInfo(packageName);
        if (!toInstall.equals(installedVersion)) {

          System.out
              .println("Package " + packageName + "[" + installedVersion
                  + "] is already installed. Replace with " + toInstall
                  + " [y/n]?");

          String response = queryUser();
          if (response != null
              && (response.equalsIgnoreCase("n") || response
                  .equalsIgnoreCase("no"))) {
            return; // bail out here
          }
          upOrDowngrading = true;
        } else {
          System.out.println("Package " + packageName + "[" + installedVersion
              + "] is already installed. Install again [y/n]?");
          String response = queryUser();
          if (response != null
              && (response.equalsIgnoreCase("n") || response
                  .equalsIgnoreCase("no"))) {
            return; // bail out here
          }
        }
      }

      // Now get a full list of dependencies for this package and
      // check for any conflicts
      Map<String, List<Dependency>> conflicts = new HashMap<String, List<Dependency>>();
      List<Dependency> dependencies = getAllDependenciesForPackage(toInstall,
          conflicts);

      if (conflicts.size() > 0) {
        System.err.println("Package " + packageName
            + " requires the following packages:\n");
        Iterator<Dependency> depI = dependencies.iterator();
        while (depI.hasNext()) {
          Dependency d = depI.next();
          System.err.println("\t" + d);
        }

        System.err.println("\nThere are conflicting dependencies:\n");
        Set<String> pNames = conflicts.keySet();
        Iterator<String> pNameI = pNames.iterator();
        while (pNameI.hasNext()) {
          String pName = pNameI.next();
          System.err.println("Conflicts for " + pName);
          List<Dependency> confsForPackage = conflicts.get(pName);
          Iterator<Dependency> confs = confsForPackage.iterator();
          while (confs.hasNext()) {
            Dependency problem = confs.next();
            System.err.println("\t" + problem);
          }
        }

        System.err.println("Unable to continue with installation.");
        return; // bail out here.
      }

      // Next check all dependencies against what is installed and
      // inform the user about which installed packages will be altered. Also
      // build the list of only those packages that need to be installed or
      // upgraded (excluding those that are already installed and are OK).
      List<PackageConstraint> needsUpgrade = new ArrayList<PackageConstraint>();
      List<Package> finalListToInstall = new ArrayList<Package>();

      Iterator<Dependency> depI = dependencies.iterator();
      while (depI.hasNext()) {
        Dependency toCheck = depI.next();
        if (toCheck.getTarget().getPackage().isInstalled()) {
          String toCheckName = toCheck.getTarget().getPackage()
              .getPackageMetaDataElement("PackageName").toString();
          Package installedVersion = PACKAGE_MANAGER
              .getInstalledPackageInfo(toCheckName);
          if (!toCheck.getTarget().checkConstraint(installedVersion)) {
            needsUpgrade.add(toCheck.getTarget());
            Package mostRecent = toCheck.getTarget().getPackage();
            if (toCheck.getTarget() instanceof org.pentaho.packageManagement.VersionPackageConstraint) {
              mostRecent = WekaPackageManager
                  .mostRecentVersionWithRespectToConstraint(toCheck.getTarget());
            }
            finalListToInstall.add(mostRecent);
          }
        } else {
          Package mostRecent = toCheck.getTarget().getPackage();
          if (toCheck.getTarget() instanceof org.pentaho.packageManagement.VersionPackageConstraint) {
            mostRecent = WekaPackageManager
                .mostRecentVersionWithRespectToConstraint(toCheck.getTarget());
          }
          finalListToInstall.add(mostRecent);
        }
      }

      if (needsUpgrade.size() > 0) {
        System.out
            .println("The following packages will be upgraded in order to install "
                + packageName);
        Iterator<PackageConstraint> upI = needsUpgrade.iterator();
        while (upI.hasNext()) {
          PackageConstraint tempC = upI.next();
          System.out.println("\t" + tempC);
        }

        System.out.print("\nOK to continue [y/n]? > ");
        String response = queryUser();
        if (response != null
            && (response.equalsIgnoreCase("n") || response
                .equalsIgnoreCase("no"))) {
          return; // bail out here
        }

        // now take a look at the other installed packages and see if
        // any would have a problem when these ones are upgraded
        boolean conflictsAfterUpgrade = false;
        List<Package> installed = getInstalledPackages();
        List<Package> toUpgrade = new ArrayList<Package>();
        upI = needsUpgrade.iterator();
        while (upI.hasNext()) {
          toUpgrade.add(upI.next().getPackage());
        }

        // add the actual package the user is wanting to install if it
        // is going to be an up/downgrade rather than a first install since
        // other installed packages may depend on the currently installed
        // version
        // and thus could be affected after the up/downgrade
        toUpgrade.add(toInstall);

        for (int i = 0; i < installed.size(); i++) {
          Package tempP = installed.get(i);
          String tempPName = tempP.getName();
          boolean checkIt = true;
          for (int j = 0; j < needsUpgrade.size(); j++) {
            if (tempPName.equals(needsUpgrade.get(j).getPackage().getName())) {
              checkIt = false;
              break;
            }
          }

          if (checkIt) {
            List<Dependency> problem = tempP
                .getIncompatibleDependencies(toUpgrade);
            if (problem.size() > 0) {
              conflictsAfterUpgrade = true;

              System.err
                  .println("Package "
                      + tempP.getName()
                      + " will have a compatibility"
                      + "problem with the following packages after upgrading them:");
              Iterator<Dependency> dI = problem.iterator();
              while (dI.hasNext()) {
                System.err.println("\t" + dI.next().getTarget().getPackage());
              }
            }
          }
        }

        if (conflictsAfterUpgrade) {
          System.err.println("Unable to continue with installation.");
          return; // bail out here
        }

      }

      if (finalListToInstall.size() > 0) {
        System.out.println("To install " + packageName
            + " the following packages will" + " be installed/upgraded:\n\n");
        Iterator<Package> i = finalListToInstall.iterator();
        while (i.hasNext()) {
          System.out.println("\t" + i.next());
        }
        System.out.print("\nOK to proceed [y/n]? > ");
        String response = queryUser();

        if (response != null
            && (response.equalsIgnoreCase("n") || response
                .equalsIgnoreCase("no"))) {
          return; // bail out here
        }
      }

      // OK, now we can download and install everything

      // First install the final list of dependencies
      installPackages(finalListToInstall, System.out);

      // Now install the package itself
      installPackageFromRepository(packageName, version, System.out);

    } else {
      // just install this package without checking/downloading dependencies
      // etc.
      installPackageFromRepository(packageName, version, System.out);
    }
  }

  private static void listPackages(String arg) throws Exception {

    if (m_offline
        && (arg.equalsIgnoreCase("all") || arg.equalsIgnoreCase("available"))) {
      System.out.println("Running offline - unable to display "
          + "available or all package information");
      return;
    }

    List<Package> packageList = null;
    useCacheOrOnlineRepository();

    if (arg.equalsIgnoreCase("all")) {
      packageList = PACKAGE_MANAGER.getAllPackages();
    } else if (arg.equals("installed")) {
      packageList = PACKAGE_MANAGER.getInstalledPackages();
    } else if (arg.equals("available")) {
      packageList = PACKAGE_MANAGER.getAvailablePackages();
    } else {
      System.err.println("[WekaPackageManager] Unknown argument " + arg);
      printUsage();
      // System.exit(1);
      return;
    }

    StringBuffer result = new StringBuffer();
    result.append("Installed\tRepository\tPackage\n");
    result.append("=========\t==========\t=======\n");

    Iterator<Package> i = packageList.iterator();
    while (i.hasNext()) {
      Package p = i.next();
      String installedV = "-----    ";
      String repositoryV = "-----     ";
      if (p.isInstalled()) {
        Package installedP = getInstalledPackageInfo(p.getName());
        installedV = installedP.getPackageMetaDataElement("Version").toString()
            + "    ";
        try {
          if (!m_offline) {
            Package repP = getRepositoryPackageInfo(p.getName());
            repositoryV = repP.getPackageMetaDataElement("Version").toString()
                + "     ";
          }
        } catch (Exception ex) {
          // not at the repository
        }
      } else {
        repositoryV = p.getPackageMetaDataElement("Version").toString()
            + "     ";
      }
      String title = p.getPackageMetaDataElement("Title").toString();
      result.append(installedV + "\t" + repositoryV + "\t" + p.getName() + ": "
          + title + "\n");
    }

    System.out.println(result.toString());
  }

  private static void printUsage() {
    System.out
        .println("Usage: weka.core.WekaPackageManager [-offline] [option]");
    System.out
        .println("Options:\n"
            + "\t-list-packages <all | installed | available>\n"
            + "\t-package-info <repository | installed | archive> "
            + "<packageName | packageZip>\n\t-install-package <packageName | packageZip | URL> [version]\n"
            + "\t-uninstall-package packageName\n" + "\t-refresh-cache");
  }

  public static void main(String[] args) {
    weka.core.logging.Logger.log(weka.core.logging.Logger.Level.INFO,
        "Logging started");
    try {

      // scan for -offline
      for (int i = 0; i < args.length; i++) {
        if (args[i].equals("-offline")) {
          m_offline = true;
          String[] temp = new String[args.length - 1];
          if (i > 0) {
            System.arraycopy(args, 0, temp, 0, i);
          }
          System.arraycopy(args, i + 1, temp, i, args.length - (i + 1));
          args = temp;
        }
      }

      establishCacheIfNeeded(System.out);
      checkForNewPackages(System.out);

      if (args.length == 0 || args[0].equalsIgnoreCase("-h")
          || args[0].equalsIgnoreCase("-help")) {
        printUsage();
        // System.exit(1);
        return;
      }

      if (args[0].equals("-package-info")) {
        if (args.length < 3) {
          printUsage();
          return;
          // System.exit(1);
        }
        if (args[1].equals("archive")) {
          printPackageArchiveInfo(args[2]);
        } else if (args[1].equals("installed")) {
          printInstalledPackageInfo(args[2]);
        } else if (args[1].equals("repository")) {
          String version = "Latest";
          if (args.length == 4) {
            version = args[3];
          }
          try {
            printRepositoryPackageInfo(args[2], version);
          } catch (Exception ex) {
            // problem with getting info on package from repository?
            // Must not be an "official" repository package
            System.out
                .println("[WekaPackageManager] Nothing known about package "
                    + args[2] + " at the repository!");
          }
        } else {
          System.err
              .println("[WekaPackageManager] Unknown argument " + args[2]);
          printUsage();
          return;
          // System.exit(1);
        }
      } else if (args[0].equals("-install-package")) {
        String targetLowerCase = args[1].toLowerCase();
        if (targetLowerCase.startsWith("http://")
            || targetLowerCase.startsWith("https://")) {
          URL packageURL = new URL(args[1]);
          installPackageFromURL(packageURL, System.out);
        } else if (targetLowerCase.endsWith(".zip")) {
          installPackageFromArchive(args[1], System.out);
        } else {
          // assume a named package at the central repository
          String version = "Latest";
          if (args.length == 3) {
            version = args[2];
          }
          installPackageFromRepository(args[1], version, false);
        }
      } else if (args[0].equals("-uninstall-package")) {
        if (args.length < 2) {
          printUsage();
          return;
          // System.exit(1);
        }
        boolean force = false;

        if (args.length == 3) {
          if (args[2].equals("-force")) {
            force = true;
          }
        }

        removeInstalledPackage(args[1], force, System.out);
        // System.exit(0);
        return;
      } else if (args[0].equals("-list-packages")) {
        if (args.length < 2) {
          printUsage();
          // System.exit(1);
          return;
        }
        listPackages(args[1]);

      } else if (args[0].equals("-refresh-cache")) {
        refreshCache(System.out);
      } else {
        System.err.println("Unknown option: " + args[0]);
        printUsage();
      }

      System.exit(0);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
