/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.truphone.lpap.card;

import com.truphone.lpa.ApduChannel;
import com.truphone.lpa.ApduTransmittedListener;
import static com.truphone.lpap.HexHelper.byteArrayToHex;
import static com.truphone.lpap.HexHelper.hexStringToByteArray;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.lang.*;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import javax.smartcardio.CardException;

/**
 *
 * @author amilcar.pereira
 */
public class ApduChannelImpl implements ApduChannel {

    private static final java.util.logging.Logger LOG = Logger.getLogger(ApduChannelImpl.class.getName());;


    private ApduTransmittedListener apduTransmittedListener;
    private Socket socket;
    private OutputStream output;
    private InputStream input;

    private byte[] xmit(byte[] apdu) {
        byte[] len = ByteBuffer.allocate(2).putShort((short)apdu.length).array();
        byte[] rx_len = ByteBuffer.allocate(2).putShort((short)0).array();
        byte[] rx = null;
        int rx_len_int = 0;

        try {
            output.write(len, 0, 2);
            output.write(apdu, 0, apdu.length);

            if (2 != input.read(rx_len, 0, 2)) {
                System.out.println("EOF");
                return null;
            }

            rx_len_int = (((short)(rx_len[0] & 0xff)) << 8) + (rx_len[1] & 0xff);

            rx = ByteBuffer.allocate(rx_len_int).array();
            if (rx_len_int != input.read(rx, 0, rx_len_int)) {
                System.out.println("EOF");
                return null;
            }

        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }

        return rx;
    }

    public ApduChannelImpl(final String cardReader) throws CardException {
        try {
            socket = new Socket("localhost", Integer.parseInt(System.getenv("APDU_PORT")));
            output = socket.getOutputStream();
            input = socket.getInputStream();

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
            System.exit(1);
            return;

        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
            System.exit(1);
            return;

        } catch (NumberFormatException ex) {
            System.out.println("Provide port in APDU_PORT env");
            System.exit(1);
            return;
        }

        byte[] apdu;
        byte[] responseApdu;

        //AP ADDDED THIS STATUS COMMAND
        //send terminal capabilities
        LOG.log(Level.INFO,("Send Terminal Capabilities"));
        apdu = hexStringToByteArray("80AA00000AA9088100820101830107");

        LOG.log(Level.INFO, byteArrayToHex(apdu));
        responseApdu = xmit(apdu);

        LOG.log(Level.INFO,("Open Logical Channel and Select ISD-R"));

        apdu = hexStringToByteArray("00A4040010A0000005591010FFFFFFFF8900000100");

        LOG.log(Level.INFO, byteArrayToHex(apdu));
        responseApdu = xmit(apdu);
        //transmitAPDU("00A4040010A0000005591010FFFFFFFF8900000100");


        //Send Status
        LOG.log(Level.INFO,("Send Status"));
        apdu = hexStringToByteArray("80F2000C00");
        LOG.log(Level.INFO, byteArrayToHex(apdu));
        responseApdu = xmit(apdu);

    }

    @Override
    public String transmitAPDU(String apdu) {
        byte[] bApdu;
        byte[] responseApdu;

        //send terminal capabilities
        bApdu = hexStringToByteArray(apdu.trim().replaceAll(" ", ""));
        responseApdu = xmit(bApdu);

        if (apduTransmittedListener != null) {
            apduTransmittedListener.onApduTransmitted();
        }

        return byteArrayToHex(responseApdu);
    }

    @Override
    public String transmitAPDUS(List<String> apdus) {
        String result = "";

        for (String apdu : apdus) {
            LOG.log(Level.INFO,"APDU: {}", apdu);
            result = transmitAPDU(apdu);

            //if has more than 4 chars for SW, then it contains data. 
            if (result.length() > 4) {
                return result;
            }

            LOG.log(Level.INFO,"Response: {}", result);
        }
        return result;
    }

    @Override
    public void sendStatus() {

    }

    @Override
    public void setApduTransmittedListener(ApduTransmittedListener apduTransmittedListener) {
        this.apduTransmittedListener = apduTransmittedListener;
    }

    @Override
    public void removeApduTransmittedListener(ApduTransmittedListener apduTransmittedListener) {
        this.apduTransmittedListener = null;
    }

    public void close() throws CardException {
        try {
            socket.close();
        } catch (IOException ex) {

        }
    }
}
