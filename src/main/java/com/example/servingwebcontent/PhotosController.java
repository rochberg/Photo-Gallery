package com.example.servingwebcontent;
import com.example.servingwebcontent.exceptionhandlers.StorageException;
import com.example.servingwebcontent.exceptionhandlers.StorageFileNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PhotosController {

    @Value("${photos.dir}")
    private String photos_dir;

    @Autowired
    private PhotoRepository photoRepository;

    private Photo[] _photos;

    public void init() {
        photoRepository.deleteAll();
        try {
            Files.createDirectories(Path.of(photos_dir));
            downloadAll();
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize src/main/resources/static/photos_dir", e);
        }
        catch (Exception e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    @RequestMapping(value="/")
    public String showPhotos(Model model) throws Exception{
        model.addAttribute("photos", photoRepository.findAll());
        addAlbumIds(model);
        return "index";
    }

    private void addAlbumIds(Model model) {
        List<Integer> album_options = photoRepository.findAll().parallelStream().map(Photo::getAlbumId).distinct().collect(Collectors.toList());
        model.addAttribute("albums", album_options);
    }

    @ResponseBody
    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    public Resource loadAsResource(String filename) {
        try {
            Path file = Path.of(photos_dir+"/"+filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);
            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @GetMapping("/downloadAll")
    public String downloadAll() throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();
        String json = restTemplate.getForObject("https://shield-j-test.s3.amazonaws.com/photo.txt", String.class);
        try {
            this._photos = mapper.readValue(json, Photo[].class);
            downloadPhotos();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "index";
    }

    private void downloadPhotos() throws IOException {
        for (Photo photo : _photos) {
            String photoName = "/photo" + photo.getId() + ".jpg";
            photo.setPath(photos_dir +photoName);
            savePhotoFromURL(photo);
        }
    }

    private void savePhotoFromURL(Photo photo) throws IOException {
        URL imageUrl = new URL(photo.getUrl());
        InputStream inputStream = imageUrl.openStream();
        OutputStream outputStream = new FileOutputStream(photo.getPathStr());
        byte[] byteArray = new byte[2048];
        int length;

        while ((length = inputStream.read(byteArray)) != -1) {
            outputStream.write(byteArray, 0, length);
        }
        inputStream.close();
        outputStream.close();
        addAttributes(photo);
    }

    private void addAttributes(Photo photo){
        photo.setDownloadedDate(new Date());
        File f = new File(photo.getPathStr());
        photo.setSize(f.length()/1000);
        photo.setPath("photos_dir/photo"+photo.getId()+".jpg");
        photo.setBuiltUri(
                MvcUriComponentsBuilder.fromMethodName(PhotosController.class,
                        "serveFile", photo.getPath().getFileName().toString()).build().toUri().toString());
        photoRepository.save(photo);
    }

    @RequestMapping("/filterByAlbum")
    public String filterByAlbum(@RequestParam("albumId") int albumId, Model model){
        model.addAttribute("photos", photoRepository.findPhotoByAlbumId(albumId));
        addAlbumIds(model);
        return "index";
    }



}
