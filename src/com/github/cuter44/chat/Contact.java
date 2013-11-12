package com.github.cuter44.chat;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.net.UnknownHostException;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.StringTokenizer;

public class Contact
{
    private static Logger logger = Logger.getLogger("com.github.cuter44.chat.Contact");

    public String name;
    public InetSocketAddress sock;

  // GETTER/SETTER
    public String getName()
    {
        return(this.name);
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public InetSocketAddress getSock()
    {
        return(this.sock);
    }

    public void setSock(InetSocketAddress sock)
    {
        this.sock = sock;
    }

    public String getIpString()
    {
        Pattern p = Pattern.compile(".*/([^/]*)$");
        Matcher m = p.matcher(this.sock.getAddress().toString());
        m.matches();
        String ipPart = m.group(m.groupCount());

        return(ipPart);
    }

    public byte[] getIpBytes()
    {
        return(this.sock.getAddress().getAddress());
    }

    public int getPort()
    {
        return(this.sock.getPort());
    }

  // HASH
    @Override
    public boolean equals(Object obj)
    {
        try
        {
            Contact c = (Contact)obj;

            return(this.sock.equals(c.sock));
        }
        catch (Exception ex)
        {
            return(false);
        }
    }

    @Override
    public int hashCode()
    {
        if (this.sock == null)
            return(0);

        return(this.sock.hashCode());
    }

    @Override
    public String toString()
    {
        return(this.name + "/" + this.sock.toString());
    }

  // CONSTRUCT
    public Contact()
    {
        return;
    }

    public Contact(String name, InetSocketAddress sock)
    {
        this();

        this.name = name;
        this.sock = sock;
    }

    public Contact(String name, InetAddress ip, int port)
    {
        this();

        this.name = name;
        this.sock = new InetSocketAddress(ip, port);
    }

    public Contact(String name, String ip, int port)
        throws IllegalArgumentException
    {
        this();

        try
        {
            this.name = name;

            InetAddress addr = InetAddress.getByName(ip);
            this.sock = new InetSocketAddress(addr, port);
        }
        catch (UnknownHostException ex)
        {
            logger.log(Level.WARNING, "Fail to parse address:"+ip, ex);
            throw(new IllegalArgumentException("Unrecognized IP", ex));
        }
    }

    public Contact(String name, byte[] ip, int port)
        throws IllegalArgumentException
    {
        this();

        try
        {

            this.name = name;

            InetAddress addr = InetAddress.getByAddress(ip);
            this.sock = new InetSocketAddress(addr, port);
        }
        catch (UnknownHostException ex)
        {
            logger.log(Level.WARNING, "Fail to parse address:"+ip, ex);
            throw(new IllegalArgumentException("Unrecognized IP", ex));
        }
    }

  // CONVERT
    public static String getIpString(InetSocketAddress a)
    {
        return(
            getIpString(a.getAddress())
        );
    }

    public static String getIpString(InetAddress a)
    {
        Pattern p = Pattern.compile(".*/([^/]*)$");
        Matcher m = p.matcher(a.toString());
        m.matches();
        String ipPart = m.group(m.groupCount());

        return(ipPart);
    }

  // VALIDATE
    public static boolean isIPv4(String ip)
    {
        try
        {
            StringTokenizer st = new StringTokenizer(ip, "\\.");
            for (int i=0; i<4; i++)
            {
                int num = Integer.valueOf(st.nextToken());
                if (num<0 || num>255)
                    return(false);
            }
            return(true);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(false);
        }
    }
}
