package bgu.spl171.net.api.Packet;


/**
 * Created by Medhopz on 1/8/2017.
 */
public class RRQpacket extends Packets {

    private String fileName;

    public RRQpacket(String _str) {
        super.opCode = 1;
        super.msgType = "RRQ";
        fileName = _str;
    }

    public String getFileName() {
        return fileName;
    }
}
