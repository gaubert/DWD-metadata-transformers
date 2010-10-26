package org.eumetsat.eoportal.dcpc.commons.conf;

import java.util.HashSet;

@SuppressWarnings("unchecked")
public class PropertySet extends HashSet
{
    /**
	 * 
	 */
    private static final long serialVersionUID = -2094375484156991229L;

    private final String      ms_ALL           = "all";

    public PropertySet()
    {
        super();
    }

    private String detectAll(String aProperty)
    {
        return aProperty.equalsIgnoreCase(ms_ALL) ? ms_ALL : aProperty;
    }

    /**
     * 
     * @param o
     * @return
     */
    public boolean add(Object o)
    {
        if (o == null)
            throw new IllegalArgumentException("null Argument not acceptable");

        if (o instanceof String)
        {
            return super.add(detectAll((String) o));
        }
        else
        {
            throw new IllegalArgumentException("Only accept String");
        }
    }

    public boolean contains(Object o)
    {
        return super.contains(ms_ALL) ? true : super.contains(o);
    }

}
