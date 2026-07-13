package com.hotelapp.dao;

import com.hotelapp.model.*;
import com.hotelapp.util.DBConnectionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingDAOImpl implements BookingDAO {

    private Connection getConn() {
        return DBConnectionManager.getInstance().getConnection();
    }

    @Override
    public Booking save(Booking booking) {
        String sql = "INSERT INTO bookings (guest_id, room_id, check_in_date, check_out_date, status, num_guests) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, booking.getGuestId());
            ps.setInt(2, booking.getRoomId());
            ps.setDate(3, Date.valueOf(booking.getCheckInDate()));
            ps.setDate(4, Date.valueOf(booking.getCheckOutDate()));
            ps.setString(5, booking.getStatus().name());
            ps.setInt(6, booking.getNumGuests());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) booking.setId(keys.getInt(1));
            }
            return booking;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save booking: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Booking booking) {
        String sql = "UPDATE bookings SET guest_id=?, room_id=?, check_in_date=?, check_out_date=?, " +
                     "status=?, num_guests=?, actual_check_in=?, actual_check_out=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, booking.getGuestId());
            ps.setInt(2, booking.getRoomId());
            ps.setDate(3, Date.valueOf(booking.getCheckInDate()));
            ps.setDate(4, Date.valueOf(booking.getCheckOutDate()));
            ps.setString(5, booking.getStatus().name());
            ps.setInt(6, booking.getNumGuests());
            ps.setTimestamp(7, booking.getActualCheckIn() != null ? Timestamp.valueOf(booking.getActualCheckIn()) : null);
            ps.setTimestamp(8, booking.getActualCheckOut() != null ? Timestamp.valueOf(booking.getActualCheckOut()) : null);
            ps.setInt(9, booking.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update booking: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM bookings WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete booking: " + e.getMessage(), e);
        }
    }

    @Override
    public Booking findById(int id) {
        String sql = baseSelect() + " WHERE b.id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find booking: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Booking> findAll() {
        String sql = baseSelect() + " ORDER BY b.check_in_date DESC";
        return runListQuery(sql, ps -> {});
    }

    @Override
    public List<Booking> findByStatus(Booking.Status status) {
        String sql = baseSelect() + " WHERE b.status=? ORDER BY b.check_in_date";
        return runListQuery(sql, ps -> ps.setString(1, status.name()));
    }

    @Override
    public List<Booking> findTodayCheckIns() {
        String sql = baseSelect() + " WHERE b.check_in_date = CURDATE() AND b.status='RESERVED' ORDER BY g.full_name";
        return runListQuery(sql, ps -> {});
    }

    @Override
    public List<Booking> findTodayCheckOuts() {
        String sql = baseSelect() + " WHERE b.check_out_date = CURDATE() AND b.status='CHECKED_IN' ORDER BY g.full_name";
        return runListQuery(sql, ps -> {});
    }

    @Override
    public boolean hasOverlappingBooking(int roomId, LocalDate checkIn, LocalDate checkOut, Integer excludeBookingId) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE room_id=? AND status IN ('RESERVED','CHECKED_IN') " +
                     "AND check_in_date < ? AND check_out_date > ?" +
                     (excludeBookingId != null ? " AND id != ?" : "");
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setDate(2, Date.valueOf(checkOut));
            ps.setDate(3, Date.valueOf(checkIn));
            if (excludeBookingId != null) ps.setInt(4, excludeBookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check booking overlap: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatus(int bookingId, Booking.Status status) {
        String sql = "UPDATE bookings SET status=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update booking status: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private String baseSelect() {
        return "SELECT b.*, g.full_name, g.nic_passport, g.phone, g.email, g.address, " +
               "r.room_number, r.room_type_id, rt.type_name, rt.base_price, rt.capacity, rt.description " +
               "FROM bookings b " +
               "JOIN guests g ON b.guest_id = g.id " +
               "JOIN rooms r ON b.room_id = r.id " +
               "JOIN room_types rt ON r.room_type_id = rt.id";
    }

    @FunctionalInterface
    private interface ParamSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    private List<Booking> runListQuery(String sql, ParamSetter setter) {
        List<Booking> bookings = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) bookings.add(mapRow(rs));
            }
            return bookings;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch bookings: " + e.getMessage(), e);
        }
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getInt("id"));
        b.setGuestId(rs.getInt("guest_id"));
        b.setRoomId(rs.getInt("room_id"));
        b.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        b.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        Timestamp actualIn = rs.getTimestamp("actual_check_in");
        if (actualIn != null) b.setActualCheckIn(actualIn.toLocalDateTime());
        Timestamp actualOut = rs.getTimestamp("actual_check_out");
        if (actualOut != null) b.setActualCheckOut(actualOut.toLocalDateTime());
        b.setStatus(Booking.Status.valueOf(rs.getString("status")));
        b.setNumGuests(rs.getInt("num_guests"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) b.setCreatedAt(created.toLocalDateTime());

        Guest guest = new Guest();
        guest.setId(rs.getInt("guest_id"));
        guest.setFullName(rs.getString("full_name"));
        guest.setNicPassport(rs.getString("nic_passport"));
        guest.setPhone(rs.getString("phone"));
        guest.setEmail(rs.getString("email"));
        guest.setAddress(rs.getString("address"));
        b.setGuest(guest);

        Room room = new Room();
        room.setId(rs.getInt("room_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setRoomTypeId(rs.getInt("room_type_id"));
        RoomType rt = new RoomType();
        rt.setId(rs.getInt("room_type_id"));
        rt.setTypeName(rs.getString("type_name"));
        rt.setBasePrice(rs.getBigDecimal("base_price"));
        rt.setCapacity(rs.getInt("capacity"));
        rt.setDescription(rs.getString("description"));
        room.setRoomType(rt);
        b.setRoom(room);

        return b;
    }
}
