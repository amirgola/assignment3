package bgu.spl171.net.api.Packet;

/**
 * Created by Medhopz on 1/9/2017.
 */
public class DISCpacket extends Packets {

    public DISCpacket() {
        super.opCode = 10;
        super.msgType = "DISC";
    }
}
