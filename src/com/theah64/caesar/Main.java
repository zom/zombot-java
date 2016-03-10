package com.theah64.caesar;

import com.theah64.caesar.bots.BasicBot;
import com.theah64.caesar.bots.CleverBot;
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

    private static final Random random = new Random();

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
                final String source = packet.getFrom();
                if (!buddyList.containsKey(source)) {

                    //Building a bot randomly
                    final BasicBot buddyBot = random.nextInt(2) == 0 ? new PandoraBot() : new CleverBot();
                    final Buddy newBuddy = new Buddy(source, buddyBot);

                    if (buddyBot instanceof PandoraBot) {
                        System.out.println("BotP assigned for " + packet.getFrom());
                    } else {
                        System.out.println("BotC assigned for " + packet.getFrom());
                    }

                    //Adding new buddy to the list
                    buddyList.put(source, newBuddy);

                    System.out.println("Listening to " + packet.getFrom());

                    //Setting listeners to the source
                    chatManager.createChat(packet.getFrom(), new MessageWatcher());
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
                    System.out.println("--------------------------");
                    final String sourceBuddyMessage = message.getBody();
                    System.out.println(String.format("%s: %s", sourceBuddy.getEmail(), sourceBuddyMessage));
                    final String wotBotThinks = sourceBuddy.getBot().getWhatBotThinks(sourceBuddyMessage);
                    try {
                        chat.sendMessage(wotBotThinks);
                        System.out.println("You: " + wotBotThinks);
                    } catch (XMPPException e) {
                        e.printStackTrace();
                        System.out.println("Failed to send reply");
                    }
                } else {
                    System.out.println("Buddy is null : " + message.getFrom());
                }
            } else {
                System.out.println(String.format("%s is typing...", message.getFrom()));
            }
        }
    }

}
