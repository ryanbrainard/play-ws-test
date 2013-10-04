package controllers;

import services.SfdcStreamer;
import play.libs.F.Callback0;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.index;

public class Application extends Controller {
    public static WebSocket<String> pingWs() {
        return new WebSocket<String>() {
            public void onReady(final WebSocket.In<String> in, final WebSocket.Out<String> out) {
                final SfdcStreamer sfdc = new SfdcStreamer();

                sfdc.start(new SfdcStreamer.Printer() {
                    @Override
                    public void println(String s) {
                        out.write(s);
                    }
                });

                in.onClose(new Callback0() {
                    @Override
                    public void invoke() throws Throwable {
                        sfdc.stop();
                    }
                });
            }

        };
    }

    public static Result pingJs() {
        return ok(views.js.ping.render());
    }

    public static Result index() {
        return ok(index.render());
    }
}
