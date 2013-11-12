package com.github.cuter44.chat;

import java.net.DatagramSocket;
import java.net.DatagramPacket;

import java.net.SocketException;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.lang.ref.WeakReference;

import java.util.Properties;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.alibaba.fastjson.*;

public class PacketReceiver
    extends Thread
{
    private static Logger logger = Logger.getLogger("com.github.cuter44.chat.PacketReceiver");

    public int PACKET_SIZE;
    public int port;

    public DatagramSocket host = null;

  // HOST
    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                byte[] buffer = new byte[this.PACKET_SIZE];
                DatagramPacket p = new DatagramPacket(buffer, buffer.length);

                this.host.receive(p);
                String content = new String(p.getData(), "utf-8");

                new PacketEventFirer(p).start();
            }
            catch (UnsupportedEncodingException ex)
            {
                ex.printStackTrace();
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "", ex);
            }
        }
    }

  // MSG_RECEIVED_TRIGGER
    public interface PacketListener
    {
        public abstract void onPacketReceived(DatagramPacket p);
    }

    private Map<String, List<WeakReference<PacketListener>>> listeners;

    public void addPacketListener(String MSG_TYPE, PacketListener l)
    {
        if (MSG_TYPE == null)
            throw(new IllegalArgumentException("MSG_TYPE must not be null."));

        List<WeakReference<PacketListener>> lisList = this.listeners.get(MSG_TYPE);
        if (lisList == null)
        {
            lisList = new ArrayList<WeakReference<PacketListener>>();
            this.listeners.put(MSG_TYPE, lisList);
        }

        lisList.add(
            new WeakReference<PacketListener>(l)
        );
    }

    public void removePacketListener(String MSG_TYPE, PacketListener l)
    {
        // NOT IMPLEMENTED
    }

    private class PacketEventFirer
        extends Thread
    {
        private DatagramPacket p;

        public PacketEventFirer(DatagramPacket p)
        {
            this.p = p;
        }

        @Override
        public void run()
        {
            try
            {
                String s = new String(p.getData(), 0, p.getLength(), "utf-8");
                JSONObject json = JSON.parseObject(s);

                List<WeakReference<PacketListener>> listenerList =
                    PacketReceiver.this.listeners.get(
                        json.getString(Constant.MSG_TYPE)
                    );

                if (listenerList == null)
                    return;

                Object[] listenerArray = listenerList.toArray();

                // ´®ÐÐÏûÏ¢²¥ËÍ
                for (int i=0; i<listenerArray.length; i++)
                {
                    try
                    {
                        // ·ÏÆúÕìÌýÆ÷½â¹Ò
                        WeakReference<PacketListener> r = (WeakReference<PacketListener>)listenerArray[i];
                        PacketListener l = r.get();
                        if (l == null)
                            listenerList.remove(r);
                        else
                            l.onPacketReceived(p);
                    }
                    catch (Exception ex)
                    {
                        logger.log(Level.WARNING, "", ex);
                    }
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                logger.log(Level.WARNING, "", ex);
            }
        }
    }

  // CONSTRUCT
    public PacketReceiver(Contact me, Properties conf)
        throws SocketException
    {
        try
        {
            this.setDaemon(true);

            this.PACKET_SIZE = Integer.valueOf(conf.getProperty(Constant.CONF_PACKET_SIZE));

            this.listeners = new HashMap<String, List<WeakReference<PacketListener>>>();

            this.host = new DatagramSocket(me.getSock());

            return;
        }
        catch (SocketException ex)
        {
            logger.log(Level.SEVERE, "Bind receiver socket failed", ex);
            throw(ex);
        }
    }

}
