package im.zom.ractive;

import im.zom.ractive.bots.BasicBot;
import im.zom.ractive.bots.RiveBot;
import im.zom.ractive.bots.SearchBot;
import im.zom.ractive.models.Buddy;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.PresenceEventListener;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.ChatStateManager;
import org.jivesoftware.smackx.omemo.OmemoConfiguration;
import org.jivesoftware.smackx.omemo.OmemoFingerprint;
import org.jivesoftware.smackx.omemo.OmemoManager;
import org.jivesoftware.smackx.omemo.OmemoService;
import org.jivesoftware.smackx.omemo.exceptions.CannotEstablishOmemoSessionException;
import org.jivesoftware.smackx.omemo.exceptions.CorruptedOmemoKeyException;
import org.jivesoftware.smackx.omemo.exceptions.CryptoFailedException;
import org.jivesoftware.smackx.omemo.exceptions.UndecidedOmemoIdentityException;
import org.jivesoftware.smackx.omemo.internal.CipherAndAuthTag;
import org.jivesoftware.smackx.omemo.internal.OmemoDevice;
import org.jivesoftware.smackx.omemo.internal.OmemoMessageInformation;
import org.jivesoftware.smackx.omemo.listener.OmemoMessageListener;
import org.jivesoftware.smackx.omemo.signal.SignalOmemoService;
import org.jivesoftware.smackx.pubsub.PubSubException;
import org.jxmpp.jid.*;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.whispersystems.libsignal.IdentityKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shifar on 7/3/16.
 */
public class Main implements Runnable {

    private static Map<BareJid, Buddy> buddyList;

    private XMPPTCPConnection mConnection;
    private Roster mRoster;

    private ChatManager mChatManager;
    private OmemoManager mOmemoManager;

    private boolean mFirstTime = false;

    private String mBotType = null;
    private String mBotParam = null;

    private Map<EntityBareJid, Chat> chatList = new HashMap<>();

    private final static String DEFAULT_FIRST_MESSAGE = "Hello, world!";

    public static void main(String[] args) throws IOException, InterruptedException, XMPPException, SmackException, NoSuchPaddingException, CorruptedOmemoKeyException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException {

        //Asking email
        String user = args[0];
        String host = args[1];
        String password = args[2];
        String botType = args[3];
        String botParam = args[4];

		new Main(host,user,password,botType, botParam);
	}
 
     public Main (String host, String user, String password, String botType, String botParam) throws IOException, InterruptedException, XMPPException, SmackException, NoSuchPaddingException, CorruptedOmemoKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException {

	    String resource = "bot";
	    mBotType = botType;
	    mBotParam = botParam;

        //Building XMPP Connection

        //This is for remove security for simple chat

        SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");

        Jid jid;
        jid= JidCreate.from(host);

        XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder();

        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.required);

        builder.setHost(host);

        //Global.builder.setResource("Phone");
        builder.setPort(5222);

        builder.setDebuggerEnabled(true);

        builder.setConnectTimeout(60000);

        builder.setServiceName((DomainBareJid) jid);


        mConnection = new XMPPTCPConnection( builder.build());
        mConnection.connect();

        //Logging in
        try {
            mConnection.login(user, password, Resourcepart.from(resource));

            Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);
            mRoster = Roster.getInstanceFor(mConnection);
            mRoster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

            mRoster.addRosterListener(new RosterListener() {
                @Override
                public void entriesAdded(Collection<Jid> collection) {

                }

                @Override
                public void entriesUpdated(Collection<Jid> collection) {

                }

                @Override
                public void entriesDeleted(Collection<Jid> collection) {

                }

                @Override
                public void presenceChanged(Presence presence) {

                }
            });

            mRoster.addPresenceEventListener(new PresenceEventListener() {
                @Override
                public void presenceAvailable(FullJid fullJid, Presence presence) {
                    buildSession(fullJid.asEntityBareJidIfPossible());
                }

                @Override
                public void presenceUnavailable(FullJid fullJid, Presence presence) {

                }

                @Override
                public void presenceError(Jid jid, Presence presence) {

                }

                @Override
                public void presenceSubscribed(BareJid bareJid, Presence presence) {
                    buildSession(bareJid.asEntityBareJidIfPossible());
                    try {
                        sendMessage(bareJid, ":)");
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (UndecidedOmemoIdentityException e) {
                        e.printStackTrace();
                    } catch (CryptoFailedException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (SmackException.NoResponseException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void presenceUnsubscribed(BareJid bareJid, Presence presence) {

                }
            });

            ReconnectionManager rMan = ReconnectionManager.getInstanceFor(mConnection);
            rMan.enableAutomaticReconnection();

            System.out.println("Logged in as " + user);

            mChatManager = ChatManager.getInstanceFor(mConnection);

            //Setting presence
            final Presence presence = new Presence(Presence.Type.available);
            mConnection.sendStanza(presence);

            //additions begin here
            SignalOmemoService.acknowledgeLicense();
            SignalOmemoService.setup();
            //path where keys get stored
            File fileOmemo = new File("omemo");

            mFirstTime = !fileOmemo.exists();
            OmemoConfiguration.setFileBasedOmemoStoreDefaultPath(fileOmemo);
                mOmemoManager = OmemoManager.getInstanceFor(mConnection);

            //Listener for incoming OMEMO messages
            mOmemoManager.addOmemoMessageListener(new OmemoMessageListener() {
                @Override
                public void onOmemoMessageReceived(String decryptedBody, Message encryptedMessage,
                                                   Message wrappingMessage, OmemoMessageInformation omemoInformation) {
                    //Get identityKey of sender
                    IdentityKey senderKey = (IdentityKey) omemoInformation.getSenderIdentityKey().getIdentityKey();
                    OmemoService<?,IdentityKey,?,?,?,?,?,?,?> service = (OmemoService<?,IdentityKey,?,?,?,?,?,?,?>) OmemoService.getInstance();

                    //get the fingerprint of the key
                    OmemoFingerprint fingerprint = service.getOmemoStoreBackend().keyUtil().getFingerprint(senderKey);
                    //Lookup trust status
                    boolean trusted = mOmemoManager.isTrustedOmemoIdentity(omemoInformation.getSenderDevice(), fingerprint);

                    System.out.println("(O) " + (trusted ? "T" : "D") + " " + encryptedMessage.getFrom() + ": " + decryptedBody);

                    System.out.println("(O) " + encryptedMessage.getFrom() + ": " + decryptedBody);

                    Jid from = encryptedMessage.getFrom();
                    Chat chat = chatList.get(from.asEntityBareJidIfPossible());
                    Message newMessage = encryptedMessage.clone();
                    newMessage.setBody(decryptedBody);
                    handleMessage(from.asEntityBareJidIfPossible(),newMessage,chat);

                }

                @Override
                public void onOmemoKeyTransportReceived(CipherAndAuthTag cipherAndAuthTag, Message message,
                                                        Message wrappingMessage, OmemoMessageInformation omemoInformation) {
                    //Not needed
                }
            });

            //list to store listening contacts
            buddyList = new HashMap<>();

            new Thread(this).start();

        } catch (XMPPException e) {
            e.printStackTrace();
            System.out.println("Failed to login: " + e.getMessage());
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public void run ()
    {
        try {
            mOmemoManager.initialize();
            if (mFirstTime) {
                mOmemoManager.regenerate();
                mOmemoManager.purgeDevices();
            }

            for (RosterEntry entry : mRoster.getEntries()) {
                buildSession(entry.getJid().asEntityBareJidIfPossible());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        while (true) {
            try {
                Thread.sleep(3000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void buildSession (EntityBareJid source)
    {
        if (!buddyList.containsKey(source)) {

            //Building a bot randomly
            BasicBot bot = null;
            if (mBotType.equalsIgnoreCase("rive")) {
                bot = new RiveBot(source.asBareJid().getLocalpartOrNull().toString(), mBotParam);
            } else if (mBotType.equalsIgnoreCase("search")) {
                String lang = "en";
                String[] loginParts = mBotParam.split(":");
                bot = new SearchBot(loginParts[0], loginParts[1], lang);
            }

            final Buddy newBuddy = new Buddy(source.asBareJid(), bot);
            //Adding new buddy to the list
            buddyList.put(source, newBuddy);

            //Setting listeners to the source
            Chat chat = mChatManager.createChat(source);
            chat.addMessageListener(new ChatMessageListener() {
                @Override
                public void processMessage(Chat chat, Message message) {
                    trustAllIdentities(message.getFrom().asBareJid());
                }
            });
            chatList.put(source, chat);

            try {

                Presence presence = new Presence(Presence.Type.subscribe);
                presence.setTo(source);
                mConnection.sendStanza(presence);

                Presence presenced = new Presence(Presence.Type.subscribed);
                presenced.setTo(source);
                mConnection.sendStanza(presenced);

                String welcome = bot.getWelcomeMessage();
                if (welcome != null)
                    sendMessage(source.asBareJid(),welcome);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void trustAllIdentities (BareJid jid)
    {
        HashMap<OmemoDevice, OmemoFingerprint> fingerprints =
                mOmemoManager.getActiveFingerprints(jid);

        //Let user decide
        for (OmemoDevice d : fingerprints.keySet()) {
            mOmemoManager.trustOmemoIdentity(d, fingerprints.get(d));
        }
    }

    public void sendMessage (Jid jid, String message) throws XMPPException, SmackException.NotConnectedException, InterruptedException, UndecidedOmemoIdentityException, CryptoFailedException, NoSuchAlgorithmException, SmackException.NoResponseException {

        //encrypt
        Message encrypted = null;
        try {
            encrypted = OmemoManager.getInstanceFor(mConnection).encrypt(jid.asEntityBareJidIfPossible(), message.toString());
        }
        // In case of undecided devices
        catch (UndecidedOmemoIdentityException e) {
            System.out.println("Undecided Identities: ");
            for (OmemoDevice device : e.getUntrustedDevices()) {
                System.out.println(device);
            }

            trustAllIdentities(jid.asBareJid());
            try {
                encrypted = OmemoManager.getInstanceFor(mConnection).encrypt(jid.asEntityBareJidIfPossible(), message.toString());
            } catch (CannotEstablishOmemoSessionException e1) {
                System.out.println("STILL Undecided Identities: ");
                for (OmemoDevice device : e.getUntrustedDevices()) {
                    System.out.println(device);
                }
            }

        }
        //In case we cannot establish session with some devices
        catch (CannotEstablishOmemoSessionException e) {
            encrypted = mOmemoManager.encryptForExistingSessions(e, message.toString());
        }

        //send
        if (encrypted != null) {
            //ChatManager.getInstanceFor(mConnection).chatWith(jid.asEntityBareJidIfPossible()).send(encrypted);
            chatList.get(jid.asEntityBareJidIfPossible()).sendMessage(encrypted);
        }

    }

    public void handleMessage(EntityBareJid entityBareJid, Message message, Chat chat) {

        buildSession(message.getFrom().asEntityBareJidIfPossible());

        if (message.getBody() != null) {

            final Buddy sourceBuddy = buddyList.get(message.getFrom().asEntityBareJidIfPossible());

            if (sourceBuddy != null) {
                try {

                    final String sourceBuddyMessage = message.getBody();

                    if (sourceBuddyMessage != null && sourceBuddyMessage.length() > 0) {

                        ChatStateManager.getInstance(mConnection).setCurrentState(ChatState.composing,chatList.get(entityBareJid));
                        handleBot (message.getFrom(), sourceBuddy.getBot(),sourceBuddyMessage);

                    }
                } catch (Exception e) {
                    //System.out.println("Failed to decrypt or reply to message: " + sourceBuddy.getEmail() + "=" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleBot (Jid jid, BasicBot bot, String request)
    {
        final ArrayList<String> wotBotThinks = bot.getWhatBotThinks(request);

        for (String response : wotBotThinks) {

            try {
                sendMessage(jid,response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
