package com.github.cuter44.chat;

import com.github.cuter44.chat.PacketReceiver.PacketListener;

//import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.lang.ref.WeakReference;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.alibaba.fastjson.*;

import java.io.UnsupportedEncodingException;

public class ChatCore
    implements PacketListener
{
    public static Properties conf;

    private static int timeout;

    public ContactList contacts;
    public PacketReceiver receiver;
    public Contact me;

    private static Logger logger = Logger.getLogger("com.github.cuter44.chat.ChatCore");

  // BASE
    public boolean send(Contact to, String content)
    {
        try
        {
            byte data[] = content.getBytes("utf-8");
            DatagramPacket p = new DatagramPacket(data, data.length, to.getSock());
            DatagramSocket ds = new DatagramSocket();
            ds.send(p);
            ds.close();

            logger.log(Level.FINE, "SEND completed:"+content+">>"+to.toString());

            return(true);
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Send failed.", ex);
            return(false);
        }
    }

  // DISCOVER
    public boolean hello(String ip, int port)
    {
        Contact c = new Contact("", ip, port);

        JSONObject json = new JSONObject();
        json.put(Constant.MSG_TYPE, Constant.MTYPE_HELO);
        json.put(Constant.M_NAME, this.me.getName());
        json.put(Constant.M_PORT, this.me.getPort());

        return(
            this.send(c, json.toString())
        );
    }

    private boolean rehello(Contact c)
    {
        JSONObject json = new JSONObject();
        json.put(Constant.MSG_TYPE, Constant.MTYPE_RELO);
        json.put(Constant.M_NAME, this.me.getName());
        json.put(Constant.M_PORT, this.me.getPort());

        return(
            this.send(c, json.toString())
        );
    }

    private boolean rehello(String ip, int port)
    {
        Contact c = new Contact("", ip, port);

        return(
            rehello(c)
        );
    }

  // SEND
    public boolean sendByName(String name, String msg)
    {
        Contact c = this.contacts.getByName(name);

        if (name == null)
        {
            logger.log(Level.WARNING, "Target name not in contact, SEND aborted:" + name);
            return(false);
        }

        return(this.send(c, msg));
    }

    public boolean sendByIp(String ip, String msg)
    {
        try
        {
            Contact to = this.contacts.getByIp(ip);

            if (to == null)
            {
                logger.info(
                    "Target IP not in contact list, default port used, try connecting:" + ip + ":" + this.me.getPort()
                );
                hello(ip, this.me.getPort());

                Thread.sleep(timeout * 2);

                to = this.contacts.getByIp(ip);
                if (to == null)
                {
                    logger.log(
                        Level.WARNING,
                        "Target IP not responsing, SEND failed:" + ip + ":" + this.me.getPort()
                    );
                    return(false);
                }
            }

            JSONObject json = new JSONObject();
            json.put(Constant.MSG_TYPE, Constant.MTYPE_MSG);
            json.put(Constant.M_CONT, msg);

            return(
                this.send(to, json.toJSONString())
            );
        }
        catch (InterruptedException ex)
        {
            logger.log(Level.WARNING, "Send process interrupted.", ex);
            return(false);
        }
    }

  // ENTER_&_EXIT
    public boolean scan(int port)
    {
        Contact c = new Contact("", "255.255.255.255", port);

        JSONObject json = new JSONObject();
        json.put(Constant.MSG_TYPE, Constant.MTYPE_HELO);
        json.put(Constant.M_NAME, this.me.getName());
        json.put(Constant.M_PORT, this.me.getPort());

        return(
            this.send(c, json.toString())
        );
    }

    public boolean scan()
    {
        return(
            this.scan(
                this.me.getPort()
            )
        );
    }

    public boolean exit(int port)
    {
        Contact c = new Contact("", "255.255.255.255", port);

        JSONObject json = new JSONObject();
        json.put(Constant.MSG_TYPE, Constant.MTYPE_EXIT);

        return(
            this.send(c, json.toJSONString())
        );
    }

    public boolean exit()
    {
        return(
            this.exit(
                this.me.getPort()
            )
        );
    }

  // EVENT
  //   MESSAGE_LISTENER
    public interface MessageListener
    {
        public abstract void onMessage(String msg, Contact sender);
    }
    private List<WeakReference<MessageListener>> messageListeners;

    public void addMessageListener(MessageListener l)
    {
        this.messageListeners.add(
            new WeakReference<MessageListener>(l)
        );
    }

    public void removeMessageListener(MessageListener l)
    {
        // NOT IMPLEMENTED
    }

    private void fireMessageEvent(String msg, Contact sender)
    {
        Object[] listenerArray = this.messageListeners.toArray();

        for (int i=0; i<listenerArray.length; i++)
        {
            try
            {
                // 废弃侦听器解挂
                WeakReference<MessageListener> r = (WeakReference<MessageListener>)listenerArray[i];
                MessageListener l = r.get();
                if (l == null)
                    messageListeners.remove(r);
                else
                    l.onMessage(msg, sender);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "", ex);
            }
        }
    }

  //   CONTACT_LISTENER
    public interface ContactListener
    {
        public abstract void onContact(Contact contact, int type);

        public static final int TYPE_ONLINE = 1;
        public static final int TYPE_OFFLINE = 2;
    }
    private List< WeakReference<ContactListener>> contactListeners;

    public void addContactListener(ContactListener l)
    {
        this.contactListeners.add(
            new WeakReference<ContactListener>(l)
        );
    }

    public void removeContactListener()
    {
        // NOT IMPLEMENTED
    }

    private void fireContactEvent(Contact contact, int type)
    {
        Object[] listenerArray = this.contactListeners.toArray();

        for (int i=0; i<listenerArray.length; i++)
        {
            try
            {
                // 废弃侦听器解挂
                WeakReference<ContactListener> r = (WeakReference<ContactListener>)listenerArray[i];
                ContactListener l = r.get();
                if (l == null)
                    this.contactListeners.remove(r);
                else
                    l.onContact(contact, type);
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, "", ex);
            }
        }
    }

  //   PACKET_LISTENER
    @Override
    public void onPacketReceived(DatagramPacket p)
    {
        try
        {
            String s = new String(p.getData(), 0, p.getLength(), "utf-8");
            JSONObject json = JSON.parseObject(s);

            String MSG_TYPE = json.getString(Constant.MSG_TYPE);

            Contact c = null;

            // 消息
            if (MSG_TYPE.equals(Constant.MTYPE_MSG))
            {
                InetAddress ip = p.getAddress();
                c = this.contacts.getByIp(
                    Contact.getIpString(ip)
                );
                if (c == null)
                    c = new Contact("(unknown)", ip, 0);

                this.fireMessageEvent(
                    json.getString(Constant.M_CONT),
                    c
                );
            }

            // 对等发现
            if (MSG_TYPE.equals(Constant.MTYPE_HELO) || MSG_TYPE.equals(Constant.MTYPE_RELO))
            {
                c = new Contact(
                    json.getString(Constant.M_NAME),
                    p.getAddress(),
                    json.getIntValue(Constant.M_PORT)
                );

                boolean flag = this.contacts.add(c);

                if (flag)
                {
                    logger.info("PEER goes in:" + c.toString());
                    this.fireContactEvent(c, ContactListener.TYPE_ONLINE);
                }

                // 对等发现_响应
                if (MSG_TYPE.equals(Constant.MTYPE_HELO))
                {
                    rehello(c);
                }
            }

            // 退出
            if (MSG_TYPE.equals(Constant.MTYPE_EXIT))
            {
                InetAddress addr = p.getAddress();
                c = this.contacts.getByIp(
                    Contact.getIpString(p.getAddress())
                );
                if (c != null)
                {
                    this.contacts.removeByIp(c.getIpString());
                    logger.info("PEER goes out:" + addr.toString());
                    this.fireContactEvent(c, ContactListener.TYPE_OFFLINE);
                }
            }

            logger.fine("RECEIVE completed:"+json.toString()+"<<"+c.toString());
        }
        catch (UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
        }
    }

  // GETTER
    public Properties getConf()
    {
        return(this.conf);
    }

    public PacketReceiver getReceiver()
    {
        return(this.receiver);
    }

    public ContactList getContacts()
    {
        return(this.contacts);
    }

    public Contact getMe()
    {
        return(this.me);
    }

  // CONSTRUCT
    public ChatCore()
        throws Exception
    {
        try
        {
            this.conf = new Properties();

            this.conf.load(
                new InputStreamReader(
                    ChatCore.class.getResourceAsStream("/chat.properties"),
                    "utf-8"
                )
            );

            timeout = Integer.valueOf(this.conf.getProperty(Constant.CONF_TIMEOUT));
            int port = Integer.valueOf(this.conf.getProperty(Constant.CONF_RECV_PORT));

            String name = this.conf.getProperty(Constant.CONF_MY_NAME);
            InetAddress local = InetAddress.getLocalHost();
            InetSocketAddress host = new InetSocketAddress(local, port);
            this.me = new Contact(name, host);

            this.contacts = new ContactList();
            this.contacts.add(me);

            this.receiver = new PacketReceiver(this.me, this.conf);

            this.messageListeners = new ArrayList< WeakReference<MessageListener> >();
            this.contactListeners = new ArrayList< WeakReference<ContactListener> >();
            //this.setDaemon(true);
        }
        catch (Exception ex)
        {
            Logger.getLogger("com.github.cuter44.chat.ChatCore");
            throw(ex);
        }
    }

    public void finalize()
    {
        this.exit(this.me.getPort());
    }

  // BOOTSTRAP
    // WARN: just for compatibility, not running asynchronous.
    public void start()
    {
        this.run();
    }

    //@Override
    public void run()
    {
        this.receiver.start();

        this.receiver.addPacketListener(Constant.MTYPE_MSG, this);
        this.receiver.addPacketListener(Constant.MTYPE_HELO, this);
        this.receiver.addPacketListener(Constant.MTYPE_RELO, this);
        this.receiver.addPacketListener(Constant.MTYPE_EXIT, this);

        this.scan(this.me.getPort());

        return;
    }
}
