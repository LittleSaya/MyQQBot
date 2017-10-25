

public class Main
{
    public static void main(String[] args)
    {
        // get QR code
        if (!CoreFunctions.getQRCode())
        {
            AuxiliaryFunctions.log("fail: CoreFunctions.getQRCode()");
            return;
        }

        // waiting for scanning
        do
        {
            if (!CoreFunctions.getQRState())
            {
                AuxiliaryFunctions.log("fail: CoreFunctions.getQRState()");
                return;
            }
            AuxiliaryFunctions.waitFor(3000);
        } while(Values.login_keyuri == null);

        AuxiliaryFunctions.log("Exiting program...");
    }
}
