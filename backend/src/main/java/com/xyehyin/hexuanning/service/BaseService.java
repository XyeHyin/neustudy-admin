package com.xyehyin.hexuanning.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public abstract class BaseService<T, ID> {
    protected abstract JpaRepository<T, ID> getRepository();

    public T save(T entity) {
        return getRepository().save(entity);
    }

    public T findById(ID id) {
        return getRepository().findById(id).orElse(null);
    }

    public List<T> findAll() {
        return getRepository().findAll();
    }

    public boolean deleteById(ID id) {
        if (getRepository().existsById(id)) {
            getRepository().deleteById(id);
            return true;
        }
        return false;
    }

    public boolean batchDelete(List<ID> ids) {
        getRepository().deleteAllById(ids);
        return true;
    }

    public Page<T> findPage(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id"));
        return getRepository().findAll(pageable);
    }

    public boolean delete(ID id){
        getRepository().deleteById(id);
        return true;
    }
}
