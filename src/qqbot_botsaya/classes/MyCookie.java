package qqbot_botsaya.classes;

import com.sun.istack.internal.NotNull;
import qqbot_botsaya.executors.AuxiliaryFunctions;
import qqbot_botsaya.constants.StringConstants;

import java.util.LinkedList;
import java.util.ListIterator;

public class MyCookie
{
    private LinkedList<StrNVPair> partedCookie;

    public MyCookie()
    {
        partedCookie = new LinkedList<>();
    }

    public MyCookie(@NotNull final String cookie)
    {
        this();
        addCookie(cookie);
    }

    public void addCookie(@NotNull final String cookie)
    {
        String[] rawPairs = cookie.split(";");
        for (final String rawPair : rawPairs)
        {
            String[] partedPair = rawPair.split("=");
            if (partedPair.length != 2) { continue; }
            addPair(partedPair[0], partedPair[1]);
        }
    }

    public void addPair(@NotNull final String name, @NotNull final String value)
    {
        partedCookie.addLast(new StrNVPair(name, value));
    }

    public String[] search(@NotNull final String name)
    {
        LinkedList<String> values = new LinkedList<>();
        ListIterator<StrNVPair> iter = partedCookie.listIterator(0);
        while (iter.hasNext())
        {
            StrNVPair pair = iter.next();
            if (pair.getName().compareTo(name) == 0)
                values.addLast(pair.getValue());
        }
        return (String[])values.toArray();
    }

    public void remove(@NotNull final String name)
    {
        ListIterator<StrNVPair> iter = partedCookie.listIterator(0);
        while (iter.hasNext())
        {
            if (iter.next().getName().compareTo(name) == 0)
                iter.remove();
        }
    }

    public String compileAll()
    {
        StringBuilder stringBuilder = new StringBuilder();
        ListIterator<StrNVPair> iter = partedCookie.listIterator(0);
        while (iter.hasNext())
        {
            StrNVPair pair = iter.next();
            stringBuilder.append(pair.getName() + '=' + pair.getValue() + ';');
        }
        return stringBuilder.toString();
    }

    public String compile(final String[] names)
    {
        StringBuilder stringBuilder = new StringBuilder();
        ListIterator<StrNVPair> iter = partedCookie.listIterator(0);
        while (iter.hasNext())
        {
            StrNVPair pair = iter.next();
            if (AuxiliaryFunctions.isStrOneOfStrs(pair.getName(), names))
                stringBuilder.append(pair.getName() + '=' + pair.getValue() + ';');
        }
        return stringBuilder.toString();
    }

    public void print()
    {
        ListIterator<StrNVPair> iter = partedCookie.listIterator(0);
        while (iter.hasNext())
        {
            StrNVPair pair = iter.next();
            System.out.println(pair.getName() + '=' + pair.getValue());
        }
    }
}

class StrNVPair // String Name-Value Pair
{
    private String name;
    private String value;

    public StrNVPair()
    {
        name = StringConstants.NULL;
        value = StringConstants.NULL;
    }

    public StrNVPair(final String p_name, final String p_value)
    {
        name = p_name;
        value = p_value;
    }

    public String getName() { return name; }
    public String getValue() { return value; }

    public void setName(@NotNull final String p_name) { name = p_name; }
    public void setValue(@NotNull final String p_value) { value = p_value; }
    public void setPair(@NotNull final String p_name, @NotNull final String p_value)
    {
        name = p_name;
        value = p_value;
    }
}