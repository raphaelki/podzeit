package de.rkirchner.podzeit.ui.common;

import android.content.Context;
import android.os.Build;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import timber.log.Timber;

public class FormatterUtil {

    private Context context;

    @Inject
    public FormatterUtil(Context context) {
        this.context = context;
    }

    public String formatFileSizeToMb(String fileSize) {
        if (fileSize == null) {
            Timber.w("File size is null");
            return null;
        }
        double fileSizeMb = Double.parseDouble(fileSize) / (1024 * 1024);
        NumberFormat numberFormat = NumberFormat.getNumberInstance(getLocalFromConfig());
        DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
        decimalFormat.applyPattern("#.#");
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        String formattedSize = decimalFormat.format(fileSizeMb);
        return formattedSize + " MB";
    }

    public String formatDuration(String duration) {
        if (duration == null) {
            Timber.w("Duration is null");
            return null;
        }
        if (duration.contains(":")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", getLocalFromConfig());
            try {
                Date date = dateFormat.parse(duration);
                return DateFormat.getTimeInstance(DateFormat.MEDIUM, getLocalFromConfig()).format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        int parsedDuration = Integer.parseInt(duration);
        int hours = parsedDuration / 3600;
        int minutes = (parsedDuration / 60) % 60;
        int seconds = parsedDuration % 60;
        return String.format(getLocalFromConfig(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    public String formatPubDate(String pubDate) {
        if (pubDate != null) {
            pubDate = pubDate.substring(0, 16);
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH);
                Date parsedDate = dateFormat.parse(pubDate);
                return DateFormat.getDateInstance(DateFormat.MEDIUM, getLocalFromConfig()).format(parsedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            Timber.w("pubDate is null");
        }
        return pubDate;
    }

    private Locale getLocalFromConfig() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0);
        } else return context.getResources().getConfiguration().locale;
    }
}
