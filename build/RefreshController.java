package com.pivotal.example.xd.controller;

import com.pivotal.example.xd.configsvc.RefreshAppplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class RefreshController {

    @Autowired
    private RefreshAppplicationContext refreshAppplicationContext;

    @Autowired
    private RefreshScope refreshScope;

    @RequestMapping(path = "/refreshall", method = RequestMethod.GET)
    public String refresh() {
        refreshScope.refreshAll();
        refreshAppplicationContext.refreshctx();
        return "Refreshed";
    }
}