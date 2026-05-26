package com.igirepay.lab2_jdbc.dao;

import java.sql.SQLException;

public interface GenericDAO<T> {
    int save(T entity) throws SQLException;
    T findById(int id) throws SQLException;
    void update(T entity) throws SQLException;
    void delete(int id) throws SQLException;
}
