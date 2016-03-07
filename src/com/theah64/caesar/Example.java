package com.theah64.caesar;


import java.io.IOException;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;


/**
 * Created by shifar on 7/3/16.
 */
public class Example {

    public static class WotThink {
        private static ChatterBotSession bot2session;

        public static void init() throws Exception {
            ChatterBotFactory factory = new ChatterBotFactory();
            ChatterBot bot2 = factory.create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477");
            bot2session = bot2.createSession();

        }

        public static String think(final String msg) throws Exception {
            if (bot2session == null) {
                init();
            }
            return bot2session.think(msg);
        }
    }

    public static class MessageParrot implements MessageListener {


        private Message msg = new Message(null, Message.Type.chat);

        // gtalk seems to refuse non-chat messages
        // messages without bodies seem to be caused by things like typing
        public void processMessage(Chat chat, Message message) {
            if (message.getType().equals(Message.Type.chat) && message.getBody() != null) {
                System.out.println("Received: " + message.getBody());
                try {
                    msg.setBody(WotThink.think(message.getBody()));
                    chat.sendMessage(msg);
                } catch (XMPPException ex) {
                    //ex.printStackTrace();
                    System.out.println("Failed to send message");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("I got a message I didn''t understand");
            }
        }
    }


    public static void main(String[] args) {

        System.out.println("Starting IM client");

        // gtalk requires this or your messages bounce back as errors
        ConnectionConfiguration connConfig = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
        XMPPConnection connection = new XMPPConnection(connConfig);

        try {
            connection.connect();
            System.out.println("Connected to " + connection.getHost());
        } catch (XMPPException ex) {
            //ex.printStackTrace();
            System.out.println("Failed to connect to " + connection.getHost());
            System.exit(1);
        }
        try {
            connection.login("shifar.shifz@gmail.com", "PwD#5689847469");
            System.out.println("Logged in as " + connection.getUser());

            Presence presence = new Presence(Presence.Type.available);
            connection.sendPacket(presence);

        } catch (XMPPException ex) {
            //ex.printStackTrace();
            System.out.println("Failed to log in as " + connection.getUser());
            System.exit(1);
        }


        final ChatManager chatmanager = connection.getChatManager();

        PacketListener pl = new PacketListener() {
            @Override
            public void processPacket(Packet p) {

                System.out.println(p.getFrom() + " (p) : " + p.toString());
                if (p instanceof Message) {
                    Message msg = (Message) p;
                    System.out.println(msg.getFrom() + "(m): " + msg.getSubject());
                }

                System.out.println("Creating chat ....");
                chatmanager.getChatListeners();
                chatmanager.createChat(p.getFrom(), new MessageParrot());

                /*try {
                    // google bounces back the default message types, you must use chat
                    Message msg = new Message(p.getFrom(), Message.Type.chat);
                    msg.setBody("Hi");
                    chat.sendMessage(msg);
                } catch (XMPPException e) {
                    System.out.println("Failed to send message");
                    // handle this how?
                }*/

                System.out.println("--------------------------");
            }
        };
        connection.addPacketListener(pl, null);

        System.out.println("Press enter to disconnect");

        try {
            System.in.read();
        } catch (IOException ex) {
            //ex.printStackTrace();
        }

        connection.disconnect();
    }
}
