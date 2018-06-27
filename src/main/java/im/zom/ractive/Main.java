package im.zom.ractive;

import im.zom.ractive.bots.PandoraBot;
import im.zom.ractive.models.Buddy;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
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
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shifar on 7/3/16.
 */
public class Main implements Runnable, IncomingChatMessageListener {

    private static Map<Jid, Buddy> buddyList;

    private XMPPTCPConnection mConnection;
    private ChatManager mChatManager;
    private OmemoManager mOmemoManager;

    private Map<EntityBareJid, Chat> chatList = new HashMap<>();

    private final static String DEFAULT_FIRST_MESSAGE = "Hello, world!";

    public static void main(String[] args) throws IOException, InterruptedException, XMPPException, SmackException, NoSuchPaddingException, CorruptedOmemoKeyException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException {

        //Asking email
        String user = args[0];
        String host = args[1];
        String password = args[2];

		new Main(host,user,password);
	}
 
     public Main (String host, String user, String password) throws IOException, InterruptedException, XMPPException, SmackException, NoSuchPaddingException, CorruptedOmemoKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException {

	    String resource = "bot";

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

        builder.setServiceName((DomainBareJid) jid);

        mConnection = new XMPPTCPConnection( builder.build());
        mConnection.connect();

        //Logging in
        try {
            mConnection.login(user, password, Resourcepart.from(resource));

            System.out.println("Logged in as " + user);

            mChatManager = ChatManager.getInstanceFor(mConnection);

            mChatManager.addIncomingListener(this);

            //Setting presence
            final Presence presence = new Presence(Presence.Type.available);
            mConnection.sendStanza(presence);

            //additions begin here
            SignalOmemoService.acknowledgeLicense();
            SignalOmemoService.setup();
            //path where keys get stored
            OmemoConfiguration.setFileBasedOmemoStoreDefaultPath(new File("/tmp/omemo"));
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
                    newIncomingMessage(from.asEntityBareJidIfPossible(),newMessage,chat);

                }

                @Override
                public void onOmemoKeyTransportReceived(CipherAndAuthTag cipherAndAuthTag, Message message,
                                                        Message wrappingMessage, OmemoMessageInformation omemoInformation) {
                    //Not needed
                }
            });


        } catch (XMPPException e) {
            e.printStackTrace();
            System.out.println("Failed to login: " + e.getMessage());
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //list to store listening contacts
        buddyList = new HashMap<>();

        new Thread(this).start();

    }

    public void run ()
    {
        while (true)
        {
            try { Thread.sleep(3000);

                mOmemoManager.initialize();
          //      mOmemoManager.regenerate();
          //      mOmemoManager.regenerate();

            } catch (Exception e){}
        }
    }

    public void buildSession (Message message)
    {
        final Jid source = message.getFrom();

        if (!buddyList.containsKey(source)) {

            if (message.getFrom() != null) {
                //Building a bot randomly
                final Buddy newBuddy = new Buddy(source.asBareJid(), new PandoraBot());

                //Adding new buddy to the list
                buddyList.put(source, newBuddy);

                System.out.println("Listening to " + message.getFrom());
                System.out.println("Buddylist size: " + buddyList.size());

                //Setting listeners to the source
                Chat chat = mChatManager.chatWith(message.getFrom().asEntityBareJidIfPossible());
                chatList.put(message.getFrom().asEntityBareJidIfPossible(), chat);

                mChatManager.addIncomingListener(Main.this);

                HashMap<OmemoDevice, OmemoFingerprint> fingerprints =
                        mOmemoManager.getActiveFingerprints(source.asBareJid());

                if (fingerprints.size() == 0)
                {
                    try {
                        mOmemoManager.buildSessionsWith(source.asBareJid());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (CannotEstablishOmemoSessionException e) {
                        e.printStackTrace();
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    } catch (SmackException.NoResponseException e) {
                        e.printStackTrace();
                    }
                }
                //Let user decide
                for (OmemoDevice d : fingerprints.keySet()) {
                    mOmemoManager.trustOmemoIdentity(d, fingerprints.get(d));
                }


            }
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
        }
        //In case we cannot establish session with some devices
        catch (CannotEstablishOmemoSessionException e) {
            encrypted = mOmemoManager.encryptForExistingSessions(e, message.toString());
        }

        //send
        if (encrypted != null) {
            ChatManager.getInstanceFor(mConnection).chatWith(jid.asEntityBareJidIfPossible()).send(encrypted);
        }

    }

    @Override
    public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {

        buildSession(message);

        if (message.getBody() != null) {

            final Buddy sourceBuddy = buddyList.get(message.getFrom());

            if (sourceBuddy != null) {
                try {

                    final String sourceBuddyMessage = message.getBody();

                    if (sourceBuddyMessage != null && sourceBuddyMessage.length() > 0) {



                        System.out.println(String.format("ZomUser: %s", sourceBuddyMessage));
                        final ArrayList<String> wotBotThinks = sourceBuddy.getBot().getWhatBotThinks(sourceBuddyMessage);

                        for (String response : wotBotThinks) {

                            sendMessage(message.getFrom(),response);
                            System.out.println("ZomBot: " + response);
                        }

                    }
                } catch (Exception e) {
                    //System.out.println("Failed to decrypt or reply to message: " + sourceBuddy.getEmail() + "=" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

}