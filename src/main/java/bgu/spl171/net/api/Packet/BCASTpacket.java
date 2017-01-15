package bgu.spl171.net.api.Packet;

/**
 * Created by Medhopz on 1/9/2017.
 */
public class BCASTpacket extends Packets {
    private String filname;
    private byte delAdd;
    public BCASTpacket(byte _delAdd, String str) {
        filname = str;
        delAdd = _delAdd;
        super.opCode = 9;
        super.msgType = "BCAST";
    }

    public String getFilname() {
        return filname;
    }

    public byte getDelAdd() {
        return delAdd;
    }
}
