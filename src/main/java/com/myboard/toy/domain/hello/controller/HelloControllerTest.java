package com.myboard.toy.domain.hello.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Controller
//@RequestMapping("/hello")
@RequiredArgsConstructor
public class HelloControllerTest {
    /*
    private final HelloRepositoryTest helloRepository;
    private final FileStore fileStore;
    */

    //@GetMapping("")
    public String hello(){
        return "hello";
    }
    
    // 새로운 Hello 객체를 만들기 위해 form 반환
    //@GetMapping("/new")
    /*
    private String newHello(@ModelAttribute HelloForm form, RedirectAttributes  redirectAttributes) throws IOException {
        return "hello/hello-form";
    }

     */

    /*
        파일 등록 기능
     */
    /*

    //@PostMapping("/new")
    public String saveHello(@ModelAttribute HelloForm form,RedirectAttributes redirectAttributes) throws IOException {

        UploadFileOfBoard attachFile = fileStore.storeFile(form.getAttachFile());

        List<UploadFileOfBoard> storeImageFiles = fileStore.storeFiles(form.getImageFiles());

        //DB에 저장

        Hello hello = new Hello(form.getHelloName(), attachFile,storeImageFiles);

        helloRepository.save(hello);

        redirectAttributes.addAttribute("helloId", hello.getId());

        return "redirect:/hello/{helloId}";
    }
    */

    /*
        조회 시 출력
     */
    /*

    //@GetMapping("/{id}")
    public String hellos(@PathVariable Long id, Model model){
        Hello hello = helloRepository.findById(id);
        model.addAttribute("hello",hello);
        return "hello/hello-view";
    }
     */

    /*

    //@ResponseBody
    //@GetMapping("/images/{filename}")
    public Resource downloadImage(@PathVariable String filename) throws MalformedURLException {
        return new UrlResource("file:"+fileStore.getFullPath(filename));
    }
    */
    /*
    //@GetMapping("/attach/{id}")
    private ResponseEntity<Resource> downloadAttach(@PathVariable Long id) throws MalformedURLException {

        Hello hello = helloRepository.findById(id);

        String storeFileName = hello.getAttachFile().getStoreFileName();
        String uploadFileName = hello.getAttachFile().getUploadFileName();

        UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(storeFileName));

        log.info("uploadFileName={}",uploadFileName);

        String encodedUploadFileName = UriUtils.encode(uploadFileName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,contentDisposition)
                .body(resource);
    }
    */
}

