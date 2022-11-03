package com.csye.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.csye.webapp.model.Document;
import com.csye.webapp.model.User;

import javax.persistence.Id;
import java.util.List;

@Repository
public interface DocRepository extends JpaRepository<Document, Integer> {

    public Document save(Document document);

    public Document findById(String id);

    public List<Document> findByName(String name);

    void deleteById(String id);

    List<Document> findAllByUserid(String id);

    // public Document save(Document document);

    // public Document findById(String id);

    // public void deleteById(String id);

    // List<Document> findAllByUserid(String user_id);

}