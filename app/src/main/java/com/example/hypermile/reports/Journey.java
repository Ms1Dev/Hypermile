package com.example.hypermile.reports;


import com.example.hypermile.dataGathering.PollCompleteListener;

import java.util.ArrayList;

public class Journey implements PollCompleteListener {

    private final ArrayList<Attribute> attributes = new ArrayList<>();

    ArrayList<ArrayList<Object>> table = new ArrayList<>();

    public Journey() {
        attributes.add(new Attribute() {
            @Override
            public Object getData() {
                return table.size();
            }

            @Override
            public String getLabel() {
                return "ID";
            }

            @Override
            public Class<?> getType() {
                return Integer.class;
            }
        });
    }

    public void addAttribute (Attribute attribute) {
        attributes.add(attribute);
    }

    @Override
    public void pollingComplete() {
        addTableRow();
    }

    private void addTableRow() {
        ArrayList<Object> row = new ArrayList<>();

        for (Attribute attribute : attributes) {
            row.add(attribute.getData());
        }

        table.add(row);
    }
}
