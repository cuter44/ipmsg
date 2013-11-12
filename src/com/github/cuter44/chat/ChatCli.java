package com.github.cuter44.chat;

import com.github.cuter44.chat.ChatCore.MessageListener;
import com.github.cuter44.chat.ChatCore.ContactListener;

import java.util.Scanner;
import java.util.Iterator;

import java.net.DatagramPacket;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;

public class ChatCli
    implements MessageListener, ContactListener
{
    private static Logger logger = Logger.getLogger("com.github.cuter44.chat.ChatCli");

    private Scanner inputScanner;
    public ChatCore core;

    private int timeout;

    public void send()
    {
        String receiver = this.inputScanner.next();
        String message = this.inputScanner.nextLine();

        boolean flag = Contact.isIPv4(receiver)?
            this.core.sendByIp(receiver, message):
            this.core.sendByName(receiver, message);

        if (flag)
            System.out.println("\n    SEND done.");

        return;
    }

    public void list()
    {
        Iterator<Contact> iter = this.core.getContacts().iterator();

        System.out.println("\n    LIST");
        while (iter.hasNext())
        {
            Contact c = iter.next();
            System.out.println(c.toString());
        }

        return;
    }

    public void scan()
    {
        try
        {
            System.out.println("\n    SCAN processing...");

            this.core.scan();

            Thread.sleep(timeout * 2);

            this.list();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void cmdLoop()
    {
        String command = "";

        while (!command.equalsIgnoreCase(Constant.CMD_EXIT))
        {
            System.out.print("\n   >");
            command = this.inputScanner.next();

            // send message
            if (command.equalsIgnoreCase(Constant.CMD_SEND) || command.equals(Constant.CMD_S))
            {
                this.send();
                continue;
            }

            // list contacts
            if (command.equalsIgnoreCase(Constant.CMD_LIST))
            {
                this.list();
                continue;
            }

            // discover peers
            if (command.equalsIgnoreCase(Constant.CMD_SCAN))
            {
                this.scan();
                continue;
            }

            // reply message
            //if (command.equals("reply") || command.equals("r"))
            //{
                //System.out.println("TODO");
            //}
        }

        this.core.exit();
    }

  // EVENT_LISTENER
    public void onMessage(String msg, Contact sender)
    {
        System.out.println("\n    RECEIVE ["+sender.toString()+"]");
        System.out.println(msg);

        return;
    }

    public void onContact(Contact contact, int type)
    {
        if (type == ContactListener.TYPE_ONLINE)
            System.out.print("\n    ONLINE [");
        if (type == ContactListener.TYPE_OFFLINE)
            System.out.print("\n    OFFLINE [");
        System.out.print(contact.toString());
        System.out.println("]");

        return;
    }

  // BOOTSTRAP
    public void start()
    {
        this.inputScanner = new Scanner(System.in);
        this.inputScanner.useDelimiter("\\s");

        this.cmdLoop();

        return;
    }

    public static void main(String[] args)
    {
        try
        {
            ChatCore core = new ChatCore();
            core.start();

            ChatCli cli = new ChatCli(core);
            cli.start();

            // ¿ªÈÕÖ¾
            Logger logger = Logger.getLogger("com.github.cuter44.chat");
            logger.setLevel(Level.ALL);

            logger = Logger.getLogger("");
            Handler h[] = logger.getHandlers();
            h[0].setLevel(Level.ALL);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return;
    }

  // CONSTRUCT
    public ChatCli(ChatCore core)
    {
        this.core = core;

        this.core.addMessageListener(this);
        this.core.addContactListener(this);

        this.timeout = Integer.valueOf(this.core.getConf().getProperty(Constant.CONF_TIMEOUT));
    }
}