package qqbot_botsaya.executors;

import com.sun.istack.internal.NotNull;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import qqbot_botsaya.constants.StringConstants;

import java.io.IOException;
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        System.out.println("[" + sdf.format(date) + "][" +
                Thread.currentThread().getName() + "]log:" + msg);
    }

    public static void error(String msg)
    {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        System.out.println("[" + sdf.format(date) + "]error:" + msg);
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

    public static boolean isStrOneOfStrs(@NotNull final String str, @NotNull final String[] strs)
    {
        for (String i : strs)
            if (i.compareTo(str) == 0)
                return true;
        return false;
    }

    public static int findStrInStrs(@NotNull final String str, @NotNull final String[] strs)
    {
        for (int i = 0; i < strs.length; ++i)
            if (str.compareTo(strs[i]) == 0)
                return i;
        return -1;
    }

    public static String[] extractCookieValues(@NotNull final Header[] cookies, @NotNull final String[] namesDomains)
    {
        String[] retArr = new String[namesDomains.length];
        String[][] partedNamesDomains = new String[namesDomains.length][2];
        // first, sperate the names and domains
        for (int i = 0; i < namesDomains.length; ++i)
        {
            String[] partedNameDomain = namesDomains[i].split(";");
            if (2 != partedNameDomain.length)
            {
                AuxiliaryFunctions.log("warning: invalid name-domain pair in AuxiliaryFunctions.extractCookieValues()");
                partedNamesDomains[i][0] = partedNamesDomains[i][1] = retArr[i] = StringConstants.NULL;
            }
            else
            {
                partedNamesDomains[i][0] = partedNameDomain[0];
                partedNamesDomains[i][1] = partedNameDomain[1];
            }
        }
        // second, choose the right value
        int counter = 0;
        for (Header cookie : cookies)// for every cookie
        {
            // extract the name and domain from the cookie
            String cookieString = cookie.getValue();
            String cookieName = extractCookieName(cookieString);
            String cookieDomain = extractCookieDomain(cookieString);
            for (int i = 0; i < namesDomains.length; ++i)
            {
                if (null == retArr[i] &&
                        0 == cookieName.compareTo(partedNamesDomains[i][0]) &&
                        0 == cookieDomain.compareTo(partedNamesDomains[i][1]))// check every name which hasn't had a value
                {
                    retArr[i] = extractCookieValue(cookieString);
                    ++counter;
                }
            }
            if (namesDomains.length == counter)
            {   break; }
        }
        for (int i = 0; i < namesDomains.length; ++i)
        {
            if (null == retArr[i])
            {   AuxiliaryFunctions.log("warning: the value of cookie \"" + namesDomains[i] + "\" was not found"); }
        }
        return retArr;
    }

    public static String extractCookieValue(@NotNull final String cookie)
    {
        int begin = cookie.indexOf('=');
        int end = cookie.indexOf(';', begin);
        if (begin < 0 || end < 0)
        {   return null; }
        else
        {   return cookie.substring(begin + 1, end); }
    }

    public static String extractCookieName(@NotNull final String cookie)
    {
        int end = cookie.indexOf('=');
        if (end < 0)
        {   return null; }
        else
        {   return cookie.substring(0, end); }
    }

    public static String extractCookieDomain(@NotNull final String cookie)
    {
        int begin = cookie.indexOf("DOMAIN=");
        int end = cookie.indexOf(';', begin);
        if (begin < 0 || end < 0)
        {   return null; }
        else
        {   return cookie.substring(begin + "DOMAIN=".length(), end); }
    }

    public static int extractStateCodeFromPtuiCB(@NotNull final String ptuiCB)
    {
        int idx = 0;
        idx = ptuiCB.indexOf('\'');
        return Integer.parseInt(ptuiCB.substring(idx + 1, ptuiCB.indexOf('\'', idx + 1)));
    }

    public static String extractAuthURIFromPtuiCB(@NotNull final String ptuiCB)
    {
        int idx = 0;
        for (int i = 0; i < 5; ++i)
        { idx = ptuiCB.indexOf('\'', idx) + 1; }
        String uri = ptuiCB.substring(idx, ptuiCB.indexOf('\'', idx));
        if (null == uri || !uri.startsWith("http"))
        { return null; }
        return uri;
    }

    public static String hash2(int uin, String ptvfwebqq)
    {
        char[] ptb = new char[4];
        for (int i = 0; i < ptvfwebqq.length(); ++i)
        {   ptb[i % 4] ^= ptvfwebqq.codePointAt(i); }
        String[] salt = new String[]{ "EC", "OK" };
        int[] uinByte = new int[4];
        uinByte[0] = (((uin >> 24) & 0xFF) ^ salt[0].codePointAt(0));
        uinByte[1] = (((uin >> 16) & 0xFF) ^ salt[0].codePointAt(1));
        uinByte[2] = (((uin >> 8) & 0xFF) ^ salt[1].codePointAt(0));
        uinByte[3] = ((uin & 0xFF) ^ salt[1].codePointAt(1));
        int[] result = new int[8];
        for (int i = 0; i < 8; ++i)
        {
            if (i % 2 == 0)
            {   result[i] = ptb[i >> 1]; }
            else
            {   result[i] = uinByte[i >> 1]; }
        }
        return byte2hex(result);
    }

    public static String byte2hex(int[] bytes)
    {
        char[] hex = "0123456789ABCDEF".toCharArray();
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i)
        {
            buf.append(hex[(bytes[i]>>4) & 0xF]);
            buf.append(hex[bytes[i] & 0xF]);
        }
        return buf.toString();
    }

    public static void consumeUnexpectedResponse(CloseableHttpResponse response)
    {
        AuxiliaryFunctions.log("consuming unexcepted response...");
        // entity
        HttpEntity entity = response.getEntity();
        try
        { EntityUtils.consume(entity); }
        catch (IOException e)
        {
            AuxiliaryFunctions.log("warning: I/O Exception occurred when EntityUtils.consume(entity)");
            e.printStackTrace();
        }
        // close response
        try
        { response.close(); }
        catch (IOException e)
        {
            AuxiliaryFunctions.log("warning: I/O Exception occurred when response.close()");
            e.printStackTrace();
        }
        AuxiliaryFunctions.log("consuming unexcepted response...complete");
    }

    public static void consumeEntityAndCloseResponse(HttpEntity entity, CloseableHttpResponse response)
    {
        AuxiliaryFunctions.log("consuming entity and closing response...");
        // entity
        try
        { EntityUtils.consume(entity); }
        catch (IOException e)
        {
            AuxiliaryFunctions.log("warning: I/O Exception occurred when EntityUtils.consume(entity)");
            e.printStackTrace();
        }
        // close response
        try
        { response.close(); }
        catch (IOException e)
        {
            AuxiliaryFunctions.log("warning: I/O Exception occurred when response.close()");
            e.printStackTrace();
        }
        AuxiliaryFunctions.log("consuming entity and closing response...complete");
    }

    public static void closeClient(CloseableHttpClient httpClient)
    {
        try
        { httpClient.close(); }
        catch (IOException e)
        {
            AuxiliaryFunctions.log("I/O Exception when httpClient.close()");
            e.printStackTrace();
        }
    }
}
