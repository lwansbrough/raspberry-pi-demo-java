package com.lwansbrough.serverfarm.core.services;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.lwansbrough.serverfarm.core.models.generated.EncryptedMessageProto.EncryptedMessage;
import com.lwansbrough.serverfarm.core.services.message.MessageEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

public class MessageService<T extends Message> implements Runnable {
    private static final int GCM_TAG_LENGTH = 16;
    private static final int GCM_NONCE_LENGTH = 12;

    private final Parser<T> parser;
    private final int listenPort;
    private SecureRandom random;
    private final SecretKey privateKey;
    private DatagramSocket datagramSocket;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    private boolean running;
    private byte[] buffer = new byte[256];

    public MessageService(Class<T> cls, int listenPort) throws Exception {
        try {
            // We could pass the parser to the constructor, but that exposes the implementation which isn't very nice.
            parser = (Parser<T>) cls.getMethod("parser").invoke(null);
        } catch (Exception ex) {
            // If T extends Message, it is a protobuf message type and has a static method `.parser()`.
            // Lets throw a generic exception to let the implementer know.
            throw new Exception("T must be a Protobuf message type.");
        }
        this.listenPort = listenPort;

        try {
            random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
        }

        byte[] decodedKey = Base64.getDecoder().decode("4iTW8QgKzkPzUCXNg6TsKDlY34Sq7AI6P7l7dbOBiAo=");
        privateKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        try {
            datagramSocket = new DatagramSocket(listenPort);
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        running = true;

        while (running) {
            this.processMessage();
        }
    }

    public void sendMessage(InetAddress address, T message) {
        EncryptionResult encrypted = encryptBytes(message.toByteArray());

        EncryptedMessage encryptedMessage = EncryptedMessage.newBuilder()
                .setNonce(ByteString.copyFrom(encrypted.getNonce()))
                .setPayload(ByteString.copyFrom(encrypted.getPayload())).build();

        byte[] encryptedMessageBytes = encryptedMessage.toByteArray();

        ByteBuffer messageBuffer = ByteBuffer.allocate(4 + encryptedMessageBytes.length);
        messageBuffer.putInt(encryptedMessageBytes.length);
        messageBuffer.put(encryptedMessageBytes);

        DatagramPacket packet = new DatagramPacket(
            messageBuffer.array(),
            messageBuffer.limit(),
            address,
            listenPort
        );

        try {
            datagramSocket.send(packet);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void processMessage() {
         DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            datagramSocket.receive(packet);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        InetAddress sender = packet.getAddress();
        int port = packet.getPort();
        packet = new DatagramPacket(buffer, buffer.length, sender, port);
        byte[] packetData = packet.getData();
        ByteBuffer wrappedPacketData = ByteBuffer.wrap(packetData);
        int messageLength = wrappedPacketData.getInt();
        byte[] messageData = new byte[messageLength];
        wrappedPacketData.get(messageData, 0, messageLength);
        T data = receiveMessage(messageData);
        MessageEvent<T> message = new MessageEvent<>(this, data);
        applicationEventPublisher.publishEvent(message);
    }

    private T receiveMessage(byte[] encryptedMessageBytes) {
        try {
            EncryptedMessage encryptedMessage = EncryptedMessage.parser().parseFrom(encryptedMessageBytes);

            byte[] decryptedBytes = decryptBytes(
                encryptedMessage.getPayload().toByteArray(),
                encryptedMessage.getNonce().toByteArray()
            );
        
            return parser.parseFrom(decryptedBytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return null;
        }
    }

    private EncryptionResult encryptBytes(byte[] bytes) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/GCM/NoPadding", "SunJCE");
            byte[] nonce = new byte[GCM_NONCE_LENGTH];

            random.nextBytes(nonce);

            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, privateKey, spec);
            return new EncryptionResult(nonce, cipher.doFinal(bytes));
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    private byte[] decryptBytes(byte[] encryptedBytes, byte[] nonce) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/GCM/NoPadding", "SunJCE");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
            cipher.init(Cipher.DECRYPT_MODE, privateKey, spec);
            return cipher.doFinal(encryptedBytes);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new byte[] { };
        }
    }

    private class EncryptionResult {
        private byte[] nonce;
        private byte[] payload;

        private byte[] getNonce() {
            return nonce;
        }

        private byte[] getPayload() {
            return payload;
        }

        public EncryptionResult(byte[] nonce, byte[] payload) {
            this.nonce = nonce;
            this.payload = payload;
        }
    }
}
