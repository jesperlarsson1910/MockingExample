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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link BookingSystem}
 * <p>
 * - {@link BookingSystem#bookRoom(String, LocalDateTime, LocalDateTime)}
 * <p>
 * - {@link BookingSystem#getAvailableRooms(LocalDateTime, LocalDateTime)}
 * <p>
 * - {@link BookingSystem#cancelBooking(String)}
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

    //Default room and booking ID
    private static final String ROOM_ID = "1001";
    private static final String BOOKING_ID = "B1234";



    //Help methods
    //Simulate return values from required interfaces
    @BeforeEach
    public void setupInterfaces(TestInfo info){
        if (info.getDisplayName().contains("null") || info.getDisplayName().contains("getAvailableRooms")) {
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
                Arguments.of(null, START, null),
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


    /*
     * Test for BookingSystem#bookRoom(String, LocalDateTime, LocalDateTime)
     *
     * - Valid parameters and room available should book
     *
     * - Valid parameters and room available should book even if notification failed
     *
     * - Valid parameters and room unavailable shouldn't book
     *
     * - Invalid parameters:
     *  - Null
     *  - Booking in the past
     *  - Booking start is after end
     *  - Room doesn't exist
     */


    @Test
    @DisplayName("bookRoom: Should return true with valid parameters and available room")
    public void tryBookingRoomAllGreen(){
        roomAvailable(true);

        Boolean booking = bookingSystem.bookRoom(ROOM_ID, START, END); //Valid booking
        assertThat(booking).isTrue();

        //Room should only be added once
        verify(room).addBooking(any(Booking.class));
        verify(roomRepository).save(room);
    }

    @Test
    @DisplayName(("bookRoom: Should return true with valid parameters and available room even if notification failed"))
    void tryBookingRoomAllGreenNotificationFail() throws NotificationException {
        roomAvailable(true);

        doThrow(new NotificationException("NotificationService is down"))
                .when(notificationService).sendBookingConfirmation(any(Booking.class));

        boolean result = bookingSystem.bookRoom(ROOM_ID, START, END);

        assertThat(result).isTrue();

        verify(room).addBooking(any(Booking.class));
        verify(roomRepository).save(room);
    }

    @Test
    @DisplayName("bookRoom: Should return false with valid parameters and unavailable room")
    public void tryBookingRoomAllGreenNoRoom(){
        roomAvailable(false);

        Boolean booking = bookingSystem.bookRoom(ROOM_ID, START, END); //Valid booking
        assertThat(booking).isFalse();

        //Room should not be saved
        verify(room,never()).addBooking(any(Booking.class));
        verify(roomRepository, never()).save(room);
    }

    @ParameterizedTest
    @DisplayName("bookRoom: Should throw exception when parameters are null (bookingRoom)")
    @MethodSource("bookingRoomNullParameterProvider")
    void nullParameters_tryBookingRoom(String roomId, LocalDateTime start, LocalDateTime end) {
        assertThatThrownBy(() -> bookingSystem.bookRoom(roomId, start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bokning kräver giltiga start- och sluttider samt rum-id");
    }

    @Test
    @DisplayName("bookRoom: Should throw exception when booking is in the past")
    public void tryBookingInPast(){
        LocalDateTime past = NOW.minusDays(7);

        assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID, past, END))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kan inte boka tid i dåtid");
    }

    @Test
    @DisplayName("bookRoom: Should throw exception when start is after end")
    void tryBookingAfterEnd() {
        LocalDateTime earlyEnd = START.minusDays(1);

        assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID, START, earlyEnd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sluttid måste vara efter starttid");
    }

    @Test
    @DisplayName("bookRoom: Should throw exception when room doesn't exist")
    void tryBookingNonExistentRoom(){
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID, START, END))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rummet existerar inte");
    }


    /*
     * Test for BookingSystem#getAvailableRooms(LocalDateTime, LocalDateTime)
     *
     * - Valid parameters should return only available rooms
     *
     * - Invalid parameters:
     *  - Null
     *  - Start is after end
     */


    @Test
    @DisplayName("getAvailableRooms: Should return available rooms with valid parameters")
    void tryGettingAvailableRooms() {
        Room roomAvail = mock(Room.class);
        Room room2UnAvail = mock(Room.class);

        when(roomAvail.isAvailable(START, END)).thenReturn(true);   //one available
        when(room2UnAvail.isAvailable(START, END)).thenReturn(false);  //one unavailable

        when(roomRepository.findAll()).thenReturn(Arrays.asList(roomAvail, room2UnAvail));

        List<Room> availableRooms = bookingSystem.getAvailableRooms(START, END);

        //should only return one room as the other one was unavailable
        assertNotNull(availableRooms);
        assertThat(availableRooms.size()).isEqualTo(1);

        //ensure we only check once
        verify(roomRepository).findAll();
    }

    @ParameterizedTest
    @DisplayName("getAvailableRooms: Should Throw exception if any parameter is null")
    @MethodSource("getAvailableRoomsNullParameterProvider")
    void nullParameters_tryGettingAvailableRooms(LocalDateTime startTime, LocalDateTime endTime) {
        assertThatThrownBy(() -> bookingSystem.getAvailableRooms(startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Måste ange både start- och sluttid");
    }

    @Test
    @DisplayName("getAvailableRooms: Should throw exception when start is after end")
    void endTimeBeforeStart_getAvailableRooms() {
        LocalDateTime earlyEnd = START.minusDays(1);

        assertThatThrownBy(() -> bookingSystem.getAvailableRooms(START, earlyEnd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sluttid måste vara efter starttid");
    }


    /*
     * Test for BookingSystem#cancelBooking(String bookingId)
     * <p>
     * - Valid parameters should cancel existing booking
     * - Valid parameters should cancel existing booking even if notification failed
     * <p>
     * - Invalid parameters:
     *  - Null
     *  - Booking has started
     */

    @Test
    @DisplayName("cancelBooking: Should cancel existing booking with valid parameters")
    void tryCancelBooking() throws NotificationException {
        Booking booking = mock(Booking.class);
        when(booking.getStartTime()).thenReturn(START);

        Room room = mock(Room.class);
        when(room.hasBooking(BOOKING_ID)).thenReturn(true);
        when(room.getBooking(BOOKING_ID)).thenReturn(booking);
        when(roomRepository.findAll()).thenReturn(List.of(room));

        boolean cancel = bookingSystem.cancelBooking(BOOKING_ID);

        //ensure booking has been "cancelled"
        assertThat(cancel).isTrue();

        //ensure booking was only removed once
        verify(room).removeBooking(BOOKING_ID);
        verify(roomRepository).save(room);
    }

    @Test
    @DisplayName("cancelBooking: Should cancel existing booking with valid parameters even if error is thrown")
    void cancelBookingEvenIfErrorIsThrown() throws NotificationException {
        Booking booking = mock(Booking.class);
        when(booking.getStartTime()).thenReturn(START);

        Room room = mock(Room.class);
        when(room.hasBooking(BOOKING_ID)).thenReturn(true);
        when(room.getBooking(BOOKING_ID)).thenReturn(booking);
        when(roomRepository.findAll()).thenReturn(List.of(room));

        doThrow(new NotificationException("NotificationService is down"))
                .when(notificationService).sendCancellationConfirmation(booking);

        boolean cancel = bookingSystem.cancelBooking(BOOKING_ID);

        //ensure booking has been "cancelled"
        assertThat(cancel).isTrue();

        //ensure booking was still removed
        verify(room).removeBooking(BOOKING_ID);
        verify(roomRepository).save(room);
    }

    @Test
    @DisplayName("cancelBooking: Should throw exception when parameter is null")
    void nullParameters_tryCancelBooking() {
        assertThatThrownBy(() -> bookingSystem.cancelBooking(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Boknings-id kan inte vara null");

        verify(roomRepository, never()).save(room);
    }

    @Test
    @DisplayName("cancelBooking: Should throw exception when booking has started")
    void lateCancellation() {
        LocalDateTime pastStart = NOW.minusDays(1);

        Booking booking = mock(Booking.class);
        when(booking.getStartTime()).thenReturn(pastStart);

        Room room = mock(Room.class);
        when(room.hasBooking(BOOKING_ID)).thenReturn(true);
        when(room.getBooking(BOOKING_ID)).thenReturn(booking);

        when(roomRepository.findAll()).thenReturn(List.of(room));

        assertThatThrownBy(() -> bookingSystem.cancelBooking(BOOKING_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Kan inte avboka påbörjad eller avslutad bokning");
    }
}