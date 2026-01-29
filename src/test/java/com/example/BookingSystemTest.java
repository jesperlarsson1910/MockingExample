package com.example;


import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    //Simulate return values from required interfaces
    @BeforeEach
    public void setupInterfaces(TestInfo info){
        if (info.getDisplayName().contains("null")){
            return; //avoid unnecessary stubbing
        }
        when(timeProvider.getCurrentTime()).thenReturn(NOW);
    }

    //Simulate room being available so both behaviours can be tested
    public void roomAvailable(boolean available){
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(room.isAvailable(START,END)).thenReturn(available);
    }

    //Provides different combos of null parameters for BookingSystem#bookRoom(String, LocalDateTime, LocalDateTime)
    private static Stream<Arguments> bookingRoomNullParameterProvider() {
        return Stream.of(
                Arguments.of(null, START, END),
                Arguments.of(ROOM_ID, null, END),
                Arguments.of(ROOM_ID, START, null),
                Arguments.of(null, null, END),
                Arguments.of(ROOM_ID, null, null),
                Arguments.of(null, null, null)
        );
    }

    //Provide different combos of null parameters for BookingSystem#getAvailableRooms(LocalDateTime, LocalDateTime)
    private static Stream<Arguments> getAvailableRoomsNullParameterProvider() {
        return Stream.of(
                Arguments.of(null, END),
                Arguments.of(START, null),
                Arguments.of(null, null)
        );
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

    @ParameterizedTest
    @DisplayName("Should throw exception when parameters are null (bookingRoom)")
    @MethodSource("bookingRoomNullParameterProvider")
    void nullParameters_bookRoom(String roomId, LocalDateTime start, LocalDateTime end) {
        assertThatThrownBy(() -> bookingSystem.bookRoom(roomId, start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bokning kräver giltiga start- och sluttider samt rum-id");
    }

    @ParameterizedTest
    @DisplayName("Should Throw exception if any parameter is null getAvailableRooms")
    @MethodSource("getAvailableRoomsNullParameterProvider")
    void nullParameters_getAvailableRooms(LocalDateTime startTime, LocalDateTime endTime) {
        assertThatThrownBy(() -> bookingSystem.getAvailableRooms(startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Måste ange både start- och sluttid");
    }

    @Test
    @DisplayName("Should throw exception when booking is in the past")
    public void tryBookingInPast(){
        LocalDateTime past = NOW.minusDays(7);

        assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID, past, END))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kan inte boka tid i dåtid");
    }

    @Test
    @DisplayName("Should throw exception when start is after end")
    void tryBookingAfterEnd() {
        LocalDateTime earlyEnd = START.minusDays(1);

        assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID, START, earlyEnd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sluttid måste vara efter starttid");
    }

    @Test
    @DisplayName("Should throw exception when room doesn't exist")
    void tryBookingNonExistentRoom(){
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID, START, END))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rummet existerar inte");
    }
}