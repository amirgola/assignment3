package bgu.spl171.net.api.Packet;

/**
 * Created by Medhopz on 1/8/2017.
 */
public abstract class Packets {
    public short opCode;
    public String msgType;

    public short getOpCode() {
        return opCode;
    }

    public String getMsgType() {
        return msgType;
    }
}
