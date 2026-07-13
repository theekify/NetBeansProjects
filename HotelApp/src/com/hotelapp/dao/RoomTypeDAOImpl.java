package com.hotelapp.dao;

import com.hotelapp.model.RoomType;
import com.hotelapp.util.DBConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomTypeDAOImpl implements RoomTypeDAO {

    private Connection getConn() {
        return DBConnectionManager.getInstance().getConnection();
    }

    @Override
    public RoomType save(RoomType roomType) {
        String sql = "INSERT INTO room_types (type_name, base_price, capacity, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, roomType.getTypeName());
            ps.setBigDecimal(2, roomType.getBasePrice());
            ps.setInt(3, roomType.getCapacity());
            ps.setString(4, roomType.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) roomType.setId(keys.getInt(1));
            }
            return roomType;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save room type: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(RoomType roomType) {
        String sql = "UPDATE room_types SET type_name=?, base_price=?, capacity=?, description=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, roomType.getTypeName());
            ps.setBigDecimal(2, roomType.getBasePrice());
            ps.setInt(3, roomType.getCapacity());
            ps.setString(4, roomType.getDescription());
            ps.setInt(5, roomType.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update room type: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM room_types WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete room type: " + e.getMessage(), e);
        }
    }

    @Override
    public RoomType findById(int id) {
        String sql = "SELECT * FROM room_types WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find room type: " + e.getMessage(), e);
        }
    }

    @Override
    public List<RoomType> findAll() {
        String sql = "SELECT * FROM room_types ORDER BY base_price";
        List<RoomType> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch room types: " + e.getMessage(), e);
        }
    }

    private RoomType mapRow(ResultSet rs) throws SQLException {
        RoomType rt = new RoomType();
        rt.setId(rs.getInt("id"));
        rt.setTypeName(rs.getString("type_name"));
        rt.setBasePrice(rs.getBigDecimal("base_price"));
        rt.setCapacity(rs.getInt("capacity"));
        rt.setDescription(rs.getString("description"));
        return rt;
    }
}
