package com.ninetwozero.iksu.utils;

import android.content.Context;

import com.ninetwozero.iksu.R;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;
import org.threeten.bp.temporal.Temporal;

public class DateUtils {
    private static final DateTimeFormatter WEEKDAY_FORMAT = DateTimeFormatter.ofPattern("EEEE");
    private static final DateTimeFormatter LITERAL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd '('EEEE')'");

    public static String getWeekday(Context context, long offset) {
        if (offset == 0) {
            return context.getString(R.string.label_today);
        } else if (offset == 1) {
            return context.getString(R.string.label_tomorrow);
        }

        final LocalDate realDate = LocalDate.now(ZoneId.of("Europe/Stockholm")).plusDays(offset);
        return realDate.format(offset < 0 ? DateTimeFormatter.ISO_DATE : WEEKDAY_FORMAT);
    }

    public static String getDate(int offset) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now(ZoneId.of("Europe/Stockholm")).plusDays(offset));
    }

    public static String getLiteralDateFromDateTime(String string) {
        return LITERAL_DATE_FORMAT.format(LocalDateTime.parse(string, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    public static long convertDateStringToLong(String string) {
        return Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(string)).toEpochMilli();
    }

    public static long nowInMillis() {
        return Instant.from(ZonedDateTime.now(ZoneId.of("Europe/Stockholm"))).toEpochMilli();
    }

    public static long countDaysBetween(Temporal from, Temporal to) {
        return ChronoUnit.DAYS.between(from, to);
    }

    public static String getTime(long timestamp) {
        return DateTimeFormatter.ISO_TIME.format(Instant.ofEpochMilli(timestamp));
    }
}
