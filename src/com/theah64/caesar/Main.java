package com.theah64.caesar;

import com.theah64.caesar.bots.BasicBot;
import com.theah64.caesar.bots.PandoraBot;
import com.theah64.caesar.models.Buddy;
import com.theah64.caesar.utils.CommonUtils;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import java.io.Console;
import java.io.IOException;
import java.util.*;

/**
 * Created by shifar on 7/3/16.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    private static Map<String, Buddy> buddyList;

    public static void main(String[] args) throws IOException {

        //Asking email
        String email;
        do {
            System.out.print("Enter your google email: ");
            email = scanner.nextLine();
        } while (!CommonUtils.isValidEmail(email));


        //Asking password
        String password;
        final Console console = System.console();
        if (console == null) {
            System.out.println("Error: couldn't find console");
            throw new IOException("Failed to get console");
        } else {
            final char[] passArr = console.readPassword("Enter password: ");
            password = new String(passArr);
        }

        //Building XMPP Connection
        final ConnectionConfiguration config = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
        final XMPPConnection xmpp = new XMPPConnection(config);

        try {
            xmpp.connect();
            System.out.println("Connected to " + xmpp.getHost());
        } catch (XMPPException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to XMPP Server : " + e.getMessage());
        }

        //Logging in
        try {
            xmpp.login(email, password);
            System.out.println("Logged in as " + xmpp.getUser());

            //Setting presence
            final Presence presence = new Presence(Presence.Type.available);
            xmpp.sendPacket(presence);

        } catch (XMPPException e) {
            e.printStackTrace();
            System.out.println("Failed to login: " + e.getMessage());
        }

        //list to store listening contacts
        buddyList = new HashMap<>();

        //Building chat part
        final ChatManager chatManager = xmpp.getChatManager();

        //Adding a packet listener
        xmpp.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {

                System.out.println("------------------------------");
                System.out.println("Packet received");

                final String source = packet.getFrom();
                if (!buddyList.containsKey(source)) {

                    //Building new buddy details
                    final BasicBot buddyBot = new PandoraBot();
                    final Buddy newBuddy = new Buddy(source, buddyBot);

                    //Adding new buddy to the list
                    buddyList.put(source, newBuddy);

                    System.out.println("New source: " + source);

                    System.out.println("Building chat listener for " + source);

                    //Setting listeners to the source
                    chatManager.createChat(packet.getFrom(), new MessageWatcher());

                } else {
                    System.out.println("Old source : " + source);
                }

            }
        }, null);

        System.out.println("Press enter to stop programme.");
        System.in.read();

    }

    private static class MessageWatcher implements MessageListener {

        @Override
        public void processMessage(Chat chat, Message message) {

            final boolean isValidMessage = message.getType().equals(Message.Type.chat) && message.getBody() != null;

            if (isValidMessage) {

                final Buddy sourceBuddy = buddyList.get(message.getFrom());

                if (sourceBuddy != null) {
                    final String sourceBuddyMessage = message.getBody();
                    final String wotBotThinks = sourceBuddy.getBot().getWhatBotThinks(sourceBuddyMessage);
                    try {
                        chat.sendMessage(wotBotThinks);
                        System.out.println("Message sent: " + wotBotThinks);
                    } catch (XMPPException e) {
                        e.printStackTrace();
                        System.out.println("Failed to send reply");
                    }
                } else {
                    System.out.println("Buddy is null : " + message.getFrom());
                }
            }
        }
    }

}
