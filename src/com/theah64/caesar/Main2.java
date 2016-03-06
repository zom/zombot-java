package com.theah64.caesar;


import org.jivesoftware.smack.*;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import java.io.IOException;


/**
 * Created by theapache64 on 6/3/16.
 */
public class Main2 {
    public static void main(String[] args) throws XMPPException, IOException {
//ConnectionConfiguration connConfig = new ConnectionConfiguration("localhost", 5222);
        //connConfig.setSASLAuthenticationEnabled(false);
        //ConnectionConfiguration connConfig = new ConnectionConfiguration("localhost", 5222);
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
            connection.login("shifar.shifz@gmail.com", "MyPass");
            System.out.println("Logged in as " + connection.getUser());

            Presence presence = new Presence(Presence.Type.available);
            connection.sendPacket(presence);

        } catch (XMPPException ex) {
            ex.printStackTrace();
            System.out.println("Failed to log in as " + connection.getUser());
            System.exit(1);
        }

        ChatManager chatmanager = connection.getChatManager();
        Chat newChat = chatmanager.createChat("theapache64@gmail.com", new MessageListener() {
            public void processMessage(Chat chat, Message message) {
                System.out.println("Received message: " + message);
            }
        });

        try {
            newChat.sendMessage("Howdy!");
            System.out.println("Message Sent...");
        } catch (XMPPException e) {
            System.out.println("Error Delivering block");
        }
    }
}
