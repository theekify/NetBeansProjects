package com.hotelapp.dao;

import com.hotelapp.model.Room;
import com.hotelapp.model.RoomType;
import com.hotelapp.util.DBConnectionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomDAOImpl implements RoomDAO {

    private Connection getConn() {
        return DBConnectionManager.getInstance().getConnection();
    }

    @Override
    public Room save(Room room) {
        String sql = "INSERT INTO rooms (room_number, room_type_id, floor_number, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, room.getRoomNumber());
            ps.setInt(2, room.getRoomTypeId());
            if (room.getFloorNumber() != null) ps.setInt(3, room.getFloorNumber()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, room.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) room.setId(keys.getInt(1));
            }
            return room;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save room: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Room room) {
        String sql = "UPDATE rooms SET room_number=?, room_type_id=?, floor_number=?, status=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, room.getRoomNumber());
            ps.setInt(2, room.getRoomTypeId());
            if (room.getFloorNumber() != null) ps.setInt(3, room.getFloorNumber()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, room.getStatus().name());
            ps.setInt(5, room.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update room: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM rooms WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete room: " + e.getMessage(), e);
        }
    }

    @Override
    public Room findById(int id) {
        String sql = "SELECT r.*, rt.type_name, rt.base_price, rt.capacity, rt.description " +
                     "FROM rooms r JOIN room_types rt ON r.room_type_id = rt.id WHERE r.id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find room: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Room> findAll() {
        String sql = "SELECT r.*, rt.type_name, rt.base_price, rt.capacity, rt.description " +
                     "FROM rooms r JOIN room_types rt ON r.room_type_id = rt.id ORDER BY r.room_number";
        List<Room> rooms = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rooms.add(mapRow(rs));
            return rooms;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch rooms: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        // A room is available if it's not MAINTENANCE, and has no RESERVED/CHECKED_IN
        // booking whose date range overlaps [checkIn, checkOut).
        String sql = "SELECT r.*, rt.type_name, rt.base_price, rt.capacity, rt.description " +
                     "FROM rooms r JOIN room_types rt ON r.room_type_id = rt.id " +
                     "WHERE r.status != 'MAINTENANCE' AND r.id NOT IN ( " +
                     "  SELECT b.room_id FROM bookings b " +
                     "  WHERE b.status IN ('RESERVED','CHECKED_IN') " +
                     "  AND b.check_in_date < ? AND b.check_out_date > ? " +
                     ") ORDER BY r.room_number";
        List<Room> rooms = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(checkOut));
            ps.setDate(2, Date.valueOf(checkIn));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rooms.add(mapRow(rs));
            }
            return rooms;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch available rooms: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatus(int roomId, Room.Status status) {
        String sql = "UPDATE rooms SET status=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update room status: " + e.getMessage(), e);
        }
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getInt("id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setRoomTypeId(rs.getInt("room_type_id"));
        int floor = rs.getInt("floor_number");
        room.setFloorNumber(rs.wasNull() ? null : floor);
        room.setStatus(Room.Status.valueOf(rs.getString("status")));

        RoomType rt = new RoomType();
        rt.setId(rs.getInt("room_type_id"));
        rt.setTypeName(rs.getString("type_name"));
        rt.setBasePrice(rs.getBigDecimal("base_price"));
        rt.setCapacity(rs.getInt("capacity"));
        rt.setDescription(rs.getString("description"));
        room.setRoomType(rt);

        return room;
    }
}
