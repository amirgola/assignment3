package bgu.spl171.net.api.Packet;

/**
 * Created by Medhopz on 1/9/2017.
 */
public class LOGRQpacket extends Packets {

    private String userName;

    public LOGRQpacket(String str) {
        userName = str;

        super.opCode = 7;
        super.msgType = "LOGRQ";
    }

    public String getUserName() {
        return userName;
    }
}
