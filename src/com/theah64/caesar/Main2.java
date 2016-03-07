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
// connect to gtalk server
        ConnectionConfiguration connConfig = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
        final XMPPConnection connection = new XMPPConnection(connConfig);
        connection.connect();

// login with username and password
        connection.login("shifar.shifz@gmail.com", "PwD#5689847469");

// set presence status info
        Presence presence = new Presence(Presence.Type.available);
        connection.sendPacket(presence);

// send a message to somebody
        final Message msg = new Message("2ze4nkikmvldi3usbkptui6w0f@public.talk.google.com", Message.Type.chat);
        msg.setBody("Evdeee maaaan ??");
        connection.sendPacket(msg);


        // receive msg
        PacketListener pl = new PacketListener() {
            @Override
            public void processPacket(Packet p) {
                System.out.println(p.getFrom() + " (p) : " + p.toString());
                if (p instanceof Message) {
                    Message msg = (Message) p;
                    System.out.println(msg.getFrom() + "(m): " + msg.getSubject());
                }
            }
        };
        connection.addPacketListener(pl, null);

// wait for user to end program
        System.in.read();

// set presence status to unavailable
        presence = new Presence(Presence.Type.unavailable);
        connection.sendPacket(presence);
    }
}
