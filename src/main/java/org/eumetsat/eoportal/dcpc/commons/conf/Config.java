/*
 * File: Config.java. Manage a Configuration a la ini file
 */

package org.eumetsat.eoportal.dcpc.commons.conf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * @author Guillaume Aubert (guillaume.aubert@eumetsat.int)
 * @version
 */
public class Config
{
    public static final long                                    _SLEEPTIME                     = 15000;

    // ~ Static fields/initializers
    // ---------------------------------------------

    private static Hashtable<String, Hashtable<Object, Object>> _groupTable                    = null;

    private static boolean                                      _exportAdded                   = false;

    /**
     * 
     */
    private static Vector<Object>                               _noReloading                   = null;

    /**
     * 
     */
    private static Vector<Object>                               _exports                       = null;

    /**
     * 
     */
    private static boolean                                      _exportSystem                  = false;

    
    private static Properties                                   _properties                    = null;

    
    private static ConfigurationMonitor                                   _survey                        = null;

    
    private static Hashtable<Object, Object>                    _defines                       = null;

    private static String                                       _filePath                      = null;

    
    private static String                                       _FQCN                          = Config.class
                                                                                                       .getName()
                                                                                                       + ".";

    private static String                                       _CONFIG_FILENAME_DEFAULT       = "config.properties";

    private static String                                       _CONFIG_RESOURCE_NAME_PROPERTY = "config.resource.name";

    private static String                                       _CONFIG_PATH_PROPERTY          = "config.path";

    // to load from a resource URL
    private static ResourceLoader                               m_ResourceLoader;

    private static InputStream getConfigStream()
    {
        InputStream stream = null;

        // check in system properties if there is a config path
        String filePath = System.getProperty(_CONFIG_PATH_PROPERTY, null);

        // Still not filePath so look under classpath
        if (filePath == null)
        {
            // check if there is a property config.resourcename
            String property = System.getProperty(
                    _CONFIG_RESOURCE_NAME_PROPERTY, _CONFIG_FILENAME_DEFAULT);

            // look in classpath
            ClassLoader loader = Thread.currentThread().getContextClassLoader();

            stream = loader.getResourceAsStream(property);

        }
        else
        {
            // create stream
            try
            {
                stream = new FileInputStream(filePath);
                _filePath = filePath;
            }
            catch (FileNotFoundException e)
            {
                ;
            }
        }

        return stream;
    }

    /**
     * check if the resource is an existing a File and if not look in the
     * ClassPath
     * 
     * @param aResource
     * @return
     */
    private static InputStream getResource(String aResource)
    {
        InputStream stream = null;

        File f = new File(aResource);

        if (f.exists())
        {
            try
            {
                stream = new FileInputStream(f);
            }
            catch (FileNotFoundException e)
            {
                ;
            }
        }
        else
        {
            // look in classpath
            ClassLoader loader = Thread.currentThread().getContextClassLoader();

            stream = loader.getResourceAsStream(aResource);

        }

        return stream;
    }

    static
    {
        new Config(getConfigStream());
    }

    static public void bootStrap()
    {
        // do nothing (create to forced the Conf object to load the Configuation
        // file)
        groups();
    }

    // ~ Constructors
    // -----------------------------------------------------------

    /**
     * Constructor
     * 
     * @param aStream the config stream
     *            
     */
    public Config(InputStream aStream)
    {
        // create resource Loader
        m_ResourceLoader = new DefaultResourceLoader();

        if (_groupTable == null)
        {
            _groupTable = new Hashtable<String, Hashtable<Object, Object>>();
        }

        if (_exports == null)
        {
            _exports = new Vector<Object>();
        }

        if (_noReloading == null)
        {
            _noReloading = new Vector<Object>();
        }

        if (_defines == null)
        {
            _defines = new Hashtable<Object, Object>();
        }

        if (_properties == null)
        {
            _properties = new Properties();
        }

        // no exit by default (do nothing)
        boolean exitOnInitFailed = System.getProperty(
                _FQCN + "exitOnInitFailed", "false").equals("true");

        try
        {
            _loadConfig(false, aStream);

            // activate survey only if we have a file (not a classpath resource)
            if ((_survey == null) && (_filePath != null))
            {
                _survey = new ConfigurationMonitor(_filePath);
                _survey.start();
            }

        }
        catch (Exception e)
        {
            if (exitOnInitFailed)
            {
                System.exit(0);
            }

        }

    }

    // ~ Methods
    // ----------------------------------------------------------------

    /**
     * return key from Group group as a long
     * 
     * @param group
     *            Group
     * @param key
     *            key
     * @param defaultValue
     *            default value returned if there is no key with such a name in
     *            the conf file
     * 
     * @return the value as a logn
     */
    public static long longAt(String group, String key, long defaultValue)
    {
        try
        {
            return Long.parseLong(_removeDoubleQuote(at(group, key)));
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * create a new group named group
     * 
     * @param group
     */
    public static void add(String group)
    {
        _groupTable.put(group, new Hashtable<Object, Object>());
    }

    /**
     * 
     * @param group
     * @param key
     * @param value
     */
    public static void add(String group, String key, String value)
    {
        _debug("add " + group + "[" + key + "]=" + value, false, null);

        Hashtable<Object, Object> temp;

        if ((temp = at(group)) == null)
        {
            add(group);
            add(group, key, value);

            return;
        }

        temp.put(key, value);
    }

    /**
     * 
     * @param group
     * @param table
     */
    public static void add(String group, Hashtable<Object, Object> table)
    {
        _groupTable.put(group, table);
    }

    public static InputStream resourcePathAsStream(String aResourceStr)
    {
        if (aResourceStr != null)
        {
            Resource r;

            if ((r = m_ResourceLoader.getResource(aResourceStr)) != null)
            {
                try
                {
                    return r.getInputStream();
                }
                catch (IOException e)
                {
                    ;
                }
            }
        }

        return null;
    }

    /**
     * 
     * @param group
     * @param key
     * @return
     */
    public static InputStream InputStreamAt(String group, String key)
    {
        String s = at(group, key);

        if (s != null)
        {
            Resource r;

            if ((r = m_ResourceLoader.getResource(s)) != null)
            {
                try
                {
                    return r.getInputStream();
                }
                catch (IOException e)
                {
                    ;
                }
            }
        }

        return null;

    }

    /**
     * Return the group named group
     * 
     * @param group
     *            to be returned
     * 
     * @return group as a Hashtable
     */
    public static Hashtable<Object, Object> at(String group)
    {
        return _groupTable.get(group);
    }

    /**
     * 
     * @param group
     * @return
     */
    public static String valuesOf(String group)
    {
        return valuesOf(at(group));
    }

    /**
     * 
     * @param values
     * @return
     */
    public static String valuesOf(Hashtable<Object, Object> values)
    {
        Vector<String> vector = new Vector<String>();

        if (values != null && !values.isEmpty())
        {
            Enumeration<Object> keys = values.keys();

            while (keys.hasMoreElements())
            {
                String key = (String) keys.nextElement();
                String value = (String) values.get(key);

                if (value.startsWith("\"") && value.endsWith("\"")
                        && (value.length() > 1))
                {
                    value = value.substring(1, value.length() - 1);
                }

                vector.add(key + "=\"" + value + "\"");
            }
        }

        Collections.sort(vector);
        String data = new String();

        for (int i = 0; i < vector.size(); i++)
        {
            data = data.concat(vector.elementAt(i) + "\n");
        }

        return data;
    }

    /**
     * 
     * @param group
     * @param defaultValue
     * @return
     */
    public static Hashtable<Object, Object> at(String group,
            Hashtable<Object, Object> defaultValue)
    {
        Hashtable<Object, Object> result;

        return (result = at(group)) == null ? defaultValue : result;
    }

    /**
     * 
     * @param group
     * @param key
     * @return
     */
    public static String at(String group, String key)
    {
        Hashtable<Object, Object> temp;

        if (((temp = at(group)) != null) && temp.containsKey(key))
        {
            return (String) temp.get(key);
        }

        return null;
    }

    /**
     * 
     * @param group
     * @param key
     * @param defaultValue
     * @return
     */
    public static String at(String group, String key, String defaultValue)
    {
        String value;

        if ((value = at(group, key)) == null)
        {
            return defaultValue;
        }

        return value;
    }

    /**
     * 
     * @return
     */
    public static Vector<Object> groups()
    {
        Vector<Object> temp = new Vector<Object>();
        Enumeration<String> e = _groupTable.keys();

        while (e.hasMoreElements())
            temp.addElement(e.nextElement());

        return temp;
    }

    /**
     * 
     * @param group
     * @return
     */
    public static Hashtable<Object, Object> valuesAt(String group)
    {
        Hashtable<Object, Object> response = new Hashtable<Object, Object>();
        Hashtable<Object, Object> temp;

        if ((temp = at(group)) != null)
        {
            Enumeration<Object> e = temp.keys();

            while (e.hasMoreElements())
            {
                String name = (String) e.nextElement();
                response.put(name, valuesAt(group, name));
            }
        }

        return response;
    }

    /**
     * 
     * @param group
     * @param key
     * @return
     */
    public static Vector<String> valuesAt(String group, String key)
    {
        Vector<String> params = null;
        String values;

        if ((values = at(group, key)) != null)
        {
            params = new Vector<String>();

            StringTokenizer tokens = new StringTokenizer(values, ",");

            while (tokens.hasMoreElements())
                params.addElement(tokens.nextToken());
        }

        return params;
    }

    /**
     * 
     * @param group
     * @param key
     * @return
     */
    public static String[] stringsAt(String group, String key)
    {
        Vector<String> vector = valuesAt(group, key);
        String[] result = null;

        if (vector != null)
        {
            result = new String[vector.size()];
            vector.copyInto(result);
        }

        return result;
    }

    /**
     * transform the comma separated params in a Set
     * 
     * @param group
     * @param key
     * @return The Set of Strings or null if empty
     */
    public static Set<String> stringsSetAt(String group, String key)
    {
        Vector<String> vector = valuesAt(group, key);
        Set<String> result = null;

        if (vector != null)
        {
            result = new HashSet<String>(vector);
        }

        return result;
    }

    /**
     * transform the comma separated params into a PropertySet If it contains
     * then all the contained(value) method will always return true
     * 
     * @param group
     * @param key
     * @return The Set of Strings or null if empty
     */
    @SuppressWarnings("unchecked")
    public static Set<String> propertySetAt(String group, String key)
    {
        Vector<String> vector = valuesAt(group, key);
        Set<String> result = null;

        if (vector != null)
        {
            result = new PropertySet();
            Iterator<String> iter = vector.iterator();
            while (iter.hasNext())
            {
                result.add(iter.next());
            }
        }

        return result;
    }

    /**
     * 
     * @param name
     * @param value
     */
    public static void addProperty(String name, String value)
    {
        _debug("add property " + name + "=" + value, false, null);
        System.setProperty(name, value);
        _properties.put(name, value);
    }

    /**
     * 
     * @param group
     * @param key
     * @param defaultValue
     * @return
     */
    public static boolean booleanAt(String group, String key,
            boolean defaultValue)
    {
        try
        {
            String bool = _removeDoubleQuote(at(group, key)).toLowerCase();
            return bool.equals("true") || bool.equals("yes");
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * 
     * @param group
     * @param key
     * @param parameters
     * @return
     */
    public static String[] getCommand(String group, String key,
            String[] parameters)
    {
        String command;

        if ((command = Config.at(group, key)) == null)
        {
            return null;
        }

        for (int i = 1; i <= parameters.length; i++)
        {
            String param = "$" + i;
            int ind;

            while ((ind = command.indexOf(param)) != -1)
                command = command.substring(0, ind) + parameters[i - 1]
                        + command.substring(ind + param.length());
        }

        StringTokenizer st = new StringTokenizer(command);
        Vector<String> vector = new Vector<String>();

        while (st.hasMoreElements())
        {
            vector.add((String) st.nextElement());
        }

        String[] string = new String[vector.size()];
        vector.copyInto(string);
        vector = null;

        return string;
    }

    /**
     * 
     * @return
     */
    public static Properties getAddedProperties()
    {
        return _exportAdded ? _properties : new Properties();
    }

    /**
     * 
     * @return
     */
    public static Hashtable<String, Hashtable<Object, Object>> getExport()
    {
        Hashtable<String, Hashtable<Object, Object>> exports = new Hashtable<String, Hashtable<Object, Object>>();

        for (int i = 0; i < _exports.size(); i++)
        {
            String group = (String) _exports.elementAt(i);
            exports.put(group, at(group));
        }

        return exports;
    }

    /**
     * 
     * @return
     */
    public static Properties getSystemProperties()
    {
        return _exportSystem ? System.getProperties() : new Properties();
    }

    /**
     * 
     * @param export
     */
    public static void importFrom(
            Hashtable<String, Hashtable<Object, Object>> export)
    {
        _groupTable = export;
    }

    /**
     * 
     * @param group
     * @param key
     * @param defaultValue
     * @return
     */
    public static int intAt(String group, String key, int defaultValue)
    {
        try
        {
            return Integer.parseInt(_removeDoubleQuote(at(group, key)));
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * 
     * @param value
     * @return
     */
    private static String _removeDoubleQuote(String value)
    {
        value = value.trim();

        if (value.startsWith("\"") && value.endsWith("\"")
                && (value.length() > 1))
        {
            value = value.substring(1, value.length() - 1);
        }

        return value;
    }

    /**
     * 
     * @param group
     * @param key
     * @param defaultValue
     * @return
     */
    public static short shortAt(String group, String key, short defaultValue)
    {
        try
        {
            return Short.parseShort(_removeDoubleQuote(at(group, key)));
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
    * 
    * @param reload
    * @param aConfStream
    * @throws Exception
    */
    private static void _loadConfig(boolean reload, InputStream aConfStream)
            throws Exception
    {
        _loadConfig(reload, aConfStream, null);
    }

    /**
     * 
     * @param reload
     * @param aConfStream
     * @param groups
     * @throws Exception
     */
    private static void _loadConfig(boolean reload, InputStream aConfStream,
            String groups) throws Exception
    {

        int row = -1;

        try
        {
            BufferedReader dis = new BufferedReader(new InputStreamReader(
                    aConfStream));
            String group = null;
            int index;
            String line;
            row = 1;

            try
            {
                while ((line = dis.readLine()) != null)
                {
                    if (line.startsWith("#include \"") && (line.length() > 11))
                    {
                        String resourceName = line.substring(10, line
                                .lastIndexOf("\""));
                        String includes = null;

                        if (((index = resourceName.indexOf("[")) > 0)
                                && resourceName.endsWith("]"))
                        {
                            includes = resourceName.substring(index + 1,
                                    resourceName.length() - 1);
                            resourceName = resourceName.substring(0, index);
                        }

                        _loadConfig(reload,
                                getResource(getValue(resourceName)), includes);
                    }
                    else if (!reload && line.startsWith("#property ")
                            && (line.length() > 10))
                    {
                        if ((index = line.indexOf('=')) != -1)
                        {
                            addProperty(line.substring(10, index),
                                    getValue(line.substring(index + 1, line
                                            .length())));
                        }
                    }
                    else if (!reload && line.startsWith("#no_reloading \"")
                            && (line.length() > 10))
                    {
                        _noReloading.add(line.substring(9, line
                                .lastIndexOf("\"")));
                    }
                    else if (!reload && line.startsWith("#export \"")
                            && (line.length() > 10))
                    {
                        _exports.add(line.substring(9, line.lastIndexOf("\"")));
                    }
                    else if (!reload
                            && line.equals("#export_system_properties"))
                    {
                        _exportSystem = true;
                    }
                    else if (!reload && line.equals("#export_added_properties"))
                    {
                        _exportAdded = true;
                    }
                    else if ((line.length() != 0) && !line.startsWith("#"))
                    {

                        if ((line.charAt(0) == '[')
                                && (line.charAt(line.length() - 1) == ']'))
                        {
                            String ext = (String) _defines.get("ext");

                            group = ((ext != null) ? (ext + "::") : "")
                                    + line.substring(1, line.length() - 1);

                            // reloading and no excluded from reloading
                            if (reload && !_noReloading.contains(group))
                            {
                                _debug("reloading group " + group, false, null);
                                _groupTable.remove(group);
                            }

                            if (!_groupTable.containsKey(group))
                            {
                                add(group);
                            }
                        }
                        else if ((group != null)
                                && ((index = line.indexOf('=')) != -1))
                        {
                            add(group, line.substring(0, index), getValue(line
                                    .substring(index + 1)));
                        }
                    }

                    row++;
                }
            }
            finally
            {
                aConfStream.close();
            }
        }
        catch (Exception e)
        {
            _debug("cannot find the property file (ex:vmc.properties)"
                    + ((row != -1) ? (" (error on line: " + row + ")") : ""),
                    true, null);

            throw e;
        }
    }

    /**
     * 
     * @param group
     * @param key
     * @return
     */
    public static String notEmptyStringAt(String group, String key)
    {
        String value;

        if (((value = at(group, key)) != null) && (value.length() > 0))
        {
            return value;
        }

        return null;
    }

    /**
     * 
     * @param group
     * @param key
     * @param defaultValue
     * @return
     */
    public static String notEmptyStringAt(String group, String key,
            String defaultValue)
    {
        String value;

        if (((value = at(group, key)) != null) && (value.length() > 0))
        {
            return value;
        }

        return defaultValue;
    }

    /**
     * 
     * @param message
     * @param force
     * @param e
     */
    private static void _debug(String message, boolean force, Throwable e)
    {
        if (force
                || System.getProperty(_FQCN + "debug", "false").equals("true"))
        {
            System.err.println("Conf: " + message);

            if (e != null)
            {
                e.printStackTrace(System.err);
            }

            System.err.flush();
        }
    }

    /**
     * 
     * @param value
     * @return
     */
    public static String getValue(String value)
    {
        if (value == null)
        {
            return null;
        }

        int start;
        int end;
        int index;

        if ((start = value.indexOf("${")) != -1)
        {
            String tag = value.substring(start + 1);

            if ((end = tag.indexOf("}")) != -1)
            {
                String toResolv = getValue(tag.substring(0, end));
                String at;

                return value.substring(0, start)
                        + ((((index = toResolv.indexOf("[")) == -1) || !toResolv
                                .endsWith("]")) ? (System.getProperties()
                                .containsKey(toResolv.substring(1)) ? System
                                .getProperty(toResolv.substring(1))
                                : ((toResolv.endsWith("()") && (toResolv
                                        .length() > 2)) ? callMethod(toResolv
                                        .substring(1, toResolv.length() - 2))
                                        : ("${" + toResolv.substring(1) + "}")))
                                : (((at = at(toResolv.substring(1, index),
                                        toResolv.substring(index + 1, toResolv
                                                .length() - 1))) != null) ? at
                                        : "${" + toResolv.substring(1) + "}"))
                        + getValue(tag.substring(end + 1));
            }
        }

        return value;
    }

    /**
     * 
     * @param name
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String callMethod(String name)
    {
        int index = name.lastIndexOf(".");

        try
        {
            Class theClass = Class.forName(name.substring(0, index));

            return (String) theClass.getMethod(name.substring(index + 1),
                    new Class[] {}).invoke(theClass.newInstance(),
                    new Object[] {});
        }
        catch (Exception e)
        {
            return name;
        }
    }

    // ~ Inner Classes
    // ----------------------------------------------------------
    /**
     * 
     */
    public class ConfigurationMonitor extends Thread
    {
        // ~ Instance fields
        // ----------------------------------------------------

        private boolean _run;

        private String  _name;

        // ~ Constructors
        // -------------------------------------------------------

        /**
         * Creates a new CnfSurvey object.
         * 
         * @param name
         *            DOCUMENT ME!
         */
        public ConfigurationMonitor(String name)
        {
            _name = name;
            _run = true;
        }

        // ~ Methods
        // ------------------------------------------------------------

        /**
         * 
         */
        public void destroy()
        {
            _run = false;
        }

        /**
         * 
         */
        public void run()
        {
            setPriority(Thread.MIN_PRIORITY);

            while (_run)
            {
                long lastModified = new File(_name).lastModified();

                try
                {
                    sleep(_SLEEPTIME);
                }
                catch (Exception ignored)
                {
                }

                if (_run && (new File(_name).lastModified() != lastModified))
                {
                    try
                    {
                        _loadConfig(true, new FileInputStream(_name));
                    }
                    catch (Exception ignored)
                    {
                        ;
                    }
                }
            }
        }
    }
}
