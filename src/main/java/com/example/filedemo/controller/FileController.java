package com.example.filedemo.controller;

import com.example.filedemo.model.UploadedPictures;
import com.example.filedemo.payload.UploadFileResponse;
import com.example.filedemo.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("mobileNumber") String mobileNumber, @RequestParam("file") MultipartFile file) {
        File directory = new File("C:/Users/ak185292/uploads/" + mobileNumber);
        if (! directory.exists()){
            directory.mkdir();
        }

        String fileName = fileStorageService.storeFile(mobileNumber, file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/"+mobileNumber+"/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    @PostMapping("/uploadMultipleFiles")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("mobileNumber") String mobileNumber, @RequestParam("files") MultipartFile[] files) {

        List<UploadFileResponse> uploadFileResponses = Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(mobileNumber, file))
                .collect(Collectors.toList());

        if(!uploadFileResponses.isEmpty()) {
            UploadedPictures up = new UploadedPictures();
            up.setMobileNumber(mobileNumber);
            up.setUploadCount(uploadFileResponses.size());
        }

        return uploadFileResponses;
    }

    @GetMapping("/downloadFile/{mobileNumber}/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String mobileNumber, @PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(mobileNumber, fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}
