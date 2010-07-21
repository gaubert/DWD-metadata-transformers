package org.eumetsat.dcpc.commons.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class SimpleNamespaceContext implements NamespaceContext
{
    private HashMap<String, String> m_Namespaces = new HashMap<String, String>();

    public static List<Object> getKeysFromValue(Map<?, ?> hm, Object value)
    {
        List<Object> list = new ArrayList<Object>();
        for (Object o : hm.keySet())
        {
            if (hm.get(o).equals(value))
            {
                list.add(o);
            }
        }
        return list;
    }

    public void addNamespace(String aPrefix, String aNamespace)
    {
        m_Namespaces.put(aPrefix, aNamespace);
    }

    public void removeNamespace(String aPrefix)
    {
        m_Namespaces.remove(aPrefix);
    }

    public String getNamespaceURI(String prefix)
    {
        String namespace = m_Namespaces.get(prefix);

        return (namespace != null) ? namespace : XMLConstants.NULL_NS_URI;
    }

    public String getPrefix(String namespace)
    {
        List<Object> l = getKeysFromValue(this.m_Namespaces, namespace);

        // return null or the first found prefix
        return (l.size() == 0) ? null : (String) l.get(0);
    }

    public Iterator<Object> getPrefixes(String namespace)
    {
        List<Object> l = getKeysFromValue(this.m_Namespaces, namespace);

        return l.iterator();
    }
}