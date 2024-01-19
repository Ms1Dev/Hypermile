package com.example.hypermile.reports;

import android.util.Log;

import java.util.Map;

public class Report {
    String dateOfReport;
    double avgMpg;
    Map<String, Map<String,Object>> journeyData;

    public Report(String dateOfReport, Map<String, Object> journeyData) {
        this.dateOfReport = dateOfReport;
        double totalMpg = 0;
        int mpgDataCount = 0;
        for (Map.Entry<String, Object> entry : journeyData.entrySet()) {
            try {
                Map<String,Object> row = (Map<String, Object>) entry.getValue();
                journeyData.put(entry.getKey(), row);
                Double mpg = (Double) row.get("MPG");
                if (mpg != null && mpg > 0 && mpg < 99.99) {
                    totalMpg += mpg;
                    mpgDataCount++;
                }
            }
            catch (ClassCastException e) {
                Log.e("Err", "Report: " + "Journey " + dateOfReport + " stored in incorrect format", e);
            }
        }
        avgMpg = totalMpg / mpgDataCount;
    }

    public String getDateOfReport() {
        return dateOfReport;
    }

    public Map<String, Map<String, Object>> getJourneyData() {
        return journeyData;
    }

    public double getAvgMpg() {
        return avgMpg;
    }
}
