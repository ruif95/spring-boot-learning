package com.example.lineserver.content.controller;

import com.example.lineserver.content.exception.BeyondEndOfFileException;
import com.example.lineserver.content.service.LineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/lines")
public class LineController {

    private final LineService service;

    @Autowired
    public LineController(LineService service) {
        this.service = service;
    }

    /**
     * Rest API entry-point for getting a specific line from the text storage.
     *
     * @param index Line number to get.
     * @return the line value.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{index}")
    public String get(@PathVariable Integer index) {
        String line;

        try {
            line = service.getLineByIndex(index);
        } catch (BeyondEndOfFileException e) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, e.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
        return line;
    }
}
