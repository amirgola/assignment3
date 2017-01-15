package bgu.spl171.net.api.Packet;

/**
 * Created by Medhopz on 1/9/2017.
 */
public class WRQpacket extends Packets {
    private String fileName;

    public WRQpacket(String _str) {
        super.opCode = 2;
        super.msgType = "WRQ";
        fileName = _str;
    }

    public String getFileName() {
        return fileName;
    }
}
