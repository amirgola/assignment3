package bgu.spl171.net.api.Packet;

/**
 * Created by Medhopz on 1/9/2017.
 */
public class DIRQpacket extends Packets {

    public DIRQpacket() {
        super.opCode = 6;
        super.msgType = "DIRQ";
    }
}
