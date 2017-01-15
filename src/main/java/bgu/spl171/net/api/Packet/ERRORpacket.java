package bgu.spl171.net.api.Packet;

/**
 * Created by Medhopz on 1/9/2017.
 */
public class ERRORpacket extends Packets {
    private short errCode;
    private String errMsg;

    public ERRORpacket(short _errCode, String str) {
        super.opCode = 5;
        super.msgType = "ERROR";

        errMsg = str;
        errCode = _errCode;
    }

    public short getErrCode() {
        return errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }
}
