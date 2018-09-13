package com.example.user.divein;

public class WishList {

    public String mTitle;
    public String mAddress;

    public WishList(String mTitle, String mAddress) {
        this.mTitle = mTitle;
        this.mAddress = mAddress;
    }

    public WishList() {}

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmAddress() {
        return mAddress;
    }

    public void setmAddress(String mAddress) {
        this.mAddress = mAddress;
    }
}
