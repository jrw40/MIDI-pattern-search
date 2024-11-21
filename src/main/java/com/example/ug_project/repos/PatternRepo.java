package com.example.ug_project.repos;

import com.example.ug_project.model.Pattern;
import com.example.ug_project.model.SongData;
import org.springframework.data.repository.CrudRepository;

public interface PatternRepo extends CrudRepository<Pattern, Integer> {

    Pattern findById(int Id);

}
