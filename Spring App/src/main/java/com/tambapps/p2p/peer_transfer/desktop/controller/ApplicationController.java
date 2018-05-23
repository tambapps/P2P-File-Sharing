package com.tambapps.p2p.peer_transfer.desktop.controller;

import com.tambapps.p2p.peer_transfer.desktop.model.FilePath;
import com.tambapps.p2p.peer_transfer.desktop.model.Peer;
import com.tambapps.p2p.peer_transfer.desktop.service.FileService;
import com.tambapps.p2p.peer_transfer.desktop.service.ReceiveService;
import com.tambapps.p2p.peer_transfer.desktop.service.SendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

@Controller
public class ApplicationController {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationController.class);

    private ConcurrentMap<Integer, FileService.FileTask> progressMap;
    private SendService sendService;
    private ReceiveService receiveService;

    public ApplicationController(ConcurrentMap<Integer, FileService.FileTask> progressMap, SendService sendService, ReceiveService receiveService) {
        this.progressMap = progressMap;
        this.sendService = sendService;
        this.receiveService = receiveService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "File Sharing App");
        model.addAttribute("message", "You can send/receive file with this app");
        return "index";

    }

    @GetMapping("/receive")
    public String receive(Model model) {
        model.addAttribute("peer", new Peer());
        return "peer";
    }

    @PostMapping("/receive")
    public String receiveSubmit(@Valid @ModelAttribute("peer") Peer peer, BindingResult bindingResult, Model model) {
        if (hasError(bindingResult, "ip", "port")) {
            return "peer";
        }
        ReceiveService.ReceiveTask receiveTask = null;
        try {
            receiveTask = receiveService.start(peer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to start receiveService", e);
        }
        model.addAttribute("currentTask", receiveTask);
        LOGGER.info("Started receive task successfully");
        return "receiving";
    }

    private boolean hasError(BindingResult bindingResult, String... fields) {
        boolean hasError = false;
        for (String field : fields) {
            if (bindingResult.hasFieldErrors(field)) {
                hasError = true;
            }
        }
        return hasError;
    }

    @GetMapping("/send")
    public String send(Model model) {
        model.addAttribute("filePath", new FilePath());
        return "selectFile";
    }


  @GetMapping("/progress/{id}")
  public ResponseEntity<Integer> getProgress(@PathVariable("id") int id) {
    FileService.FileTask task =  progressMap.get(id);
    if (task == null) {
      return ResponseEntity.ok(-1);
    }
    return ResponseEntity.ok(task.getProgress());
  }

    @PostMapping("/send")
    public String sendSubmit(@Valid @ModelAttribute("filePath") FilePath filePath, BindingResult bindingResult, Model model) {
        if (hasError(bindingResult, "path")) {
            return "selectFile";
        }
        File file = new File(filePath.getPath());
        if (!file.exists()) {
            bindingResult.rejectValue("path", "file.notFound", "the requested file was not found");
            return "selectFile";
        }
        SendService.SendTask sendTask;
        try {
            sendTask = sendService.start(filePath.getPath());
            model.addAttribute("currentTask", sendTask);
            LOGGER.info("Started send task successfully");
        } catch (IOException e) {
            throw new RuntimeException("Failed to start sendService", e);
        }

        return "sending";
    }
}
