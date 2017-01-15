package bgu.spl171.net.api.Packet;

/**
 * Created by Medhopz on 1/9/2017.
 */
public class DATApacket extends Packets {


    private short packetSize;
    private short blockNumber;
    private byte[] data;

    public DATApacket(short _packetSize, short _blockNumber, byte[] _data) {

        super.opCode = 3;
        super.msgType = "DATA";
        packetSize = _packetSize;
        blockNumber = _blockNumber;
        data = _data;

    }

    public byte[] getData() {
        return data;
    }

    public short getBlockNumber() {
        return blockNumber;
    }

    public short getPacketSize() {
        return packetSize;
    }
}
