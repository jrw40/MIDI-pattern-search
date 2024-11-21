package com.example.ug_project.repos;

import com.example.ug_project.model.Cluster;
import org.springframework.data.repository.CrudRepository;

public interface ClusterRepo extends CrudRepository<Cluster, Integer> {

    Cluster findById(int Id);

}
