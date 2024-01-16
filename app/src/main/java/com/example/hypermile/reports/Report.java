package com.example.hypermile.reports;

import java.util.Map;

public class Report {
    String dateOfReport;
    Map<String, Map<String,Object>> journeyData;

    public Report(String dateOfReport, Map<String, Object> journeyData) {
        this.dateOfReport = dateOfReport;

        for (Map.Entry<String, Object> entry : journeyData.entrySet()) {
            try {
                journeyData.put(entry.getKey(), (Map<String, Object>) entry.getValue());
            }
            catch (ClassCastException e) {}
        }
    }

    public String getDateOfReport() {
        return dateOfReport;
    }
}
