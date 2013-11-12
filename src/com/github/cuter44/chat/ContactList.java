package com.github.cuter44.chat;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import java.util.logging.Logger;

public class ContactList
{
    private static Logger logger = Logger.getLogger("com.github.cuter44.chat.ContactList");

    public Set<Contact> pool;

    public Map<String, Contact> nameMap;
    public Map<String, Contact> ipMap;

    public ContactList()
    {
        this.pool = new HashSet<Contact>();
        this.nameMap = new HashMap<String, Contact>();
        this.ipMap = new HashMap<String, Contact>();

        return;
    }

    public Iterator<Contact> iterator()
    {
        return(this.pool.iterator());
    }

    public Contact getByName(String name)
    {
        return(
            this.nameMap.get(name)
        );
    }

    public Contact getByIp(String ip)
    {
        return(
            this.ipMap.get(ip)
        );
    }

    public boolean add(Contact c)
    {
        boolean flag = this.pool.add(c);

        if (flag)
        {
            this.nameMap.put(c.getName(), c);
            this.ipMap.put(c.getIpString(), c);
        }

        return(flag);
    }

    public boolean remove(Contact c)
    {
        boolean flag = this.pool.remove(c);
        if (flag)
            return(false);

        this.nameMap.remove(c.getName());
        this.ipMap.remove(c.getIpString());

        return(true);
    }

    public boolean removeByName(String name)
    {
        Contact c = nameMap.get(name);
        if (c == null)
            return(false);

        String ip = c.getIpString();

        this.pool.remove(c);
        this.ipMap.remove(ip);
        this.nameMap.remove(name);

        return(true);
    }

    public boolean removeByIp(String ip)
    {
        Contact c = ipMap.get(ip);
        if (c == null)
            return(false);

        String name = c.getName();

        this.pool.remove(c);
        this.ipMap.remove(ip);
        this.nameMap.remove(name);

        return(true);
    }

    public void clear()
    {
        this.pool.clear();
        this.ipMap.clear();
        this.nameMap.clear();

        return;
    }
}
