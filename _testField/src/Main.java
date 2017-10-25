import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Main
{
    public static void main(String[] args)
    {
//        printTZ();
        SimpleDateFormat sdf = new SimpleDateFormat("z时区 yyyy年MM月dd日 HH时mm分ss秒SSS毫秒");
        Date date = new Date();

        System.out.println("default time zone: " + sdf.format(date));

        sdf.setTimeZone(TimeZone.getTimeZone("CTT"));
        System.out.println("CTT " + sdf.format(date));

        sdf.setTimeZone(TimeZone.getTimeZone("Europe/London"));
        System.out.println("Europe/London" + sdf.format(date));

        sdf.setTimeZone(TimeZone.getTimeZone("UCT"));
        System.out.println("UCT" + sdf.format(date));
    }

    public static void printTZ()
    {
        String[] strs = TimeZone.getAvailableIDs();
        for (String str : strs)
            System.out.println(str);
    }
}
