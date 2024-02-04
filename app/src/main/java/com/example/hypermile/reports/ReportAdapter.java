package com.example.hypermile.reports;

import android.content.Context;
import android.icu.text.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.hypermile.R;
import com.example.hypermile.bluetooth.DiscoveredDevice;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Array adapter used to show a list of reports
 */
public class ReportAdapter extends ArrayAdapter<Report> {
    private final static java.text.DateFormat dateFormat = new SimpleDateFormat("HH:mm • EEEE d MMM yyyy", Locale.ENGLISH);

    public ReportAdapter(@NonNull Context context, List<Report> reportList) {
        super(context, 0, reportList);
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.report_list_item, parent, false);
        }

        Report report = getItem(position);
        TextView reportId = convertView.findViewById(R.id.reportId);
        TextView mpgValue = convertView.findViewById(R.id.mpgValue);

        if (report != null) {
            String reportDate = report.getDateOfReport();
            try{
                Date date = new Date(Long.parseLong(reportDate));
                reportDate = Report.DATE_FORMAT.format(date);
            }
            catch (Exception e){}

            reportId.setText(reportDate);
            mpgValue.setText(String.valueOf(Math.round( report.getAvgMpg() )));
        }

        return convertView;
    }
}
