import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CoreFunctions
{
    // get and save the QRCode
    // extract qrsig from cookie and calculate ptqrtoken
    public static boolean getQRCode()
    {
        AuxiliaryFunctions.log("getting QRCode...");
        boolean retVal = false;

        // init the URI for ptqrshow (the QRCode)
        if (URIs.getPtqrshow() == null && !URIs.setPtqrshow())
        {
            AuxiliaryFunctions.log("fail: URIs.setPtqrshow()");
            return false;
        }

        // create GET request
        HttpGet httpGet = new HttpGet(URIs.getPtqrshow());

        // send request, get response & process response
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try
        {
            // get response and entity
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();

            AuxiliaryFunctions.log("response status: " + response.getStatusLine());

            if (response.getStatusLine().getStatusCode() == 200)
            {
                // cookie
                Values.cookie = response.getFirstHeader("Set-Cookie").getValue();
                // qrsig
                int qrsigBegin = 0, qrsigEnd = 0;
                qrsigBegin = Values.cookie.indexOf("qrsig") + 6;
                qrsigEnd = Values.cookie.indexOf(';', qrsigBegin);
                Values.login_qrsig = Values.cookie.substring(qrsigBegin, qrsigEnd);
                // ptqrtoken
                Values.login_ptqrtoken = AuxiliaryFunctions.hash33(Values.login_qrsig);

                // save the QRCode in png
                AuxiliaryFunctions.log("saving the QRCode...");
                byte[] qrcodeData = EntityUtils.toByteArray(entity);
                File qrcodeFile = new File("qrcode.png");
                if (!qrcodeFile.exists())
                {
                    qrcodeFile.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(qrcodeFile);
                fos.write(qrcodeData);
                fos.flush();
                fos.close();
                AuxiliaryFunctions.log("QRCode has been successfully saved");
                retVal = true;
            }
            else
            {
                AuxiliaryFunctions.log("fail: unexpected status code " +
                        response.getStatusLine().getStatusCode());
                retVal = false;
            }

            // release resources
            EntityUtils.consume(entity);
            response.close();
            client.close();
            return retVal;
        }
        catch (IOException e)
        {
            AuxiliaryFunctions.log("fail: IOException in getQRCode()");
            e.printStackTrace();
            return false;
        }
    }

    // ask server of the state of the QRCode
    // the response will be stored in Values.login_ptuiCB
    // if the QRCode has been scanned, then
    //      Values.login_keyuri = a proper uri
    // otherwise
    //      Values.login_keyuri = null
    public static boolean getQRState()
    {
        AuxiliaryFunctions.log("getting QRState...");
        boolean retVal = false;

        // init the URI for ptqrlogin (the state of QRCode)
        // refresh the URI everytime
        if (!URIs.setPtqrlogin(Values.login_ptqrtoken))
        {
            AuxiliaryFunctions.log("fail: URIs.setPtqrlogin(Values.login_ptqrtoken)");
            return false;
        }

        // create GET request
        HttpGet httpGet = new HttpGet(URIs.getPtqrlogin());
        // carry the cookie
        httpGet.addHeader("cookie", "qrsig=" + Values.login_qrsig + ";");

        // send request, get response & process response
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try
        {
            // get response and entity
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();

            AuxiliaryFunctions.log("response status: " + response.getStatusLine());

            // fill Values.login_ptuiCB
            Values.login_ptuiCB = EntityUtils.toString(entity);
            AuxiliaryFunctions.log("response entity:\n\t" + Values.login_ptuiCB);

            // analyze ptuiCB
            String[] ptuiCBStrings = AuxiliaryFunctions.ptuiCBResolve(Values.login_ptuiCB);
            AuxiliaryFunctions.log("after analyze:");
            for (int i = 0; i < ptuiCBStrings.length; ++i)
                System.out.println(i + " : " + ptuiCBStrings[i]);

            int val1 = Integer.valueOf(ptuiCBStrings[0]);
            if (val1 == 0 && ptuiCBStrings[2].compareTo("") != 0)
            {
                AuxiliaryFunctions.log("login successful");
                Values.login_keyuri = ptuiCBStrings[2];
            }
            else if (val1 == 66)
            {
                AuxiliaryFunctions.log("waiting for scanning...");
                Values.login_keyuri = null;
            }
            else if (val1 == 65)
            {
                AuxiliaryFunctions.log("QRCode timeout");
                Values.login_keyuri = null;
            }
            else
            {
                AuxiliaryFunctions.log("Unrecognized ptuiCB[0]");
                Values.login_keyuri = null;
            }

            // release resources
            EntityUtils.consume(entity);
            response.close();
            client.close();
            return true;
        }
        catch (IOException e)
        {
            AuxiliaryFunctions.log("fail: IOException in getQRState()");
            e.printStackTrace();
            return false;
        }
    }
}
