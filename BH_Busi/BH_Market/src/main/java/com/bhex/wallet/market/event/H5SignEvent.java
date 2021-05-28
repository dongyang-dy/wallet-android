package com.bhex.wallet.market.event;

import com.bhex.wallet.market.model.H5Sign;

import java.util.List;

public class H5SignEvent {
    public List<H5Sign> h5Sign;
    public String data;

    public H5SignEvent(List<H5Sign> h5Sign, String data) {
        this.h5Sign = h5Sign;
        this.data = data;
    }

}
