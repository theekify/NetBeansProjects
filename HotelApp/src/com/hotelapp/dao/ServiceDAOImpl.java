package com.hotelapp.dao;

import com.hotelapp.model.Service;
import com.hotelapp.util.DBConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAOImpl implements ServiceDAO {

    private Connection getConn() {
        return DBConnectionManager.getInstance().getConnection();
    }

    @Override
    public Service save(Service service) {
        String sql = "INSERT INTO services (service_name, price) VALUES (?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, service.getServiceName());
            ps.setBigDecimal(2, service.getPrice());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) service.setId(keys.getInt(1));
            }
            return service;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save service: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Service service) {
        String sql = "UPDATE services SET service_name=?, price=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, service.getServiceName());
            ps.setBigDecimal(2, service.getPrice());
            ps.setInt(3, service.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update service: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM services WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete service: " + e.getMessage(), e);
        }
    }

    @Override
    public Service findById(int id) {
        String sql = "SELECT * FROM services WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find service: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Service> findAll() {
        String sql = "SELECT * FROM services ORDER BY service_name";
        List<Service> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch services: " + e.getMessage(), e);
        }
    }

    private Service mapRow(ResultSet rs) throws SQLException {
        Service s = new Service();
        s.setId(rs.getInt("id"));
        s.setServiceName(rs.getString("service_name"));
        s.setPrice(rs.getBigDecimal("price"));
        return s;
    }
}
