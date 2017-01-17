package bgu.spl171.net.api.bidi;

import bgu.spl171.net.api.Packet.*;
import bgu.spl171.net.srv.NonBlockingConnectionHandler;
import bgu.spl171.net.srv.bidi.ConnectionHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by Medhopz on 1/6/2017.
 */
public class BidiMsgProtoImp implements BidiMessagingProtocol<Packets> {

    private int connectionId;
    private Connections connections;
    private ConcurrentHashMap<Integer,String> userNameMap;
    private int packetsLeft;
    ArrayList<DATApacket> temp;
    private boolean recievingData;
    private String tempFileName;
    private String workingDirectory = System.getProperty("user.dir");
    private ConcurrentHashMap<Integer, byte[]> orgenizeData;
    private ConcurrentLinkedDeque<String> namesOfFiles;
    private int sendFileNamePackets;
    private ArrayList<DATApacket> fileNamePackets;
    private boolean sendingFileNames;
    private short tempBlkNum;
    private short lastBlk;
    private boolean shouldTerminate;
    private boolean loggedIn;

    public void start(int connectionId, Connections connections) {
            this.connectionId = connectionId;
            this.connections = connections;
            this.userNameMap = new ConcurrentHashMap<>();
            this.packetsLeft = 0;
            this.sendFileNamePackets = 0;
            this.fileNamePackets = new ArrayList<>();
            recievingData = false;
            sendingFileNames = false;
            this.shouldTerminate = false;
            this.lastBlk = 0;
            this.loggedIn = false;
            namesOfFiles = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void process(Packets message) {
        String msgType = message.getMsgType();

        if(msgType.equalsIgnoreCase("LOGRQ")) {
            if ( !loggedIn(((LOGRQpacket) message).getUserName()) ) {
                //do we need a waiting list for everyone that sent a request?
                userNameMap.put(this.connectionId, ((LOGRQpacket) message).getUserName());
                loggedIn = true;
                connections.send(this.connectionId, new ACKpacket((short) 0));
            } else {
                connections.send(this.connectionId, new ERRORpacket((short) 7, "User already exists"));
            }
        }
    if(!loggedIn) {
        switch (msgType) {
                case "DELRQ":
                    if (findFile(((DELRQpacket) message).getFileName())) {
                        deleteFromFolder(((DELRQpacket) message).getFileName());
                        connections.send(this.connectionId, new ACKpacket((short) 0));
                        namesOfFiles.remove(((DELRQpacket) message).getFileName());
                        this.connections.broadcast(new BCASTpacket((byte) 0, "BCAST del " + ((DELRQpacket) message).getFileName()));
                    } else {
                        connections.send(this.connectionId, new ERRORpacket((short) 1, "File not found"));
                    }
                    break;

                case "RRQ":
                    if (findFile(((RRQpacket) message).getFileName())) {
                        temp = fileToDataPacket(((RRQpacket) message).getFileName());
                        connections.send(this.connectionId, temp.remove(0)); // send first packet
                        packetsLeft = temp.size();
                    } else {
                        connections.send(this.connectionId, new ERRORpacket((short) 1, "File not found"));
                    }
                    break;
                case "WRQ":
                    if (!findFile(((WRQpacket) message).getFileName())) {
                        connections.send(this.connectionId, new ACKpacket((short) 0));
                        tempFileName = ((WRQpacket) message).getFileName();
                        File temp = new File(workingDirectory + File.separator + tempFileName);
                        recievingData = true;
                    } else {
                        connections.send(this.connectionId, new ERRORpacket((short) 5, "File already in server"));
                    }
                    break;

                case "ACK":
                    if (packetsLeft > 0 && ((ACKpacket) message).getBlockNumber() == temp.get(0).getBlockNumber() - 1) {
                        connections.send(this.connectionId, temp.remove(0));
                        packetsLeft = packetsLeft - 1;
                    }
                    if (sendingFileNames) {
                        if (fileNamePackets.size() > 0 && ((ACKpacket) message).getBlockNumber() == tempBlkNum) {
                            tempBlkNum++; // or fileNamePackets.get(0).getBlockNumber()
                            connections.send(this.connectionId, fileNamePackets.remove(0));
                        }
                        if (fileNamePackets.size() > 0) {
                            sendingFileNames = false;
                            tempBlkNum = 0;
                        }
                    }
                    break;
                case "DATA":
                    if (recievingData) {
                        orgenizeData.put((int) ((DATApacket) message).getBlockNumber(), ((DATApacket) message).getData());
                        connections.send(this.connectionId, new ACKpacket((short) ((DATApacket) message).getBlockNumber()));
                        if (((DATApacket) message).getData().length < 512) {//check if we have enough packets
                            lastBlk = ((DATApacket) message).getBlockNumber();
                        }
                        if (lastBlk == orgenizeData.size()) {
                            recievingData = false;
                            FileOutputStream out = null;
                            try {
                                out = new FileOutputStream(workingDirectory + File.separator + tempFileName);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            try {
                                for (Integer i : orgenizeData.keySet()) {
                                    out.write(orgenizeData.get(Integer.valueOf(i)));
                                }
                                out.close();
                                namesOfFiles.add(tempFileName);
                                tempFileName = "";
                                this.connections.broadcast(new BCASTpacket((byte) 1, "BCAST add " + tempFileName));
                                lastBlk = 0;
                                orgenizeData = null;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case "DISC":
                    userNameMap.remove(this.connectionId);
                    this.shouldTerminate = true;
                    connections.disconnect(this.connectionId);
                    connections.send(this.connectionId, new ACKpacket((short) 0));
                    break;
                case "DIRQ":
                    sendingFileNames = true;
                    fileNamePackets = sendFileNames();
                    tempBlkNum = 1;
                    connections.send(this.connectionId, fileNamePackets.remove(0)); // send first packet
                    break;
            }
        } else {
        connections.send(this.connectionId, new ERRORpacket((short) 7, "User already exists"));
        }
    }

    private ArrayList<DATApacket> sendFileNames() {
        String allStrings= "";
        for (String s:namesOfFiles) {
            allStrings += s;
            allStrings += "\0";
        }
        byte[] buf = new byte[512];
        ArrayList<DATApacket> res = new ArrayList<>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        short blkNumber = 1;
        try {
            InputStream in = new ByteArrayInputStream(allStrings.getBytes());
            while(in.read(buf) != -1) {
                out.write(buf);
                byte[] data = out.toByteArray();
                res.add(new DATApacket((short)data.length, blkNumber , data));
                blkNumber++;
                out.reset();
                //do we need to reset the buf array as well?
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }


    private ArrayList<DATApacket> fileToDataPacket(String fileName) {
        File fileToSend = new File(workingDirectory + File.separator + fileName);
        byte[] buf = new byte[512];
        ArrayList<DATApacket> res = new ArrayList<>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        short blkNumber = 1;
        try {
            FileInputStream in = new FileInputStream(fileToSend);
            while(in.read(buf) != -1) {
                out.write(buf);
                byte[] data = out.toByteArray();
                res.add(new DATApacket((short)data.length, blkNumber , data));
                blkNumber++;
                out.reset();
                //do we need to reset the buf array as well?
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }


    private void deleteFromFolder(String fileName) {
        File del = new File(workingDirectory + File.separator + fileName);
        del.delete();
    }

    private boolean findFile(String fileName) {
        String workingDirectory = System.getProperty("user.dir");
        File folder = new File(workingDirectory + File.separator);

        File[] files = folder.listFiles();
        if (files != null)
            for (File fil : files) {
                if (fileName.equalsIgnoreCase(fil.getName())) {
                    return true;
                }
            }
        return false;
    }


    private boolean loggedIn(String userName) {
        return userNameMap.containsValue(userName);
    }


    @Override
    public boolean shouldTerminate() {
        return this.shouldTerminate;
    }
}
