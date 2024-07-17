package com.myboard.toy.domain.hello.controller;

import com.myboard.toy.domain.file.FileStore;
import com.myboard.toy.domain.file.UploadFileOfBoard;
import com.myboard.toy.domain.hello.Hello;
import com.myboard.toy.domain.hello.dto.HelloForm;
import com.myboard.toy.domain.hello.repository.HelloRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/hello")
public class HelloControllerV1 {

    private final HelloRepository helloRepository;
    private final FileStore fileStore;

    @GetMapping("")
    public String hello(){
        return "hello";
    }

    public HelloControllerV1(HelloRepository helloRepository, FileStore fileStore) {
        this.helloRepository = helloRepository;
        this.fileStore = fileStore;
    }

    @GetMapping("/new")
    private String newHello(@ModelAttribute HelloForm form, RedirectAttributes redirectAttributes){
        return "/hello/hello-form";
    }

    @PostMapping("/new")
    public String saveHello(@ModelAttribute HelloForm form, RedirectAttributes redirectAttributes) throws IOException, IOException {

        UploadFileOfBoard attachFile = fileStore.storeFile(form.getAttachFile());
        List<UploadFileOfBoard> storeImageFiles = fileStore.storeFiles(form.getImageFiles());

        Hello hello = new Hello(form.getHelloName(), attachFile, storeImageFiles);
        helloRepository.save(hello);

        redirectAttributes.addAttribute("helloId", hello.getId());

        return "redirect:/hello/{helloId}";
    }

    @GetMapping("/{id}")
    public String hellos(@PathVariable Long id, Model model) {
        Hello hello = helloRepository.findById(id).orElse(null);
        model.addAttribute("hello", hello);
        return "hello/hello-view";
    }

    @ResponseBody
    @GetMapping("/images/{filename}")
    public Resource downloadImage(@PathVariable String filename) throws MalformedURLException {
        return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

    @GetMapping("/attach/{id}")
    private ResponseEntity<Resource> downloadAttach(@PathVariable Long id) throws MalformedURLException {
        Hello hello = helloRepository.findById(id).orElse(null);

        if (hello == null || hello.getAttachFile() == null) {
            return ResponseEntity.notFound().build();
        }

        String storeFileName = hello.getAttachFile().getStoreFileName();
        String uploadFileName = hello.getAttachFile().getUploadFileName();

        UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(storeFileName));
        log.info("uploadFileName={}", uploadFileName);

        String encodedUploadFileName = UriUtils.encode(uploadFileName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

}
