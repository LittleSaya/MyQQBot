package application;

import qqbot_botsaya.executors.ClientManager;

public class Application
{
    public static void main(String[] args)
    {
        ClientManager clientManager = new ClientManager();
        clientManager.launch();
    }
}
