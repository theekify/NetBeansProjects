package com.hotelapp.dao;

import com.hotelapp.model.User;
import com.hotelapp.util.DBConnectionManager;

import java.sql.*;

public class UserDAOImpl implements UserDAO {

    private Connection getConn() {
        return DBConnectionManager.getInstance().getConnection();
    }

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setFullName(rs.getString("full_name"));
                    u.setRole(User.Role.valueOf(rs.getString("role")));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
                    return u;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user: " + e.getMessage(), e);
        }
    }
}
