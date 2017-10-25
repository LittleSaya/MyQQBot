import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

public class URIs
{
    private static URI ptqrshow = null;
    private static URI ptqrlogin = null;

    public static URI getPtqrshow() { return ptqrshow; }
    public static URI getPtqrlogin() { return ptqrlogin; }

    public static boolean setPtqrshow()
    {
        try
        {
            ptqrshow = new URIBuilder()
                    .setScheme("https")
                    .setHost("ssl.ptlogin2.qq.com")
                    .setPath("/ptqrshow")
                    .setParameter("appid", "501004106")
                    .setParameter("e", "2")
                    .setParameter("l", "M")
                    .setParameter("s", "3")
                    .setParameter("d", "72")
                    .setParameter("v", "4")
                    .setParameter("t", "0.1")
                    .setParameter("daid", "164")
                    .setParameter("pt_3rd_aid", "0")
                    .build();
            return true;
        }
        catch (URISyntaxException e)
        {
            return false;
        }
    }

    public static boolean setPtqrlogin(String ptqrtoken)
    {
        try
        {
            ptqrlogin = new URIBuilder()
                    .setScheme("https")
                    .setHost("ssl.ptlogin2.qq.com")
                    .setPath("ptqrlogin")
                    .setParameter("u1", "http://w.qq.com/proxy.html")
                    .setParameter("ptqrtoken", ptqrtoken)
                    .setParameter("ptredirect", "0")
                    .setParameter("h", "1")
                    .setParameter("t", "1")
                    .setParameter("g", "1")
                    .setParameter("from_ui", "1")
                    .setParameter("ptlang", "2052")
                    .setParameter("action",
                            "0-0-" + String.valueOf(System.currentTimeMillis()))
                    .setParameter("js_ver", "10230")
                    .setParameter("js_type", "1")
                    .setParameter("login_sig", "")
                    .setParameter("pt_uistyle", "40")
                    .setParameter("aid", "501004106")
                    .setParameter("daid", "164")
                    .setParameter("mibao_css", "m_webqq")
                    .build();
            return true;
        }
        catch (URISyntaxException e)
        {
            return false;
        }
    }
}
