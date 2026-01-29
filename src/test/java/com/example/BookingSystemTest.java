package com.example;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link BookingSystem}
 * <p>
 * - {@link BookingSystem#bookRoom(String, LocalDateTime, LocalDateTime)}
 * <p>
 * - {@link BookingSystem#cancelBooking(String)}
 * <p>
 * - {@link BookingSystem#getAvailableRooms(LocalDateTime, LocalDateTime)}
 */

@ExtendWith(MockitoExtension.class)
public class BookingSystemTest {

    //Mock classes
    @Mock
    private NotificationService notificationService;
    @Mock
    private Room room;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private TimeProvider timeProvider;



    @InjectMocks
    private BookingSystem bookingSystem;

    //Default time values
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime START = LocalDateTime.of(2026, 12, 22, 11, 0);
    private static final LocalDateTime END = LocalDateTime.of(2027, 1, 2, 14, 0);

    //Default room ID
    private static final String ROOM_ID = "1001";

    //Help methods
    //Simulate return values from interfaces
    @BeforeEach
    public void setupInterfaces(){
        when(timeProvider.getCurrentTime()).thenReturn(NOW);
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
    }

    //Simulate room being available so both behaviours can be tested
    public void roomAvailable(boolean available){
        when(room.isAvailable(START,END)).thenReturn(available);
    }

    /**
     * Test for {@link BookingSystem#bookRoom(String, LocalDateTime, LocalDateTime)}
     *<p>
     * - Valid parameters and room available
     * <p>
     * - Valid parameters and room unavailable
     * <p>
     * - Invalid parameters:
     *  - Null
     *  - Booking in the past
     *  - Booking start is after end
     *  - Room doesn't exist
     */


    @Test
    @DisplayName("Should return true with valid parameters and available room")
    public void tryBookingRoomAllGreen(){
        roomAvailable(true);

        Boolean booking = bookingSystem.bookRoom(ROOM_ID, START, END); //Valid booking
        assertThat(booking).isTrue();

        //Room should only be added once
        verify(room).addBooking(any(Booking.class));
        verify(roomRepository).save(room);
    }

    @Test
    @DisplayName("Should return false with valid parameters and unavailable room")
    public void tryBookingRoomAllGreenNoRoom(){
        roomAvailable(false);

        Boolean booking = bookingSystem.bookRoom(ROOM_ID, START, END); //Valid booking
        assertThat(booking).isFalse();

        //Room should not be saved
        verify(room,never()).addBooking(any(Booking.class));
        verify(roomRepository, never()).save(room);
    }
}
