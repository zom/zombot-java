package com.theah64.caesar;

import com.theah64.caesar.bots.BasicBot;
import com.theah64.caesar.bots.CleverBot;
import com.theah64.caesar.bots.KalaBot;
import com.theah64.caesar.bots.PandoraBot;
import com.theah64.caesar.models.Buddy;
import com.theah64.caesar.utils.CommonUtils;
import net.java.otr4j.session.Session;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import java.io.Console;
import java.io.IOException;
import java.util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.List;



import net.java.otr4j.OtrEngineListener;
import net.java.otr4j.OtrException;
import net.java.otr4j.OtrKeyManagerImpl;
import net.java.otr4j.OtrKeyManagerListener;
import net.java.otr4j.OtrPolicy;
import net.java.otr4j.session.SessionID;
import net.java.otr4j.session.SessionStatus;
import net.java.otr4j.session.TLV;

/**
 * Created by shifar on 7/3/16.
 */
public class Main implements Runnable {

    private static Map<String, Buddy> buddyList;

	private OtrKeyManagerImpl otrKeyManager;
	private static OtrEngineHostImpl otrEngine;
    private XMPPConnection xmpp;

    private Map<String, Chat> chatList = new HashMap<>();

    private final static String DEFAULT_FIRST_MESSAGE = "Tashi Delek! བཀྲ་ཤིས་བདེ་ལེགས\n\nI am the first Zom chatbot. I can talk about most anything you like!";

    public static void main(String[] args) throws IOException {

        //Asking email
        String user = args[0];
        String host = args[1];
        String password = args[2];

		new Main(host,user,password);
	}
 
     public Main (String host, String user, String password) throws IOException
	{

      //  KalaBot.init();

        //Building XMPP Connection
        final ConnectionConfiguration config = new ConnectionConfiguration(host, 5222);
        xmpp = new XMPPConnection(config);

        try {
            xmpp.connect();
            System.out.println("Connected to " + xmpp.getHost());
        } catch (XMPPException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to XMPP Server : " + e.getMessage());
        }

        //Logging in
        try {
            xmpp.login(user, password);
            System.out.println("Logged in as " + xmpp.getUser());

            //Setting presence
            final Presence presence = new Presence(Presence.Type.available);
            xmpp.sendPacket(presence);

        } catch (XMPPException e) {
            e.printStackTrace();
            System.out.println("Failed to login: " + e.getMessage());
        }

        otrKeyManager = new OtrKeyManagerImpl("sample-keystore");
		otrKeyManager.addListener(new OtrKeyManagerListener() {
			public void verificationStatusChanged(SessionID session) {
			//	System.out.println(session + ": verification status=" + otrKeyManager.isVerified(session));
			}
		});
		
		otrEngine = new OtrEngineHostImpl(this);

        //list to store listening contacts
        buddyList = new HashMap<>();

        //Building chat part
        final ChatManager chatManager = xmpp.getChatManager();

        System.out.println("roster size: " + xmpp.getRoster().getEntries().size());
        for (RosterGroup group : xmpp.getRoster().getGroups())
        {
            System.out.println(group.getName() + ": roster size: " + group.getEntries().size());
        }

        //Adding a packet listener
        xmpp.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {

                final String source = packet.getFrom();
                if (!buddyList.containsKey(source)) {

                    if (packet.getFrom() != null) {
                        //Building a bot randomly
                        final Buddy newBuddy = new Buddy(source, new PandoraBot());

                        //Adding new buddy to the list
                        buddyList.put(source, newBuddy);

                        System.out.println("Listening to " + packet.getFrom());
                        System.out.println("Buddylist size: " + buddyList.size());

                        //Setting listeners to the source
                        Chat chat = chatManager.createChat(packet.getFrom(), new MessageWatcher(source));
                        chatList.put(packet.getFrom(), chat);

                    }
                }

            }
        }, null);

        new Thread(this).start();

    }

    public void run ()
    {
        while (true)
        {
            try { Thread.sleep(3000); } catch (Exception e){}
        }
    }

    public void sendMessage (String to, String message) throws XMPPException
    {
        Chat chat = chatList.get(to);
        if (chat != null)
            chat.sendMessage(message);
    }



    private static class MessageWatcher implements MessageListener {

	    SessionID sessionID = null;
        Session session = null;
        boolean isFirstMessage = true;

        public MessageWatcher (String from)
        {
                sessionID = new SessionID("default", from, "xmpp");
                session = new Session(sessionID, otrEngine);
        }

        private String decrypt (Session session, String message) throws OtrException
        {
            return session.transformReceiving(message);
        }

        private String[] encrypt (Session session, String message) throws OtrException
        {
            return session.transformSending(message);
        }

        @Override
        public synchronized void processMessage(Chat chat, Message message) {


            final boolean isValidMessage = message.getType().equals(Message.Type.chat) && message.getBody() != null && message.getBody().length() > 0;

            if (isValidMessage) {

                final Buddy sourceBuddy = buddyList.get(message.getFrom());

                if (sourceBuddy != null) {
                    try {

                        final String sourceBuddyMessage = decrypt(session, message.getBody());

                        if (sourceBuddyMessage != null && sourceBuddyMessage.length() > 0) {

                            if (isFirstMessage)
                            {
                                String[] outMessages = encrypt(session, DEFAULT_FIRST_MESSAGE);

                                for (String outMessage : outMessages)
                                    chat.sendMessage(outMessage);

                                isFirstMessage = false;
                            }

                            System.out.println(String.format("ZomUser: %s", sourceBuddyMessage));
                            final ArrayList<String> wotBotThinks = sourceBuddy.getBot().getWhatBotThinks(sourceBuddyMessage);

                            for (String response : wotBotThinks) {
                                String[] outMessages = encrypt(session, response);

                                for (String outMessage : outMessages)
                                    chat.sendMessage(outMessage);

                                System.out.println("ZomBot: " + response);
                            }

                        }
                    }
                    catch (Exception e)
                    {
                       //System.out.println("Failed to decrypt or reply to message: " + sourceBuddy.getEmail() + "=" + e.getMessage());
                    }
                }
            }
        }
    }

    public void refreshSession (SessionID sid)
    {

    }

}
