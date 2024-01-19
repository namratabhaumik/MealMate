package com.groupeleven.mealmate;

public class CategoryData {
    private String categoryName;

    private String categoryUrl;

    public CategoryData(String categoryName, String categoryUrl) {
        this.categoryName = categoryName;
        this.categoryUrl = categoryUrl;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryUrl() {
        return categoryUrl;
    }

    public void setCategoryUrl(String categoryUrl) {
        this.categoryUrl = categoryUrl;
    }
}
