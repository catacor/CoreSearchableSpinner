package com.catacore.coresearchablespinner.spinnerItem.model;

import java.util.ArrayList;

public class SearchableItem {
    private String displayText;
    private ArrayList<String> tags;

    public SearchableItem(String displayText, ArrayList<String> tags) {
        this.displayText = displayText;
        this.tags = tags;
    }

    public SearchableItem(String displayText) {
        this.displayText = displayText;
        this.tags = new ArrayList<>();
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public boolean equal(SearchableItem second)
    {
        if(!displayText.equals(second.displayText))
            return false;
        if(tags.size()!= second.tags.size())
            return false;

        for(int i=0;i<tags.size();i++)
        {
            if(!tags.get(i).equals(second.tags.get(i)))
                return false;
        }

        return true;
    }
}
