package com.example.servingwebcontent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PhotoRepository extends JpaRepository<Photo, Integer> {

//    @Query("select photo from PHOTO photo where concat(photo.albumId, '') like  ?1")
    public Iterable<Photo> findPhotoByAlbumId(int albumId);

}
