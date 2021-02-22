package FileTransfer;

public class UITask implements Runnable
{

    public void exec() throws Exception {

    }

    public void execFinal() throws Exception {

    }

    @Override public void run()
    {
        try {
            exec();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        if(FileTransferController.onStandby() && FileTransferController.equalsTask(this))
        {
            try {
                execFinal();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            FileTransferController.exitStandbyMode();
        }
        else
            System.out.println("Cancelled task");
    }

}
