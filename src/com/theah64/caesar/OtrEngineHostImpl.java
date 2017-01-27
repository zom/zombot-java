package com.theah64.caesar;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.logging.Logger;

import net.java.otr4j.OtrEngineHost;
import net.java.otr4j.OtrException;
import net.java.otr4j.OtrPolicy;
import net.java.otr4j.crypto.OtrCryptoEngine;
import net.java.otr4j.crypto.OtrCryptoException;
import net.java.otr4j.session.InstanceTag;
import net.java.otr4j.session.SessionID;

public class OtrEngineHostImpl implements OtrEngineHost {

	    private HashMap<SessionID, KeyPair> keypairs = new HashMap<>();
		private Logger logger;
        private Main main;

		public OtrEngineHostImpl (Main main)
		{
			logger = Logger.getLogger("chatbot");
			this.main = main;
		}

		public void verify (SessionID sessionID, String address, boolean verified)
		{

		}

		public void injectMessage(SessionID sessionID, String msg) throws OtrException {

		    try {
                main.sendMessage(sessionID.getUserID(), msg);
                logger.finest("IM injects message: " + msg);
            }
            catch (Exception e)
            {
                logger.finest("IM sending error: " + e);
            }


		}

		public void smpError(SessionID sessionID, int tlvType, boolean cheated)
				throws OtrException {
			logger.severe("SM verification error with user: " + sessionID);
		}

		public void smpAborted(SessionID sessionID) throws OtrException {
			logger.severe("SM verification has been aborted by user: "
					+ sessionID);
		}

		public void finishedSessionMessage(SessionID sessionID, String msgText) throws OtrException {
			logger.severe("SM session was finished. You shouldn't send messages to: "
					+ sessionID);
		}

		public void requireEncryptedMessage(SessionID sessionID, String msgText)
				throws OtrException {
			logger.severe("Message can't be sent while encrypted session is not established: "
					+ sessionID);
		}

		public void unreadableMessageReceived(SessionID sessionID)
				throws OtrException {
			logger.warning("Unreadable message received from: " + sessionID);
		}

		public void unencryptedMessageReceived(SessionID sessionID, String msg)
				throws OtrException {
			logger.warning("Unencrypted message received: " + msg + " from "
					+ sessionID);
		}

		public void showError(SessionID sessionID, String error)
				throws OtrException {
			logger.severe("IM shows error to user: " + error);
		}

        public KeyPair getLocalKeyPair(SessionID paramSessionID) {
            KeyPair keypair = this.keypairs.get(paramSessionID);
            if (keypair == null) {
                try {
                    KeyPairGenerator kg = KeyPairGenerator.getInstance("DSA");
                    keypair = kg.genKeyPair();
                    this.keypairs.put(paramSessionID, keypair);
                } catch (NoSuchAlgorithmException e) {
                    logger.severe(e.getMessage());
                }
            }
            return keypair;
        }

		public OtrPolicy getSessionPolicy(SessionID ctx) {
			return new OtrPolicy(OtrPolicy.OTRL_POLICY_ALWAYS);
		}

		public byte[] getLocalFingerprintRaw(SessionID sessionID) {
			try {
				return OtrCryptoEngine.getFingerprintRaw(getLocalKeyPair(sessionID)
								.getPublic());
			} catch (OtrCryptoException e) {
				e.printStackTrace();
			}
			return null;
		}

		public void askForSecret(SessionID sessionID, InstanceTag receiverTag, String question) {
		}

		public void unverify(SessionID sessionID, String fingerprint) {
            logger.fine("Session was not verified: " + sessionID + "  fingerprint: " + fingerprint);
		}

		public String getReplyForUnreadableMessage(SessionID sessionID) {
            		return "You sent me an unreadable encrypted message.";
		}

		public String getFallbackMessage(SessionID sessionID) {
            		return "Off-the-Record private conversation has been requested. However, you do not have a plugin to support that.";
		}

		public void messageFromAnotherInstanceReceived(SessionID sessionID) {

		}

		public void multipleInstancesDetected(SessionID sessionID) {

		}

		public int getMaxFragmentSize(SessionID sessionID) {
			return Integer.MAX_VALUE;
		}
	}
