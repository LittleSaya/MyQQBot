import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AuxiliaryFunctions
{
    public static String hash33(String qrsig)
    {
        int token = 0;
        for (int i = 0; i < qrsig.length(); ++i)
        {
            token += (token << 5) + qrsig.codePointAt(i);
        }
        return String.valueOf(2147483647 & token);
    }

    public static void log(String msg)
    {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY/MM/dd HH:mm:ss.SSS");
        System.out.println("[" + sdf.format(date) + "]" + msg);
    }

    public static void printResponseHeaders(HttpResponse response)
    {
        Header[] headers = response.getAllHeaders();
        for (Header header : headers)
        {
            System.out.println("\t" + header.getName() + ": " + header.getValue());
        }
        System.out.println();
    }

    public static void waitFor(long t)
    {
        try
        {
            Thread.sleep(t);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static String[] ptuiCBResolve(String ptuiCB)
    {
        int sqCount = 0;
        for (int i = 0; i < ptuiCB.length(); ++i)
            if (ptuiCB.charAt(i) == '\'') ++ sqCount;
        if (sqCount % 2 != 0) return null;
        String[] strArr = new String[sqCount / 2];
        int idx1 = 0, idx2 = 0, ctr = 0;
        while (true)
        {
            idx1 = ptuiCB.indexOf('\'', idx2);
            idx2 = ptuiCB.indexOf('\'', idx1 + 1);
            if (idx1 < 0 || idx2 < 0) break;
            strArr[ctr++] = ptuiCB.substring(idx1 + 1, idx2);
            ++idx2;
        }
        return strArr;
    }
}
