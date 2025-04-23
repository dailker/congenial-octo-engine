package com.humber.Tasky.repository;

import com.humber.Tasky.model.File;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileRepository extends MongoRepository<File, String> {
}
