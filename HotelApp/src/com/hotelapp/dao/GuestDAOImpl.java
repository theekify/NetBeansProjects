package com.hotelapp.dao;

import com.hotelapp.model.Guest;
import com.hotelapp.util.DBConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GuestDAOImpl implements GuestDAO {

    private Connection getConn() {
        return DBConnectionManager.getInstance().getConnection();
    }

    @Override
    public Guest save(Guest guest) {
        String sql = "INSERT INTO guests (full_name, nic_passport, phone, email, address) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, guest.getFullName());
            ps.setString(2, guest.getNicPassport());
            ps.setString(3, guest.getPhone());
            ps.setString(4, guest.getEmail());
            ps.setString(5, guest.getAddress());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    guest.setId(keys.getInt(1));
                }
            }
            return guest;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save guest: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Guest guest) {
        String sql = "UPDATE guests SET full_name=?, nic_passport=?, phone=?, email=?, address=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, guest.getFullName());
            ps.setString(2, guest.getNicPassport());
            ps.setString(3, guest.getPhone());
            ps.setString(4, guest.getEmail());
            ps.setString(5, guest.getAddress());
            ps.setInt(6, guest.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update guest: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM guests WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete guest: " + e.getMessage(), e);
        }
    }

    @Override
    public Guest findById(int id) {
        String sql = "SELECT * FROM guests WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find guest: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Guest> findAll() {
        String sql = "SELECT * FROM guests ORDER BY full_name";
        List<Guest> guests = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                guests.add(mapRow(rs));
            }
            return guests;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch guests: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Guest> search(String keyword) {
        String sql = "SELECT * FROM guests WHERE full_name LIKE ? OR nic_passport LIKE ? OR phone LIKE ? ORDER BY full_name";
        List<Guest> guests = new ArrayList<>();
        String like = "%" + keyword + "%";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    guests.add(mapRow(rs));
                }
            }
            return guests;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search guests: " + e.getMessage(), e);
        }
    }

    private Guest mapRow(ResultSet rs) throws SQLException {
        Guest g = new Guest();
        g.setId(rs.getInt("id"));
        g.setFullName(rs.getString("full_name"));
        g.setNicPassport(rs.getString("nic_passport"));
        g.setPhone(rs.getString("phone"));
        g.setEmail(rs.getString("email"));
        g.setAddress(rs.getString("address"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            g.setCreatedAt(ts.toLocalDateTime());
        }
        return g;
    }
}
