package com.lindar.bingo.controller;

import com.lindar.bingo.helper.StripHelper;
import com.lindar.bingo.model.Ticket;
import com.lindar.bingo.service.StripGeneratorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("bingo")
public class BingoController {

    private final Logger log = LogManager.getLogger(getClass());

    @Autowired
    private StripGeneratorService stripGeneratorService;

    @GetMapping(value = "/generateStrip", produces = "text/formatted")
    public ResponseEntity<String> generateStrips() {
        List<Ticket> strip = stripGeneratorService.generateStrip();
        return ResponseEntity.ok(StripHelper.displayTickets(strip));
    }
}