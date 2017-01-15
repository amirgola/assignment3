package bgu.spl171.net.api.Packet;

/**
 * Created by Medhopz on 1/9/2017.
 */
public class DELRQpacket extends Packets {

    private String fileName;
    public DELRQpacket(String str) {
        fileName = str;

        super.opCode = 8;
        super.msgType = "DELRQ";
    }

    public String getFileName() {
        return fileName;
    }
}
