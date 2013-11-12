package com.github.cuter44.chat;

class Constant
{
    // CONFIG_KEY
    public static final String CONF_MY_NAME = "chat.me.name";
    public static final String CONF_RECV_PORT = "chat.receiver.port";
    public static final String CONF_PACKET_SIZE = "chat.receiver.packetsize";
    public static final String CONF_TIMEOUT = "chat.network.timeout";

    // MSG_TYPE
    public static final String MSG_TYPE = "type";
    public static final String MTYPE_HELO = "helo";
    public static final String MTYPE_RELO = "relo";
    public static final String MTYPE_MSG = "msg";
    public static final String MTYPE_EXIT = "exit";

    // MSG_TYPE.HELO
    // MSG_TYPE.RELO
    public static final String M_NAME = "n";
    public static final String M_IP = "ip";
    public static final String M_PORT = "p";

    // MSG_TYPE.MSG
    public static final String M_CONT = "m";

    // CMD_MSG_TYPE
    public static final String CMD_S = "s";
    public static final String CMD_SEND = "send";
    public static final String CMD_LIST = "list";
    public static final String CMD_SCAN = "scan";
    public static final String CMD_EXIT = "exit";
}
