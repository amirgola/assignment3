package bgu.spl171.net.api;

import bgu.spl171.net.api.Packet.*;
import sun.misc.IOUtils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Medhopz on 1/7/2017.
 */
public class EncDec implements MessageEncoderDecoder<Packets> {
    short opCode;
    private byte[] bytes = new byte[518];
    private byte[] opCodeBytes = new byte[2];
    private int opCodeLen = 0;
    private int len = 0;
    String msgType;
    private String str;
    private boolean finishFirstTwoBytes = false;

    private ByteBuffer buffer = ByteBuffer.allocate(518);
    private boolean firstTime = true;

    //for data packet
    private short pckSize=0; // not sure where to init!
    private short blkNum=0;
    private byte[] dataArr = new byte[0];
    private int dataIndex = 0;

    @Override
    //maybe need to switch to byte buffer instead of array
    public Packets decodeNextByte(byte nextByte) {
        if(!finishFirstTwoBytes)
            findOpCode(nextByte);
        else {
            switch( msgType ) {
                case "LOGRQ":
                    if( firstTime ) {
                        buffer = ByteBuffer.allocate(512);
                        firstTime = false;
                    }
                    if(nextByte != '\0') {
                        buffer.put(nextByte);
                    } else {
                        try {
                            //maybe need to rewind here!
                            str = new String(buffer.array(), "UTF-8");
                            resetBuffer();
                            return new LOGRQpacket(str);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                case "DELRQ":
                    if( firstTime ) {
                        buffer = ByteBuffer.allocate(512);
                        firstTime = false;
                    }
                    if(nextByte != '\0') {
                        buffer.put(nextByte);
                    } else {
                        try {
                            str = new String(buffer.array(), "UTF-8");
                            resetBuffer();
                            return new DELRQpacket(str);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }

                case "RRQ":
                    if( firstTime ) {
                        buffer = ByteBuffer.allocate(512);
                        firstTime = false;
                    }
                    if(nextByte != '\0') {
                        buffer.put(nextByte);
                    } else {
                        try {
                            str = new String(buffer.array(), "UTF-8");
                            resetBuffer();
                            return new RRQpacket(str);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case "WRQ":
                    if( firstTime ) {
                        buffer = ByteBuffer.allocate(512);
                        firstTime = false;
                    }
                    if(nextByte != '\0') {
                        buffer.put(nextByte);
                    } else {
                        try {
                            str = new String(buffer.array(), "UTF-8");
                            resetBuffer();
                            return new WRQpacket(str);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case "DIRQ":
                    resetBuffer();
                    return new DIRQpacket();

                case "DATA":

                    if(firstTime) {
                        buffer = ByteBuffer.allocate(4);
                        firstTime = false;

                    }
                    if(buffer.position() < 4) { // 0,1,2,3 position get filled in buffer
                        buffer.put(nextByte);
                        if (buffer.position() == 2) {
                            buffer.rewind();
                            pckSize = buffer.getShort();
                            dataArr = new byte[pckSize];
                        }
                        if (buffer.position() == 4) {
                            buffer.position(2);
                            blkNum = buffer.getShort();
                        }
                    } else {
                        dataArr[dataIndex++] = nextByte;
                        if (dataIndex == pckSize) {
                            resetBuffer();
                            return new DATApacket(pckSize, blkNum, dataArr);
                        }
                    }
                    break;
                case "ACK":
                    if(firstTime) {
                        buffer = ByteBuffer.allocate(2);
                        firstTime = false;
                    }
                    buffer.put(nextByte);
                    if(buffer.position() >= 2) {
                        bytes = new byte[2];
                        if(buffer.hasArray()) // for safety, we should never have this problem though...
                            bytes = buffer.array();
                        resetBuffer();
                        return new ACKpacket(bytesToShort(bytes));
                    }
                    break;

//                case "BCAST":
//                    if(firstTime) {
//                        buffer = ByteBuffer.allocate(518);
//                        firstTime = false;
//                    }
//
//                    if(nextByte != '\0') {
//                        buffer.put(nextByte);
//                    } else {
//                        try {
//                            buffer.flip();
//                            byte delAdd = buffer.get(0);
//                            str = new String(buffer.array(), "UTF-8");
//                            resetBuffer();
//                            return new BCASTpacket(delAdd, str);
//                        } catch (UnsupportedEncodingException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    break;

                case "ERROR":
                    if(firstTime) {
                        buffer = ByteBuffer.allocate(518);
                        firstTime = false;
                    }

                    if(nextByte != '\0') {
                        buffer.put(nextByte);
                    } else {
                        try {
                            buffer.flip(); // begin reading
                            buffer.rewind(); // set position to zero
                            short errCode = buffer.getShort();
                            str = new String(bufferToByteArray(), "UTF-8");
                            resetBuffer();
                            return new ERRORpacket(errCode, str);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case "DISC":
                    resetBuffer();
                    return new DISCpacket();

                case "UNKNOWN":
                    resetBuffer();
                    return new ERRORpacket((short) 4, "unknown OP code");

            }
        }
        return null;
    }

    @Override
    public byte[] encode(Packets message) {
        byte[] opCodeArray = new byte[2] ;
        byte[] blkNumArray = new byte[2];
        byte[] temp = new byte[2];
        byte[] res = new byte[512]; // use a a different array than bytes
        msgType = message.getMsgType();

        switch (msgType) {

            case "ACK":

                res = new byte[4];
                blkNumArray = shortToBytes( ((ACKpacket)message).getBlockNumber());
                opCodeArray = shortToBytes(message.getOpCode());
                res[0] = opCodeArray[0];
                res[1] = opCodeArray[1];
                res[2] = blkNumArray[0];
                res[3] = blkNumArray[1];

                return res;


            case "BCAST":
                res = new byte[((BCASTpacket)message).getFilname().length()+4];
                opCodeArray = shortToBytes(message.getOpCode());
                res[0] = opCodeArray[0];
                res[1] = opCodeArray[1];
                res[2] = ((BCASTpacket)message).getDelAdd();
                temp = new byte[((BCASTpacket)message).getFilname().length()];
                temp = ((BCASTpacket)message).getFilname().getBytes();
                for (int i = 3; i < res.length; i++) {
                    res[i] = temp[i-3];
                }
                return res;

            case "DATA":
                res = new byte[((DATApacket)message).getPacketSize()+6];
                blkNumArray = new byte[2];
                byte[] pctSizeArray = new byte[2];
                opCodeArray = shortToBytes(message.getOpCode());
                pctSizeArray = shortToBytes( ((DATApacket)message).getPacketSize());
                blkNumArray = shortToBytes( ((DATApacket)message).getBlockNumber());
                temp = new byte[((DATApacket)message).getData().length];
                temp = ((DATApacket)message).getData();
                res[0] = opCodeArray[0];
                res[1] = opCodeArray[1];
                res[2] = pctSizeArray[0];
                res[3] = pctSizeArray[1];
                res[4] = blkNumArray[0];
                res[5] = blkNumArray[1];

                for (int i = 6; i < res.length; i++) {
                    res[i] = temp[i-6];
                }

                return res;

            case "ERROR":
                res = new byte[((ERRORpacket)message).getErrMsg().length()+5];
                byte[] errCodeArray = new byte[2];
                opCodeArray = shortToBytes(message.getOpCode());
                errCodeArray = shortToBytes( ((ERRORpacket)message).getErrCode());
                temp = new byte[((ERRORpacket)message).getErrMsg().length()];
                temp = ((ERRORpacket)message).getErrMsg().getBytes();
                res[0] = opCodeArray[0];
                res[1] = opCodeArray[1];
                res[2] = errCodeArray[0];
                res[3] = errCodeArray[1];
                for (int i = 4; i < res.length; i++) {
                    res[i] = temp[i-4];
                }
                return res;

        }
        return new byte[0];
    }

    private byte[] bufferToByteArray() {
        if (buffer.hasArray()) {
            final byte[] array = buffer.array();
            final int arrayOffset = buffer.arrayOffset(); // make this always 0?
            return Arrays.copyOfRange(array, arrayOffset + buffer.position(),
                    arrayOffset + buffer.limit());
        }
        //if we get here there is a problem
        return new byte[0];
    }

    private void findOpCode(byte nextByte) {
            opCodeBytes[opCodeLen++] = nextByte;
            if(opCodeLen == 2) {
                finishFirstTwoBytes = true;
                msgType = whichMsg(bytesToShort(opCodeBytes));

            }
    }

    private void resetBuffer() {
        finishFirstTwoBytes = false;
        buffer.clear();
        buffer.rewind(); // not sure if nessessary
        firstTime = true;
        //reset the opcode array
        opCodeLen = 0;
        opCodeBytes[0] = 0;
        opCodeBytes[1] = 0;
    }

    private String whichMsg(short i) {
        if( i == 1) msgType = "RRQ";
        else if(i==2) msgType = "WRQ";
        else if(i==3) msgType = "DATA";
        else if(i==4) msgType = "ACK";
        else if(i==5) msgType = "ERROR";
        else if(i==6) msgType = "DIRQ";
        else if(i==7) msgType = "LOGRQ";
        else if(i==8) msgType = "DELRQ";
        else if(i==9) msgType = "BCAST";
        else if(i==10) msgType = "DISC";
        else msgType = "UNKNOWN";
        return msgType;
    }


    private short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }
    private byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
}
