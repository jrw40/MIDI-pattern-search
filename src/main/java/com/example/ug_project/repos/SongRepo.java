package com.example.ug_project.repos;

import com.example.ug_project.model.SongData;
import org.springframework.data.repository.CrudRepository;


public interface SongRepo extends CrudRepository<SongData, Integer> {

    SongData findByName(String name);

}
