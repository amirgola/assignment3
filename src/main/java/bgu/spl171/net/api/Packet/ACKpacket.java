package bgu.spl171.net.api.Packet;

/**
 * Created by Medhopz on 1/9/2017.
 */
public class ACKpacket extends Packets {
    private short blockNumber;

    public ACKpacket(short i) {

        blockNumber = i;
        super.opCode = 4;
        super.msgType = "ACK";
    }

    public short getBlockNumber() {
        return blockNumber;
    }
}
