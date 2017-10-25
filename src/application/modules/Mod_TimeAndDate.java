package application.modules;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Mod_TimeAndDate
{
    public static final String[] dict = new String[]
            {
                    /* 0 */"!时间", "！时间",
                    /* 2 */"!日期", "！日期",
                    /* 4 */"!北京时间", "！北京时间",
                    /* 6 */"!英国时间", "！英国时间",
                    /* 8 */"!大英帝国时间", "！大英帝国时间",
                    /* 10 */"!协调世界时", "！协调世界时"
            };

    public static String respond(int idx)
    {
        if (0 == idx || 1 == idx)
        {   return new SimpleDateFormat("HH时mm分ss秒SSS毫秒").format(new Date()); }
        else if (2 == idx || 3 == idx)
        {   return new SimpleDateFormat("yyyy年MM月dd日").format(new Date()); }
        else if (4 == idx || 5 == idx)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒SSS毫秒");
            sdf.setTimeZone(TimeZone.getTimeZone("CTT"));
            return sdf.format(new Date());
        }
        else if (6 == idx || 7 == idx || 8 == idx || 9 == idx)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒SSS毫秒");
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/London"));
            return sdf.format(new Date());
        }
        else
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒SSS毫秒");
            sdf.setTimeZone(TimeZone.getTimeZone("UCT"));
            return sdf.format(new Date());
        }
    }
}
