package bgu.spl171.net.impl.TFTPreactor;

import bgu.spl171.net.api.EncDec;
import bgu.spl171.net.api.bidi.BidiMsgProtoImp;
import bgu.spl171.net.srv.Server;

import java.io.IOException;

/**
 * Created by Medhopz on 1/16/2017.
 */
public class ReactorMain  {

    private static ReactorMain server;


    public static void main(String[] args) throws IOException {

        int port = Integer.parseInt(args[0]);

        Server.reactor(
                2,
                port, //port
                () -> new BidiMsgProtoImp(), //protocol factory
                () -> new EncDec() //message encoder decoder factory
        ).serve();
    }
}
