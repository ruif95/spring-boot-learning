package com.example.spring_boot_learning.content.repository;

import com.example.spring_boot_learning.content.model.Content;
import org.springframework.data.repository.ListCrudRepository;

public interface ContentRepository extends ListCrudRepository<Content, Integer> {
}
