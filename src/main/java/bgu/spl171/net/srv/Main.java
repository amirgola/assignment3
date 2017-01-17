package bgu.spl171.net.srv;

import bgu.spl171.net.api.EncDec;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.BidiMsgProtoImp;
import bgu.spl171.net.impl.rci.ObjectEncoderDecoder;
import bgu.spl171.net.impl.rci.RemoteCommandInvocationProtocol;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Created by Medhopz on 1/16/2017.
 */
public class Main  {

    private static Main server;


    public static void main(String[] args) throws IOException {

        int port = 666; // get the main from args
        // int port = Integer.parseInt(args[0]);
        BidiMessagingProtocol bid = new BidiMsgProtoImp();

        Server.reactor(
                2,
                port, //port
                ()-> new BidiMsgProtoImp(), //protocol factory
                () ->new EncDec() //message encoder decoder factory
        ).serve();


    }


}
