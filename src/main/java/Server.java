import spread.MembershipInfo;
import spread.SpreadException;
import spread.SpreadGroup;

import java.util.ArrayList;
import java.util.List;

public class Server {

    public static void main(String[] args) {

        //ConcreteState s = new

        try {

            ActiveReplication ar = new ActiveReplication(Integer.parseInt(args[0]), true);

            ar.open().join();

            ar.join("all");

            // lista que contem todos os tipos de requests que podem ser recebidos
            // TODO preencher lista
            List<Class<?>> types = new ArrayList<>();

            ar.update(types, Server::setState, Server::getState, Server::updateState)
                    .thenRun(() -> {
                        // TODO definir handlers

                        ar.handler(MembershipInfo.class, (msg, req) -> {
                            SpreadGroup client;
                            if (req.isCausedByDisconnect())
                                client = req.getDisconnected();
                            else if (req.isCausedByLeave())
                                client = req.getLeft();
                            else
                                return;

                            // TODO tornar tarefas do cliente disponiveis
                        });
                    });


        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

    private static void setState(State state) {
        // s = state;
    }

    private static State getState(Void v) {
        // return s;
        return null;
    }

    private static void updateState(Tuple<?> t) {
        // if(t.type.equals(...class) {
        // ...
    }
}
