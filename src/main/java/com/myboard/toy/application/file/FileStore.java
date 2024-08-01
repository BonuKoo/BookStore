package com.myboard.toy.application.file;

import com.myboard.toy.domain.file.board.UploadFileOfBoard;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FileStore {

    //Board용 파일 저장 경로
    @Value("${file.dir}")
    private String fileDir;

    public String getFullPath(String filename){ //파일 저장 경로 + 파일이름
        return fileDir + filename;
    }

    //이미지 하나만 저장
    public UploadFileOfBoard storeFile(MultipartFile multipartFile) throws IOException {

        if (multipartFile.isEmpty()){
            return null;
        }

        //서버에서 받은 originalFile 이름
        String originalFilename = multipartFile.getOriginalFilename();
        //파일 이름 -> UUID + 확장자
        String storeFileName = createStoreFileName(originalFilename);
        //파일 경로
        String filePath = getFullPath(storeFileName);
        //파일 형식
        String fileType = multipartFile.getContentType();
        //파일 크기
        long fileSize = multipartFile.getSize();
        File file = new File(filePath);
        //저장
        multipartFile.transferTo(file);

        return new UploadFileOfBoard(originalFilename,storeFileName,filePath,fileType,fileSize);
    }


    //이미지 여러 개 저장
    public List<UploadFileOfBoard> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
        List<UploadFileOfBoard> storeFileResult = new ArrayList<>();

        for (MultipartFile multipartFile : multipartFiles){
            if (!multipartFile.isEmpty()){
                storeFileResult.add(storeFile(multipartFile));
            }
        }
        return storeFileResult;
    }

    /*
        extractMethod
     */
    
    // 확장자 명
    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }
    // 파일 이름 (UUID + 확장명)
    private String  createStoreFileName(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        //확장자 명
        String ext = extractExt(originalFilename);
        return uuid + "." + ext;
    }



}
