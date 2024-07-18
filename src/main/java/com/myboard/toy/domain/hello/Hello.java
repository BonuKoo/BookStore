package com.myboard.toy.domain.hello;

//@Data
//@NoArgsConstructor @AllArgsConstructor
//@Entity
public class Hello {

    /*

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String helloName;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "attach_file_id")
    private UploadFileOfBoard attachFile;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "hello_id") //외부키 생성
    private List<UploadFileOfBoard> imageFiles = new ArrayList<>();

    public Hello(String helloName, UploadFileOfBoard attachFile, List<UploadFileOfBoard> imageFiles) {
        this.helloName = helloName;
        this.attachFile = attachFile;
        this.imageFiles = imageFiles;
    }

     */
}
